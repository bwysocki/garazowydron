package pl.stalostech.drongarazowy.funkcje;

/**
 * Klasa Hex zawiera metody pomocnicze do konwersji między tablicami bajtów a ciągami w formacie szesnastkowym (HEX).
 */
public class Hex {

    /**
     * Konwertuje tablicę bajtów na reprezentację w formacie HEX (dużymi literami, z odstępami między bajtami).
     *
     * @param bytes tablica bajtów do konwersji
     * @return ciąg znaków w formacie HEX (np. "0A FF 1B")
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    /**
     * Konwertuje ciąg znaków w formacie HEX na tablicę bajtów.
     * Akceptuje zarówno ciąg bez odstępów, jak i z odstępami między bajtami.
     *
     * @param hex ciąg znaków reprezentujący bajty w HEX (np. "0AFF1B" lub "0A FF 1B")
     * @return tablica bajtów odpowiadająca podanemu ciągowi HEX
     * @throws IllegalArgumentException jeśli ciąg jest null, ma nieparzystą długość,
     *         lub zawiera nieprawidłowe znaki
     */
    public static byte[] hexToBytes(String hex) {
        if (hex == null) {
            throw new IllegalArgumentException("Invalid HEX string");
        }

        // Usunięcie wszystkich odstępów
        hex = hex.replaceAll("\\s+", "");

        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid HEX string length");
        }

        int length = hex.length();
        byte[] bytes = new byte[length / 2];

        // Konwersja każdej pary znaków HEX na bajt
        for (int i = 0; i < length; i += 2) {
            String byteString = hex.substring(i, i + 2);
            bytes[i / 2] = (byte) Integer.parseInt(byteString, 16);
        }

        return bytes;
    }

}
