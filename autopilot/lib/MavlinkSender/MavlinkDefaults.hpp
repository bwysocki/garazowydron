#pragma once

#include <cstdint>
#include "../Mavlink/common/mavlink.h"

// Drone ID 
constexpr uint8_t DRONE_ID = 5;

// Domyślny system_id dla GCS (Ground Control Station)
constexpr uint8_t SYSTEM_ID_GCS = 255;

// Domyślny component_id np. dla MissionPlanner
constexpr uint8_t COMPONENT_ID_MISSIONPLANNER = MAV_COMP_ID_MISSIONPLANNER;
