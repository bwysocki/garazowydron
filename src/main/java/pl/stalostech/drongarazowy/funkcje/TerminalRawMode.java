package pl.stalostech.drongarazowy.funkcje;

import java.io.IOException;

public class TerminalRawMode {

    public static void enable() throws IOException, InterruptedException {
        new ProcessBuilder("sh", "-c", "stty raw -echo").inheritIO().start().waitFor();
    }

    public static void disable() throws IOException, InterruptedException {
        new ProcessBuilder("sh", "-c", "stty sane").inheritIO().start().waitFor();
    }

}
