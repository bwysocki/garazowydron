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

TEST(MavlinkSenderTest, SendManualControl_Default) {
    ByteCapture capture;
    MavlinkSender sender([&](uint8_t byte) { capture.write(byte); });

    // Wysyłamy komendę manual control
    sender.sendManualControl(1, 100, -200, 300, -400);

    ASSERT_GT(capture.buffer.size(), 0);

    // Sprawdź start bajt MAVLink 2.0
    EXPECT_EQ(capture.buffer[0], 0xFD); // MAVLink v2

    // Przechodzimy do dekodowania wiadomości z bufora
    mavlink_message_t msg;
    mavlink_status_t status;
    bool parsed = false;

    for (uint8_t byte : capture.buffer) {
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
