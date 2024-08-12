cmake_minimum_required(VERSION 3.22.1)
project(luabins)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../luajit/artifacts/headers/)
file(GLOB_RECURSE SOURCES ${CMAKE_CURRENT_SOURCE_DIR}/*.c)
add_library(${PROJECT_NAME} SHARED ${SOURCES})
target_link_libraries(${PROJECT_NAME} lua)