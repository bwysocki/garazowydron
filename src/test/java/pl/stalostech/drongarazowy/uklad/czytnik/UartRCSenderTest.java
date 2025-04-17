package pl.stalostech.drongarazowy.uklad.czytnik;

import com.fazecast.jSerialComm.SerialPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.stalostech.drongarazowy.protokol.rc.KeyboardControl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UartRCSenderTest {

    @InjectMocks
    private UartRCSender uartRCSender;

    @Mock
    private SerialPort serialPort;

    @Mock
    private KeyboardControl keyboardControl;

    @BeforeEach
    void setUp() {
        uartRCSender.serialPort = serialPort;
    }

    @Test
    void shouldUpdateLastCommandOnSend() {
        uartRCSender.send(keyboardControl);

        assertEquals(keyboardControl, uartRCSender.lastCommand);
    }

    @Test
    void shouldNotSendWhenLastCommandIsNull() {
        uartRCSender.lastCommand = null;

        uartRCSender.sendLastCommand();

        verifyNoInteractions(serialPort);
    }

    @Test
    void shouldSendEncodedPayloadWhenLastCommandExists() {
        uartRCSender.lastCommand = keyboardControl;

        byte[] payload = new byte[]{1, 2, 3};
        when(keyboardControl.encode()).thenReturn(payload);

        uartRCSender.sendLastCommand();

        verify(serialPort).writeBytes(payload, payload.length);
    }
}
