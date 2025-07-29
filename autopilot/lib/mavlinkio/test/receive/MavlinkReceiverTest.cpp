#include <gtest/gtest.h>
#include <vector>
#include "../../MavlinkMessageHandler.hpp"
#include "../../MavlinkReceiver.hpp"
#include "../../../uart/UartRxQueue.hpp" 

using namespace mavlinkio;

// Prosty handler do testów, zapisuje ostatnią wiadomość
class TestMessageHandler : public MavlinkMessageHandler {
public:
    bool received = false;
    mavlink_message_t lastMsg;

    void onMessageReceived(const mavlink_message_t& msg) override {
        received = true;
        lastMsg = msg;
    }
};

TEST(MavlinkReceiverTest, ParsesValidHeartbeatMessage) {
    TestMessageHandler handler;
    MavlinkReceiver receiver(handler);

    mavlink_message_t msg;
    uint8_t buffer[MAVLINK_MAX_PACKET_LEN];

    // Pakujemy wiadomość typu HEARTBEAT
    mavlink_msg_heartbeat_pack(
        1, 200, &msg,
        MAV_TYPE_GENERIC,
        MAV_AUTOPILOT_GENERIC,
        MAV_MODE_MANUAL_ARMED,
        0,
        MAV_STATE_ACTIVE);

    size_t len = mavlink_msg_to_send_buffer(buffer, &msg);

    // Wysyłamy bajty pojedynczo (symulacja odbioru przez UART)
    for (size_t i = 0; i < len; ++i) {
        receiver.receiveByte(buffer[i]);
    }

    ASSERT_TRUE(handler.received) << "Nie odebrano żadnej wiadomości";
    EXPECT_EQ(handler.lastMsg.msgid, MAVLINK_MSG_ID_HEARTBEAT);

    // Dekodujemy i sprawdzamy zawartość
    mavlink_heartbeat_t heartbeat;
    mavlink_msg_heartbeat_decode(&handler.lastMsg, &heartbeat);

    EXPECT_EQ(heartbeat.type, MAV_TYPE_GENERIC);
    EXPECT_EQ(heartbeat.autopilot, MAV_AUTOPILOT_GENERIC);
    EXPECT_EQ(heartbeat.base_mode, MAV_MODE_MANUAL_ARMED);
    EXPECT_EQ(heartbeat.system_status, MAV_STATE_ACTIVE);
}

TEST(MavlinkReceiverTest, ParsesValidHeartbeatMessageUsingReceiveBuffer) {
    TestMessageHandler handler;
    MavlinkReceiver receiver(handler);

    mavlink_message_t msg;
    uint8_t buffer[MAVLINK_MAX_PACKET_LEN];

    // Pakujemy wiadomość typu HEARTBEAT
    mavlink_msg_heartbeat_pack(
        1, 200, &msg,
        MAV_TYPE_GENERIC,
        MAV_AUTOPILOT_GENERIC,
        MAV_MODE_MANUAL_ARMED,
        0,
        MAV_STATE_ACTIVE);

    size_t len = mavlink_msg_to_send_buffer(buffer, &msg);

    // Wysyłamy całość bufora na raz
    receiver.receiveBuffer(buffer, len);

    ASSERT_TRUE(handler.received) << "Nie odebrano żadnej wiadomości";
    EXPECT_EQ(handler.lastMsg.msgid, MAVLINK_MSG_ID_HEARTBEAT);

    // Dekodowanie i sprawdzanie zawartości
    mavlink_heartbeat_t heartbeat;
    mavlink_msg_heartbeat_decode(&handler.lastMsg, &heartbeat);

    EXPECT_EQ(heartbeat.type, MAV_TYPE_GENERIC);
    EXPECT_EQ(heartbeat.autopilot, MAV_AUTOPILOT_GENERIC);
    EXPECT_EQ(heartbeat.base_mode, MAV_MODE_MANUAL_ARMED);
    EXPECT_EQ(heartbeat.system_status, MAV_STATE_ACTIVE);
}

TEST(MavlinkReceiverTest, ParsesHeartbeatFromUartRxQueue) {
    TestMessageHandler handler;
    MavlinkReceiver receiver(handler);
    uart::UartRxQueue rxQueue;

    mavlink_message_t msg;
    uint8_t buffer[MAVLINK_MAX_PACKET_LEN];

    // Pakujemy wiadomość typu HEARTBEAT
    mavlink_msg_heartbeat_pack(
        1, 200, &msg,
        MAV_TYPE_GENERIC,
        MAV_AUTOPILOT_GENERIC,
        MAV_MODE_MANUAL_ARMED,
        0,
        MAV_STATE_ACTIVE);

    size_t len = mavlink_msg_to_send_buffer(buffer, &msg);

    // Symulujemy odbiór bajtów w ISR (wrzucenie do bufora)
    for (size_t i = 0; i < len; ++i) {
        rxQueue.onByteReceived(buffer[i]);
    }

    // Pętla główna – przetwarzanie danych z bufora
    receiver.processRxQueue(rxQueue);

    ASSERT_TRUE(handler.received) << "Nie odebrano wiadomości z bufora UART";
    EXPECT_EQ(handler.lastMsg.msgid, MAVLINK_MSG_ID_HEARTBEAT);

    mavlink_heartbeat_t heartbeat;
    mavlink_msg_heartbeat_decode(&handler.lastMsg, &heartbeat);

    EXPECT_EQ(heartbeat.type, MAV_TYPE_GENERIC);
    EXPECT_EQ(heartbeat.autopilot, MAV_AUTOPILOT_GENERIC);
    EXPECT_EQ(heartbeat.base_mode, MAV_MODE_MANUAL_ARMED);
    EXPECT_EQ(heartbeat.system_status, MAV_STATE_ACTIVE);
}