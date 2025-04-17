package pl.stalostech.drongarazowy.protokol.rc;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class KeyboardControlTest {

    @Test
    void shouldEncodeAndDecodeCorrectly() {
        KeyboardControl control = new KeyboardControl();
        control.increaseMotor();
        control.increaseLp();
        control.decreaseGd();

        byte[] encoded = control.encode();

        KeyboardControl decoded = KeyboardControl.decode(encoded);

        assertThat(decoded.getMotor()).isEqualTo(control.getMotor());
        assertThat(decoded.getLp()).isEqualTo(control.getLp());
        assertThat(decoded.getGd()).isEqualTo(control.getGd());
    }

    @Test
    void shouldClampMotorCorrectly() {
        KeyboardControl control = new KeyboardControl();

        for (int i = 0; i < 20; i++) {
            control.increaseMotor();
        }
        assertThat(control.getMotor()).isEqualTo(7);

        for (int i = 0; i < 20; i++) {
            control.decreaseMotor();
        }
        assertThat(control.getMotor()).isEqualTo(0);
    }

    @Test
    void shouldClampLpCorrectly() {
        KeyboardControl control = new KeyboardControl();

        for (int i = 0; i < 20; i++) {
            control.increaseLp();
        }
        assertThat(control.getLp()).isEqualTo(6);

        for (int i = 0; i < 20; i++) {
            control.decreaseLp();
        }
        assertThat(control.getLp()).isEqualTo(0);
    }

    @Test
    void shouldClampGdCorrectly() {
        KeyboardControl control = new KeyboardControl();

        for (int i = 0; i < 20; i++) {
            control.increaseGd();
        }
        assertThat(control.getGd()).isEqualTo(6);

        for (int i = 0; i < 20; i++) {
            control.decreaseGd();
        }
        assertThat(control.getGd()).isEqualTo(0);
    }

    @Test
    void shouldThrowExceptionWhenFrameIsInvalidLength() {
        byte[] invalidFrame = new byte[]{(byte) 0xAA, 0x00, 0x00}; // tylko 3 bajty
        assertThrows(IllegalArgumentException.class, () -> KeyboardControl.decode(invalidFrame));
    }

    @Test
    void shouldThrowExceptionWhenFrameHasInvalidStartStopBytes() {
        byte[] invalidFrame = new byte[]{0x00, 0x00, 0x00, 0x00}; // brak start i stop
        assertThrows(IllegalArgumentException.class, () -> KeyboardControl.decode(invalidFrame));
    }

}
