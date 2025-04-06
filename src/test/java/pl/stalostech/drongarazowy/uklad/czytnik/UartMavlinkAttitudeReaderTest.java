package pl.stalostech.drongarazowy.uklad.czytnik;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.stalostech.drongarazowy.funkcje.Hex;
import pl.stalostech.drongarazowy.protokol.telemetria.mavlink.MavLinkMessage;
import pl.stalostech.drongarazowy.seria.d002.Drone002TestConfig;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Drone002TestConfig.class)
public class UartMavlinkAttitudeReaderTest {

    @Autowired
    private UartMavlinkAttitudeReader uartMavlinkAttitudeReader;

    private UartMavlinkAttitudeReader.MavLinkReadStatus mavLinkReadStatus;

    @BeforeEach
    public void setUp() {
        mavLinkReadStatus = new UartMavlinkAttitudeReader.MavLinkReadStatus();
    }

    @Test
    public void shouldClearBufferWhenFirstByteIsInvalid() {
        byte invalidStartByte = (byte) 0x00;
        Optional<MavLinkMessage> result = uartMavlinkAttitudeReader.parseMavLinkChar(invalidStartByte, mavLinkReadStatus);

        assertTrue(result.isEmpty());
        assertEquals(0, mavLinkReadStatus.buffer.size());
    }

    @Test
    public void shouldNotParseIncompletePacket() {
        mavLinkReadStatus.buffer.add(MavLinkMessage.MAVLINK_STX); // poprawny start
        mavLinkReadStatus.buffer.add((byte) 5); // payload length
        // tylko np. 8 bajtów
        for (int i = 2; i < 8; i++) {
            mavLinkReadStatus.buffer.add((byte) i);
        }

        Optional<MavLinkMessage> result = uartMavlinkAttitudeReader.parseMavLinkChar((byte) 7, mavLinkReadStatus);

        assertTrue(result.isEmpty());
        assertFalse(mavLinkReadStatus.buffer.isEmpty());
    }

    @Test
    public void shouldParseCompletePacketSuccessfully() {
        byte[] validPacket = new byte[10 + 5 + 2 + 13]; // header + payload + checksum + signature
        validPacket[0] = MavLinkMessage.MAVLINK_STX;
        validPacket[1] = 5; // payload length = 5

        for (byte b : validPacket) {
            mavLinkReadStatus.buffer.add(b);
        }

        Optional<MavLinkMessage> result = uartMavlinkAttitudeReader.parseMavLinkChar((byte) 0x00, mavLinkReadStatus);

        assertTrue(result.isPresent());
        assertEquals(1, mavLinkReadStatus.packetRxSuccessCount);
        assertEquals(0, mavLinkReadStatus.packetRxDropCount);
        assertTrue(mavLinkReadStatus.buffer.isEmpty());
    }

    @Test
    public void shouldParseRealMessageSuccessfully() {
        byte[] frame = Hex.hexToBytes("FD 04 01 02 0F 0C 01 05 00 00 74 65 73 74 E8 73 F7 CE 51 38 DA D0 F5 EA 19 FD 35 C6 0A");

        Optional<MavLinkMessage> result = Optional.empty();
        for (byte b : frame) {
            result = uartMavlinkAttitudeReader.parseMavLinkChar(b, mavLinkReadStatus);
        }

        assertTrue(result.isPresent());
        assertEquals(1, mavLinkReadStatus.packetRxSuccessCount);
        assertEquals(0, mavLinkReadStatus.packetRxDropCount);
        assertTrue(mavLinkReadStatus.buffer.isEmpty());

        assertEquals("MavLinkMessage{payloadLength=4, incompatFlags=1, compatFlags=2, sequence=15, systemId=12, componentId=1, messageId=5, checksum=0}",
                result.get().toString());
    }

}
