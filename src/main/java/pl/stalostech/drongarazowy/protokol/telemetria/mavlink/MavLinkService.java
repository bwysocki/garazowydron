package pl.stalostech.drongarazowy.protokol.telemetria.mavlink;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.stalostech.drongarazowy.funkcje.Hex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pl.stalostech.drongarazowy.protokol.telemetria.mavlink.MavLinkMessage.MAVLINK_SIGNATURE_LEN;
import static pl.stalostech.drongarazowy.protokol.telemetria.mavlink.MavLinkMessage.MAVLINK_STX;

@Service
@Slf4j
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
        if (message.getChecksum() != checksum) {
            throw new IllegalArgumentException("Invalid MavLinkMessage checksum:" + Hex.bytesToHex(data));
        }
        if (secretKey != null) { //todo secret not validated
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
    public MavLinkPayload30 getPayload30(MavLinkMessage mavLinkMessage) {
        byte[] payload = mavLinkMessage.getPayload();
        int messageId = mavLinkMessage.getMessageId();

        if (payload == null || payload.length == 0) {
            throw new IllegalStateException("No payload");
        }

        List<String> rawHexPayload = new ArrayList<>();
        for (byte b : payload) {
            rawHexPayload.add(String.format("%02X", b));
        }

        if (messageId == 30) { // ATTITUDE
            long timeBootMs = ((payload[0] & 0xFFL)) |
                    ((payload[1] & 0xFFL) << 8) |
                    ((payload[2] & 0xFFL) << 16) |
                    ((payload[3] & 0xFFL) << 24);

            float roll = getFloat32LE(payload, 4);
            float pitch = getFloat32LE(payload, 8);
            float yaw = getFloat32LE(payload, 12);
            float rollSpeed = getFloat32LE(payload, 16);
            float pitchSpeed = getFloat32LE(payload, 20);
            float yawSpeed = getFloat32LE(payload, 24);

            //System.out.println(roll + " " + pitch + " " + yaw );

            // Odfiltrowanie śmieci
            if (isInvalidFloat(roll)) {
                log.warn("Ignoruję nieprawidłową wartość roll: {}", roll);
                roll = 0;
            }
            if (isInvalidFloat(pitch)) {
                log.warn("Ignoruję nieprawidłową wartość pitch: {}", pitch);
                pitch = 0;
            }
            if (isInvalidFloat(yaw)) {
                log.warn("Ignoruję nieprawidłową wartość yaw: {}", yaw);
                yaw = 0;
            }

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

    // Pomocnicza metoda do odczytu float z bajtów little-endian
    private float getFloat32LE(byte[] data, int offset) {
        int intBits = ((data[offset] & 0xFF)) |
                ((data[offset + 1] & 0xFF) << 8) |
                ((data[offset + 2] & 0xFF) << 16) |
                ((data[offset + 3] & 0xFF) << 24);
        return Float.intBitsToFloat(intBits);
    }

    // Walidacja floatów (czy nie są zbyt małe/duże/NaN/Infinity)
    private boolean isInvalidFloat(float value) {
        return !Float.isFinite(value) || Math.abs(value) >= 1_000;
    }

}
