package pl.stalostech.drongarazowy.protokol.telemetria.mavlink;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.stalostech.drongarazowy.funkcje.Hex;
import pl.stalostech.drongarazowy.seria.d002.Drone002TestConfig;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Drone002TestConfig.class)
public class MavLinkServiceTest {

    public static final String MY_SECRET = "my-secret";

    @Autowired
    private MavLinkService mavLinkService;

    @Test
    public void testDecode() {
        MavLinkMessage mavLinkMessage = mavLinkService.decode(
                Hex.hexToBytes("FD 04 01 02 0F 0C 01 05 00 00 74 65 73 74 E8 73 F7 CE 51 38 DA D0 F5 EA 19 FD 35 C6 0A"),
                MY_SECRET.getBytes()
        );
        assertEquals("MavLinkMessage{payloadLength=4, incompatFlags=1, compatFlags=2, sequence=15, systemId=12, componentId=1, messageId=5, checksum=29672}",
                mavLinkMessage.toString());
    }

    @Test
    void testDecode_invalidChecksum() {
        byte[] payload = "test".getBytes();

        MavLinkMessage message = new MavLinkMessage(
                (byte) 1, (byte) 2, (byte) 15, (byte) 12, (byte) 1, 5, payload, MY_SECRET.getBytes()
        );

        byte[] encoded = message.encode();

        // Uszkodź checksum (np. 2 ostatnie bajty)
        encoded[encoded.length - 14] ^= 0xFF; // Flip a byte in checksum

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            mavLinkService.decode(encoded, MY_SECRET.getBytes());
        });

        assertTrue(exception.getMessage().contains("Invalid MavLinkMessage checksum"));
    }

    @Test
    void testDecode_invalidSignature() {
        byte[] payload = "test".getBytes();

        MavLinkMessage message = new MavLinkMessage(
                (byte) 1, (byte) 2, (byte) 15, (byte) 12, (byte) 1, 5, payload, MY_SECRET.getBytes()
        );

        byte[] encoded = message.encode();

        // Uszkodź podpis – np. ostatni bajt
        encoded[encoded.length - 1] ^= 0xFF;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            mavLinkService.decode(encoded, MY_SECRET.getBytes());
        });

        assertTrue(exception.getMessage().contains("Invalid MavLinkMessage signature"));
    }


    @Test
    public void testDecodeArduino() {
        //todo - w bibliotece arduino i tutaj wystepuje inny sposob liczenie podpisu - do naprawy
        MavLinkMessage mavLinkMessage = mavLinkService.decode(
                Hex.hexToBytes("FD 1C 00 00 01 01 01 1E 00 00 07 00 00 00 D1 4F FD 3C 95 C8 83 BA 88 DF 18 38 3B 36 FD BC F5 3E 86 3C AB D5 60 3C CB C9 64 16 B8 11 79 13 21 BE A8 50 98 ED 35"),
                null
        );
        assertEquals("MavLinkMessage{payloadLength=28, incompatFlags=0, compatFlags=0, sequence=1, systemId=1, componentId=1, messageId=30, checksum=51659}",
                mavLinkMessage.toString());
    }

    @Test
    public void testVizualize() {
        MavLinkMessage mavLinkMessage = mavLinkService.decode(
                Hex.hexToBytes("FD 1C 00 00 00 01 01 1E 00 00 03 BB 00 00 CD CC CC 3D 9A 99 99 3E 00 00 00 3F 0A D7 23 3C 0A D7 A3 3C 8F C2 F5 3C 23 27 D9 4D 5E 30 CB 0B 00 3B 3B 36 2B 0D 0A"),
                null
        );
        assertEquals("""
                        Payload30{yawSpeed=0.03, pitchSpeed=0.02, rollSpeed=0.01, yaw=0.5, pitch=0.3, roll=0.1, timeBootMs=47875, messageId=30, messageType='ATTITUDE'}
                        """.trim(),
                mavLinkService.getPayload30(mavLinkMessage).toString());
    }

}
