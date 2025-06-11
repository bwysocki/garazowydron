#include "MavlinkSender.hpp"

MavlinkSender::MavlinkSender(ByteWriter writer)
    : writeByte(std::move(writer)) {}

void MavlinkSender::sendHeartbeat(
    uint8_t system_id,
    uint8_t component_id,
    uint8_t type,
    uint8_t autopilot,
    uint8_t base_mode,
    uint32_t custom_mode,
    uint8_t system_status)
{
    mavlink_message_t msg;
    mavlink_msg_heartbeat_pack(
        system_id,
        component_id,
        &msg,
        type,
        autopilot,
        base_mode,
        custom_mode,
        system_status);
    sendMessage(msg);
}

void MavlinkSender::sendManualControl(
    uint8_t target_id,
    int16_t x,
    int16_t y,
    int16_t z,
    int16_t r)
{
    mavlink_message_t msg;

    mavlink_msg_manual_control_pack(
        SYSTEM_ID_GCS,               // system_id nadawcy (np. 255)
        COMPONENT_ID_MISSIONPLANNER, // component_id nadawcy (np. 190)
        &msg,
        target_id,       // target system (id drona)
        x,               // pitch (X)
        y,               // roll (Y)
        z,               // throttle (Z)
        r,               // yaw (R)
        0,               // buttons
        0,               // buttons2
        0,               // enabled_extensions (żaden bit nie jest ustawiony)
        0, 0,            // s, t
        0, 0, 0, 0, 0, 0 // aux1–aux6
    );
    sendMessage(msg);
}

void MavlinkSender::sendArmCommand(
    uint8_t target_system,
    bool arm)
{
    mavlink_message_t msg;

    mavlink_msg_command_long_pack(
        SYSTEM_ID_GCS,               // system_id nadawcy
        COMPONENT_ID_MISSIONPLANNER, // component_id nadawcy
        &msg,
        target_system,                // ID drona
        0,                            // target_component (0 = all)
        MAV_CMD_COMPONENT_ARM_DISARM, // komenda ARM/DISARM
        1,                            // confirmation
        arm ? 1.0f : 0.0f,            // param1: 1 = arm, 0 = disarm
        0.0f,                         // param2
        0.0f,                         // param3
        0.0f,                         // param4
        0.0f,                         // param5
        0.0f,                         // param6
        0.0f                          // param7
    );

    sendMessage(msg);
}

void MavlinkSender::sendMessage(const mavlink_message_t &msg)
{
    uint8_t buffer[MAVLINK_MAX_PACKET_LEN];
    uint16_t len = mavlink_msg_to_send_buffer(buffer, &msg);
    for (uint16_t i = 0; i < len; ++i)
    {
        writeByte(buffer[i]);
    }
}
