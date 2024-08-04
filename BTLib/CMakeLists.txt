cmake_minimum_required(VERSION 3.22.1)
project(BTLib)

# luajava
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/jni/luajava)
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/jni/bit)
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/jni/lmarshal)
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/jni/luabins)
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/jni/sqlite3)
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/jni/lsqlite3)
