#pragma once
#include <cmath>

/**
 * @class Quaternion
 * @brief Klasa reprezentująca kwaternion jako narzędzie do opisu orientacji w 3D.
 *
 * Klasa Quaternion służy do reprezentowania obrotów w przestrzeni trójwymiarowej
 * w sposób bardziej stabilny i wydajny niż kąty Eulera czy macierze obrotu (DCM).
 *
 * Kwaternion składa się z 4 składników: w, x, y, z, gdzie:
 * - `w` to część rzeczywista - oznacza cosinus połowy kąta obrotu,
 * - `x, y, z` to część urojona (wektorowa) - tworzą wektor jednostkowy opisujący oś obrotu,  
 *   a ich długość (razem) odpowiada sinusowi połowy kąta obrotu.
 *
 * Innymi słowy:
 * - Kąt obrotu θ:     θ = 2 * acos(w)
 * - Oś obrotu (x, y, z): znormalizowany wektor (x, y, z) / sin(θ / 2)
 *
 * Obrót obiektu można przedstawić jako:
 *     q = w + xi + yj + zk
 *
 */
class Quaternion {
public:
    double w, x, y, z; ///< Składniki kwaternionu: w – rzeczywisty, x/y/z – urojone

    Quaternion(double w, double x, double y, double z);
    
    /**
     * @brief Normalizuje kwaternion do długości 1.
     * Przydatne, gdy kwaternion został przekształcony i może mieć długość inną niż 1.
     */
    void normalize();

    /**
     * @brief Oblicza kąt roll (obrotu wokół osi X) w radianach.
     * @return roll w radianach
     */
    double getRoll() const;

    /**
     * @brief Oblicza kąt pitch (obrotu wokół osi Y) w radianach.
     * @return pitch w radianach
     */
    double getPitch() const;

    /**
     * @brief Oblicza kąt yaw (obrotu wokół osi Z) w radianach.
     * @return yaw w radianach
     */
    double getYaw() const;

     /**
     * @brief Mnożenie dwóch kwaternionów – kompozycja obrotów.
     * @param other Drugi kwaternion (obrót wykonywany po tym)
     * @return Nowy kwaternion będący złożeniem obrotów
     */
    Quaternion operator*(const Quaternion& other) const;

    /**
     * Statyczna metoda tworząca kwaternion obrotu na podstawie:
     *  - surowych danych z żyroskopu (gx, gy, gz),
     *  - czasu delta `dt` w sekundach.
     *
     * MPU6050 przy zakresie ±250°/s daje 131 jednostek na stopień/sekundę.
     * Funkcja konwertuje te dane na radiany/s, oblicza kąt obrotu
     * i tworzy odpowiedni kwaternion obrotu.
     */
    static Quaternion fromGyroDelta(int16_t gx, int16_t gy, int16_t gz, float dt);

    /**
     * @brief Konwertuje radiany na stopnie.
     * @param radians Kąt w radianach
     * @return Kąt w stopniach
     */
    static double toDegrees(double radians);
};
