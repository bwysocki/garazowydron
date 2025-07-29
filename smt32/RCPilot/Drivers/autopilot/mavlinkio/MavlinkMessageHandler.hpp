#pragma once

#include "../mavlink/common/mavlink.h"

namespace mavlinkio {

/**
 * @brief Handler odbieranych wiadomości MAVLink.
 *
 * Klasa, która implementuje ten interfejs, musi obsłużyć odebrane
 * wiadomości MAVLink.
 */
class MavlinkMessageHandler {
public:
    virtual ~MavlinkMessageHandler() = default;

    /**
     * @brief Wywoływane, gdy odebrano poprawną wiadomość MAVLink.
     *
     * @param msg Rozkodowana wiadomość MAVLink.
     */
    virtual void onMessageReceived(const mavlink_message_t& msg) = 0;
};

} // namespace mavlinkio
