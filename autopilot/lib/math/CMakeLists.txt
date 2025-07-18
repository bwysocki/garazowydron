cmake_minimum_required(VERSION 3.10)
project(Math)

set(CMAKE_CXX_STANDARD 17)

enable_testing()

# Source files
add_library(quaternion Quaternion.cpp)

# Test files
add_executable(QuaternionTest test/QuaternionTest.cpp)
add_executable(RangeMapperTest test/RangeMapperTest.cpp)

target_link_libraries(QuaternionTest quaternion gtest gtest_main pthread)
target_link_libraries(RangeMapperTest quaternion gtest gtest_main pthread)

include(GoogleTest)
gtest_discover_tests(QuaternionTest)
gtest_discover_tests(RangeMapperTest)
