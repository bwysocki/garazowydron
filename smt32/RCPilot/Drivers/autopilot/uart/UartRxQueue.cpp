#include "UartRxQueue.hpp"

namespace uart
{

    void UartRxQueue::onByteReceived(uint8_t byte)
    {
        uint16_t nextHead = (head + 1) % BUFFER_SIZE;
        if (nextHead != tail)
        {
            buffer[head] = byte;
            head = nextHead;
        }
        else
        {
            // Bufor pełny — bajt zostaje zignorowany
            overflowFlag = true;
            overflowCounter++;
        }
    }

    bool UartRxQueue::pollByte(uint8_t &outByte)
    {
        if (tail == head)
        {
            return false; // Bufor pusty
        }

        outByte = buffer[tail];
        tail = (tail + 1) % BUFFER_SIZE;
        return true;
    }

    bool UartRxQueue::hasOverflowed() const
    {
        return overflowFlag;
    }

    void UartRxQueue::clearOverflowFlag()
    {
        overflowFlag = false;
    }

    uint32_t UartRxQueue::getOverflowCount() const
    {
        return overflowCounter;
    }

}