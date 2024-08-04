#!/bin/bash

set -e
set -a
set -o pipefail

# LuaJIT version to use. Can be a SHA, tag or any other ref git can handle
# See: https://github.com/LuaJIT/LuaJIT.git
readonly LUAJIT_REF="v2.1"
if [[ ! -d ./LuaJIT ]]; then
  echo "Cloning repo..."
  git clone https://github.com/LuaJIT/LuaJIT.git
fi
cd LuaJIT
git stash
git checkout "${LUAJIT_REF}"

# NDK management
if [[ -z "${NDKDIR}" ]]; then
  echo "Please set up the variable NDKDIR."
  exit 1
fi

echo "Using NDKDIR '${NDKDIR}'."
if [[ ! -d ${NDKDIR} ]]; then
  echo "Can't find directory ${NDKDIR}."
  exit 1
fi

readonly NDKBIN="${NDKDIR}/toolchains/llvm/prebuilt/linux-x86_64/bin"
readonly SYSROOT="${NDKDIR}/toolchains/llvm/prebuilt/linux-x86_64/sysroot"
readonly NDKABI=29

# In order to cross-compile you might need to install the cross-compile libs in your machine, e.g.:
#
# libc6-dev:amd64
# libc6-dev-arm64-cross
# libc6-dev-armhf-cross
# libc6-dev-i386
# libc6-dev-x32
# List of architectures we will compile for:
# - aarch64 : arm 64bit
# - armv7a  : arm 32bit
# - x86_64  : x86 64bit
# - i686    : x86 32bit # Not supported?
# - x86     : x86 32bit # Not supported?
declare -a ARCHS
#ARCHS=("aarch64" "arm64-v8a")
#ARCHS=("x86_64" "x86" "aarch64")
ARCHS=("i686" "x86_64" "aarch64" "armv7a")
readonly ARCHS
readonly ARCHS_32BIT_REGEXP="^(armv7a|i686)$"

for arch in "${ARCHS[@]}"; do
  NDKCROSS="${NDKDIR}/${arch}-linux-android-"
  NDKCC="${NDKBIN}/${arch}-linux-androideabi${NDKABI}-clang"

  if [[ ! -f "${NDKCC}" ]]; then
    if [[ -f "${NDKBIN}/${arch}-linux-android${NDKABI}-clang" ]]; then
      NDKCC="${NDKBIN}/${arch}-linux-android${NDKABI}-clang"
    fi
  fi

  echo "================================="
  echo "Compiling for ${arch}..."
  echo "STATIC_CC=${NDKCC}"
  echo "DYNAMIC_CC=${NDKCC} -fPIC"
  echo "================================="

  make clean

  # 32bit
  if [[ "${arch}" =~ ${ARCHS_32BIT_REGEXP} ]]; then
    HOST_CC="gcc -m32"
    echo "Using HOST_CC=${HOST_CC}"

    make CROSS="${NDKCROSS}" \
         STATIC_CC="${NDKCC}  -fPIC" \
         DYNAMIC_CC="${NDKCC} -fPIC" \
         HOST_CC="${HOST_CC}" \
         TARGET_LD="${NDKCC}" \
         TARGET_AR="${NDKBIN}/llvm-ar rcus" \
         TARGET_STRIP="${NDKBIN}/llvm-strip" \
         TARGET_FLAGS="--sysroot ${SYSROOT}"
  else
      make CROSS="${NDKCROSS}" \
           STATIC_CC="${NDKCC}  -fPIC" \
           DYNAMIC_CC="${NDKCC} -fPIC" \
           TARGET_LD="${NDKCC}" \
           TARGET_AR="${NDKBIN}/llvm-ar rcus" \
           TARGET_STRIP="${NDKBIN}/llvm-strip" \
           TARGET_FLAGS="--sysroot ${SYSROOT}"
  fi

  # These artifacts are required to create the JNIs used in all architectures
  mv src/libluajit.a ../BTLib/jni/luajava/libluajit-${arch}.a
  mv src/libluajit.so ../BTLib/jni/luajava/libluajit-${arch}.so
