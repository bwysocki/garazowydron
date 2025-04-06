package pl.stalostech.drongarazowy.protokol.telemetria.mavlink;

import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pl.stalostech.drongarazowy.protokol.telemetria.mavlink.MavLinkMessage.MAVLINK_SIGNATURE_LEN;
import static pl.stalostech.drongarazowy.protokol.telemetria.mavlink.MavLinkMessage.MAVLINK_STX;

@Service
public class MavLinkService {

    /**
     * Dekoduje surową wiadomość MAVLink z bajtów do obiektu MavLinkMessage.
     *
     * @param data      Surowe bajty wiadomości
     * @param secretKey Klucz używany do weryfikacji podpisu (opcjonalny)
     * @return Obiekt MavLinkMessage zawierający zdekodowane dane
     */
    public MavLinkMessage decode(byte[] data, byte[] secretKey) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        if (buffer.get() != MAVLINK_STX) {
            throw new IllegalArgumentException("Invalid MAVLink message");
        }

        byte payloadLength = buffer.get();
        byte incompatFlags = buffer.get();
        byte compatFlags = buffer.get();
        byte sequence = buffer.get();
        byte systemId = buffer.get();
        byte componentId = buffer.get();
        int messageId = (buffer.get() & 0xFF) | ((buffer.get() & 0xFF) << 8) | ((buffer.get() & 0xFF) << 16);

        byte[] payload = new byte[payloadLength];
        buffer.get(payload);

        //int checksum = buffer.getShort() & 0xFFFF;
        int ckA = buffer.get() & 0xFF;  // Młodszy bajt (LSB)
        int ckB = buffer.get() & 0xFF;  // Starszy bajt (MSB)

        int checksum = (ckB << 8) | ckA;

        byte[] signature = null;

        if (buffer.remaining() >= MAVLINK_SIGNATURE_LEN) {
            signature = new byte[MAVLINK_SIGNATURE_LEN];
            buffer.get(signature);
        }

        MavLinkMessage message = new MavLinkMessage(incompatFlags, compatFlags, sequence, systemId, componentId, messageId, payload, secretKey);
        if (secretKey != null) {
            if (message.getChecksum() != checksum) {
                throw new IllegalArgumentException("Invalid MavLinkMessage checksum");
            }
            if (!Arrays.equals(signature, message.getSignature())) {
                throw new IllegalArgumentException("Invalid MavLinkMessage signature");
            }
        }

        return message;
    }

    /**
     * Wizualizuje i dekoduje payload wiadomości o typie 30 (ATTITUDE)
     *
     * @param mavLinkMessage Wiadomość MAVLink
     * @return Obiekt zawierający rozkodowane wartości fizyczne z payloadu
     */
    public MavLinkPayload30 visualizePayload(MavLinkMessage mavLinkMessage) {
        byte[] payload = mavLinkMessage.getPayload();
        int messageId = mavLinkMessage.getMessageId();

        if (payload == null || payload.length == 0) {
            throw new IllegalStateException("No payload");
        }

        List<String> rawHexPayload = new ArrayList<>();
        for (byte b : payload) {
            rawHexPayload.add(String.format("%02X", b));
        }

        // Sprawdzenie rodzaju wiadomości
        if (messageId == 30) {  // Wiadomość ATTITUDE
            long timeBootMs = ((payload[0] & 0xFFL)) |
                    ((payload[1] & 0xFFL) << 8) |
                    ((payload[2] & 0xFFL) << 16) |
                    ((payload[3] & 0xFFL) << 24);

            float roll = Float.intBitsToFloat(((payload[4] & 0xFF)) |
                    ((payload[5] & 0xFF) << 8) |
                    ((payload[6] & 0xFF) << 16) |
                    ((payload[7] & 0xFF) << 24));

            float pitch = Float.intBitsToFloat(((payload[8] & 0xFF)) |
                    ((payload[9] & 0xFF) << 8) |
                    ((payload[10] & 0xFF) << 16) |
                    ((payload[11] & 0xFF) << 24));

            float yaw = Float.intBitsToFloat(((payload[12] & 0xFF)) |
                    ((payload[13] & 0xFF) << 8) |
                    ((payload[14] & 0xFF) << 16) |
                    ((payload[15] & 0xFF) << 24));

            float rollSpeed = Float.intBitsToFloat(((payload[16] & 0xFF)) |
                    ((payload[17] & 0xFF) << 8) |
                    ((payload[18] & 0xFF) << 16) |
                    ((payload[19] & 0xFF) << 24));

            float pitchSpeed = Float.intBitsToFloat(((payload[20] & 0xFF)) |
                    ((payload[21] & 0xFF) << 8) |
                    ((payload[22] & 0xFF) << 16) |
                    ((payload[23] & 0xFF) << 24));

            float yawSpeed = Float.intBitsToFloat(((payload[24] & 0xFF)) |
                    ((payload[25] & 0xFF) << 8) |
                    ((payload[26] & 0xFF) << 16) |
                    ((payload[27] & 0xFF) << 24));

            return MavLinkPayload30.builder()
                    .messageId(messageId)
                    .messageType("ATTITUDE")
                    .timeBootMs(timeBootMs)
                    .roll(roll)
                    .pitch(pitch)
                    .yaw(yaw)
                    .rollSpeed(rollSpeed)
                    .pitchSpeed(pitchSpeed)
                    .yawSpeed(yawSpeed)
                    .rawHexPayload(rawHexPayload)
                    .build();
        }
        throw new IllegalStateException("Not valid payload");
    }

}
