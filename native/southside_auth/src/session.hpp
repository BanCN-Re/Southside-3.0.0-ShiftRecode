#pragma once

#include <atomic>
#include <condition_variable>
#include <cstdint>
#include <jni.h>
#include <memory>
#include <mutex>
#include <string>

namespace southside::auth {

enum class LoginOperation : std::int32_t {
    login = 1,
    registration = 4,
    topUp = 5,
};

class Session {
public:
    Session(JNIEnv* env, jobject client);
    ~Session();

    bool showLogin();
    bool reconnect();
    void onOpen();
    void receive(const std::string& text);
    void disconnected();
    bool authenticated() const;
    bool chat(const std::string& text);
    bool presence(const std::string& text, bool state);

private:
    bool initialize(LoginOperation operation, const std::string& username,
                    const std::string& password, const std::string& card,
                    std::int32_t timeoutSeconds);
    bool send(const std::string& packet);
    void connect();
    void dispatchApplicationPacket(std::int32_t type, const std::string& text);
    void saveLoginState() const;

    JavaVM* jvm_ = nullptr;
    jobject client_ = nullptr;
    jclass clientClass_ = nullptr;
    jmethodID connectTransport_ = nullptr;
    jmethodID sendServerMessage_ = nullptr;
    jmethodID receiveApplicationPacket_ = nullptr;

    mutable std::mutex mutex_;
    std::string endpoint_ = "wss://wss.eastsidenel.online/ws/secure";
    std::string username_;
    std::string password_;
    std::string card_;
    std::string token_;
    std::string pendingPacket_;
    std::atomic<bool> open_{false};
    std::atomic<bool> authenticated_{false};
    std::atomic<bool> closed_{false};
    std::mutex authenticationMutex_;
    std::condition_variable authenticationCondition_;
};

struct SessionDeleter {
    void operator()(Session* session) const;
};

using SessionPtr = std::unique_ptr<Session, SessionDeleter>;

} // namespace southside::auth
