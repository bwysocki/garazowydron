cmake_minimum_required(VERSION 3.10)
project(MavlinkSender)

set(CMAKE_CXX_STANDARD 17)

enable_testing()

# Source files
add_library(mavlinkSender MavlinkSender.cpp ../uart/UartTxQueue.cpp)


# Test files
add_executable(HeartBeatTest test/HeartBeatTest.cpp)
add_executable(ManualControlTest test/ManualControlTest.cpp)
add_executable(ArmTest test/ArmTest.cpp)
add_executable(AckTest test/AckTest.cpp)

target_link_libraries(HeartBeatTest mavlinkSender gtest gtest_main pthread)
target_link_libraries(ManualControlTest mavlinkSender gtest gtest_main pthread)
target_link_libraries(ArmTest mavlinkSender gtest gtest_main pthread)
target_link_libraries(AckTest mavlinkSender gtest gtest_main pthread)

include(GoogleTest)
gtest_discover_tests(HeartBeatTest)
gtest_discover_tests(ManualControlTest)
gtest_discover_tests(ArmTest)
gtest_discover_tests(AckTest)
