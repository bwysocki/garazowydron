package pl.stalostech.drongarazowy.uklad.czytnik;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;


@Service
public class UartMavlinkPacketReaderWritter extends SerialPortReader {

    private static final Logger logger = LoggerFactory.getLogger(UartMavlinkPacketReaderWritter.class);

    public Thread listen(Consumer<MAVLinkPacket> listener) {
        connect(9600);

        return new Thread(() -> {
            InputStream inputStream = new BufferedInputStream(getSerialPortInputStream());
            byte[] buffer = new byte[280];
            byte[] previousBuffer = null;

            while (true) {
                try {
                    int numBytes = inputStream.read(buffer);
                    if (numBytes > 0) {
                        previousBuffer = processBytes(buffer, numBytes, listener, previousBuffer);
                    }
                    Thread.sleep(200);
                } catch (IOException e) {
                    logger.warn("Błąd odczytu danych", e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


    public byte[] processBytes(
            byte[] buffer,
            int length,
            Consumer<MAVLinkPacket> listener,
            byte[] previousBuffer
    ) {

        // Połącz previousBuffer z nowym buforem
        byte[] combined;
        if (previousBuffer != null && previousBuffer.length > 0) {
            combined = new byte[previousBuffer.length + length];
            System.arraycopy(previousBuffer, 0, combined, 0, previousBuffer.length);
            System.arraycopy(buffer, 0, combined, previousBuffer.length, length);
        } else {
            combined = Arrays.copyOf(buffer, length);
        }

        int i = 0;
        while (i < combined.length) {
            // Szukaj bajtu startowego MAVLink v2
            if ((combined[i] & 0xFF) == 0xFD) {
                if (i + 1 >= combined.length) break; // Brak danych o długości payloadu

                int payloadLen = combined[i + 1] & 0xFF;
                int packetLen = 10 + payloadLen + 2; // header (10) + payload + CRC (2) - bez podpisu

                if (i + packetLen <= combined.length) {
                    // Spróbuj sparsować całą wiadomość
                    Parser parser = new Parser();
                    for (int j = 0; j < packetLen; j++) {
                        MAVLinkPacket packet = parser.mavlink_parse_char(combined[i + j] & 0xFF);
                        if (packet != null) {
                            listener.accept(packet);
                            break;
                        }
                    }
                    i += packetLen;
                } else {
                    // Za mało danych na cały pakiet – przerywamy i buforujemy resztę
                    break;
                }
            } else {
                // Nieprawidłowy bajt startowy – pomiń
                i++;
            }
        }

        // Zwrot: reszta bajtów, które mogą należeć do niedokończonej wiadomości
        if (i < combined.length) {
            int remainingFrom = Math.max(i, previousBuffer != null ? previousBuffer.length : 0);
            int unparsedLength = combined.length - remainingFrom;
            byte[] remaining = new byte[unparsedLength];
            System.arraycopy(combined, remainingFrom, remaining, 0, unparsedLength);
            return remaining;
        } else {
            return null;
        }
    }


    public void send(MAVLinkMessage message) {
        try {
            MAVLinkPacket packet = message.pack();
            packet.sysid = 1;
            packet.compid = 1;
            byte[] bytes = packet.encodePacket();

            OutputStream outputStream = getSerialPortOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            logger.warn("Błąd podczas wysyłania pakietu MAVLink", e);
        }
    }


}
