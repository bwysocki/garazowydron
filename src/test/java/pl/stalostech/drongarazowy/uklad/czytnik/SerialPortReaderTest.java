package pl.stalostech.drongarazowy.uklad.czytnik;

import com.fazecast.jSerialComm.SerialPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SerialPortReaderTest {

    private static final Logger logger = LoggerFactory.getLogger(SerialPortReaderTest.class);

    @Mock
    private SerialPort serialPortMock;

    private SerialPortReader reader;

    private MockedStatic<SerialPort> mockedSerialPort;

    @BeforeEach
    void setUp() {
        mockedSerialPort = mockStatic(SerialPort.class);
        mockedSerialPort.when(() -> SerialPort.getCommPort("/dev/ttyACM0")).thenReturn(serialPortMock);

        reader = new SerialPortReader() {
        };
    }

    @AfterEach
    void tearDown() {
        mockedSerialPort.close();
    }

    @Test
    void testConnect_Success() {
        when(serialPortMock.getSystemPortName()).thenReturn("/dev/ttyACM0");
        when(serialPortMock.openPort()).thenReturn(true);
        when(serialPortMock.isOpen()).thenReturn(true);

        reader.connect(115200);

        // Weryfikacja wywołań metod na porcie szeregowym
        verify(serialPortMock).setBaudRate(115200);
        verify(serialPortMock).setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        verify(serialPortMock).openPort();

        assertTrue(reader.isConnected(), "Port powinien być otwarty po udanym połączeniu.");
    }

    @Test
    void testConnect_Failure() {
        when(serialPortMock.openPort()).thenReturn(false);
        when(serialPortMock.isOpen()).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> reader.connect(115200));
        assertEquals("Nie można otworzyć portu! Sprawdź uprawnienia lub czy port nie jest zajęty.", exception.getMessage());

        verify(serialPortMock).openPort();
        assertFalse(reader.isConnected(), "Port nie powinien być otwarty po nieudanej próbie połączenia.");
    }

    @Test
    void testDisconnect_Success() {
        when(serialPortMock.getSystemPortName()).thenReturn("/dev/ttyACM0");
        when(serialPortMock.openPort()).thenReturn(true);
        when(serialPortMock.isOpen()).thenReturn(true);

        reader.connect(115200);
        reader.disconnect();

        verify(serialPortMock).closePort();
    }

}
