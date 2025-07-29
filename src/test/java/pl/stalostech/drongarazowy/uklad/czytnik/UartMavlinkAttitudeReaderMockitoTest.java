package pl.stalostech.drongarazowy.uklad.czytnik;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.stalostech.drongarazowy.funkcje.Hex;
import pl.stalostech.drongarazowy.protokol.telemetria.mavlink.MavLinkMessage;
import pl.stalostech.drongarazowy.protokol.telemetria.mavlink.MavLinkService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Consumer;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UartMavlinkAttitudeReaderMockitoTest {

    private UartMavlinkAttitudeReader reader;

    @Mock
    private Consumer<MavLinkMessage> listenerMock;

    @Captor
    private ArgumentCaptor<MavLinkMessage> messageCaptor;

    @Mock
    private MavLinkService mavLinkService;

    @BeforeEach
    void setup() {
        reader = spy(new UartMavlinkAttitudeReader(mavLinkService));
        doNothing().when(reader).connect(9600);
    }

    @Test
    void testListen_ValidMavlinkFrame() {
        byte[] validFrame = Hex.hexToBytes("FD 04 01 02 0F 0C 01 05 00 00 74 65 73 74 E8 73 F7 CE 51 38 DA D0 F5 EA 19 FD 35 C6 0A");
        InputStream inputStream = new ByteArrayInputStream(validFrame);

        doReturn(inputStream).when(reader).getSerialPortInputStream();

        MavLinkMessage mockMessage = mock(MavLinkMessage.class);
        when(mavLinkService.decode(any(), eq(null))).thenReturn(mockMessage);

        Thread thread = reader.listen(listenerMock);
        thread.start();

        try {
            thread.join(1000);
        } catch (InterruptedException e) {
            fail("Wątek został przerwany.");
        }

        verify(listenerMock, atLeastOnce()).accept(messageCaptor.capture());
        MavLinkMessage captured = messageCaptor.getValue();

        assertEquals(mockMessage, captured);
    }

}
