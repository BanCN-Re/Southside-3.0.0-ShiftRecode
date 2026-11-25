#include "login_window.hpp"

#include "session.hpp"

#include <windows.h>
#include <d3d11.h>

#include <imgui.h>
#include <imgui_impl_dx11.h>
#include <imgui_impl_win32.h>

#include <atomic>
#include <cstring>
#include <string>
#include <thread>

extern IMGUI_IMPL_API LRESULT ImGui_ImplWin32_WndProcHandler(HWND, UINT, WPARAM, LPARAM);

namespace southside::auth {
namespace {

ID3D11Device* g_device = nullptr;
ID3D11DeviceContext* g_deviceContext = nullptr;
IDXGISwapChain* g_swapChain = nullptr;
ID3D11RenderTargetView* g_renderTarget = nullptr;

void createRenderTarget() {
    ID3D11Texture2D* backBuffer = nullptr;
    if (SUCCEEDED(g_swapChain->GetBuffer(0, IID_PPV_ARGS(&backBuffer))) && backBuffer) {
        g_device->CreateRenderTargetView(backBuffer, nullptr, &g_renderTarget);
        backBuffer->Release();
    }
}

void cleanupRenderTarget() {
    if (g_renderTarget) {
        g_renderTarget->Release();
        g_renderTarget = nullptr;
    }
}

bool createDevice(HWND hwnd) {
    DXGI_SWAP_CHAIN_DESC desc{};
    desc.BufferCount = 2;
    desc.BufferDesc.Format = DXGI_FORMAT_R8G8B8A8_UNORM;
    desc.BufferDesc.RefreshRate.Numerator = 60;
    desc.BufferDesc.RefreshRate.Denominator = 1;
    desc.BufferUsage = DXGI_USAGE_RENDER_TARGET_OUTPUT;
    desc.OutputWindow = hwnd;
    desc.SampleDesc.Count = 1;
    desc.Windowed = TRUE;
    desc.SwapEffect = DXGI_SWAP_EFFECT_DISCARD;
    desc.Flags = DXGI_SWAP_CHAIN_FLAG_ALLOW_MODE_SWITCH;

    const D3D_FEATURE_LEVEL levels[] = {D3D_FEATURE_LEVEL_11_0, D3D_FEATURE_LEVEL_10_0};
    D3D_FEATURE_LEVEL selected;
    if (FAILED(D3D11CreateDeviceAndSwapChain(nullptr, D3D_DRIVER_TYPE_HARDWARE, nullptr, 0, levels, 2,
                                             D3D11_SDK_VERSION, &desc, &g_swapChain, &g_device, &selected,
                                             &g_deviceContext))) {
        return false;
    }
    createRenderTarget();
    return true;
}

void cleanupDevice() {
    cleanupRenderTarget();
    if (g_swapChain) {
        g_swapChain->Release();
        g_swapChain = nullptr;
    }
    if (g_deviceContext) {
        g_deviceContext->Release();
        g_deviceContext = nullptr;
    }
    if (g_device) {
        g_device->Release();
        g_device = nullptr;
    }
}

LRESULT WINAPI windowProc(HWND hwnd, UINT message, WPARAM wParam, LPARAM lParam) {
    if (ImGui_ImplWin32_WndProcHandler(hwnd, message, wParam, lParam)) {
        return true;
    }
    switch (message) {
        case WM_SIZE:
            if (g_device && wParam != SIZE_MINIMIZED) {
                cleanupRenderTarget();
                g_swapChain->ResizeBuffers(0, LOWORD(lParam), HIWORD(lParam), DXGI_FORMAT_UNKNOWN, 0);
                createRenderTarget();
            }
            return 0;
        case WM_SYSCOMMAND:
            if ((wParam & 0xfff0) == SC_KEYMENU) {
                return 0;
            }
            break;
        case WM_DESTROY:
            PostQuitMessage(0);
            return 0;
        default:
            break;
    }
    return DefWindowProcW(hwnd, message, wParam, lParam);
}

void copyInput(char* destination, std::size_t size, const std::string& source) {
    std::strncpy(destination, source.c_str(), size - 1);
    destination[size - 1] = '\0';
}

} // namespace

bool showLoginWindow(const std::function<bool(LoginOperation, const LoginSubmission&)>& submit) {
    const wchar_t* className = L"ModernAuthClient";
    WNDCLASSEXW wc{};
    wc.cbSize = sizeof(wc);
    wc.style = CS_CLASSDC;
    wc.lpfnWndProc = windowProc;
    wc.hInstance = GetModuleHandleW(nullptr);
    wc.lpszClassName = className;
    wc.hCursor = LoadCursorW(nullptr, IDC_ARROW);
    if (!RegisterClassExW(&wc)) {
        return false;
    }

    HWND hwnd = CreateWindowExW(WS_EX_APPWINDOW, className, L"Southside", WS_OVERLAPPED | WS_CAPTION | WS_SYSMENU,
                                100, 100, 520, 390, nullptr, nullptr, wc.hInstance, nullptr);
    if (!hwnd || !createDevice(hwnd)) {
        DestroyWindow(hwnd);
        UnregisterClassW(className, wc.hInstance);
        return false;
    }

    ShowWindow(hwnd, SW_SHOWDEFAULT);
    UpdateWindow(hwnd);

    IMGUI_CHECKVERSION();
    ImGui::CreateContext();
    ImGuiIO& io = ImGui::GetIO();
    io.ConfigFlags |= ImGuiConfigFlags_NavEnableKeyboard;
    io.IniFilename = nullptr;
    ImGui::StyleColorsDark();
    ImFontConfig fontConfig;
    fontConfig.FontDataOwnedByAtlas = false;
    io.Fonts->AddFontFromFileTTF("C:\\Windows\\Fonts\\msyh.ttc", 18.0F, &fontConfig,
                                 io.Fonts->GetGlyphRangesChineseFull());
    ImGui_ImplWin32_Init(hwnd);
    ImGui_ImplDX11_Init(g_device, g_deviceContext);

    char username[128]{};
    char password[128]{};
    char card[128]{};
    bool done = false;
    bool authenticated = false;
    std::atomic<bool> busy{false};
    std::atomic<bool> requestFinished{false};
    std::atomic<bool> requestAccepted{false};
    std::string error;
    std::thread worker;

    while (!done) {
        MSG message;
        while (PeekMessageW(&message, nullptr, 0, 0, PM_REMOVE)) {
            TranslateMessage(&message);
            DispatchMessageW(&message);
            if (message.message == WM_QUIT) {
                done = true;
            }
        }
        if (done) {
            break;
        }

        ImGui_ImplDX11_NewFrame();
        ImGui_ImplWin32_NewFrame();
        ImGui::NewFrame();
        ImGui::SetNextWindowPos(ImVec2(0, 0));
        ImGui::SetNextWindowSize(io.DisplaySize);
        ImGui::Begin("Main UI", nullptr,
                     ImGuiWindowFlags_NoDecoration | ImGuiWindowFlags_NoMove | ImGuiWindowFlags_NoSavedSettings);

        ImGui::TextUnformatted("Southside Authentication");
        ImGui::Separator();
        ImGui::BeginDisabled(busy);
        ImGui::InputText("##user", username, sizeof(username));
        ImGui::SameLine();
        ImGui::TextUnformatted("Username");
        ImGui::InputText("##pass", password, sizeof(password), ImGuiInputTextFlags_Password);
        ImGui::SameLine();
        ImGui::TextUnformatted("Password");
        ImGui::InputText("##card", card, sizeof(card));
        ImGui::SameLine();
        ImGui::TextUnformatted("Card key");

        LoginSubmission submission{username, password, card};
        auto launch = [&](LoginOperation operation) {
            if (busy) {
                return;
            }
            busy = true;
            requestFinished = false;
            requestAccepted = false;
            error.clear();
            worker = std::thread([operation, submission, &submit, &requestAccepted, &requestFinished, &busy]() mutable {
                bool accepted = false;
                try {
                    accepted = submit(operation, submission);
                } catch (...) {
                    accepted = false;
                }
                requestAccepted = accepted;
                requestFinished = true;
                busy = false;
            });
        };

        if (ImGui::Button("Login", ImVec2(145, 36))) {
            launch(LoginOperation::login);
        }
        ImGui::SameLine();
        if (ImGui::Button("Register", ImVec2(145, 36))) {
            launch(LoginOperation::registration);
        }
        ImGui::SameLine();
        if (ImGui::Button("Top-up", ImVec2(145, 36))) {
            launch(LoginOperation::topUp);
        }
        ImGui::EndDisabled();

        if (busy) {
            ImGui::TextUnformatted("Waiting for authentication...");
        } else if (!error.empty()) {
            ImGui::TextColored(ImVec4(1.0F, 0.35F, 0.35F, 1.0F), "%s", error.c_str());
        }
        ImGui::End();
        ImGui::Render();

        if (requestFinished) {
            if (worker.joinable()) {
                worker.join();
            }
            if (requestAccepted) {
                authenticated = true;
                done = true;
            } else {
                error = "Login failed. Check console.";
            }
            requestFinished = false;
        }

        const float clearColor[4] = {0.035F, 0.04F, 0.055F, 1.0F};
        g_deviceContext->OMSetRenderTargets(1, &g_renderTarget, nullptr);
        g_deviceContext->ClearRenderTargetView(g_renderTarget, clearColor);
        ImGui_ImplDX11_RenderDrawData(ImGui::GetDrawData());
        g_swapChain->Present(1, 0);
    }

    if (worker.joinable()) {
        worker.join();
    }
    ImGui_ImplDX11_Shutdown();
    ImGui_ImplWin32_Shutdown();
    ImGui::DestroyContext();
    cleanupDevice();
    DestroyWindow(hwnd);
    UnregisterClassW(className, wc.hInstance);
    return authenticated;
}

} // namespace southside::auth
