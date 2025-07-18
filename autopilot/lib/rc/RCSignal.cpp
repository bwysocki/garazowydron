#include "RCSignal.hpp"

bool RCSignal::isArmed() const {
    return flags & ARMED;
}

uint8_t RCSignal::calculateCRC() const {
	uint8_t result = 0;
	result ^= throttle;
	result ^= pitch;
	result ^= roll;
	result ^= yaw;
	result ^= aux1;
	result ^= aux2;
	result ^= flags;
	return result;
}