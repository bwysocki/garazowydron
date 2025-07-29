cmake_minimum_required(VERSION 3.10)
project(mavlinkio)

set(CMAKE_CXX_STANDARD 17)

enable_testing()

# Source files
add_library(mavlinkio MavlinkSender.cpp ../uart/UartTxQueue.cpp ../uart/UartRxQueue.cpp)


# Test files
add_executable(HeartBeatTest test/send/HeartBeatTest.cpp)
add_executable(ManualControlTest test/send/ManualControlTest.cpp)
add_executable(ArmTest test/send/ArmTest.cpp)
add_executable(AckTest test/send/AckTest.cpp)
add_executable(MavlinkReceiverTest test/receive/MavlinkReceiverTest.cpp)

target_link_libraries(HeartBeatTest mavlinkio gtest gtest_main pthread)
target_link_libraries(ManualControlTest mavlinkio gtest gtest_main pthread)
target_link_libraries(ArmTest mavlinkio gtest gtest_main pthread)
target_link_libraries(AckTest mavlinkio gtest gtest_main pthread)
target_link_libraries(MavlinkReceiverTest mavlinkio gtest gtest_main pthread)

include(GoogleTest)
gtest_discover_tests(HeartBeatTest)
gtest_discover_tests(ManualControlTest)
gtest_discover_tests(ArmTest)
gtest_discover_tests(AckTest)
gtest_discover_tests(MavlinkReceiverTest)
