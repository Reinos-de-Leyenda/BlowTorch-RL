cmake_minimum_required(VERSION 3.22.1)
project(lua)

##################################
# LuaJIT <> android arch mapper
##################################
if (${CMAKE_ANDROID_ARCH_ABI} STREQUAL arm64-v8a)
    set(ARCH_NAME aarch64)
elseif (${CMAKE_ANDROID_ARCH_ABI} STREQUAL armeabi-v7a)
    set(ARCH_NAME armv7a)
elseif (${CMAKE_ANDROID_ARCH_ABI} STREQUAL x86)
    set(ARCH_NAME i686)
else()
    set(ARCH_NAME ${CMAKE_ANDROID_ARCH_ABI})
endif()

##################################
# LuaJIT shared & static libs
##################################
add_library(luajit STATIC IMPORTED)
set_target_properties(
        luajit
        PROPERTIES
        IMPORTED_LOCATION ${LIBLUAJIT_STATIC}
        ${CMAKE_CURRENT_SOURCE_DIR}/libluajit-${ARCH_NAME}.a
)

add_library(${PROJECT_NAME} SHARED luajava.c luajava.h)
target_compile_options(${PROJECT_NAME} PRIVATE -DLUA_USE_DLOPEN)
target_link_libraries(${PROJECT_NAME}
        PRIVATE luajit
        PRIVATE -latomic
        PRIVATE -lm
)