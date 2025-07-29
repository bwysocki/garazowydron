package pl.stalostech.drongarazowy.uklad.czytnik;

import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Abstrakcyjna klasa do zarządzania połączeniem z urządzeniem szeregowym (np. Arduino).
 * Umożliwia nawiązanie połączenia przez port szeregowy oraz ustawienie parametrów komunikacji.
 * Implementacja oparta na bibliotece jSerialComm.
 */
abstract class SerialPortReader {

    private static final Logger logger = LoggerFactory.getLogger(SerialPortReader.class);

    @Value("${serial.port:/dev/ttyUSB0}")
    private String PORT = "/dev/ttyUSB0";

    protected SerialPort serialPort;

    /**
     * Nawiązuje połączenie z portem szeregowym o podanej prędkości transmisji (baud rate).
     * Ustawia parametry połączenia i otwiera port.
     *
     * @param baudRate prędkość transmisji (np. 9600, 115200)
     * @throws RuntimeException jeśli nie uda się otworzyć portu szeregowego
     */
    public void connect(int baudRate) {
        serialPort = SerialPort.getCommPort(PORT);
        serialPort.setBaudRate(baudRate);

        //serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        if (!serialPort.openPort()) {
            throw new RuntimeException("Nie można otworzyć portu (%s)! Sprawdź uprawnienia lub czy port nie jest zajęty.".formatted(PORT));
        }

        logger.info("Połączono na porcie: {}", serialPort.getSystemPortName());
    }

    public InputStream getSerialPortInputStream() {
        if (isConnected()) {
            return serialPort.getInputStream();
        }
        throw new IllegalStateException("Serial port nie jest polaczony.");
    }

    public OutputStream getSerialPortOutputStream() {
        if (isConnected()) {
            return serialPort.getOutputStream();
        }
        throw new IllegalStateException("Serial port nie jest połączony.");
    }

    /**
     * Zamyka port szeregowy, jeśli jest otwarty.
     */
    public void disconnect() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            logger.info("Rozłączono na porcie: {}", serialPort.getSystemPortName());
        } else {
            logger.warn("Port nie był otwarty lub nie istnieje!");
        }
    }

    /**
     * Sprawdza, czy port szeregowy jest otwarty.
     *
     * @return true jeśli port jest otwarty, false w przeciwnym razie
     */
    public boolean isConnected() {
        return serialPort != null && serialPort.isOpen();
    }

}
