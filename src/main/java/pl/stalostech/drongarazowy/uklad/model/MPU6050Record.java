package pl.stalostech.drongarazowy.uklad.model;


import lombok.Getter;
import lombok.Setter;

/**
 * Reprezentuje dane z czujnika MPU6050, który integruje:
 * - Akcelerometr 3-osiowy (X, Y, Z) do pomiaru przyspieszenia.
 * - Żyroskop 3-osiowy (X, Y, Z) do pomiaru prędkości kątowej.
 * <p>
 * Czujnik MPU6050 jest często stosowany w aplikacjach dronów, robotyki i stabilizacji,
 * pozwalając na monitorowanie orientacji i przyspieszeń w przestrzeni.
 * <p>
 * Opis osi:
 * - ax - Przyspieszenie w osi X (lewo-prawo).
 * - ay - Przyspieszenie w osi Y (przód-tył).
 * - az - Przyspieszenie w osi Z (góra-dół).
 * - gx - Prędkość obrotowa wokół osi X (Roll).
 * - gy - Prędkość obrotowa wokół osi Y (Pitch).
 * - gz - Prędkość obrotowa wokół osi Z (Yaw).
 */
public record MPU6050Record(
        int ax, int ay, int az, int gx, int gy, int gz
) {

    /**
     * Oblicza kąt przechyłu (Roll), czyli pochylanie w lewo/prawo.
     * <p>
     * Roll jest obliczany jako kąt między osią Y a płaszczyzną utworzoną przez osie X i Z.
     * Wartość wyrażona jest w stopniach i określa, jak bardzo urządzenie pochyla się w lewo lub w prawo.
     *
     * @return Kąt przechyłu (Roll) w stopniach.
     */
    public double getRoll() {
        return Math.toDegrees(Math.atan2(ay, Math.sqrt(ax * ax + az * az)));
    }

    /**
     * Oblicza kąt nachylenia (Pitch), czyli pochylanie w przód/tył.
     * <p>
     * Pitch jest obliczany jako kąt między osią X a płaszczyzną utworzoną przez osie Y i Z.
     * Wartość wyrażona jest w stopniach i określa, jak bardzo urządzenie pochyla się w przód lub w tył.
     *
     * @return Kąt nachylenia (Pitch) w stopniach.
     */
    public double getPitch() {
        return Math.toDegrees(Math.atan2(-ax, Math.sqrt(ay * ay + az * az)));
    }

    /**
     * Oblicza kąt obrotu wokół osi Z (Yaw), czyli obrót w poziomie.
     * <p>
     * Yaw określa, jak bardzo urządzenie obraca się wokół osi Z (Yaw).
     * Metoda uwzględnia czas od ostatniego pomiaru, aby obliczyć nową wartość kąta.
     *
     * @param yaw Obiekt Yaw przechowujący bieżący kąt oraz czas poprzedniego pomiaru.
     * @param currentTimeNano Bieżący czas w nanosekundach (np. System.nanoTime()).
     * @return Nowa wartość kąta Yaw w stopniach.
     */
    public double getYaw(Yaw yaw, long currentTimeNano) {
        double dt = (currentTimeNano - yaw.getLastTimeNano()) / 1_000_000_000.0; // czas w sekundach

        // Przeliczenie gz na stopnie/sekundę (zakładając zakres ±250°/s)
        double gzDegreesPerSecond = gz / 131.0;

        // Całkowanie: nowy Yaw
        double newYaw = yaw.getCurrentYaw() + gzDegreesPerSecond * dt;

        // Ograniczenie do [-180°, 180°]
        if (newYaw > 180) newYaw -= 360;
        if (newYaw < -180) newYaw += 360;

        // Aktualizacja obiektu Yaw
        yaw.setCurrentYaw(newYaw);
        yaw.setLastTimeNano(currentTimeNano);

        return newYaw;
    }

    @Setter
    @Getter
    public static class Yaw {
        private double currentYaw = 0.0;
        private long lastTimeNano = System.nanoTime();
    }

}
