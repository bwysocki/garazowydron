package pl.stalostech.drongarazowy.funkcje;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HexTest {

    @Test
    void testBytesToHex_withStandardInput() {
        byte[] input = {0x0A, (byte) 0xFF, 0x1B};
        String expected = "0A FF 1B";
        String result = Hex.bytesToHex(input);
        assertEquals(expected, result);
    }

    @Test
    void testBytesToHex_withEmptyArray() {
        byte[] input = {};
        String expected = "";
        String result = Hex.bytesToHex(input);
        assertEquals(expected, result);
    }

    @Test
    void testHexToBytes_withStandardInput() {
        String input = "0A FF 1B";
        byte[] expected = {0x0A, (byte) 0xFF, 0x1B};
        byte[] result = Hex.hexToBytes(input);
        assertArrayEquals(expected, result);
    }

    @Test
    void testHexToBytes_withoutSpaces() {
        String input = "0AFF1B";
        byte[] expected = {0x0A, (byte) 0xFF, 0x1B};
        byte[] result = Hex.hexToBytes(input);
        assertArrayEquals(expected, result);
    }

    @Test
    void testHexToBytes_withMixedWhitespace() {
        String input = "0A\tFF\n1B ";
        byte[] expected = {0x0A, (byte) 0xFF, 0x1B};
        byte[] result = Hex.hexToBytes(input);
        assertArrayEquals(expected, result);
    }

    @Test
    void testHexToBytes_withEmptyString() {
        String input = "";
        byte[] expected = {};
        byte[] result = Hex.hexToBytes(input);
        assertArrayEquals(expected, result);
    }

    @Test
    void testHexToBytes_throwsOnNullInput() {
        assertThrows(IllegalArgumentException.class, () -> Hex.hexToBytes(null));
    }

    @Test
    void testHexToBytes_throwsOnOddLengthInput() {
        assertThrows(IllegalArgumentException.class, () -> Hex.hexToBytes("ABC"));
    }

    @Test
    void testHexToBytes_throwsOnInvalidCharacters() {
        assertThrows(NumberFormatException.class, () -> Hex.hexToBytes("0A ZZ"));
    }

    @Test
    void testBytesToHexAndBack_isSymmetric() {
        byte[] original = {0x00, 0x10, 0x20, 0x30, (byte) 0xFE, (byte) 0xFF};
        String hex = Hex.bytesToHex(original);
        byte[] result = Hex.hexToBytes(hex);
        assertArrayEquals(original, result);
    }

}
