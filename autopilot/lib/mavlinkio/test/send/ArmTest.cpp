#include <gtest/gtest.h>
#include <vector>
#include "../../MavlinkSender.hpp"
#include "../../../uart/test/MockUartDriver.cpp"


TEST(MavlinkSenderTest, SendArmCommand_ArmTrue) {
    MockUartDriver mockDriver;
    UartTxQueue txQueue(mockDriver);
    mavlinkio::MavlinkSender sender(txQueue);

    // Wysyłamy komendę uzbrojenia (ARM)
    sender.sendArmCommand(1, true);

    // Symulujemy zakończenie transmisji
    txQueue.onTransmitComplete();

    // Sprawdzamy, że coś zostało wysłane
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
    EXPECT_EQ(msg.msgid, MAVLINK_MSG_ID_COMMAND_LONG);

    // Dekodowanie struktury
    mavlink_command_long_t cmd;
    mavlink_msg_command_long_decode(&msg, &cmd);

    // Sprawdzenie pól
    EXPECT_EQ(cmd.target_system, 1);
    EXPECT_EQ(cmd.target_component, 0);
    EXPECT_EQ(cmd.command, MAV_CMD_COMPONENT_ARM_DISARM);
    EXPECT_EQ(cmd.confirmation, 1);
    EXPECT_FLOAT_EQ(cmd.param1, 1.0f); // arm = true
    EXPECT_FLOAT_EQ(cmd.param2, 0.0f);
}
