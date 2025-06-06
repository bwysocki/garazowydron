package pl.stalostech.drongarazowy.symulacja.kontrola;

import pl.stalostech.drongarazowy.funkcje.TerminalRawMode;
import pl.stalostech.drongarazowy.protokol.rc.KeyboardControl;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class KeyboardInputLoop {

    private final Consumer<KeyboardControl> consumer;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread loopThread;

    public KeyboardInputLoop(Consumer<KeyboardControl> consumer) {
        this.consumer = consumer;
    }

    public void start() {
        if (running.get()) return; // już działa

        running.set(true);
        loopThread = new Thread(() -> {
            KeyboardControl control = new KeyboardControl();

            System.out.println("Drone 003 started");
            System.out.println("Sterowanie:");
            System.out.println(" ← →  = lewo / prawo (lp)");
            System.out.println(" ↑ ↓  = góra / dół (gd)");
            System.out.println(" a / d = zmniejsz / zwiększ motor");
            System.out.println(" p = wyjdz");

            try {
                TerminalRawMode.enable();

                while (running.get()) {
                    if (System.in.available() > 0) {
                        int input = System.in.read();

                        if (input == 27 && System.in.available() >= 2) { // ESC
                            System.in.read(); // skip '['
                            int direction = System.in.read();

                            switch (direction) {
                                case 65 -> control.increaseGd(); // ↑
                                case 66 -> control.decreaseGd(); // ↓
                                case 67 -> control.increaseLp(); // →
                                case 68 -> control.decreaseLp(); // ←
                            }
                        } else {
                            switch (input) {
                                case 'a' -> {
                                    control.decreaseMotor();
                                }
                                case 'd' -> {
                                    control.increaseMotor();
                                }
                                case 'p' -> {
                                    stop(); // zakończ pętlę
                                }
                            }
                        }
                    }

                    consumer.accept(control);

                    Thread.sleep(50);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    TerminalRawMode.disable();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        loopThread.start();
    }

    public void stop() {
        running.set(false);
        if (loopThread != null) {
            loopThread.interrupt();
        }
    }
}

