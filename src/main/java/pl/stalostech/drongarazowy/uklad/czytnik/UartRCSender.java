package pl.stalostech.drongarazowy.uklad.czytnik;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.stalostech.drongarazowy.funkcje.Hex;
import pl.stalostech.drongarazowy.protokol.rc.KeyboardControl;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class UartRCSender extends SerialPortReader {

    private static final long SEND_INTERVAL_MS = 200;
    volatile KeyboardControl lastCommand;

    //@PostConstruct
    public void init() {
        connect(9600);

        // startuje osobny wątek który co SEND_INTERVAL_MS wysyła ostatnią komendę
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::sendLastCommand, 0, SEND_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    public void send(KeyboardControl keyboardControl) {
        this.lastCommand = keyboardControl;
    }

    public void sendLastCommand() {
        if (lastCommand == null) {
            return;
        }

        System.out.print("\r" + "Sending last command: " + lastCommand);
        System.out.flush();

        byte[] payload = lastCommand.encode();
        serialPort.writeBytes(payload, payload.length);
    }

}
