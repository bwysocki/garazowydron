#pragma once

#include <cstdint>

/**
 * @brief Sygnał RC, służący do przesyłania poleceń do drona.
 * 
 * Wszystkie wartości w zakresie 0–255, gdzie:
 * - 0 odpowiada wartości minimalnej (np. 1000us)
 * - 255 odpowiada wartości maksymalnej (np. 2000us)
 * 
 * Pole `flags` to bajt bitowy:
 * - bit 0: ARM (1 = uzbrojony)
 * - bit 1–7: zarezerwowane
 * 
 * Pole `crc` to 8-bitowa suma kontrolna XOR dla bezpieczeństwa transmisji.
 * Obliczana na podstawie poprzednich pól (bez `crc`).
 */
struct RCSignal {
	uint8_t throttle = 0;
	uint8_t pitch = 127;
	uint8_t roll = 127;
	uint8_t yaw = 127;
	uint8_t aux1 = 0;
	uint8_t aux2 = 0;
	uint8_t flags = 0;
	uint8_t crc = 0;

	enum Flag : uint8_t {
		ARMED    = 1 << 0
	};

	/// Informuje o tym, czy dron jest uzbrojony
	bool isArmed() const;

	/// Wylicza crc
	uint8_t calculateCRC() const;

};