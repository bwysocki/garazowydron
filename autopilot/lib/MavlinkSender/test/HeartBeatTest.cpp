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

// MAVLink v2 header constants
constexpr size_t MAVLINK_V2_HEADER_LEN = 10;
constexpr size_t MAVLINK_V2_MSGID_OFFSET = 7;
constexpr size_t MAVLINK_V2_SYSID_OFFSET = 5;
constexpr size_t MAVLINK_V2_COMPID_OFFSET = 6;

// Test: czy wiadomość HEARTBEAT jest poprawnie generowana
TEST(MavlinkSenderTest, SendHeartbeat_Default) {
    ByteCapture capture;
    MavlinkSender sender([&](uint8_t byte) { capture.write(byte); });

    sender.sendHeartbeat();

    // Sprawdzamy czy coś zostało wysłane
    ASSERT_GT(capture.buffer.size(), 0);

    // Sprawdź start bajt MAVLink v2
    EXPECT_EQ(capture.buffer[0], 0xFD);  // MAVLink 2.0 start byte

    // Sprawdź długość payloadu
    uint8_t payload_len = capture.buffer[1];
    EXPECT_EQ(payload_len, 9);  // HEARTBEAT ma 9 bajtów payloadu

    // Sprawdź system_id i component_id
    EXPECT_EQ(capture.buffer[MAVLINK_V2_SYSID_OFFSET], SYSTEM_ID_GCS);    // default system_id
    EXPECT_EQ(capture.buffer[MAVLINK_V2_COMPID_OFFSET], COMPONENT_ID_MISSIONPLANNER); // default component_id

    // Sprawdź ID wiadomości (HEARTBEAT = 0)
    uint32_t msg_id =
        capture.buffer[MAVLINK_V2_MSGID_OFFSET] |
        (capture.buffer[MAVLINK_V2_MSGID_OFFSET + 1] << 8) |
        (capture.buffer[MAVLINK_V2_MSGID_OFFSET + 2] << 16);

    EXPECT_EQ(msg_id, MAVLINK_MSG_ID_HEARTBEAT);

    // Offset payloadu = 10 bajtów nagłówka
    const size_t payload_offset = MAVLINK_V2_HEADER_LEN;

    // Sprawdź pole `type` (pierwszy bajt payloadu)
    uint8_t type = capture.buffer[payload_offset + 4];  // offset type w payloadzie
    EXPECT_EQ(type, MAV_TYPE_GCS);
}

// Test: custom heartbeat (inne system_id/component_id/type)
TEST(MavlinkSenderTest, SendHeartbeat_CustomSystemComponent) {
    ByteCapture capture;
    MavlinkSender sender([&](uint8_t byte) { capture.write(byte); });

    sender.sendHeartbeat(42, 77, MAV_TYPE_QUADROTOR);  // custom system_id, comp_id, typ GCS

    ASSERT_GT(capture.buffer.size(), 0);

    // Sprawdź poprawne ID nadawcy
    EXPECT_EQ(capture.buffer[MAVLINK_V2_SYSID_OFFSET], 42);
    EXPECT_EQ(capture.buffer[MAVLINK_V2_COMPID_OFFSET], 77);

    // Sprawdź ID wiadomości
    uint32_t msg_id =
        capture.buffer[MAVLINK_V2_MSGID_OFFSET] |
        (capture.buffer[MAVLINK_V2_MSGID_OFFSET + 1] << 8) |
        (capture.buffer[MAVLINK_V2_MSGID_OFFSET + 2] << 16);

    EXPECT_EQ(msg_id, MAVLINK_MSG_ID_HEARTBEAT);

    // Sprawdź typ pojazdu w payloadzie
    const size_t payload_offset = MAVLINK_V2_HEADER_LEN;
    uint8_t type = capture.buffer[payload_offset + 4];
    EXPECT_EQ(type, MAV_TYPE_QUADROTOR);
}
