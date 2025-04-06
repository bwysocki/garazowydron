package pl.stalostech.drongarazowy.protokol.telemetria.mavlink;

import org.junit.jupiter.api.Test;
import pl.stalostech.drongarazowy.funkcje.Hex;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MavLinkMessageTest {

    public static final String MY_SECRET = "my-secret";

    @Test
    public void testMessageCreation() {
        byte[] payload = "test".getBytes();
        MavLinkMessage mavLinkMessage = new MavLinkMessage(
                (byte) 1, (byte) 2, (byte) 15, (byte) 12, (byte) 1, 5, payload, MY_SECRET.getBytes()
        );
        assertEquals("MavLinkMessage{payloadLength=4, incompatFlags=1, compatFlags=2, sequence=15, systemId=12, componentId=1, messageId=5, checksum=29672}",
                mavLinkMessage.toString());
    }

    @Test
    public void testEncode() {
        byte[] payload = "test".getBytes();
        MavLinkMessage mavLinkMessage = new MavLinkMessage(
                (byte) 1, (byte) 2, (byte) 15, (byte) 12, (byte) 1, 5, payload, MY_SECRET.getBytes()
        );
        byte[] signature = mavLinkMessage.getSignature();

        org.junit.jupiter.api.Assertions.assertEquals("F7 CE 51 38 DA D0 F5 EA 19 FD 35 C6 0A",
                Hex.bytesToHex(signature));

        byte[] signedMessage = mavLinkMessage.encode();

        assertEquals("FD 04 01 02 0F 0C 01 05 00 00 74 65 73 74 E8 73 F7 CE 51 38 DA D0 F5 EA 19 FD 35 C6 0A",
                Hex.bytesToHex(signedMessage));
    }

    @Test
    public void testChecksum() {
        byte[] payload = new byte[]{
                (byte) 0x00, (byte) 0x03, (byte) 0xBB, (byte) 0x00, (byte) 0x00,
                (byte) 0xCD, (byte) 0xCC, (byte) 0xCC, (byte) 0x3D, (byte) 0x9A,
                (byte) 0x99, (byte) 0x99, (byte) 0x3E, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x3F, (byte) 0x0A, (byte) 0xD7, (byte) 0x23,
                (byte) 0x3C, (byte) 0x0A, (byte) 0xD7, (byte) 0xA3, (byte) 0x3C,
                (byte) 0x8F, (byte) 0xC2, (byte) 0xF5, (byte) 0x3C
        };

        MavLinkMessage mavLinkMessage = new MavLinkMessage(
                (byte) 1, (byte) 0, (byte) 0, (byte) 89, (byte) 1, 30, payload, MY_SECRET.getBytes()
        );

        assertEquals(30007, mavLinkMessage.getChecksum());
    }

}
