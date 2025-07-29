#include "Quaternion.hpp"
#include <cmath>

namespace math
{

    Quaternion::Quaternion(double w_, double x_, double y_, double z_)
        : w(w_), x(x_), y(y_), z(z_) {}

    void Quaternion::normalize()
    {
        double norm = std::sqrt(w * w + x * x + y * y + z * z);
        w /= norm;
        x /= norm;
        y /= norm;
        z /= norm;
    }

    double Quaternion::getRoll() const
    {
        double sinr_cosp = 2 * (w * x + y * z);
        double cosr_cosp = 1 - 2 * (x * x + y * y);
        return std::atan2(sinr_cosp, cosr_cosp);
    }

    double Quaternion::getPitch() const
    {
        double sinp = 2 * (w * y - z * x);
        if (std::abs(sinp) >= 1)
            return std::copysign(M_PI / 2, sinp);
        return std::asin(sinp);
    }

    double Quaternion::getYaw() const
    {
        double siny_cosp = 2 * (w * z + x * y);
        double cosy_cosp = 1 - 2 * (y * y + z * z);
        return std::atan2(siny_cosp, cosy_cosp);
    }

    Quaternion Quaternion::operator*(const Quaternion &q) const
    {
        return Quaternion(
            w * q.w - x * q.x - y * q.y - z * q.z, // nowa część rzeczywista
            w * q.x + x * q.w + y * q.z - z * q.y, // nowy x
            w * q.y - x * q.z + y * q.w + z * q.x, // nowy y
            w * q.z + x * q.y - y * q.x + z * q.w  // nowy z
        );
    }

    Quaternion Quaternion::fromGyroDelta(int16_t gx, int16_t gy, int16_t gz, float dt)
    {
        double wx = gx / 131.0 * (M_PI / 180.0);
        double wy = gy / 131.0 * (M_PI / 180.0);
        double wz = gz / 131.0 * (M_PI / 180.0);

        double omega_mag = std::sqrt(wx * wx + wy * wy + wz * wz);
        if (omega_mag < 1e-6)
            return Quaternion(1.0, 0.0, 0.0, 0.0);

        double ux = wx / omega_mag;
        double uy = wy / omega_mag;
        double uz = wz / omega_mag;

        double theta = omega_mag * dt;
        double half_theta = theta / 2.0;
        double sin_half = std::sin(half_theta);
        double cos_half = std::cos(half_theta);

        return Quaternion(
            cos_half,
            ux * sin_half,
            uy * sin_half,
            uz * sin_half);
    }

    double Quaternion::toDegrees(double radians)
    {
        return radians * 180.0 / M_PI;
    }

}