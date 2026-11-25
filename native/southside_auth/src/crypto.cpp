#include "crypto.hpp"

#include <windows.h>
#include <bcrypt.h>

#include <algorithm>
#include <array>
#include <cctype>
#include <fstream>
#include <stdexcept>

namespace southside::auth {
namespace {

constexpr std::string_view kRequestSecret = "YUCcKp93q0whbW7ce07VbfM0NOsAZCLHnlA9KwaPAvU=";
constexpr std::string_view kCredentialSalt = "hsrhGdgfe6DnFjsfwa7Bd87jndwadsh";
constexpr std::size_t kNonceSize = 12;
constexpr std::size_t kTagSize = 16;
constexpr std::size_t kKeySize = 32;
constexpr char kBase64Alphabet[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

struct AlgorithmHandle {
    BCRYPT_ALG_HANDLE value = nullptr;
    ~AlgorithmHandle() {
        if (value) {
            BCryptCloseAlgorithmProvider(value, 0);
        }
    }
};

struct HashHandle {
    BCRYPT_HASH_HANDLE value = nullptr;
    ~HashHandle() {
        if (value) {
            BCryptDestroyHash(value);
        }
    }
};

struct KeyHandle {
    BCRYPT_KEY_HANDLE value = nullptr;
    ~KeyHandle() {
        if (value) {
            BCryptDestroyKey(value);
        }
    }
};

void check(NTSTATUS status, const char* message) {
    if (status < 0) {
        throw std::runtime_error(message);
    }
}

std::string environmentValue(const char* name) {
    const std::size_t required = GetEnvironmentVariableA(name, nullptr, 0);
    if (required == 0) {
        return {};
    }
    std::string result(required, '\0');
    const DWORD written = GetEnvironmentVariableA(name, result.data(), required);
    result.resize(written);
    return result;
}

std::string normalizedHash(std::string_view input) {
    std::string encoded = base64Encode(sha256(input));
    encoded.erase(std::remove_if(encoded.begin(), encoded.end(), [](unsigned char c) {
        return std::isalnum(c) == 0;
    }), encoded.end());
    return encoded;
}

std::array<std::uint8_t, kKeySize> credentialKey(std::string_view device) {
    std::array<std::uint8_t, kKeySize> key{};
    const std::size_t count = std::min(device.size(), key.size());
    std::copy_n(device.begin(), count, key.begin());
    return key;
}

Bytes aesGcmCrypt(std::string_view keyMaterial, const Bytes& input, bool encrypt) {
    if (keyMaterial.size() != kKeySize) {
        throw std::invalid_argument("AES-256 requires a 32-byte key");
    }

    AlgorithmHandle algorithm;
    check(BCryptOpenAlgorithmProvider(&algorithm.value, BCRYPT_AES_ALGORITHM, nullptr, 0), "AES provider unavailable");
    check(BCryptSetProperty(algorithm.value, BCRYPT_CHAINING_MODE,
                            reinterpret_cast<PUCHAR>(const_cast<wchar_t*>(BCRYPT_CHAIN_MODE_GCM)),
                            sizeof(BCRYPT_CHAIN_MODE_GCM), 0), "AES mode unavailable");

    DWORD objectSize = 0;
    DWORD resultSize = 0;
    check(BCryptGetProperty(algorithm.value, BCRYPT_OBJECT_LENGTH,
                            reinterpret_cast<PUCHAR>(&objectSize), sizeof(objectSize), &resultSize, 0),
          "AES object size unavailable");
    Bytes keyObject(objectSize);

    KeyHandle key;
    check(BCryptGenerateSymmetricKey(algorithm.value, &key.value, keyObject.data(), objectSize,
                                     reinterpret_cast<PUCHAR>(const_cast<char*>(keyMaterial.data())),
                                     static_cast<ULONG>(keyMaterial.size()), 0),
          "AES key initialization failed");

    Bytes nonce(kNonceSize);
    Bytes tag(kTagSize);
    Bytes ciphertext;
    if (encrypt) {
        check(BCryptGenRandom(nullptr, nonce.data(), static_cast<ULONG>(nonce.size()),
                              BCRYPT_USE_SYSTEM_PREFERRED_RNG), "Random nonce generation failed");
        ciphertext.resize(input.size());
    } else {
        if (input.size() < kNonceSize + kTagSize) {
            throw std::invalid_argument("Invalid credential nonce or tag length");
        }
        std::copy_n(input.begin(), kNonceSize, nonce.begin());
        std::copy(input.end() - kTagSize, input.end(), tag.begin());
        ciphertext.assign(input.begin() + kNonceSize, input.end() - kTagSize);
    }

    BCRYPT_AUTHENTICATED_CIPHER_MODE_INFO authInfo;
    BCRYPT_INIT_AUTH_MODE_INFO(authInfo);
    authInfo.pbNonce = nonce.data();
    authInfo.cbNonce = static_cast<ULONG>(nonce.size());
    authInfo.pbTag = tag.data();
    authInfo.cbTag = static_cast<ULONG>(tag.size());

    Bytes output(encrypt ? input.size() : ciphertext.size());
    ULONG written = 0;
    NTSTATUS status;
    if (encrypt) {
        status = BCryptEncrypt(key.value,
                               const_cast<PUCHAR>(input.data()), static_cast<ULONG>(input.size()),
                               &authInfo, nullptr, 0, output.data(), static_cast<ULONG>(output.size()),
                               &written, 0);
    } else {
        status = BCryptDecrypt(key.value, ciphertext.data(), static_cast<ULONG>(ciphertext.size()),
                               &authInfo, nullptr, 0, output.data(), static_cast<ULONG>(output.size()),
                               &written, 0);
    }
    check(status, encrypt ? "AES encryption failed" : "AES decryption failed");
    output.resize(written);

    if (encrypt) {
        Bytes encoded = nonce;
        encoded.insert(encoded.end(), output.begin(), output.end());
        encoded.insert(encoded.end(), tag.begin(), tag.end());
        return encoded;
    }
    return output;
}

} // namespace

Bytes sha256(std::string_view input) {
    AlgorithmHandle algorithm;
    check(BCryptOpenAlgorithmProvider(&algorithm.value, BCRYPT_SHA256_ALGORITHM, nullptr, 0),
          "BCryptOpenAlgorithmProvider failed");

    DWORD objectSize = 0;
    DWORD digestSize = 0;
    DWORD resultSize = 0;
    check(BCryptGetProperty(algorithm.value, BCRYPT_OBJECT_LENGTH,
                            reinterpret_cast<PUCHAR>(&objectSize), sizeof(objectSize), &resultSize, 0),
          "Hash object size unavailable");
    check(BCryptGetProperty(algorithm.value, BCRYPT_HASH_LENGTH,
                            reinterpret_cast<PUCHAR>(&digestSize), sizeof(digestSize), &resultSize, 0),
          "Hash length unavailable");

    Bytes hashObject(objectSize);
    HashHandle hash;
    check(BCryptCreateHash(algorithm.value, &hash.value, hashObject.data(), objectSize, nullptr, 0, 0),
          "BCryptCreateHash failed");
    if (!input.empty()) {
        check(BCryptHashData(hash.value, reinterpret_cast<PUCHAR>(const_cast<char*>(input.data())),
                             static_cast<ULONG>(input.size()), 0), "BCryptHashData failed");
    }
    Bytes digest(digestSize);
    check(BCryptFinishHash(hash.value, digest.data(), digestSize, 0), "BCryptFinishHash failed");
    return digest;
}

std::string base64Encode(const Bytes& input) {
    std::string output;
    output.reserve(((input.size() + 2) / 3) * 4);
    for (std::size_t i = 0; i < input.size(); i += 3) {
        const std::uint32_t a = input[i];
        const std::uint32_t b = i + 1 < input.size() ? input[i + 1] : 0;
        const std::uint32_t c = i + 2 < input.size() ? input[i + 2] : 0;
        const std::uint32_t value = (a << 16) | (b << 8) | c;
        output.push_back(kBase64Alphabet[(value >> 18) & 63]);
        output.push_back(kBase64Alphabet[(value >> 12) & 63]);
        output.push_back(i + 1 < input.size() ? kBase64Alphabet[(value >> 6) & 63] : '=');
        output.push_back(i + 2 < input.size() ? kBase64Alphabet[value & 63] : '=');
    }
    return output;
}

Bytes base64Decode(std::string_view input) {
    std::array<int, 256> table{};
    table.fill(-1);
    for (int i = 0; i < 64; ++i) {
        table[static_cast<unsigned char>(kBase64Alphabet[i])] = i;
    }
    Bytes output;
    int value = 0;
    int bits = -8;
    for (unsigned char c : input) {
        if (c == '=') {
            break;
        }
        const int decoded = table[c];
        if (decoded < 0) {
            continue;
        }
        value = (value << 6) | decoded;
        bits += 6;
        if (bits >= 0) {
            output.push_back(static_cast<std::uint8_t>((value >> bits) & 0xff));
            bits -= 8;
        }
    }
    return output;
}

std::string deriveToken(std::string_view first, std::string_view second) {
    std::string material;
    material.reserve(first.size() + second.size());
    material.append(first);
    material.append(second);
    return normalizedHash(material);
}

std::string deviceIdentifier() {
    const std::string machine = environmentValue("COMPUTERNAME") + environmentValue("PROCESS_IDENTIFIER");
    return deriveToken(kCredentialSalt, machine);
}

std::string signRequest(std::string_view material) {
    return deriveToken(kRequestSecret, material);
}

Bytes aesGcmEncrypt(std::string_view key, const Bytes& plaintext) {
    return aesGcmCrypt(key, plaintext, true);
}

Bytes aesGcmDecrypt(std::string_view key, const Bytes& encoded) {
    return aesGcmCrypt(key, encoded, false);
}

std::filesystem::path credentialPath() {
    return std::filesystem::current_path() / "SouthCredentials.dat";
}

void saveCredentials(std::string_view device, std::string_view credentials) {
    const auto key = credentialKey(device);
    const Bytes plaintext(credentials.begin(), credentials.end());
    const Bytes encrypted = aesGcmCrypt(std::string_view(reinterpret_cast<const char*>(key.data()), key.size()),
                                        plaintext, true);
    std::ofstream output(credentialPath(), std::ios::binary | std::ios::trunc);
    if (!output) {
        throw std::runtime_error("Could not open credential file");
    }
    output.write(reinterpret_cast<const char*>(encrypted.data()), static_cast<std::streamsize>(encrypted.size()));
}

std::string loadCredentials(std::string_view device) {
    std::ifstream input(credentialPath(), std::ios::binary | std::ios::ate);
    if (!input) {
        return {};
    }
    const auto size = input.tellg();
    if (size <= 0) {
        return {};
    }
    Bytes encrypted(static_cast<std::size_t>(size));
    input.seekg(0);
    input.read(reinterpret_cast<char*>(encrypted.data()), size);
    const auto key = credentialKey(device);
    const Bytes decrypted = aesGcmCrypt(std::string_view(reinterpret_cast<const char*>(key.data()), key.size()),
                                        encrypted, false);
    return std::string(decrypted.begin(), decrypted.end());
}

} // namespace southside::auth
