#include "../UartRxQueue.hpp"
#include <gtest/gtest.h>

using namespace uart;

class UartRxQueueTest : public ::testing::Test {
protected:
    UartRxQueue queue;
};

TEST_F(UartRxQueueTest, InitiallyBufferIsEmpty) {
    uint8_t byte;
    EXPECT_FALSE(queue.pollByte(byte));
    EXPECT_FALSE(queue.hasOverflowed());
    EXPECT_EQ(queue.getOverflowCount(), 0u);
}

TEST_F(UartRxQueueTest, AddAndReadSingleByte) {
    queue.onByteReceived(0xAB);

    uint8_t byte;
    EXPECT_TRUE(queue.pollByte(byte));
    EXPECT_EQ(byte, 0xAB);

    // Bufor powinien być pusty po odczycie
    EXPECT_FALSE(queue.pollByte(byte));
}

TEST_F(UartRxQueueTest, AddMultipleBytesInOrder) {
    for (uint8_t i = 0; i < 10; ++i) {
        queue.onByteReceived(i);
    }

    for (uint8_t i = 0; i < 10; ++i) {
        uint8_t byte;
        EXPECT_TRUE(queue.pollByte(byte));
        EXPECT_EQ(byte, i);
    }

    uint8_t byte;
    EXPECT_FALSE(queue.pollByte(byte));
}

TEST_F(UartRxQueueTest, OverflowIsDetected) {
    // Wypełnij bufor do pełna
    for (uint16_t i = 0; i < UartRxQueue::BUFFER_SIZE; ++i) {
        queue.onByteReceived(i & 0xFF);
    }

    // Teraz każdy dodatkowy bajt powinien zostać zignorowany
    queue.onByteReceived(0xAA);
    queue.onByteReceived(0xBB);

    EXPECT_TRUE(queue.hasOverflowed());
    EXPECT_GE(queue.getOverflowCount(), 2u);
}

TEST_F(UartRxQueueTest, ClearOverflowFlagWorks) {
    for (uint16_t i = 0; i < UartRxQueue::BUFFER_SIZE; ++i) {
        queue.onByteReceived(i & 0xFF);
    }
    queue.onByteReceived(0xCC);

    ASSERT_TRUE(queue.hasOverflowed());

    queue.clearOverflowFlag();
    EXPECT_FALSE(queue.hasOverflowed());
}

TEST_F(UartRxQueueTest, BufferWrapAroundWorksCorrectly) {
    // Wypełnij trochę, potem odczytaj, a następnie dodaj ponownie (sprawdza zawijanie)
    for (uint8_t i = 0; i < 10; ++i) {
        queue.onByteReceived(i);
    }

    uint8_t byte;
    for (uint8_t i = 0; i < 10; ++i) {
        EXPECT_TRUE(queue.pollByte(byte));
        EXPECT_EQ(byte, i);
    }

    // Powtórz cykl – bufor powinien działać dalej
    for (uint8_t i = 10; i < 20; ++i) {
        queue.onByteReceived(i);
    }

    for (uint8_t i = 10; i < 20; ++i) {
        EXPECT_TRUE(queue.pollByte(byte));
        EXPECT_EQ(byte, i);
    }
}