done

echo "================================="
echo "Copying header files..."
echo "================================="
cp src/lauxlib.h ../BTLib/jni/luajava/lauxlib.h
cp src/lua.h ../BTLib/jni/luajava/lua.h
cp src/luaconf.h ../BTLib/jni/luajava/luaconf.h
cp src/luajit.h ../BTLib/jni/luajava/luajit.h
cp src/lualib.h ../BTLib/jni/luajava/lualib.h

echo "================================="
echo "Applying patches..."
echo "================================="

# We need LuaJIT 2.1.0 for aarch support, but that version dropped some retrocompat that is nowaday
# required. This ugly hack makes it work for now.
# See commit dc320ca70f2c5bb3977b82853bcee6dad2523d01 for LuaJIT
sed -i '17i #define luaL_setn(L,i,j)        ((void)0)  /* no op! */' ../BTLib/jni/luajava/lauxlib.h
sed -i '17i #define luaL_getn(L,i)          ((int)lua_objlen(L, i))' ../BTLib/jni/luajava/lauxlib.h
sed -i '160i #define luaL_reg       luaL_Reg' ../BTLib/jni/luajava/lauxlib.h

echo "================================="
echo "🥳 Done"
echo "================================="

#NDK=$NDK_HOME
#MAKE_BIN_DIR="${NDK_HOME}/prebuilt/linux-x86_64/bin"
#
##The target LuaJit Library source archive unpacked location.
#LUAJIT="LuaJIT-2.0.5"
#cd ./$LUAJIT
#echo `pwd`
##Make sure that the luaconf.h file has been appropriately modified to include the search path ./lib
##This is very important.
#
#echo "**********************************************"
#echo "********* Cleaning prior builds. *************"
#echo "**********************************************"
#${MAKE_BIN_DIR}/make clean
#cd ..
#cd BTLib
#$NDK/ndk-build clean || /bin/true
#rm -rf ./jni/luajava/luaconf.h
#rm -rf ./jni/luajava/lualib.h
#rm -rf ./jni/luajava/luajit.h
#rm -rf ./jni/luajava/lua.h
#rm -rf ./jni/luajava/libluajit-armeabi.so
#rm -rf ./jni/luajava/libluajit-armeabi.a
#rm -rf ./jni/luajava/libluajit-armv7-a.a
#rm -rf ./jni/luajava/libluajit-armv7-a.so
#rm -rf ./jni/luajava/libluajit-mips.a
#rm -rf ./jni/luajava/libluajit-mips.so
#rm -rf ./jni/luajava/libluajit-x86.a
#rm -rf ./jni/luajava/libluajit-x86.so
#rm -rf ./jni/luajava/lauxlib.h
#cd ..
#
#echo "**********************************************"
#echo "*************  STARTING BUILD ****************"
#echo "**********************************************"
#cd ./$LUAJIT
##start building.
#NDKABI=14
#
#NDKVER=$NDK/toolchains/arm-linux-androideabi-4.9
#DK_HOST_CC_TARGET <- darwin-x86_64 for osx, linux-x86_64 for ubuntu
#NDKP=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/$NDK_HOST_CC_TARGET/bin/arm-linux-androideabi-

