package pl.stalostech.drongarazowy.uklad.czytnik;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.stalostech.drongarazowy.funkcje.Hex;
import pl.stalostech.drongarazowy.protokol.telemetria.mavlink.MavLinkMessage;
import pl.stalostech.drongarazowy.protokol.telemetria.mavlink.MavLinkService;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static pl.stalostech.drongarazowy.protokol.telemetria.mavlink.MavLinkMessage.MAVLINK_CHECKSUM_LEN;
import static pl.stalostech.drongarazowy.protokol.telemetria.mavlink.MavLinkMessage.MAVLINK_HEADER_LEN;


@Service
public class UartMavlinkAttitudeReader extends SerialPortReader {

    public static class MavLinkReadStatus {
        public int packetRxSuccessCount = 0;
        public int packetRxDropCount = 0;
        public List<Byte> buffer = new ArrayList<>();
    }

    private static final Logger logger = LoggerFactory.getLogger(UartMavlinkAttitudeReader.class);

    private final MavLinkService mavLinkService;

    @Autowired
    public UartMavlinkAttitudeReader(MavLinkService mavLinkService) {
        this.mavLinkService = mavLinkService;
    }

    public Thread listen(Consumer<MavLinkMessage> listener) {
        connect(115200);

        return new Thread(() -> {
            InputStream inputStream = new BufferedInputStream(getSerialPortInputStream());

            MavLinkReadStatus mavLinkReadStatus = new MavLinkReadStatus();
            byte[] buffer = new byte[280];
            while (true) {
                try {
                    int numBytes = inputStream.read(buffer);
                    if (numBytes > 0) {
                        for (int i = 0; i < numBytes; i++) {
                            Optional<MavLinkMessage> mavLinkMessage = parseMavLinkChar(buffer[i], mavLinkReadStatus);
                            if (mavLinkMessage.isPresent()) {
                                /*int from = Math.max(0, i - 10);
                                int to = Math.min(buffer.length, i + 50);
                                byte[] slice = Arrays.copyOfRange(buffer, from, to);

                                logger.info("Odebrano wiadomość (od i={}): {}", i, Hex.bytesToHex(slice));*/
                                listener.accept(mavLinkMessage.get());
                            }
                        }

                    }
                } catch (IOException e) {
                    logger.warn("Blad odczytu danych", e);
                }

            }
        });
    }

    public Optional<MavLinkMessage> parseMavLinkChar(byte inputByte, MavLinkReadStatus status) {
        // Dodaj bajt do ramki
        status.buffer.add(inputByte);

        // Sprawdzenie początku ramki
        if (status.buffer.size() == 1 && status.buffer.get(0) != MavLinkMessage.MAVLINK_STX) {
            // Jeśli pierwszy bajt nie jest poprawnym nagłówkiem, resetuj
            status.buffer.clear();
            return Optional.empty();
        }

        // Sprawdzenie minimalnej długości nagłówka (10 bajtów w MAVLink 2)
        if (status.buffer.size() >= MAVLINK_HEADER_LEN) {
            int length = status.buffer.get(1) & 0xFF;  // Długość ładunku
            int expectedSize = MAVLINK_HEADER_LEN + length + MAVLINK_CHECKSUM_LEN;    // Nagłówek + payload + checksum + signature  + 13

            // Jeśli długość ramki jest kompletna
            if (status.buffer.size() >= expectedSize) {
                byte[] packet = new byte[status.buffer.size()];
                for (int i = 0; i < status.buffer.size(); i++) {
                    packet[i] = status.buffer.get(i);
                }

                try {
                    //logger.info("Probuje zdekodowac pakiet: " + Hex.bytesToHex(packet));
                    MavLinkMessage message = mavLinkService.decode(packet, null); //null = brak sprawdzenia signature
                    status.packetRxSuccessCount++;
                    status.buffer.clear();
                    return Optional.of(message);
                } catch (Exception e) {
                    status.packetRxDropCount++;
                    logger.info("MAVLink decode error: " + e.getMessage());
                    status.buffer.clear();
                }
            }
        }

        return Optional.empty();  // Ramka jeszcze nie jest kompletna
    }


}
