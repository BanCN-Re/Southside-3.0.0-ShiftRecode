#pragma once

#include <cstddef>
#include <cstdint>
#include <filesystem>
#include <string>
#include <string_view>
#include <vector>

namespace southside::auth {

using Bytes = std::vector<std::uint8_t>;

Bytes sha256(std::string_view input);
std::string base64Encode(const Bytes& input);
Bytes base64Decode(std::string_view input);
std::string deriveToken(std::string_view first, std::string_view second);
std::string deviceIdentifier();
std::string signRequest(std::string_view material);

Bytes aesGcmEncrypt(std::string_view key, const Bytes& plaintext);
Bytes aesGcmDecrypt(std::string_view key, const Bytes& encoded);
std::filesystem::path credentialPath();
void saveCredentials(std::string_view device, std::string_view credentials);
std::string loadCredentials(std::string_view device);

} // namespace southside::auth
