#include <gtest/gtest.h>
#include "../MavlinkSender.hpp"

#include <vector>

class ByteCapture {
public:
    void write(uint8_t byte) {
        buffer.push_back(byte);
    }

    std::vector<uint8_t> buffer;
};

TEST(MavlinkSenderTest, SendArmCommand_ArmTrue) {
    ByteCapture capture;
    MavlinkSender sender([&](uint8_t byte) { capture.write(byte); });

    // Wysyłamy komendę uzbrojenia
    sender.sendArmCommand(1, true);

    ASSERT_GT(capture.buffer.size(), 0);

    // Sprawdź start bajt MAVLink v2
    EXPECT_EQ(capture.buffer[0], 0xFD);

    // Parsowanie wiadomości
    mavlink_message_t msg;
    mavlink_status_t status;
    bool parsed = false;

    for (uint8_t byte : capture.buffer) {
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
