cmake_minimum_required(VERSION 3.24)
project(southside_auth LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

if(NOT DEFINED JNI_INCLUDE_DIRS)
    if(DEFINED ENV{JAVA_HOME})
        set(JNI_INCLUDE_DIRS "$ENV{JAVA_HOME}/include" "$ENV{JAVA_HOME}/include/win32")
    endif()
endif()

add_library(southside_auth SHARED
    src/southside_auth.cpp
    src/session.cpp
    src/crypto.cpp
    src/login_window.cpp
    src/southside_auth.def
    vendor/imgui/imgui.cpp
    vendor/imgui/imgui_draw.cpp
    vendor/imgui/imgui_tables.cpp
    vendor/imgui/imgui_widgets.cpp
    vendor/imgui/backends/imgui_impl_win32.cpp
    vendor/imgui/backends/imgui_impl_dx11.cpp
)

target_include_directories(southside_auth PRIVATE
    ${JNI_INCLUDE_DIRS}
    src
    vendor/json/single_include
    vendor/imgui
    vendor/imgui/backends
)

target_link_libraries(southside_auth PRIVATE
    bcrypt
    d3d11
    d3dcompiler_47
    dwmapi
    gdi32
    shell32
)

set_target_properties(southside_auth PROPERTIES
    PREFIX ""
    OUTPUT_NAME "southside_auth"
    CXX_VISIBILITY_PRESET hidden
)

if(MINGW)
    target_link_options(southside_auth PRIVATE -static-libgcc -static-libstdc++)
endif()
