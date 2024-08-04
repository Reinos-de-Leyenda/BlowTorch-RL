cmake_minimum_required(VERSION 3.22.1)
project(luabins)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../luajava)
file(GLOB_RECURSE SOURCES ${CMAKE_CURRENT_SOURCE_DIR}/*.c)
file(GLOB_RECURSE HEADERS ${CMAKE_CURRENT_SOURCE_DIR}/*.h)

add_library(${PROJECT_NAME} SHARED ${SOURCES} ${HEADERS})
target_link_libraries(${PROJECT_NAME} lua)