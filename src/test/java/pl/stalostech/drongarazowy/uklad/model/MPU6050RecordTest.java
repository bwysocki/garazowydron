package pl.stalostech.drongarazowy.uklad.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MPU6050RecordTest {

    private static final double DELTA = 0.01;

    @Test
    void testGetRoll_WhenFlat() {
        // Dane: brak przyspieszenia w osiach X i Y, grawitacja w osi Z (góra-dół)
        MPU6050Record record = new MPU6050Record(0, 0, 16384, 0, 0, 0);
        double roll = record.getRoll();
        assertEquals(0.0, roll, DELTA, "Oczekiwany kąt przechyłu (Roll) wynosi 0° dla płaskiego ustawienia.");
    }

    @Test
    void testGetRoll_WhenTiltedRight() {
        // Dane: przechylenie w prawo (oś Y), grawitacja w osi Z
        MPU6050Record record = new MPU6050Record(0, 8192, 8192, 0, 0, 0);
        double roll = record.getRoll();
        assertEquals(45.0, roll, DELTA, "Oczekiwany kąt przechyłu (Roll) wynosi 45° przy nachyleniu w prawo.");
    }

    @Test
    void testGetRoll_WhenTiltedLeft() {
        // Dane: przechylenie w lewo (ujemna oś Y), grawitacja w osi Z
        MPU6050Record record = new MPU6050Record(0, -8192, 8192, 0, 0, 0);
        double roll = record.getRoll();
        assertEquals(-45.0, roll, DELTA, "Oczekiwany kąt przechyłu (Roll) wynosi -45° przy nachyleniu w lewo.");
    }

    @Test
    void testGetRoll_WhenCompletelySideways() {
        // Dane: pełne przechylenie w prawo (cała siła grawitacji w osi Y)
        MPU6050Record record = new MPU6050Record(0, 16384, 0, 0, 0, 0);
        double roll = record.getRoll();
        assertEquals(90.0, roll, DELTA, "Oczekiwany kąt przechyłu (Roll) wynosi 90° przy pełnym pochyleniu w prawo.");
    }

    @Test
    void testGetRoll_WhenTiltedAt30Degrees() {
        // Dane: nachylenie w prawo o 30° (sin(30) * 16384 = 8192)
        int ax = 0;
        int ay = (int) (Math.sin(Math.toRadians(30)) * 16384);
        int az = (int) (Math.cos(Math.toRadians(30)) * 16384);
        MPU6050Record record = new MPU6050Record(ax, ay, az, 0, 0, 0);
        double roll = record.getRoll();
        assertEquals(30.0, roll, DELTA, "Oczekiwany kąt przechyłu (Roll) wynosi 30° przy odpowiednim nachyleniu.");
    }

    @Test
    void testGetPitch_WhenFlat() {
        // Dane: brak przyspieszenia w osiach X i Y, grawitacja w osi Z (góra-dół)
        MPU6050Record record = new MPU6050Record(0, 0, 16384, 0, 0, 0);
        double pitch = record.getPitch();
        assertEquals(0.0, pitch, DELTA, "Oczekiwany kąt nachylenia (Pitch) wynosi 0° dla płaskiego ustawienia.");
    }

    @Test
    void testGetPitch_WhenTiltedForward() {
        // Dane: przechylenie w przód (oś X), grawitacja w osi Z
        MPU6050Record record = new MPU6050Record(8192, 0, 8192, 0, 0, 0);
        double pitch = record.getPitch();
        assertEquals(-45.0, pitch, DELTA, "Oczekiwany kąt nachylenia (Pitch) wynosi -45° przy pochyleniu w przód.");
    }

    @Test
    void testGetPitch_WhenTiltedBackward() {
        // Dane: przechylenie w tył (ujemna oś X), grawitacja w osi Z
        MPU6050Record record = new MPU6050Record(-8192, 0, 8192, 0, 0, 0);
        double pitch = record.getPitch();
        assertEquals(45.0, pitch, DELTA, "Oczekiwany kąt nachylenia (Pitch) wynosi 45° przy pochyleniu w tył.");
    }

    @Test
    void testGetPitch_WhenCompletelyForward() {
        // Dane: pełne nachylenie w przód (cała siła grawitacji w osi X)
        MPU6050Record record = new MPU6050Record(16384, 0, 0, 0, 0, 0);
        double pitch = record.getPitch();
        assertEquals(-90.0, pitch, DELTA, "Oczekiwany kąt nachylenia (Pitch) wynosi -90° przy pełnym pochyleniu w przód.");
    }

    @Test
    void testGetPitch_WhenCompletelyBackward() {
        // Dane: pełne nachylenie w tył (cała siła grawitacji w ujemnej osi X)
        MPU6050Record record = new MPU6050Record(-16384, 0, 0, 0, 0, 0);
        double pitch = record.getPitch();
        assertEquals(90.0, pitch, DELTA, "Oczekiwany kąt nachylenia (Pitch) wynosi 90° przy pełnym pochyleniu w tył.");
    }

    @Test
    void testGetPitch_WhenTiltedAt30DegreesForward() {
        // Dane: nachylenie w przód o 30° (sin(30) * 16384 = 8192)
        int ax = (int) (Math.sin(Math.toRadians(30)) * 16384);
        int ay = 0;
        int az = (int) (Math.cos(Math.toRadians(30)) * 16384);
        MPU6050Record record = new MPU6050Record(ax, ay, az, 0, 0, 0);
        double pitch = record.getPitch();
        assertEquals(-30.0, pitch, DELTA, "Oczekiwany kąt nachylenia (Pitch) wynosi -30° przy odpowiednim nachyleniu.");
    }

    @Test
    void testGetPitch_WhenTiltedAt30DegreesBackward() {
        // Dane: nachylenie w tył o 30° (sin(30) * 16384 = 8192)
        int ax = (int) (-Math.sin(Math.toRadians(30)) * 16384);
        int ay = 0;
        int az = (int) (Math.cos(Math.toRadians(30)) * 16384);
        MPU6050Record record = new MPU6050Record(ax, ay, az, 0, 0, 0);
        double pitch = record.getPitch();
        assertEquals(30.0, pitch, DELTA, "Oczekiwany kąt nachylenia (Pitch) wynosi 30° przy odpowiednim nachyleniu w tył.");
    }

    @Test
    void testGetYaw_ZeroRotation() {
        // Gdy brak rotacji (gz = 0), yaw powinno pozostać bez zmian
        MPU6050Record record = new MPU6050Record(0, 0, 0, 0, 0, 0);
        MPU6050Record.Yaw yaw = new MPU6050Record.Yaw();

        double result = record.getYaw(yaw, yaw.getLastTimeNano());
        assertEquals(0.0, result, DELTA, "Yaw powinno pozostać 0 przy braku rotacji.");
    }

    @Test
    void testGetYaw_PositiveRotation() {
        MPU6050Record record = new MPU6050Record(0, 0, 0, 0, 0, 1000);
        MPU6050Record.Yaw yaw = new MPU6050Record.Yaw();
        yaw.setCurrentYaw(0.0);
        yaw.setLastTimeNano(0L);

        double result = record.getYaw(yaw, 1_000_000_000L); // 1 sekunda później
        double expectedYaw = (1000 / 131.0); // Przez 1 sekundę
        assertEquals(expectedYaw, result, DELTA, "Yaw powinno wzrosnąć przy rotacji w prawo.");
    }

    @Test
    void testGetYaw_NegativeRotation() {
        MPU6050Record record = new MPU6050Record(0, 0, 0, 0, 0, -1000);
        MPU6050Record.Yaw yaw = new MPU6050Record.Yaw();
        yaw.setCurrentYaw(0.0);
        yaw.setLastTimeNano(0L);

        double result = record.getYaw(yaw, 1_000_000_000L); // 1 sekunda później
        double expectedYaw = (-1000 / 131.0); // Przez 1 sekundę
        assertEquals(expectedYaw, result, DELTA, "Yaw powinno zmniejszyć się przy rotacji w lewo.");
    }

    @Test
    void testGetYaw_YawWrapAroundPositive() {
        // Ustawienie kąta blisko +180° i rotacja w prawo
        MPU6050Record record = new MPU6050Record(0, 0, 0, 0, 0, 1000);
        MPU6050Record.Yaw yaw = new MPU6050Record.Yaw();
        yaw.setCurrentYaw(179.0);
        yaw.setLastTimeNano(0L);

        // Czas trwania powinien być krótki, aby wykonać mały obrót
        long now = 100_000_000L; // 100 ms
        double result = record.getYaw(yaw, now);

        // Oczekiwana wartość: 179 + (1000 / 131) * 0.1 = 179.7634
        double expectedYaw = 179.7634;
        assertEquals(expectedYaw, result, DELTA, "Yaw powinno wzrosnąć do około 179.76°.");
    }

    @Test
    void testGetYaw_YawWrapAroundNegative() {
        // Ustawienie kąta blisko -180° i rotacja w lewo
        MPU6050Record record = new MPU6050Record(0, 0, 0, 0, 0, -1000);
        MPU6050Record.Yaw yaw = new MPU6050Record.Yaw();
        yaw.setCurrentYaw(-179.0);
        yaw.setLastTimeNano(0L);

        // Czas trwania powinien być krótki, aby wykonać mały obrót
        long now = 100_000_000L; // 100 ms
        double result = record.getYaw(yaw, now);

        // Oczekiwana wartość: -179 - (1000 / 131) * 0.1 = -179.7634
        double expectedYaw = -179.7634;
        assertEquals(expectedYaw, result, DELTA, "Yaw powinno zmniejszyć się do około -179.76°.");
    }

    @Test
    void testGetYaw_MultipleRotations() {
        MPU6050Record record = new MPU6050Record(0, 0, 0, 0, 0, 500);
        MPU6050Record.Yaw yaw = new MPU6050Record.Yaw();
        yaw.setCurrentYaw(0.0);
        yaw.setLastTimeNano(0L);

        long currentTime = 0L;
        for (int i = 0; i < 100; i++) {
            currentTime += 10_000_000L; // 10 ms
            record.getYaw(yaw, currentTime);
        }

        double result = yaw.getCurrentYaw();
        assertEquals(result, result % 360, DELTA, "Yaw powinno pozostawać w zakresie [-180°, 180°].");
    }

}
