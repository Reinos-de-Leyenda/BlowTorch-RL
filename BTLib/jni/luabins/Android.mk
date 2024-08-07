LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libluabins
LOCAL_MODULE_FILENAME := libluabins
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../luajava
LOCAL_SHARED_LIBRARIES := lua
LOCAL_CFLAGS := -O3 -fPIC -shared -std=c90
LOCAL_SRC_FILES := ./fwrite.c \
				   ./load.c \
				   ./luabins.c \
				   ./save.c \
				   ./luainternals.c \
				   ./savebuffer.c \
				   ./write.c

include $(BUILD_SHARED_LIBRARY)

#
#ifeq ($(TARGET_ARCH_ABI),armeabi)
#include $(CLEAR_VARS)
#LOCAL_MODULE := libluabins
#LOCAL_MODULE_FILENAME := libluabins
#LOCAL_C_INCLUDES += $(LOCAL_PATH)/../luajava
#LOCAL_SHARED_LIBRARIES := lua
#LOCAL_CFLAGS := -O3 -fPIC -shared -std=c99
#LOCAL_SRC_FILES := ./fwrite.c \
#				   ./load.c \
#				   ./luabins.c \
#				   ./save.c \
#				   ./luainternals.c \
#				   ./savebuffer.c \
#				   ./write.c
#
#include $(BUILD_SHARED_LIBRARY)
#
#endif
#
#ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
#include $(CLEAR_VARS)
#LOCAL_MODULE := libluabins
#LOCAL_MODULE_FILENAME := libluabins
#LOCAL_C_INCLUDES += $(LOCAL_PATH)/../luajava
#LOCAL_SHARED_LIBRARIES := lua
#LOCAL_CFLAGS := -O3 -fPIC -shared -std=c99
#LOCAL_SRC_FILES := ./fwrite.c \
#				   ./load.c \
#				   ./luabins.c \
#				   ./luainternals.c \
#				   ./save.c \
#				   ./savebuffer.c \
#				   ./write.c
#
#include $(BUILD_SHARED_LIBRARY)
#endif
#
#ifeq ($(TARGET_ARCH_ABI),mips)
#include $(CLEAR_VARS)
#LOCAL_MODULE := libluabins
#LOCAL_MODULE_FILENAME := libluabins
#LOCAL_C_INCLUDES += $(LOCAL_PATH)/../luajava
#LOCAL_SHARED_LIBRARIES := lua
#LOCAL_CFLAGS := -O3 -fPIC -shared -std=c99
#LOCAL_SRC_FILES := ./fwrite.c \
#				   ./load.c \
#				   ./luabins.c \
#				   ./save.c \
#				   ./luainternals.c \
#				   ./savebuffer.c \
#				   ./write.c
#
#include $(BUILD_SHARED_LIBRARY)
#
#endif
#
#ifeq ($(TARGET_ARCH_ABI),x86)
#include $(CLEAR_VARS)
#LOCAL_MODULE := libluabins
#LOCAL_MODULE_FILENAME := libluabins
#LOCAL_C_INCLUDES += $(LOCAL_PATH)/../luajava
#LOCAL_SHARED_LIBRARIES := lua
#LOCAL_CFLAGS := -O3 -fPIC -shared -std=c99
#LOCAL_SRC_FILES := ./fwrite.c \
#				   ./load.c \
#				   ./luabins.c \
#				   ./luainternals.c \
#				   ./save.c \
#				   ./savebuffer.c \
#				   ./write.c
#
#include $(BUILD_SHARED_LIBRARY)
#endif
