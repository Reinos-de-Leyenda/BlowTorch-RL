#!/bin/bash

set -e
set -a
set -o pipefail

####################################################################################################
# Variables
####################################################################################################

readonly PROJECT_ROOT_DIR="$(pwd)"
# Path where LUAJIT will be cloend
readonly LUAJIT_ROOT_DIR="${PROJECT_ROOT_DIR}/BTLib/jni/luajit"
readonly LUAJIT_REPO_DIR="${LUAJIT_ROOT_DIR}/repo"
readonly LUAJIT_LIBS_DIR="${LUAJIT_ROOT_DIR}/artifacts/lib"
readonly LUAJIT_HEADERS_DIR="${LUAJIT_ROOT_DIR}/artifacts/headers"

# LuaJIT version to use. Can be a SHA, tag or any other ref git can handle
# See: https://github.com/LuaJIT/LuaJIT.git
# 04dca7911ea255f37be799c18d74c305b921c1a6 is acommit of v2.1 branch pinned for consistency
readonly LUAJIT_REF="04dca7911ea255f37be799c18d74c305b921c1a6"

# NDK setup
readonly NDKBIN="${NDKDIR}/toolchains/llvm/prebuilt/linux-x86_64/bin"
readonly SYSROOT="${NDKDIR}/toolchains/llvm/prebuilt/linux-x86_64/sysroot"
readonly NDKABI=29

# List of architectures we will compile for:
# - aarch64 : arm 64bit (AKA arm64-v8a)
# - armv7a  : arm 32bit (AKA armeabi-v7a, deprecated one: armeabi)
# - x86_64  : x86 64bit
# - i686    : x86 32bit # Not supported?
# - x86     : x86 32bit # Not supported?
# - mips    : deprecated
declare -a ARCHS
ARCHS=("i686" "x86_64" "aarch64" "armv7a")
readonly ARCHS

# A regexp that identifies the architectures that are 32bits
readonly ARCHS_32BIT_REGEXP="^(armv7a|i686)$"

####################################################################################################
# Functions
####################################################################################################

function header() {
  echo "================================="
  echo "$1"
  echo "================================="
}

function info() {
  echo "$(date +%FT%TZ%Z) [info]: $1" >& 2
}

function warning() {
  echo "$date +%FT%TZ%Z) [warn]: $1" >& 2
}

function error() {
  echo "$date +%FT%TZ%Z) [error]: $1" >& 2
}

function setup_luajit_repo() {
  if [[ -d "${LUAJIT_REPO_DIR}" ]]; then
    info "LuaJIT repo already exists, skipping clone."
  else
    info "Cloning LuaJIT repo..."
    git clone https://github.com/LuaJIT/LuaJIT.git "${LUAJIT_REPO_DIR}"
  fi

  info "Checking out LuaJIT ${LUAJIT_REF}..."
  cd "${LUAJIT_REPO_DIR}"
  git fetch --all
  git stash > /dev/null || /bin/true
  git checkout "${LUAJIT_REF}"
  git stash pop > /dev/null || /bin/true
  info "Checked out LuaJIT."
  cd ..
}

function build_static_libraries() {
  info "Cleaning old artifacts..."
  if ls "${LUAJIT_HEADERS_DIR}/*.h"; then
    rm "${LUAJIT_HEADERS_DIR}/*.h"
  fi

  if ls "${LUAJIT_LIBS_DIR}/*.a"; then
    rm "${LUAJIT_LIBS_DIR}/*.a"
  fi

  info "Starting compilation..."
  cd "${LUAJIT_REPO_DIR}"
  for arch in "${ARCHS[@]}"; do
    NDKCROSS="${NDKDIR}/${arch}-linux-android-"
    NDKCC="${NDKBIN}/${arch}-linux-androideabi${NDKABI}-clang"

    if [[ ! -f "${NDKCC}" ]]; then
      if [[ -f "${NDKBIN}/${arch}-linux-android${NDKABI}-clang" ]]; then
        NDKCC="${NDKBIN}/${arch}-linux-android${NDKABI}-clang"
      fi
    fi

    header "Compiling for ${arch} (STATIC_CC=${NDKCC}, DYNAMIC_CC=${NDKCC} -fPIC)"

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

    # Static artifacts required to create the JNIs used in all architectures
    mv src/libluajit.a "${LUAJIT_LIBS_DIR}/libluajit-${arch}.a"
  done

  header "Copying header files..."
  cp src/lauxlib.h "${LUAJIT_HEADERS_DIR}"
  cp src/lua.h "${LUAJIT_HEADERS_DIR}"
  cp src/luaconf.h "${LUAJIT_HEADERS_DIR}"
  cp src/luajit.h "${LUAJIT_HEADERS_DIR}"
  cp src/lualib.h "${LUAJIT_HEADERS_DIR}"
}

function apply_patches() {
  header "Applying patches..."

  # We need LuaJIT 2.1.0 for aarch support, but that version dropped some retrocompat that is
  # nowadays required. This ugly hack makes it work for now. Please preserve the whitespaces
  # See commit dc320ca70f2c5bb3977b82853bcee6dad2523d01 for LuaJIT
  sed -i '17i #define luaL_setn(L,i,j)        ((void)0)  /* no op! */' "${LUAJIT_HEADERS_DIR}/lauxlib.h"
  sed -i '17i #define luaL_getn(L,i)          ((int)lua_objlen(L, i))' "${LUAJIT_HEADERS_DIR}/lauxlib.h"
  sed -i '160i #define luaL_reg       luaL_Reg' "${LUAJIT_HEADERS_DIR}/lauxlib.h"

  info "Patches applied."
}

####################################################################################################
# Main loop
####################################################################################################
function main() {
  if [[ ! -d "./BTLib" ]] || [[ ! -d "./BT_Free" ]]; then
    error "This script needs to be executed from the project's root directory."
    exit 1
  fi

  # NDK management
  if [[ -z "${NDKDIR}" ]]; then
    error "Please set up the variable NDKDIR."
    exit 1
  fi

  info "Using NDKDIR '${NDKDIR}'."
  if [[ ! -d ${NDKDIR} ]]; then
    error "Can't find directory ${NDKDIR}."
    exit 1
  fi

  # Clone the specified LuaJIT version
  setup_luajit_repo

  # Clean up artifacts, build new ones, move the header files to the luajit dir
  build_static_libraries

  # Nowadays we need some workarounds for everything to work together
  apply_patches

  header "🥳 Done"
}

main "$@"
