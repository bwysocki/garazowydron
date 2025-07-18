#include "gtest/gtest.h"
#include "../RangeMapper.hpp"
#include <cmath>

TEST(RangeMapperTest, mapLinear_BasicMapping) {
    // Test mapowania z zakresu [0, 100] do [0, 200]
    EXPECT_EQ(mapLinear<int16_t>(0, 0, 100, 0, 200), 0);
    EXPECT_EQ(mapLinear<int16_t>(50, 0, 100, 0, 200), 100);
    EXPECT_EQ(mapLinear<int16_t>(100, 0, 100, 0, 200), 200);
}

TEST(RangeMapperTest, mapLinear_NegativeRange) {
    // Test mapowania z zakresu [-100, 100] do [-1000, 1000]
    EXPECT_EQ(mapLinear<int16_t>(-100, -100, 100, -1000, 1000), -1000);
    EXPECT_EQ(mapLinear<int16_t>(0, -100, 100, -1000, 1000), 0);
    EXPECT_EQ(mapLinear<int16_t>(100, -100, 100, -1000, 1000), 1000);
}

TEST(RangeMapperTest, mapWithDeadzone_CenterZero) {
    // Test martwej strefy wokół środka
    int16_t adcMin = 0;
    int16_t adcMax = 4095;
    int16_t deadzone = 10;
    int16_t center = (adcMin + adcMax) / 2;

    EXPECT_EQ(mapWithDeadzone(center, adcMin, adcMax, 1000, deadzone), 0);
    EXPECT_EQ(mapWithDeadzone(center + 5, adcMin, adcMax, 1000, deadzone), 0);
    EXPECT_EQ(mapWithDeadzone(center - 5, adcMin, adcMax, 1000, deadzone), 0);
}

TEST(RangeMapperTest, mapWithDeadzone_OutsideDeadzonePositive) {
    int16_t result = mapWithDeadzone(3000, 0, 4095, 1000, 10);
    EXPECT_GT(result, 0);  // powinno być dodatnie
    EXPECT_LE(result, 1000);
}

TEST(RangeMapperTest, mapWithDeadzone_OutsideDeadzoneNegative) {
    int16_t result = mapWithDeadzone(1000, 0, 4095, 1000, 10);
    EXPECT_LT(result, 0);  // powinno być ujemne
    EXPECT_GE(result, -1000);
}

TEST(RangeMapperTest, mapWithDeadzone_AtRangeEdges) {
    EXPECT_EQ(mapWithDeadzone(0, 0, 4095, 1000, 10), -1000);
    EXPECT_EQ(mapWithDeadzone(4095, 0, 4095, 1000, 10), 1000);
}
