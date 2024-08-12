cmake_minimum_required(VERSION 3.22.1)
project(marshal)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../luajit/artifacts/headers/)
add_library(${PROJECT_NAME} SHARED lmarshal.c)
target_link_libraries(${PROJECT_NAME} lua)
