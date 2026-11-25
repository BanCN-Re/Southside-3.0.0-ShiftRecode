#include "session.hpp"

#include "crypto.hpp"
#include "login_window.hpp"

#include <nlohmann/json.hpp>

#include <chrono>
#include <iostream>
#include <utility>

namespace southside::auth {
namespace {

struct EnvGuard {
    JavaVM* jvm;
    JNIEnv* env = nullptr;
    bool attached = false;

    explicit EnvGuard(JavaVM* vm) : jvm(vm) {
        if (!jvm) {
            return;
        }
        const jint result = jvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);
        if (result == JNI_EDETACHED) {
            if (jvm->AttachCurrentThread(reinterpret_cast<void**>(&env), nullptr) == JNI_OK) {
                attached = true;
            }
        }
    }

    ~EnvGuard() {
        if (attached) {
            jvm->DetachCurrentThread();
        }
    }

    explicit operator bool() const {
        return env != nullptr;
    }
};

std::string jsonString(const nlohmann::json& packet, std::initializer_list<const char*> keys) {
    for (const char* key : keys) {
        const auto it = packet.find(key);
        if (it != packet.end() && it->is_string()) {
            return it->get<std::string>();
        }
    }
    return {};
}

} // namespace

Session::Session(JNIEnv* env, jobject client) {
    if (!env || !client) {
        throw std::invalid_argument("Invalid JNI client");
    }
    if (env->GetJavaVM(&jvm_) != JNI_OK) {
        throw std::runtime_error("Could not acquire JavaVM");
    }

    client_ = env->NewGlobalRef(client);
    clientClass_ = static_cast<jclass>(env->NewGlobalRef(env->GetObjectClass(client)));
    connectTransport_ = env->GetMethodID(clientClass_, "connectTransport", "(Ljava/lang/String;)V");
    sendServerMessage_ = env->GetMethodID(clientClass_, "sendServerMessage", "(Ljava/lang/String;)Z");
    receiveApplicationPacket_ = env->GetMethodID(clientClass_, "receiveApplicationPacket", "(I[B)V");
    if (!client_ || !clientClass_ || !connectTransport_ || !sendServerMessage_ || !receiveApplicationPacket_) {
        throw std::runtime_error("Native login transport methods are missing");
    }
}

Session::~Session() {
    closed_ = true;
    disconnected();
    EnvGuard env(jvm_);
    if (env) {
        if (client_) {
            env.env->DeleteGlobalRef(client_);
        }
        if (clientClass_) {
            env.env->DeleteGlobalRef(clientClass_);
        }
    }
}

bool Session::showLogin() {
    return showLoginWindow([this](LoginOperation operation, const LoginSubmission& submission) {
        username_ = submission.username;
        password_ = submission.password;
        card_ = submission.card;
        return initialize(operation, username_, password_, card_, 60);
    });
}

bool Session::reconnect() {
    if (closed_) {
        return false;
    }
    try {
        const std::string saved = loadCredentials(deviceIdentifier());
        if (saved.empty()) {
            return false;
        }
        const auto credentials = nlohmann::json::parse(saved);
        username_ = jsonString(credentials, {"u"});
        password_ = jsonString(credentials, {"p"});
        token_ = jsonString(credentials, {"tk"});
        if (username_.empty() || password_.empty()) {
            return false;
        }
        return initialize(LoginOperation::login, username_, password_, {}, 10);
    } catch (const std::exception& error) {
        std::cerr << "[Native Login Error] " << error.what() << '\n';
        return false;
    }
}

void Session::onOpen() {
    open_ = true;
    std::string packet;
    {
        std::lock_guard lock(mutex_);
        packet = std::move(pendingPacket_);
        pendingPacket_.clear();
    }
    if (!packet.empty() && !send(packet)) {
        std::lock_guard lock(mutex_);
        pendingPacket_ = std::move(packet);
    }
}

void Session::receive(const std::string& text) {
    if (closed_) {
        return;
    }
    try {
        const auto packet = nlohmann::json::parse(text);
        if (!packet.is_object()) {
            throw std::invalid_argument("Invalid Packet.");
        }
        const int type = packet.value("t", 0);
        if (type >= 20 && type <= 22) {
            std::string payload = jsonString(packet, {"m", "ui"});
            if (payload.empty()) {
                payload = packet.dump();
            }
            dispatchApplicationPacket(type, payload);
            return;
        }

        const std::string message = jsonString(packet, {"m"});
        if (!message.empty()) {
            std::cerr << message << '\n';
        }

        bool accepted = false;
        if (const auto it = packet.find("a"); it != packet.end() && it->is_boolean()) {
            accepted = it->get<bool>();
        } else if (const auto it = packet.find("success"); it != packet.end() && it->is_boolean()) {
            accepted = it->get<bool>();
        }
        if (!accepted) {
            authenticated_ = false;
            return;
        }

        token_ = jsonString(packet, {"tk", "token"});
        authenticated_ = true;
        authenticationCondition_.notify_all();
        saveLoginState();
    } catch (const std::exception& error) {
        std::cerr << "[Native Receive Error] " << error.what() << '\n';
    }
}

