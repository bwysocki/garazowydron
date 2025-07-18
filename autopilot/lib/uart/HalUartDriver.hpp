#pragma once
#include "UartDriver.hpp"
#include "stm32f1xx_hal.h"  ///< Dostosuj do swojej serii STM32

/**
 * @brief Implementacja interfejsu UartDriver oparta na HAL UART z STM32.
 *
 * Klasa `HalUartDriver` implementuje transmisję UART z użyciem HAL-a STM32,
 * najczęściej przy pomocy DMA.
 *
 * Służy jako adapter pomiędzy warstwą ogólnej logiki wysyłania (np. `UartTxQueue`)
 * a konkretną implementacją sprzętową.
 */
class HalUartDriver : public UartDriver {
public:
    /**
     * @brief Tworzy obiekt sterownika UART opartego na HAL.
     * 
     * @param huart Wskaźnik na strukturę HAL UART skonfigurowaną wcześniej
     *             (np. przez STM32CubeMX).
     */
    explicit HalUartDriver(UART_HandleTypeDef* huart);

    /**
     * @brief Rozpoczyna transmisję danych przez HAL UART (zwykle z DMA).
     *
     * Funkcja wywołuje `HAL_UART_Transmit_DMA`, przekazując dane do wysłania.
     * Po zakończeniu transmisji użytkownik powinien wywołać `onTransmitComplete()`
     * w `UartTxQueue`, np. z `HAL_UART_TxCpltCallback`.
     *
     * @param data Wskaźnik do danych do wysłania.
     * @param length Liczba bajtów do wysłania.
     */
    void transmit(const uint8_t* data, uint16_t length) override;

private:
    UART_HandleTypeDef* huart;  ///< Wskaźnik na strukturę HAL UART.
};
