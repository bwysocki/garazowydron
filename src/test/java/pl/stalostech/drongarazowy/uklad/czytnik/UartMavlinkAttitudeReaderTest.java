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
        byte[] validPacket = new byte[10 + 5 + 2]; // header + payload + checksum + (13 signature)
        validPacket[0] = MavLinkMessage.MAVLINK_STX;
        validPacket[1] = 5; // payload length = 5
        int checksum = 23070; // 0x5A2E
        validPacket[validPacket.length - 2] = (byte) (checksum & 0xFF);
        validPacket[validPacket.length - 1] = (byte) ((checksum >> 8) & 0xFF);

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
        byte[] frame = Hex.hexToBytes("FD 04 01 02 00 0C 01 02 00 00 74 65 70 74 CE 1C");

        Optional<MavLinkMessage> result = Optional.empty();
        for (byte b : frame) {
            result = uartMavlinkAttitudeReader.parseMavLinkChar(b, mavLinkReadStatus);
        }

        assertTrue(result.isPresent());
        assertEquals(1, mavLinkReadStatus.packetRxSuccessCount);
        assertEquals(0, mavLinkReadStatus.packetRxDropCount);
        assertTrue(mavLinkReadStatus.buffer.isEmpty());

        assertEquals("MavLinkMessage{payloadLength=4, incompatFlags=1, compatFlags=2, sequence=0, systemId=12, componentId=1, messageId=2, checksum=7374}",
                result.get().toString());
    }

}
