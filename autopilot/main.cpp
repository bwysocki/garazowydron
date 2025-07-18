
#include <iostream>
#include <cstring>       
#include "lib/mavlink/common/mavlink.h"       

using namespace std;

int main() {
    mavlink_message_t msg{};
    uint8_t buffer[MAVLINK_MAX_PACKET_LEN];

    // Spakuj wiadomość HEARTBEAT
    mavlink_msg_heartbeat_pack(
        1,                      // system_id
        200,                    // component_id
        &msg,
        MAV_TYPE_QUADROTOR,     // typ pojazdu
        MAV_AUTOPILOT_GENERIC,  // typ autopilota
        MAV_MODE_GUIDED_ARMED,  // tryb pracy
        0,                      // custom_mode
        MAV_STATE_ACTIVE        // status
    );

    // Zakoduj wiadomość do bajtów (np. do wysłania po UART)
    uint16_t len = mavlink_msg_to_send_buffer(buffer, &msg);

    std::cout << "HEARTBEAT sent (" << len << " bytes): ";
    for (uint16_t i = 0; i < len; ++i) {
        printf("%02X ", buffer[i]);
    }
    std::cout << std::endl;

    return 0;
}
