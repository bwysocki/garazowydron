#include <gtest/gtest.h>
#include "../UartTxQueue.hpp"

using namespace uart;

/**
 * @brief Mockowa implementacja UartDriver do testów jednostkowych.
 *
 * Przechowuje dane ostatnio wysłanego pakietu oraz zlicza liczbę transmisji.
 */
class MockUartDriver : public UartDriver {
public:
    bool autoComplete = false;
    UartTxQueue* queue = nullptr;

    std::vector<std::vector<uint8_t>> sentPackets;

    void transmit(const uint8_t* data, uint16_t length) override {
        sentPackets.emplace_back(data, data + length);
        if (autoComplete && queue) {
            queue->onTransmitComplete();
        }
    }

    void reset() {
        sentPackets.clear();
    }

    size_t transmissions() const {
        return sentPackets.size();
    }

    const std::vector<uint8_t>& lastPacket() const {
        return sentPackets.back();
    }
};

TEST(UartTxQueueTest, SinglePacketTransmitsImmediately) {
    MockUartDriver mock;
    UartTxQueue queue(mock);

    uint8_t msg[] = {0xAA, 0xBB};
    ASSERT_TRUE(queue.enqueue(msg, sizeof(msg)));

    ASSERT_EQ(mock.transmissions(), 1);
    ASSERT_EQ(mock.lastPacket(), std::vector<uint8_t>({0xAA, 0xBB}));
}

TEST(UartTxQueueTest, MultiplePacketsAreQueuedAndTransmittedSequentially) {
    MockUartDriver mock;
    UartTxQueue queue(mock);

    uint8_t msg1[] = {0x01};
    uint8_t msg2[] = {0x02};
    uint8_t msg3[] = {0x03};

    ASSERT_TRUE(queue.enqueue(msg1, sizeof(msg1)));
    ASSERT_TRUE(queue.enqueue(msg2, sizeof(msg2)));
    ASSERT_TRUE(queue.enqueue(msg3, sizeof(msg3)));

    // Po pierwszym enqueue transmisja powinna być natychmiastowa
    ASSERT_EQ(mock.transmissions(), 1);
    ASSERT_EQ(mock.lastPacket(), std::vector<uint8_t>({0x01}));

    // symulujemy zakończenie transmisji 1T
}

TEST(UartTxQueueTest, RejectsTooLargePacket) {
    MockUartDriver mock;
    UartTxQueue queue(mock);

    uint8_t tooBig[65] = {0}; // MAX_PACKET_SIZE is 64
    ASSERT_FALSE(queue.enqueue(tooBig, sizeof(tooBig)));

    ASSERT_EQ(mock.transmissions(), 0);
}

TEST(UartTxQueueTest, RejectsWhenQueueFull) {
    MockUartDriver mock;
    UartTxQueue queue(mock);
    mock.queue = &queue;
    mock.autoComplete = false; // nie kończ transmisji automatycznie

    uint8_t data[] = {0x00};
    for (int i = 0; i < 9; ++i) {
        ASSERT_TRUE(queue.enqueue(data, sizeof(data)));
    }

    // Dziesiąta próba powinna zwrócić false
    ASSERT_FALSE(queue.enqueue(data, sizeof(data)));
}
