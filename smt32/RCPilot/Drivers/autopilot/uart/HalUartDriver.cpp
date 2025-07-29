#include "HalUartDriver.hpp"

namespace uart
{

    HalUartDriver::HalUartDriver(UART_HandleTypeDef *huart)
        : huart(huart) {}

    void HalUartDriver::transmit(const uint8_t *data, uint16_t length)
    {
        HAL_UART_Transmit_DMA(huart, const_cast<uint8_t *>(data), length);
    	//HAL_UART_Transmit_IT(huart, const_cast<uint8_t *>(data), length);
    }

    /**
     * @example
     * // Przykład użycia HalUartDriver + UartTxQueue do wysyłania danych przez HC-12:
     *
     * #include "UartTxQueue.hpp"
     * #include "HalUartDriver.hpp"
     *
     * extern UART_HandleTypeDef huart1;
     *
     * // Inicjalizacja sterownika i kolejki (np. w main.cpp)
     * HalUartDriver halDriver(&huart1);
     * UartTxQueue txQueue(halDriver);
     *
     * void loop() {
     *     // Dane do wysłania przez HC-12 (np. manual control)
     *     uint8_t message[] = { 0xAA, 0x01, 0x02, 0x03, 0x55 };
     *     txQueue.enqueue(message, sizeof(message));
     *
     *     HAL_Delay(20); // np. wysyłka co 20ms
     * }
     *
     * // Callback zakończenia transmisji DMA
     * extern "C" void HAL_UART_TxCpltCallback(UART_HandleTypeDef* huart) {
     *     if (huart->Instance == USART1) {
     *         txQueue.onTransmitComplete();
     *     }
     * }
     */
}
