#include "../UartDriver.hpp"
#include <functional>

using namespace uart;

class MockUartDriver : public UartDriver {
public:
    std::vector<std::vector<uint8_t>> sentPackets;

    void transmit(const uint8_t* data, uint16_t length) override {
        sentPackets.emplace_back(data, data + length);
    }
};
