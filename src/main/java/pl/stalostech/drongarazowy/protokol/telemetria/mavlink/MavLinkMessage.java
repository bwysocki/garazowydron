package pl.stalostech.drongarazowy.protokol.telemetria.mavlink;

import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Getter
public class MavLinkMessage {

    public static final byte MAVLINK_STX = (byte) 0xFD;  // Magic number for MAVLink 2
    public static final int MAVLINK_HEADER_LEN = 10;
    public static final int MAVLINK_CHECKSUM_LEN = 2;
    public static final int MAVLINK_SIGNATURE_LEN = 13;

    private byte payloadLength;    // Długość ładunku danych (payload) w bajtach - maksymalnie 255 bajtów.
    private byte incompatFlags;    // Flagi niekompatybilności - określają funkcje, które nie są kompatybilne z MAVLink 1.
    private byte compatFlags;      // Flagi kompatybilności - określają funkcje kompatybilne z MAVLink 1 i 2.
    private byte sequence;         // Numer sekwencyjny wiadomości - używany do wykrywania utraty pakietów i ich kolejności.
    private byte systemId;         // Identyfikator systemu (np. konkretnego drona) - identyfikuje jednostkę wysyłającą wiadomość.
    private byte componentId;      // Identyfikator komponentu - określa moduł lub urządzenie wysyłające wiadomość (np. autopilot).
    private int messageId;         // Identyfikator typu wiadomości - unikalny numer, który identyfikuje rodzaj danych w wiadomości.
    private byte[] payload;        // Dane ładunku - zawiera właściwą treść wiadomości, np. pozycję GPS, status drona.
    private int checksum;          // Suma kontrolna (CRC) - sprawdza integralność danych, aby wykryć uszkodzenie podczas transmisji.
    private byte[] signature;      // Podpis cyfrowy (13 bajtów) - zabezpiecza wiadomość przed nieautoryzowaną modyfikacją (MAVLink 2).

    public MavLinkMessage(byte incompatFlags, byte compatFlags, byte sequence, byte systemId, byte componentId, int messageId, byte[] payload, byte[] secretKey) {
        this.payloadLength = (byte) payload.length;
        this.incompatFlags = incompatFlags;
        this.compatFlags = compatFlags;
        this.sequence = sequence;
        this.systemId = systemId;
        this.componentId = componentId;
        this.messageId = messageId;
        this.payload = payload;
        if (secretKey != null) {
            this.checksum = calculateChecksum(this);

            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");

                ByteBuffer mavlinkByteBuffer = ByteBuffer.allocate(MAVLINK_HEADER_LEN + payload.length + MAVLINK_CHECKSUM_LEN).order(ByteOrder.LITTLE_ENDIAN);
                asByteBuffer(mavlinkByteBuffer);

                md.update(mavlinkByteBuffer.array());
                md.update(secretKey);

                byte[] hash = md.digest();
                signature = new byte[13];
                System.arraycopy(hash, 0, signature, 0, Math.min(hash.length, MAVLINK_SIGNATURE_LEN));
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Not possible to sign MavLinkMessage", e);
            }
        }
    }

    @Override
    public String toString() {
        return "MavLinkMessage{" +
                "payloadLength=" + payloadLength +
                ", incompatFlags=" + incompatFlags +
                ", compatFlags=" + compatFlags +
                ", sequence=" + sequence +
                ", systemId=" + systemId +
                ", componentId=" + componentId +
                ", messageId=" + messageId +
                ", checksum=" + checksum +
                '}';
    }

    public byte[] encode() {
        ByteBuffer mavlinkByteBuffer = ByteBuffer.allocate(MAVLINK_HEADER_LEN + payload.length + MAVLINK_CHECKSUM_LEN + MAVLINK_SIGNATURE_LEN).order(ByteOrder.LITTLE_ENDIAN);
        asByteBuffer(mavlinkByteBuffer);
        mavlinkByteBuffer.put(signature);

        return mavlinkByteBuffer.array();
    }

    private void asByteBuffer(ByteBuffer mavLinkByteBuffer) {
        mavLinkByteBuffer.put(MAVLINK_STX);
        mavLinkByteBuffer.put(payloadLength);
        mavLinkByteBuffer.put(incompatFlags);
        mavLinkByteBuffer.put(compatFlags);
        mavLinkByteBuffer.put(sequence);
        mavLinkByteBuffer.put(systemId);
        mavLinkByteBuffer.put(componentId);
        mavLinkByteBuffer.put((byte) (messageId & 0xFF));
        mavLinkByteBuffer.put((byte) ((messageId >> 8) & 0xFF));
        mavLinkByteBuffer.put((byte) ((messageId >> 16) & 0xFF));
        mavLinkByteBuffer.put(payload);
        mavLinkByteBuffer.putShort((short) (checksum & 0xFFFF));
    }

    private static int calculateChecksum(MavLinkMessage mavLinkMessage) {
        int crc = 0xFFFF;

        // 1. Symulacja struktury nagłówka z C++ (buf[1] - length)
        crc = crcAccumulate(mavLinkMessage.getPayloadLength(), crc); // Length
        crc = crcAccumulate(mavLinkMessage.getIncompatFlags(), crc); // Incompat flags
        crc = crcAccumulate(mavLinkMessage.getCompatFlags(), crc);   // Compat flags
        crc = crcAccumulate(mavLinkMessage.getSequence(), crc);      // Sequence
        crc = crcAccumulate(mavLinkMessage.getSystemId(), crc);      // System ID
        crc = crcAccumulate(mavLinkMessage.getComponentId(), crc);   // Component ID

        // 2. Obliczanie CRC dla identyfikatora wiadomości (3 bajty)
        crc = crcAccumulate((byte) (mavLinkMessage.getMessageId() & 0xFF), crc);         // LSB
        crc = crcAccumulate((byte) ((mavLinkMessage.getMessageId() >> 8) & 0xFF), crc);  // Middle byte
        crc = crcAccumulate((byte) ((mavLinkMessage.getMessageId() >> 16) & 0xFF), crc); // MSB

        crc = crcAccumulateBuffer(crc, mavLinkMessage.getPayload());
        crc = crcAccumulate((byte) 30, crc);

        // 5. Zwrócenie CRC jako wartość 16-bitowa
        return crc & 0xFFFF;
    }


    private static int crcAccumulate(byte b, int crc) {
        int tmp = (b ^ (crc & 0xFF)) & 0xFF;
        tmp = (tmp ^ (tmp << 4)) & 0xFF;
        return ((crc >> 8) ^ (tmp << 8) ^ (tmp << 3) ^ (tmp >> 4)) & 0xFFFF;
    }

    private static int crcAccumulateBuffer(int crcAccum, byte[] buffer) {
        for (byte b : buffer) {
            int unsignedByte = b & 0xFF;
            crcAccum = crcAccumulate((byte) unsignedByte, crcAccum);
        }
        return crcAccum;
    }

}
