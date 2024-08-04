LOCAL_PATH := $(call my-dir)

#arm64-v8a
#armeabi-v7a
#x86
#x86_64

# ARM64
ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
include $(CLEAR_VARS)
LOCAL_MODULE := luajit
LOCAL_SRC_FILES := libluajit-aarch64.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := lua
LOCAL_STATIC_LIBRARIES := luajit
LOCAL_CFLAGS := -O3 -shared -std=c90 -DLUA_USE_DLOPEN -v
LOCAL_SRC_FILES := luajava.c
include $(BUILD_SHARED_LIBRARY)
endif

# ARM32
ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
include $(CLEAR_VARS)
LOCAL_MODULE := luajit
LOCAL_SRC_FILES := libluajit-armv7a.a
LOCAL_CFLAGS := -fPIC -v
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := lua
LOCAL_STATIC_LIBRARIES := luajit
LOCAL_CFLAGS := -O3 -shared -std=c90 -DLUA_USE_DLOPEN -v
LOCAL_SRC_FILES := luajava.c
include $(BUILD_SHARED_LIBRARY)
endif

# X86_64
ifeq ($(TARGET_ARCH_ABI),x86_64)
include $(CLEAR_VARS)
LOCAL_MODULE := luajit
LOCAL_SRC_FILES := libluajit-x86_64.a
LOCAL_CFLAGS := -fPIC -v
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := lua
LOCAL_STATIC_LIBRARIES := luajit
LOCAL_CFLAGS := -O3 -shared -std=c90 -DLUA_USE_DLOPEN -v
LOCAL_SRC_FILES := luajava.c
include $(BUILD_SHARED_LIBRARY)
endif

# X86
ifeq ($(TARGET_ARCH_ABI),x86)
include $(CLEAR_VARS)
LOCAL_MODULE := luajit
LOCAL_SRC_FILES := libluajit-i686.a
LOCAL_CFLAGS := -fPIC -v
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := lua
LOCAL_STATIC_LIBRARIES := luajit
LOCAL_CFLAGS := -O3 -shared -std=c90 -DLUA_USE_DLOPEN -v
LOCAL_SRC_FILES := luajava.c
include $(BUILD_SHARED_LIBRARY)
endif
