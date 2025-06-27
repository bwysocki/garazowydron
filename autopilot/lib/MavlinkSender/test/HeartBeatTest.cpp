#include <gtest/gtest.h>
#include <vector>
#include "../MavlinkSender.hpp"
#include "../../Uart/test/MockUartDriver.cpp"


// MAVLink v2 header constants
constexpr size_t MAVLINK_V2_HEADER_LEN = 10;
constexpr size_t MAVLINK_V2_MSGID_OFFSET = 7;
constexpr size_t MAVLINK_V2_SYSID_OFFSET = 5;
constexpr size_t MAVLINK_V2_COMPID_OFFSET = 6;

TEST(MavlinkSenderTest, SendHeartbeat_Default) {
    MockUartDriver mockDriver;
    UartTxQueue txQueue(mockDriver);
    MavlinkSender sender(txQueue);

    sender.sendHeartbeat();

    // Symulujemy zakończenie transmisji, aby dane były dostępne
    txQueue.onTransmitComplete();

    // Sprawdzamy czy coś zostało wysłane
    ASSERT_FALSE(mockDriver.sentPackets.empty());
    const auto& buffer = mockDriver.sentPackets[0];

    ASSERT_GT(buffer.size(), 0u);
    EXPECT_EQ(buffer[0], 0xFD);  // MAVLink 2.0 start byte

    // Sprawdź długość payloadu
    uint8_t payload_len = buffer[1];
    EXPECT_EQ(payload_len, 9);  // HEARTBEAT ma 9 bajtów payloadu

    // Sprawdź system_id i component_id
    EXPECT_EQ(buffer[MAVLINK_V2_SYSID_OFFSET], SYSTEM_ID_GCS);  // default system_id
    EXPECT_EQ(buffer[MAVLINK_V2_COMPID_OFFSET], COMPONENT_ID_MISSIONPLANNER); // default component_id

    // Sprawdź ID wiadomości (HEARTBEAT = 0)
    uint32_t msg_id =
        buffer[MAVLINK_V2_MSGID_OFFSET] |
        (buffer[MAVLINK_V2_MSGID_OFFSET + 1] << 8) |
        (buffer[MAVLINK_V2_MSGID_OFFSET + 2] << 16);

    EXPECT_EQ(msg_id, MAVLINK_MSG_ID_HEARTBEAT);

    // Sprawdź pole `type` (5. bajt payloadu = offset 4)
    const size_t payload_offset = MAVLINK_V2_HEADER_LEN;
    uint8_t type = buffer[payload_offset + 4];
    EXPECT_EQ(type, MAV_TYPE_GCS);
}

TEST(MavlinkSenderTest, SendHeartbeat_CustomSystemComponent) {
    MockUartDriver mockDriver;
    UartTxQueue txQueue(mockDriver);
    MavlinkSender sender(txQueue);

    sender.sendHeartbeat(42, 77, MAV_TYPE_QUADROTOR);  // custom system_id, comp_id, typ

    txQueue.onTransmitComplete();

    ASSERT_FALSE(mockDriver.sentPackets.empty());
    const auto& buffer = mockDriver.sentPackets[0];

    ASSERT_GT(buffer.size(), 0u);

    // Sprawdź poprawne ID nadawcy
    EXPECT_EQ(buffer[MAVLINK_V2_SYSID_OFFSET], 42);
    EXPECT_EQ(buffer[MAVLINK_V2_COMPID_OFFSET], 77);

    // Sprawdź ID wiadomości
    uint32_t msg_id =
        buffer[MAVLINK_V2_MSGID_OFFSET] |
        (buffer[MAVLINK_V2_MSGID_OFFSET + 1] << 8) |
        (buffer[MAVLINK_V2_MSGID_OFFSET + 2] << 16);

    EXPECT_EQ(msg_id, MAVLINK_MSG_ID_HEARTBEAT);

    // Sprawdź typ pojazdu w payloadzie
    const size_t payload_offset = MAVLINK_V2_HEADER_LEN;
    uint8_t type = buffer[payload_offset + 4];
    EXPECT_EQ(type, MAV_TYPE_QUADROTOR);
}

