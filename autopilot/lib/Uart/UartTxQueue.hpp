#pragma once
#include "UartDriver.hpp"

/**
 * @brief Kolejka do buforowania i wysyłania danych przez UART z użyciem DMA/przerwań.
 *
 * Klasa `UartTxQueue` odpowiada za kolejkowanie pakietów danych i ich sekwencyjne
 * wysyłanie przez interfejs UART za pomocą podanego sterownika (`UartDriver`).
 *
 * Może być używana do różnych zastosowań: transmisja MAVLink, debug, dane binarne itd.
 * Obsługuje transmisję asynchroniczną (DMA, IT), bez blokowania CPU.
 */
class UartTxQueue {
public:
    /**
     * @brief Tworzy kolejkę UART opartą na podanym sterowniku.
     * 
     * @param driver Referencja do obiektu implementującego interfejs `UartDriver`,
     *               odpowiedzialnego za rozpoczęcie fizycznej transmisji.
     */
    explicit UartTxQueue(UartDriver& driver);

    /**
     * @brief Dodaje dane do kolejki wysyłkowej.
     *
     * Jeśli kolejka nie jest pełna, dane są dodawane i (jeśli to potrzebne)
     * uruchamiana jest transmisja przez UART.
     *
     * @param data Wskaźnik do danych do wysłania.
     * @param length Długość danych w bajtach (maks. MAX_PACKET_SIZE).
     * @return true jeśli dane zostały poprawnie dodane do kolejki,
     *         false jeśli długość przekracza limit lub kolejka jest pełna.
     */
    bool enqueue(const uint8_t* data, uint16_t length);

    /**
     * @brief Callback wywoływany po zakończeniu jednej transmisji.
     *
     * Funkcja powinna być wywołana np. z `HAL_UART_TxCpltCallback`, aby poinformować
     * kolejkę, że poprzedni pakiet został wysłany i można rozpocząć transmisję kolejnego.
     */
    void onTransmitComplete();

private:
    /// Maksymalna liczba pakietów w kolejce.
    static constexpr int QUEUE_SIZE = 10;

    /// Maksymalna długość pojedynczego pakietu danych.
    static constexpr int MAX_PACKET_SIZE = 64;

    /**
     * @brief Struktura przechowująca jeden pakiet danych.
     */
    struct Packet {
        uint8_t data[MAX_PACKET_SIZE]; ///< Bufor danych pakietu.
        uint16_t length;               ///< Długość danych w bajtach.
    };

    Packet queue[QUEUE_SIZE];         ///< Bufor cykliczny na pakiety.
    volatile uint8_t head = 0;        ///< Indeks pierwszego wolnego miejsca do zapisu.
    volatile uint8_t tail = 0;        ///< Indeks pakietu oczekującego na wysłanie.
    volatile bool busy = false;       ///< Czy aktualnie trwa transmisja.

    UartDriver& driver;               ///< Referencja do interfejsu sterownika UART.

    /**
     * @brief Rozpoczyna transmisję kolejnego pakietu z kolejki.
     *
     * Funkcja jest wywoływana automatycznie po dodaniu danych lub zakończeniu transmisji.
     */
    void startNext();
};
