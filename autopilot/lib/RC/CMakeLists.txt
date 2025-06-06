cmake_minimum_required(VERSION 3.10)
project(RC)

set(CMAKE_CXX_STANDARD 17)

enable_testing()

# Source files
add_library(rcsignal RCSignal.cpp)

# Test files
add_executable(RCSignalTest test/RCSignalTest.cpp)

target_link_libraries(RCSignalTest rcsignal gtest gtest_main pthread)

include(GoogleTest)
gtest_discover_tests(RCSignalTest)
