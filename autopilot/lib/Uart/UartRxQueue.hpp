#pragma once
#include <cstdint>

/**
 * @brief Bufor cykliczny do odbierania danych UART (np. z HC-12) zgodnych z MAVLink.
 *
 * Klasa `UartRxQueue` umożliwia bezpieczne buforowanie odebranych bajtów w ISR,
 * a następnie ich przetwarzanie w pętli głównej. Przydaje się do odbioru danych
 * z użyciem MAVLink lub innego binarnego protokołu.
 */
class UartRxQueue {
public:
    /**
     * @brief Dodaje pojedynczy bajt do bufora (np. z ISR).
     *
     * Jeśli bufor nie jest pełny, bajt zostaje zapisany.
     * Jeśli pełny — bajt zostaje zignorowany, a licznik przepełnień zwiększony.
     *
     * @param byte Odebrany bajt.
     */
    void onByteReceived(uint8_t byte);

    /**
     * @brief Pobiera kolejny bajt z bufora, jeśli dostępny.
     *
     * @param outByte Zmienna, do której zostanie zapisany bajt.
     * @return true jeśli bajt był dostępny, false jeśli bufor pusty.
     */
    bool pollByte(uint8_t& outByte);

    /**
     * @brief Czy wystąpiło przepełnienie bufora od ostatniego sprawdzenia.
     *
     * @return true jeśli nastąpiło przepełnienie, false w przeciwnym razie.
     */
    bool hasOverflowed() const;

    /**
     * @brief Czyści flagę informującą o przepełnieniu.
     */
    void clearOverflowFlag();

    /**
     * @brief Zwraca całkowitą liczbę zgubionych bajtów od uruchomienia.
     *
     * @return liczba przepełnień (bajtów, które się nie zmieściły).
     */
    uint32_t getOverflowCount() const;

private:
    static constexpr uint16_t BUFFER_SIZE = 512; ///< Rozmiar bufora kołowego.

    uint8_t buffer[BUFFER_SIZE]; ///< Bufor bajtów.
    volatile uint16_t head = 0;  ///< Wskaźnik zapisu (ISR).
    volatile uint16_t tail = 0;  ///< Wskaźnik odczytu (główna pętla).

    volatile bool overflowFlag = false;   ///< Czy wystąpiło przepełnienie bufora.
    volatile uint32_t overflowCounter = 0;///< Licznik utraconych bajtów.
};
