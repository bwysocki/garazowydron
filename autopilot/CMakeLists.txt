cmake_minimum_required(VERSION 3.10)
project(autopilot VERSION 0.01 LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
enable_testing()

include_directories(${CMAKE_SOURCE_DIR}/smt32/lib)

# Tests
add_subdirectory(lib/uart)
add_subdirectory(lib/mavlinkio)
add_subdirectory(lib/math)

add_executable(${PROJECT_NAME})                   
target_sources(${PROJECT_NAME} PRIVATE main.cpp) 
