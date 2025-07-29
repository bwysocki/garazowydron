#pragma once

#include "MavlinkMessageHandler.hpp"
#include "../mavlink/common/mavlink.h"
#include "../uart/UartRxQueue.hpp"

namespace mavlinkio {

/**
 * @brief Parser strumienia bajtów MAVLink.
 *
 * Klasa `MavlinkReceiver` odpowiedzialna jest za przyjmowanie bajtów (np. z UART),
 * analizowanie ich przy użyciu parsera `mavlink_parse_char` oraz przekazywanie
 * poprawnie zdekodowanych wiadomości do handlera `MavlinkMessageHandler`.
 *
 * Umożliwia to niezależne od platformy przetwarzanie ramek MAVLink, np. z modułu HC-12,
 * portu szeregowego lub symulacji w testach jednostkowych.
 */
class MavlinkReceiver {
public:
    /**
     * @brief Konstruktor.
     * @param handler Obiekt obsługujący odebrane wiadomości MAVLink.
     */
    explicit MavlinkReceiver(MavlinkMessageHandler& handler)
        : handler(handler) {}

    /**
     * @brief Przekazuje pojedynczy bajt do parsera MAVLink.
     *
     * Funkcja powinna być wywoływana dla każdego odebranego bajtu,
     * np. w przerwaniu UART lub w pętli głównej.
     *
     * @param byte Odebrany bajt.
     */
    void receiveByte(uint8_t byte)
    {
        if (mavlink_parse_char(MAVLINK_COMM_0, byte, &msg, &status)) {
            handler.onMessageReceived(msg);
        }
    }

    /**
     * @brief Przekazuje ciąg bajtów do parsera MAVLink.
     *
     * Funkcja pomocnicza do przetwarzania bufora bajtów — wywołuje `receiveByte()`
     * dla każdego bajtu w tablicy wejściowej.
     *
     * @param data Bufor bajtów.
     * @param length Liczba bajtów do przetworzenia.
     */
    void receiveBuffer(const uint8_t* data, size_t length)
    {
        for (size_t i = 0; i < length; ++i) {
            receiveByte(data[i]);
        }
    }

    /**
     * @brief Przetwarza bajty z bufora UART (UartRxQueue).
     *
     * Funkcja odczytuje bajty z `UartRxQueue` i przekazuje je do parsera MAVLink.
     *
     * @param rxQueue Bufor odbiorczy UART.
     */
    void processRxQueue(uart::UartRxQueue& rxQueue)
    {
        uint8_t byte;
        while (rxQueue.pollByte(byte)) {
            receiveByte(byte);
        }
    }

private:
    MavlinkMessageHandler& handler;
    mavlink_message_t msg;
    mavlink_status_t status{};
};

} // namespace mavlinkio
