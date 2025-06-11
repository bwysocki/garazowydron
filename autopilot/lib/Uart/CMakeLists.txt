cmake_minimum_required(VERSION 3.10)
project(Uart CXX)

set(CMAKE_CXX_STANDARD 17)
set(STM32_INC_PATH ${CMAKE_CURRENT_SOURCE_DIR}/../../smt32/Inc)

add_definitions(-DSTM32F103xB)

enable_testing()

# === Biblioteka ===
add_library(uart
    UartTxQueue.cpp
    HalUartDriver.cpp
)

target_include_directories(uart PUBLIC ${CMAKE_CURRENT_SOURCE_DIR} ${STM32_INC_PATH})

# === Testy ===
add_executable(UartTxQueueTest
    test/UartTxQueueTest.cpp
)

target_link_libraries(UartTxQueueTest
    uart
    gtest
    gtest_main
    pthread
)

include(GoogleTest)
gtest_discover_tests(UartTxQueueTest)
