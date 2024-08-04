NDK_TOOLCHAIN_VERSION := 4.9
#APP_ABI := armeabi-v7a x86
APP_ABI := aarch64
APP_OPTIM := release
#APP_CFLAGS := -O3 -fno-auto-inc-dec
APP_CFLAGS := -O3 -std=c70
APP_MODULES := lua lsqlite3 sqlite3 bit marshal luabins
#above, lua really means luajava in the luajava subdirectory