#arm
#NDKF="--sysroot $NDK/platforms/android-$NDKABI/arch-arm"
#echo "Building ARMEABI Targets"
#${MAKE_BIN_DIR}/make HOST_CC="gcc-7 -m32" CROSS=$NDKP TARGET_FLAGS="$NDKF" TARGET_SYS=Other#
#mv src/libluajit.a src/libluajit-armeabi.a
#mv src/libluajit.so src/libluajit-armeabi.so
#
##armv7a
#NDKF="--sysroot $NDK/platforms/android-$NDKABI/arch-arm"
#NDKARCH="-march=armv7-a -mfloat-abi=softfp -Wl,--fix-cortex-a8"
#echo "Building ARMv7A TARGETS"
#${MAKE_BIN_DIR}/make clean
#${MAKE_BIN_DIR}/make HOST_CC="gcc-7 -m32" CROSS=$NDKP TARGET_FLAGS="$NDKF $NDKARCH" TARGET_SYS=Other
#mv src/libluajit.a src/libluajit-armv7-a.a
#mv src/libluajit.so src/libluajit-armv7-a.so
#
##mips
#NDKVER=$NDK/toolchains/mipsel-linux-android-4.9
#NDKP=$NDKVER/prebuilt/$NDK_HOST_CC_TARGET/bin/mipsel-linux-android-
#NDKF="--sysroot=$NDK/platforms/android-$NDKABI/arch-mips/"
#echo "Building MIPS Targets"
#${MAKE_BIN_DIR}/make clean
#${MAKE_BIN_DIR}/make HOST_CC="gcc-7 -m32" CROSS=$NDKP TARGET_FLAGS="$NDKF" TARGET_SYS=Other
#mv src/libluajit.a src/libluajit-mips.a
#mv src/libluajit.so src/libluajit-mips.so
#
#NDKVER=$NDK/toolchains/x86-4.9
#NDKP=$NDKVER/prebuilt/$NDK_HOST_CC_TARGET/bin/i686-linux-android-
#NDKF="--sysroot $NDK/platforms/android-$NDKABI/arch-x86/"
#echo "Making X86 Targets"
#${MAKE_BIN_DIR}/make clean
#${MAKE_BIN_DIR}/make HOST_CC="gcc-7 -m32" CROSS=$NDKP TARGET_FLAGS="$NDKF" TARGET_SYS=Other
#mv src/libluajit.a src/libluajit-x86.a
#mv src/libluajit.so src/libluajit-x86.so
#
##need to modify the above to produce output for the arm-v7 abi and the x86 and possibly mips.
##these libraries must be build with a "-<arch>.so" prefix that will match names in the android jni project.
#
##at this point the lua library should be ready to copy into the jni project in the BTLib subfolder to build luajava and the other lua extensions.
#echo "Copying $LUAJIT output to LuaJava jni project in BTLib"
#cp src/libluajit-armeabi.a ../BTLib/jni/luajava/libluajit-armeabi.a
#cp src/libluajit-armeabi.so ../BTLib/jni/luajava/libluajit-armeabi.so
#cp src/libluajit-armv7-a.a ../BTLib/jni/luajava/libluajit-armv7-a.a
#cp src/libluajit-armv7-a.so ../BTLib/jni/luajava/libluajit-armv7-a.so
#cp src/libluajit-x86.a ../BTLib/jni/luajava/libluajit-x86.a
#cp src/libluajit-x86.so ../BTLib/jni/luajava/libluajit-x86.so
#cp src/libluajit-mips.a ../BTLib/jni/luajava/libluajit-mips.a
#cp src/libluajit-mips.so ../BTLib/jni/luajava/libluajit-mips.so
#
#
##copy the relevant header files into the luajava jni project folder in BTLib/jni/luajava, the other projects will reference it there.
#echo "Copying $LUAJIT source headers to LuaJava jni project in BTLib"
#cp src/lauxlib.h ../BTLib/jni/luajava/lauxlib.h
#cp src/lua.h ../BTLib/jni/luajava/lua.h
#cp src/luaconf.h ../BTLib/jni/luajava/luaconf.h
#cp src/luajit.h ../BTLib/jni/luajava/luajit.h
#cp src/lualib.h ../BTLib/jni/luajava/lualib.h
#
##move into the BTLib folder and build the android ndk projects (the lua extensions)
#echo "************************************************"
#echo "********** STARTING ANDROID NDK BUILD **********"
#echo "************************************************"
#cd ../BTLib
#$NDK/ndk-build
#
##the libraries should all be nicely packaged into the libs folder in the BTLib project and we shouldn't need to do anything else, if the luaconfig trick worked.
