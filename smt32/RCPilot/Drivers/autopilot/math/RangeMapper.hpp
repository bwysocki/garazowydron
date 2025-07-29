#pragma once

#include <cstdint>
#include <cmath>

namespace math
{

    /**
     * @brief Liniowo mapuje wartość z jednego zakresu na inny.
     *
     * @tparam T Typ danych (np. int16_t, int32_t)
     * @param x        Wartość do zmapowania
     * @param in_min   Dolna granica zakresu wejściowego
     * @param in_max   Górna granica zakresu wejściowego
     * @param out_min  Dolna granica zakresu wyjściowego
     * @param out_max  Górna granica zakresu wyjściowego
     * @return T       Zmapowana wartość
     */
    template <typename T>
    constexpr T mapLinear(T x, T in_min, T in_max, T out_min, T out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    /**
     * @brief Mapuje wartość z zakresu wejściowego na przedział [-scale, +scale] z martwą strefą wokół środka.
     *
     * Przykład: mapowanie wartości ADC z zakresu [0, 4095] na [-1000, 1000] z deadzone 10.
     *
     * @param inputValue   Wartość wejściowa do zmapowania (np. odczyt z ADC)
     * @param inMin        Minimalna wartość wejściowa (np. 0)
     * @param inMax        Maksymalna wartość wejściowa (np. 4095)
     * @param scale        Zakres wyjściowy: np. 1000 oznacza przedział [-1000, 1000]
     * @param deadzone     Szerokość martwej strefy wokół środka (np. 10)
     * @return int16_t     Zmapowana wartość w zakresie [-scale, scale]
     */
    inline int16_t mapWithDeadzone(int16_t inputValue, int16_t inMin, int16_t inMax, int16_t scale, int16_t deadzone = 20)
    {
        int16_t center = (inMax + inMin) / 2;
        int16_t offset = inputValue - center;

        if (std::abs(offset) < deadzone)
        {
            return 0;
        }

        if (inputValue < center)
        {
            return mapLinear<int16_t>(inputValue, inMin, center - deadzone, -scale, 0);
        }
        else
        {
            return mapLinear<int16_t>(inputValue, center + deadzone, inMax, 0, scale);
        }
    }

}