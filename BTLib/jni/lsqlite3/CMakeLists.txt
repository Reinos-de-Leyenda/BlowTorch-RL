cmake_minimum_required(VERSION 3.22.1)
project(lsqlite3)

include_directories(
        ${CMAKE_CURRENT_SOURCE_DIR}/../sqlite3
        ${CMAKE_CURRENT_SOURCE_DIR}/../luajit/artifacts/headers/
)
add_library(${PROJECT_NAME} SHARED lsqlite3.c)
target_link_libraries(${PROJECT_NAME} sqlite3)
target_link_libraries(${PROJECT_NAME} lua)