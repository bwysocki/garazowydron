package pl.stalostech.drongarazowy.uklad.czytnik;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.stalostech.drongarazowy.uklad.model.MPU6050Record;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;

/**
 * Service do odczytu danych z czujnika MPU6050 przy użyciu portu szeregowego (np Arduino -> komputer).
 * <p>
 * Odczytuje dane przyspieszenia i prędkości kątowej z portu szeregowego i przetwarza je w czasie rzeczywistym.
 * Dane są przekazywane w formie obiektu MPU6050Record do dostarczonego listenera.
 * <p>
 * Klasa rozszerza SerialPortReader, który zajmuje się otwieraniem i zamykaniem portu szeregowego.
 */
@Service
public class MPU6050RecordReader extends SerialPortReader {

    private static final Logger logger = LoggerFactory.getLogger(MPU6050RecordReader.class);

    /**
     * Nasłuchuje danych z portu szeregowego i przetwarza je na obiekty MPU6050Record.
     * <p>
     * Otwiera połączenie szeregowe z domyślną prędkością transmisji 115200 bodów.
     * Tworzy nowy wątek, który nasłuchuje danych przychodzących z portu szeregowego
     * i przekazuje je do listenera w formie obiektu MPU6050Record.
     *
     * @param listener Obiekt konsumenta, który odbiera przetworzone dane z czujnika (MPU6050Record).
     * @return Wątek nasłuchujący danych z portu szeregowego.
     */
    public Thread listen(Consumer<MPU6050Record> listener) {
        // Nawiązanie połączenia szeregowego z prędkością transmisji 115200 bodów
        connect(115200);

        // Tworzenie i zwracanie nowego wątku nasłuchującego danych z portu
        return new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getSerialPortInputStream()));

            // Inicjalizacja zmiennych do sumowania wartości pomiarów (filtr offsetu)
            int ax_sum = 0;
            int ay_sum = 0;
            int az_sum = 0;
            int gx_sum = 0;
            int gy_sum = 0;
            int gz_sum = 0;

            String line;
            while (true) {
                try {
                    if ((line = reader.readLine()) == null) break;
                } catch (IOException e) {
                    throw new RuntimeException("IOException while reading line", e);
                }

                String[] values = line.split(",");
                if (values.length == 6) {
                    try {
                        int ax = Integer.parseInt(values[0]) - ax_sum;
                        int ay = Integer.parseInt(values[1]) - ay_sum;
                        int az = Integer.parseInt(values[2]) - az_sum;
                        int gx = Integer.parseInt(values[3]) - gx_sum;
                        int gy = Integer.parseInt(values[4]) - gy_sum;
                        int gz = Integer.parseInt(values[5]) - gz_sum;

                        // Utworzenie obiektu MPU6050Record z odczytanymi wartościami i przekazanie go do listenera
                        listener.accept(new MPU6050Record(ax, ay, az, gx, gy, gz));
                    } catch (NumberFormatException e) {
                        logger.error("Błąd parsowania danych: '{}', przyczyna: {}", line, e.getMessage());
                    }
                } else {
                    logger.warn("Nieprawidłowa liczba wartości w danych: '{}'", line);
                }
            }

            logger.info("Zakończono nasłuch danych z portu szeregowego.");
        });
    }


}
