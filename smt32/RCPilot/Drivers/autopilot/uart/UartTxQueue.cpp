#include "UartTxQueue.hpp"
#include <cstring>

namespace uart
{

    UartTxQueue::UartTxQueue(UartDriver &driver)
        : driver(driver) {}

    bool UartTxQueue::enqueue(const uint8_t *data, uint16_t length)
    {
        if (length > MAX_PACKET_SIZE)
            return false;

        uint8_t nextHead = (head + 1) % QUEUE_SIZE;
        if (nextHead == tail)
            return false; // kolejka pełna

        std::memcpy(queue[head].data, data, length);
        queue[head].length = length;
        head = nextHead;

        if (!busy)
        {
            startNext();
        }

        return true;
    }

    void UartTxQueue::startNext()
    {
        if (tail == head)
        {
            busy = false;
            return;
        }

        busy = true;
        driver.transmit(queue[tail].data, queue[tail].length);
    }

    void UartTxQueue::onTransmitComplete()
    {
        tail = (tail + 1) % QUEUE_SIZE;
        startNext();
    }

}