#pragma once

#include <functional>
#include <cstdint>
#include "./MavlinkDefaults.hpp"
#include "../Mavlink/common/mavlink.h"

class MavlinkSender
{
public:
    using ByteWriter = std::function<void(uint8_t)>;

    explicit MavlinkSender(ByteWriter writer);

    /**
     * @brief Wysyła wiadomość MAVLink HEARTBEAT do zdalnego odbiorcy (np. autopilota).
     *
     * Wiadomość `HEARTBEAT` służy do identyfikacji nadawcy (np. GCS, autopilota) oraz
     * sygnalizowania jego aktualnego trybu i stanu. To jedna z podstawowych wiadomości MAVLink
     * wysyłanych cyklicznie w celu podtrzymania łączności i synchronizacji.
     *
     * Funkcja korzysta z przekazanej wcześniej funkcji `ByteWriter`, która odpowiada za
     * faktyczne przesłanie bajtów (np. przez UART, USB, itp.).
     *
     * @param system_id       ID systemu (np. GCS = 255, dron = 1). Określa źródło wiadomości.
     * @param component_id    ID komponentu w systemie (np. 190 = Mission Planner).
     * @param type            Typ systemu, np. MAV_TYPE_GCS, MAV_TYPE_QUADROTOR.
     * @param autopilot       Typ autopilota, np. MAV_AUTOPILOT_GENERIC, MAV_AUTOPILOT_ARDUPILOTMEGA.
     * @param base_mode       Bity trybu działania autopilota. Definiuje, czy tryb jest ręczny,
     *                        zdalny, czy oparty na custom_mode.
     *                        Przykład: MAV_MODE_FLAG_CUSTOM_MODE_ENABLED |
     *                                 MAV_MODE_FLAG_MANUAL_INPUT_ENABLED |
     *                                 MAV_MODE_FLAG_GUIDED_ENABLED
     * @param custom_mode     32-bitowa wartość definiująca szczegółowy tryb autopilota (np. GUIDED, AUTO, LOITER).
     *                        Interpretacja zależna od typu autopilota (np. ArduPilot, PX4).
     * @param system_status   Status systemu (np. MAV_STATE_ACTIVE, MAV_STATE_STANDBY).
     *
     * @note Funkcja serializuje strukturę `mavlink_heartbeat_t` do bajtów zgodnie z protokołem MAVLink 2.0
     *       i przekazuje ją do funkcji `writeByte(uint8_t)`, przekazanej do `MavlinkSender` podczas tworzenia.
     *
     * @see MAV_TYPE
     * @see MAV_AUTOPILOT
     * @see MAV_MODE_FLAG
     * @see MAV_STATE
     * @see mavlink_msg_heartbeat_pack()
     */
    void sendHeartbeat(
        uint8_t system_id = SYSTEM_ID_GCS,
        uint8_t component_id = COMPONENT_ID_MISSIONPLANNER,
        uint8_t type = MAV_TYPE_GCS,
        uint8_t autopilot = MAV_AUTOPILOT_GENERIC,
        uint8_t base_mode = MAV_MODE_FLAG_CUSTOM_MODE_ENABLED |
                            MAV_MODE_FLAG_MANUAL_INPUT_ENABLED |
                            MAV_MODE_FLAG_GUIDED_ENABLED,
        uint32_t custom_mode = 0,
        uint8_t system_status = MAV_STATE_ACTIVE);

    /**
     * @brief Wysyła wiadomość MAVLink MANUAL_CONTROL do zdalnego systemu (np. drona).
     *
     * Wiadomość `MANUAL_CONTROL` pozwala przesłać bezpośrednie komendy sterujące (RC) do pojazdu.
     * Zawiera znormalizowane wartości na osiach X, Y, Z, R w zakresie [-1000, 1000].
     *
     * Funkcja serializuje strukturę `mavlink_manual_control_t` i przekazuje wynik do funkcji `writeByte(uint8_t)`,
     * przekazanej wcześniej do `MavlinkSender`.
     *
     * @param target_id   ID systemu docelowego (np. 1 = dron)
     * @param x           Oś X (pitch), [-1000, 1000], INT16_MAX = brak
     * @param y           Oś Y (roll), [-1000, 1000], INT16_MAX = brak
     * @param z           Oś Z (throttle), [-1000, 1000], INT16_MAX = brak
     * @param r           Oś R (yaw), [-1000, 1000], INT16_MAX = brak
     *
     * @note Używane tylko podstawowe pola wiadomości. Rozszerzenia i przyciski są pomijane.
     *
     * @see mavlink_msg_manual_control_pack()
     */
    void sendManualControl(
        uint8_t target_id,
        int16_t x,
        int16_t y,
        int16_t z,
        int16_t r);

    /**
     * @brief Wysyła komendę uzbrojenia lub rozbrojenia drona (ARM/DISARM).
     *
     * Wiadomość `COMMAND_LONG` z komendą `MAV_CMD_COMPONENT_ARM_DISARM` służy do zdalnego
     * uzbrojenia (ARM) lub rozbrojenia (DISARM) systemu.
     *
     * @param target_id          ID systemu docelowego (np. 1 = dron)
     * @param arm               `true` aby uzbroić, `false` aby rozbroić
     *
     * @see MAV_CMD_COMPONENT_ARM_DISARM
     * @see mavlink_msg_command_long_pack()
     */
    void sendArmCommand(
        uint8_t target_id,
        bool arm = true);

private:
    ByteWriter writeByte;
    void sendMessage(const mavlink_message_t &msg);
};