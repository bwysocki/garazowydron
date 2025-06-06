#include <gtest/gtest.h>
#include "../RCSignal.cpp"

// Test: domyślne wartości
TEST(RCSignalTest, DefaultValues) {
    RCSignal s;

    EXPECT_EQ(s.throttle, 0);
    EXPECT_EQ(s.pitch, 127);
    EXPECT_EQ(s.roll, 127);
    EXPECT_EQ(s.yaw, 127);
    EXPECT_EQ(s.aux1, 0);
    EXPECT_EQ(s.aux2, 0);
    EXPECT_EQ(s.flags, 0);

    EXPECT_FALSE(s.isArmed());
}

// Test: ustawienie flagi ARMED
TEST(RCSignalTest, SetArmedFlag) {
    RCSignal s;
    s.flags |= RCSignal::ARMED;

    EXPECT_TRUE(s.isArmed());
}

// Test: modyfikacja wartości kanałów
TEST(RCSignalTest, ModifyChannelValues) {
    RCSignal s;
    s.throttle = 200;
    s.pitch = 100;
    s.roll = 150;
    s.yaw = 50;

    EXPECT_EQ(s.throttle, 200);
    EXPECT_EQ(s.pitch, 100);
    EXPECT_EQ(s.roll, 150);
    EXPECT_EQ(s.yaw, 50);
}

// Test: wyliczanie CRC
TEST(RCSignalTest, CalculateCRC) {
    RCSignal signal;
    signal.throttle = 100;
    signal.pitch = 150;
    signal.roll = 200;
    signal.yaw = 50;
    signal.aux1 = 25;
    signal.aux2 = 75;
    signal.flags = RCSignal::ARMED;

    EXPECT_EQ(signal.calculateCRC(), 91);  // 0x5B
}
