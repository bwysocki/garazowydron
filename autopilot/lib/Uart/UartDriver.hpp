#pragma once
#include <cstdint>

/**
 * @brief Abstrakcyjny interfejs sterownika UART.
 *
 * Klasa `UartDriver` definiuje interfejs do wysyłania danych przez UART.
 * Jest używana przez `UartTxQueue` do rozpoczęcia transmisji danych.
 *
 * Dzięki tej abstrakcji można oddzielić logikę kolejki od konkretnej implementacji
 * sprzętowej (np. STM32 HAL, LL, symulacja w testach, itp.).
 */
class UartDriver {
public:
    /**
     * @brief Wirtualny destruktor.
     */
    virtual ~UartDriver() = default;

    /**
     * @brief Rozpoczyna transmisję danych przez UART.
     *
     * Funkcja powinna natychmiast rozpocząć wysyłkę danych,
     * np. za pomocą DMA lub przerwań. Nie powinna blokować CPU.
     * Po zakończeniu transmisji należy wywołać `onTransmitComplete()` w `UartTxQueue`.
     *
     * @param data Wskaźnik do danych do wysłania.
     * @param length Długość danych w bajtach.
     */
    virtual void transmit(const uint8_t* data, uint16_t length) = 0;
};
