package pl.stalostech.drongarazowy.symulacja.kontrola;

import static pl.stalostech.drongarazowy.protokol.rc.KeyboardControl.*;

public class ControlMapper {

    public static short mapGd(int gd) {
        return mapCenteredAxis(gd, GD_MIDDLE);
    }

    public static short mapLp(int lp) {
        return mapCenteredAxis(lp, LP_MIDDLE);
    }

    /**
     * Mapuje moc silnika (motor) z zakresu 0–7 na zakres -1000 do 1000.
     * 0 = wyłączony, 7 = maksymalna moc
     */
    public static short mapMotor(int motor) {
        return (short) ((motor / (double)MOTOR_MAX) * 1000); // 0 do 1000
    }

    /**
     * Pomocnicza funkcja do mapowania osi centrowanej (np. gd, lp).
     * Zakłada środek w środkuZakresu (np. 3), daje -1000 do +1000.
     */
    private static short mapCenteredAxis(int value, int środekZakresu) {
        int przesunięcie = value - środekZakresu;
        return (short) (-1 * (przesunięcie / (double) środekZakresu) * 1000);
    }
}

