cmake_minimum_required(VERSION 3.10)
project(Math)

set(CMAKE_CXX_STANDARD 17)

enable_testing()

# Source files
add_library(quaternion Quaternion.cpp)

# Test files
add_executable(QuaternionTest test/QuaternionTest.cpp)

target_link_libraries(QuaternionTest quaternion gtest gtest_main pthread)

include(GoogleTest)
gtest_discover_tests(QuaternionTest)
