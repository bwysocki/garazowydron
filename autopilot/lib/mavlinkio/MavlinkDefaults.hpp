#pragma once

#include <cstdint>
#include "../mavlink/common/mavlink.h"

namespace mavlinkio {

    // Drone  
    constexpr uint8_t DRONE_ID = 5;
    constexpr uint8_t COMPONENT_ID_AUTOPILOT = MAV_COMP_ID_AUTOPILOT1;

    // GCS (Ground Control Station)
    constexpr uint8_t SYSTEM_ID_GCS = 255;
    constexpr uint8_t COMPONENT_ID_MISSIONPLANNER = MAV_COMP_ID_MISSIONPLANNER;

}