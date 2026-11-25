#include "session.hpp"

#include <atomic>
#include <iostream>
#include <memory>
#include <mutex>
#include <string>
#include <unordered_map>

namespace {

JavaVM* g_jvm = nullptr;
std::mutex g_sessionsMutex;
std::unordered_map<jlong, std::shared_ptr<southside::auth::Session>> g_sessions;
jlong g_nextHandle = 1;

std::shared_ptr<southside::auth::Session> findSession(jlong handle) {
    std::lock_guard lock(g_sessionsMutex);
    const auto it = g_sessions.find(handle);
    return it == g_sessions.end() ? nullptr : it->second;
}

std::string fromJavaString(JNIEnv* env, jstring value) {
    if (!value) {
        return {};
    }
    const char* data = env->GetStringUTFChars(value, nullptr);
    if (!data) {
        throw std::runtime_error("GetStringUTFChars failed");
    }
    const std::string result(data);
    env->ReleaseStringUTFChars(value, data);
    return result;
}

std::string fromJavaBytes(JNIEnv* env, jbyteArray value) {
    if (!value) {
        return {};
    }
    const jsize size = env->GetArrayLength(value);
    std::string result(static_cast<std::size_t>(size), '\0');
    env->GetByteArrayRegion(value, 0, size, reinterpret_cast<jbyte*>(result.data()));
    return result;
}

void reportException(JNIEnv* env, const char* message) {
    if (env->ExceptionCheck()) {
        return;
    }
    const jclass type = env->FindClass("java/lang/IllegalStateException");
    if (type) {
        env->ThrowNew(type, message);
    }
}

} // namespace

extern "C" {

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
    g_jvm = vm;
    return JNI_VERSION_1_6;
}

JNIEXPORT jlong JNICALL Java_IIIIllllIIlIIIIlllllIlIl_create0(JNIEnv* env, jobject client) {
    try {
        auto session = std::make_shared<southside::auth::Session>(env, client);
        std::lock_guard lock(g_sessionsMutex);
        const jlong handle = g_nextHandle++;
        g_sessions.emplace(handle, std::move(session));
        return handle;
    } catch (const std::exception& error) {
        reportException(env, error.what());
        return 0;
    }
}

JNIEXPORT jboolean JNICALL Java_IIIIllllIIlIIIIlllllIlIl_showLogin0(JNIEnv*, jobject, jlong handle) {
    const auto session = findSession(handle);
    return session && session->showLogin() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_IIIIllllIIlIIIIlllllIlIl_reconnect0(JNIEnv*, jobject, jlong handle) {
    const auto session = findSession(handle);
    return session && session->reconnect() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_IIIIllllIIlIIIIlllllIlIl_onOpen0(JNIEnv*, jobject, jlong handle) {
    if (const auto session = findSession(handle)) {
        session->onOpen();
    }
}

JNIEXPORT void JNICALL Java_IIIIllllIIlIIIIlllllIlIl_receive0(JNIEnv* env, jobject, jlong handle, jstring text) {
    try {
        if (const auto session = findSession(handle)) {
            session->receive(fromJavaString(env, text));
        }
    } catch (const std::exception& error) {
        reportException(env, error.what());
    }
}

JNIEXPORT void JNICALL Java_IIIIllllIIlIIIIlllllIlIl_disconnected0(JNIEnv*, jobject, jlong handle) {
    if (const auto session = findSession(handle)) {
        session->disconnected();
    }
}

JNIEXPORT jboolean JNICALL Java_IIIIllllIIlIIIIlllllIlIl_authenticated0(JNIEnv*, jobject, jlong handle) {
    const auto session = findSession(handle);
    return session && session->authenticated() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_IIIIllllIIlIIIIlllllIlIl_release0(JNIEnv*, jobject, jlong handle) {
    std::lock_guard lock(g_sessionsMutex);
    g_sessions.erase(handle);
}

JNIEXPORT jboolean JNICALL Java_IIIIllllIIlIIIIlllllIlIl_chat0(JNIEnv* env, jobject, jlong handle, jbyteArray text) {
    try {
        const auto session = findSession(handle);
        return session && session->chat(fromJavaBytes(env, text)) ? JNI_TRUE : JNI_FALSE;
    } catch (const std::exception& error) {
        reportException(env, error.what());
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL Java_IIIIllllIIlIIIIlllllIlIl_presence0(JNIEnv* env, jobject, jlong handle, jstring text,
                                                                     jboolean state) {
    try {
        const auto session = findSession(handle);
        return session && session->presence(fromJavaString(env, text), state == JNI_TRUE) ? JNI_TRUE : JNI_FALSE;
    } catch (const std::exception& error) {
        reportException(env, error.what());
        return JNI_FALSE;
    }
}

} // extern "C"
