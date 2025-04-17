package pl.stalostech.drongarazowy.protokol.rc;

import lombok.Getter;
import pl.stalostech.drongarazowy.funkcje.Hex;

/**
 * KeyboardControl odpowiada za trzymanie i kodowanie sterowania RC do drona.
 * <p>
 * Wartości:
 * - motor (0-7) — moc silnika
 * - lp (0-6) — ruch lewo-prawo (3 - rownowaga)
 * - gd (0-6) — ruch góra-dół (3 - rownowaga)
 * <p>
 * Format ramki: [START_BYTE][motor + lp][gd][STOP_BYTE]
 * <p>
 * START_BYTE = 0xAA
 * STOP_BYTE  = 0x55
 */
@Getter
public class KeyboardControl {

    private static final byte START_BYTE = (byte) 0xAA;
    private static final byte STOP_BYTE = (byte) 0x55;

    private static final int MOTOR_MIN = 0;
    private static final int MOTOR_MAX = 7;
    private static final int LP_MIN = 0;
    private static final int LP_MAX = 6;
    private static final int GD_MIN = 0;
    private static final int GD_MAX = 6;

    private int motor = 0;
    private int lp = 3;
    private int gd = 3;

    // Modyfikatory motor
    public void increaseMotor() {
        motor = clamp(motor + 1, MOTOR_MIN, MOTOR_MAX);
    }

    public void decreaseMotor() {
        motor = clamp(motor - 1, MOTOR_MIN, MOTOR_MAX);
    }

    // Modyfikatory lewo-prawo
    public void increaseLp() {
        lp = clamp(lp + 1, LP_MIN, LP_MAX);
    }

    public void decreaseLp() {
        lp = clamp(lp - 1, LP_MIN, LP_MAX);
    }

    // Modyfikatory góra-dół
    public void increaseGd() {
        gd = clamp(gd + 1, GD_MIN, GD_MAX);
    }

    public void decreaseGd() {
        gd = clamp(gd - 1, GD_MIN, GD_MAX);
    }

    /**
     * Koduje aktualny stan sterowania do 4 bajtów:
     * [START][motor i lp][gd][STOP]
     */
    public byte[] encode() {
        byte motorAndLp = (byte) ((motor << 3) | lp);
        byte gdByte = (byte) gd;

        return new byte[]{START_BYTE, motorAndLp, gdByte, STOP_BYTE};
    }

    /**
     * Odtwarza wartości motor, lp, gd na podstawie ramki danych
     */
    public static KeyboardControl decode(byte[] data) {
        if (data == null || data.length != 4) {
            throw new IllegalArgumentException("Invalid frame length");
        }

        if (data[0] != START_BYTE || data[3] != STOP_BYTE) {
            throw new IllegalArgumentException("Invalid start or stop byte");
        }
        KeyboardControl keyboardControl = new KeyboardControl();
        keyboardControl.motor = clamp((data[1] >> 3) & 0b111, MOTOR_MIN, MOTOR_MAX);
        keyboardControl.lp = clamp(data[1] & 0b111, LP_MIN, LP_MAX);
        keyboardControl.gd = clamp(data[2] & 0b111, GD_MIN, GD_MAX);

        return keyboardControl;
    }

    @Override
    public String toString() {
        return "KeyboardControl{" +
                "motor=" + motor +
                ", lp=" + lp +
                ", gd=" + gd +
                ", HEX=" + Hex.bytesToHex(encode()) +
                '}';
    }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}

