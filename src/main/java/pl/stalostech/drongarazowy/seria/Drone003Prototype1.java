package pl.stalostech.drongarazowy.seria;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import pl.stalostech.drongarazowy.funkcje.TerminalRawMode;
import pl.stalostech.drongarazowy.protokol.rc.KeyboardControl;
import pl.stalostech.drongarazowy.uklad.czytnik.UartRCSender;

import java.io.IOException;

@SpringBootApplication
@ComponentScan({"pl.stalostech.drongarazowy.uklad", "pl.stalostech.drongarazowy.protokol.telemetria.mavlink"})
@Slf4j
public class Drone003Prototype1 {

    private static ApplicationContext context;

    public static void main(String[] args) throws InterruptedException, IOException {
        context = SpringApplication.run(Drone003Prototype1.class, args);
        UartRCSender uartRCSender = context.getBean(UartRCSender.class);

        KeyboardControl control = new KeyboardControl();


        System.out.println("Drone 003 started");
        System.out.println("Sterowanie:");
        System.out.println(" ← →  = lewo / prawo (lp)");
        System.out.println(" ↑ ↓  = góra / dół (gd)");
        System.out.println(" a / d = zmniejsz / zwiększ motor");
        System.out.println(" p = wyjdz");

        TerminalRawMode.enable();
        try {
            while (true) {
                if (System.in.available() > 0) {
                    int input = System.in.read();

                    boolean changed = false;

                    if (input == 27 && System.in.available() >= 2) { // ESC
                        System.in.read(); // skip '['
                        int direction = System.in.read();

                        switch (direction) {
                            case 65 -> control.increaseGd(); // ↑
                            case 66 -> control.decreaseGd(); // ↓
                            case 67 -> control.increaseLp(); // →
                            case 68 -> control.decreaseLp(); // ←
                        }

                        changed = true;
                    } else {
                        switch (input) {
                            case 'a' -> {
                                control.decreaseMotor();
                                changed = true;
                            }
                            case 'd' -> {
                                control.increaseMotor();
                                changed = true;
                            }
                            case 'p' -> {
                                throw new InterruptedException("Bye");
                            }
                        }
                    }

                    uartRCSender.send(control);

                    if (changed) {
                        //System.out.print("\r" + control + "          ");
                        //System.out.flush();
                    }
                }

                Thread.sleep(50);
            }
        } finally {
            uartRCSender.disconnect();
            TerminalRawMode.disable();
        }

    }

}