void Session::disconnected() {
    open_ = false;
    authenticationCondition_.notify_all();
}

bool Session::authenticated() const {
    return authenticated_ && open_;
}

bool Session::chat(const std::string& text) {
    if (!authenticated()) {
        return false;
    }
    const nlohmann::json packet = {{"t", 7}, {"m", text}};
    return send(packet.dump());
}

bool Session::presence(const std::string& text, bool state) {
    if (!authenticated()) {
        return false;
    }
    const nlohmann::json packet = {{"t", 6}, {"ui", text}, {"a", state}};
    return send(packet.dump());
}

bool Session::initialize(LoginOperation operation, const std::string& username,
                         const std::string& password, const std::string& card,
                         std::int32_t timeoutSeconds) {
    if (closed_ || username.empty() || password.empty()) {
        return false;
    }
    if ((operation == LoginOperation::registration || operation == LoginOperation::topUp) && card.empty()) {
        return false;
    }

    const std::string device = deviceIdentifier();
    const auto timestamp = std::chrono::duration_cast<std::chrono::milliseconds>(
        std::chrono::system_clock::now().time_since_epoch()).count();

    std::string material;
    material.reserve(username.size() + password.size() + device.size() + card.size() + 32);
    material.append(username);
    material.append(password);
    material.append(device);
    if (operation == LoginOperation::registration || operation == LoginOperation::topUp) {
        material.append(card);
    }
    material.append(std::to_string(timestamp));

    nlohmann::json packet = {
        {"t", static_cast<std::int32_t>(operation)},
        {"u", username},
        {"p", password},
        {"h", device},
        {"ts", timestamp},
        {"s", signRequest(material)},
    };
    if (operation == LoginOperation::registration || operation == LoginOperation::topUp) {
        packet["ck"] = card;
    }

    const std::string serialized = packet.dump();
    if (!send(serialized)) {
        {
            std::lock_guard lock(mutex_);
            pendingPacket_ = serialized;
        }
        connect();
    }

    std::unique_lock lock(authenticationMutex_);
    authenticationCondition_.wait_for(lock, std::chrono::seconds(timeoutSeconds), [this] {
        return authenticated_.load() || closed_.load();
    });
    return authenticated_;
}

bool Session::send(const std::string& packet) {
    if (closed_) {
        return false;
    }
    EnvGuard env(jvm_);
    if (!env) {
        return false;
    }
    const jstring value = env.env->NewStringUTF(packet.c_str());
    if (!value) {
        return false;
    }
    const jboolean result = env.env->CallBooleanMethod(client_, sendServerMessage_, value);
    env.env->DeleteLocalRef(value);
    return result == JNI_TRUE;
}

void Session::connect() {
    EnvGuard env(jvm_);
    if (!env) {
        return;
    }
    const jstring endpoint = env.env->NewStringUTF(endpoint_.c_str());
    if (endpoint) {
        env.env->CallVoidMethod(client_, connectTransport_, endpoint);
        env.env->DeleteLocalRef(endpoint);
    }
}

void Session::dispatchApplicationPacket(std::int32_t type, const std::string& text) {
    EnvGuard env(jvm_);
    if (!env) {
        return;
    }
    const jbyteArray data = env.env->NewByteArray(static_cast<jsize>(text.size()));
    if (!data) {
        return;
    }
    env.env->SetByteArrayRegion(data, 0, static_cast<jsize>(text.size()),
                                reinterpret_cast<const jbyte*>(text.data()));
    env.env->CallVoidMethod(client_, receiveApplicationPacket_, type, data);
    env.env->DeleteLocalRef(data);
}

void Session::saveLoginState() const {
    try {
        const nlohmann::json credentials = {{"u", username_}, {"p", password_}, {"tk", token_}};
        saveCredentials(deviceIdentifier(), credentials.dump());
    } catch (const std::exception& error) {
        std::cerr << "[Native Login Error] " << error.what() << '\n';
    }
}

void SessionDeleter::operator()(Session* session) const {
    delete session;
}

} // namespace southside::auth
