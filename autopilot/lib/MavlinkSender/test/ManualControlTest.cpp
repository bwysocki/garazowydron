#include <gtest/gtest.h>
#include <vector>
#include "../MavlinkSender.hpp"
#include "../../Uart/test/MockUartDriver.cpp"


TEST(MavlinkSenderTest, SendManualControl_Default) {
    MockUartDriver mockDriver;
    UartTxQueue txQueue(mockDriver);
    MavlinkSender sender(txQueue);

    // Wysyłamy komendę manual control
    sender.sendManualControl(1, 100, -200, 300, -400);

    // Symulujemy zakończenie transmisji
    txQueue.onTransmitComplete();

    // Sprawdzamy, czy coś zostało wysłane
    ASSERT_FALSE(mockDriver.sentPackets.empty());
    const auto& buffer = mockDriver.sentPackets[0];

    ASSERT_GT(buffer.size(), 0u);
    EXPECT_EQ(buffer[0], 0xFD); // MAVLink v2 start byte

    // Dekodowanie wiadomości z bufora
    mavlink_message_t msg;
    mavlink_status_t status;
    bool parsed = false;

    for (uint8_t byte : buffer) {
        if (mavlink_parse_char(MAVLINK_COMM_0, byte, &msg, &status)) {
            parsed = true;
            break;
        }
    }

    ASSERT_TRUE(parsed) << "Nie udało się sparsować wiadomości MAVLink z bufora";

    // Sprawdź czy to MANUAL_CONTROL
    EXPECT_EQ(msg.msgid, MAVLINK_MSG_ID_MANUAL_CONTROL);

    // Dekodujemy strukturę
    mavlink_manual_control_t manual;
    mavlink_msg_manual_control_decode(&msg, &manual);

    // Sprawdzamy wartości
    EXPECT_EQ(manual.target, 1);
    EXPECT_EQ(manual.x, 100);
    EXPECT_EQ(manual.y, -200);
    EXPECT_EQ(manual.z, 300);
    EXPECT_EQ(manual.r, -400);
    EXPECT_EQ(manual.buttons, 0);
}
