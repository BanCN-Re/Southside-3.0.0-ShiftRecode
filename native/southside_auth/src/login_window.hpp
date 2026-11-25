#pragma once

#include <functional>
#include <string>

namespace southside::auth {

enum class LoginOperation;

struct LoginSubmission {
    std::string username;
    std::string password;
    std::string card;
};

bool showLoginWindow(const std::function<bool(LoginOperation, const LoginSubmission&)>& submit);

} // namespace southside::auth
