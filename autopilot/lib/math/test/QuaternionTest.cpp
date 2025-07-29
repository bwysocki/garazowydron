#include "gtest/gtest.h"
#include "../Quaternion.hpp"
#include <cmath>

using namespace math;

bool isClose(double a, double b, double eps = 1e-4) {
    return std::abs(a - b) < eps;
}

TEST(QuaternionTest, Normalize) {
    Quaternion q(2, 0, 0, 0);
    q.normalize();
    EXPECT_TRUE(isClose(q.w, 1.0));
    EXPECT_TRUE(isClose(q.x, 0.0));
    EXPECT_TRUE(isClose(q.y, 0.0));
    EXPECT_TRUE(isClose(q.z, 0.0));
}

TEST(QuaternionTest, Roll90Deg) {
    double angle = M_PI / 2;
    Quaternion q(cos(angle/2), sin(angle/2), 0, 0);
    EXPECT_TRUE(isClose(Quaternion::toDegrees(q.getRoll()), 90.0));
}

TEST(QuaternionTest, Pitch90Deg) {
    double angle = M_PI / 2;
    Quaternion q(cos(angle/2), 0, sin(angle/2), 0);
    EXPECT_TRUE(isClose(Quaternion::toDegrees(q.getPitch()), 90.0));
}

TEST(QuaternionTest, Yaw90Deg) {
    double angle = M_PI / 2;
    Quaternion q(cos(angle/2), 0, 0, sin(angle/2));
    EXPECT_TRUE(isClose(Quaternion::toDegrees(q.getYaw()), 90.0));
}

TEST(QuaternionTest, QuaternionMultiplication) {
    double angle = M_PI / 2;
    Quaternion qx(cos(angle / 2), sin(angle / 2), 0, 0); // 90° wokół X
    Quaternion qy(cos(angle / 2), 0, sin(angle / 2), 0); // 90° wokół Y

    Quaternion q = qy * qx;
    q.normalize();

    Quaternion expected(
        0.5,                  // w
        0.5,                  // x
        0.5,                  // y
        -0.5                   // z
    );
    expected.normalize();

    EXPECT_TRUE(isClose(q.w, expected.w));
    EXPECT_TRUE(isClose(q.x, expected.x));
    EXPECT_TRUE(isClose(q.y, expected.y));
    EXPECT_TRUE(isClose(q.z, expected.z));
}

TEST(QuaternionTest, FromGyroDeltaYaw1Deg) {
    // 131 jednostek z MPU6050 = 1 °/s (zakres ±250 °/s)
    int16_t gx = 0;
    int16_t gy = 0;
    int16_t gz = 131; // czyli 1°/s wokół Z

    float dt = 1.0f; // czas = 1 sekunda
    Quaternion q = Quaternion::fromGyroDelta(gx, gy, gz, dt);

    float yawDeg = Quaternion::toDegrees(q.getYaw());

    EXPECT_TRUE(isClose(yawDeg, 1.0f)) << "Expected yaw ≈ 1.0°, got: " << yawDeg;
}

