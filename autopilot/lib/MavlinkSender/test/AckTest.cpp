#include <gtest/gtest.h>
#include <vector>
#include "../MavlinkSender.hpp"
#include "../../Uart/test/MockUartDriver.cpp"


TEST(MavlinkSenderTest, Ack_SendsCorrectCommandAck) {
    MockUartDriver mockDriver;
    UartTxQueue txQueue(mockDriver);
    MavlinkSender sender(txQueue);

    // Wysyłamy ACK dla komendy ARM z wynikiem ACCEPTED
    sender.ack(
        MAV_CMD_COMPONENT_ARM_DISARM, // command
        MAV_RESULT_ACCEPTED,          // result
        255,                          // target_system (GCS)
        190,                          // target_component (Mission Planner)
        0,                            // progress
        0                             // result_param2
    );
    
    txQueue.onTransmitComplete();

    // Sprawdź, że coś zostało wysłane
    ASSERT_FALSE(mockDriver.sentPackets.empty());
    const auto& buffer = mockDriver.sentPackets[0];

    ASSERT_GT(buffer.size(), 0u);
    EXPECT_EQ(buffer[0], 0xFD) << "Pierwszy bajt powinien być znacznikiem MAVLink 2.0 (0xFD)";

    // Parsowanie wiadomości
    mavlink_message_t msg;
    mavlink_status_t status;
    bool parsed = false;

    for (uint8_t byte : buffer) {
        if (mavlink_parse_char(MAVLINK_COMM_0, byte, &msg, &status)) {
            parsed = true;
            break;
        }
    }

    ASSERT_TRUE(parsed) << "Nie udało się sparsować wiadomości MAVLink";

    // Sprawdź typ wiadomości
    EXPECT_EQ(msg.msgid, MAVLINK_MSG_ID_COMMAND_ACK);

    // Dekodowanie struktury
    mavlink_command_ack_t ack;
    mavlink_msg_command_ack_decode(&msg, &ack);

    // Sprawdzenie pól
    EXPECT_EQ(ack.command, MAV_CMD_COMPONENT_ARM_DISARM);
    EXPECT_EQ(ack.result, MAV_RESULT_ACCEPTED);
    EXPECT_EQ(ack.target_system, 255);
    EXPECT_EQ(ack.target_component, 190);
    EXPECT_EQ(ack.progress, 0);
    EXPECT_EQ(ack.result_param2, 0);
}
