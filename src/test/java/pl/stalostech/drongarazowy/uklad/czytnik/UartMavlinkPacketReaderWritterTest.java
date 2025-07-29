package pl.stalostech.drongarazowy.uklad.czytnik;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;
import com.MAVLink.enums.MAV_AUTOPILOT;
import com.MAVLink.enums.MAV_STATE;
import com.MAVLink.enums.MAV_TYPE;
import com.MAVLink.minimal.msg_heartbeat;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class UartMavlinkPacketReaderWritterTest {

    @Test
    void shouldParseCompletePacketInSingleBuffer() {
        // given
        byte[] mavlinkPacket = createValidMavlinkPacket(); // np. FD 09 ... CRC
        List<MAVLinkPacket> packets = new ArrayList<>();
        Consumer<MAVLinkPacket> listener = packets::add;

        // when
        UartMavlinkPacketReaderWritter reader = new UartMavlinkPacketReaderWritter();
        byte[] remaining = reader.processBytes(mavlinkPacket, mavlinkPacket.length, listener, null);

        // then
        assertEquals(1, packets.size(), "Powinien zostać sparsowany 1 pakiet");
        assertNull(remaining, "Nie powinno zostać nic w buforze");
    }

    @Test
    void shouldParsePacketSplitAcrossBuffers() {
        // given
        byte[] part1 = createValidMavlinkPacketPart1(); // np. FD 09 00
        byte[] part2 = createValidMavlinkPacketPart2(); // reszta pakietu
        List<MAVLinkPacket> packets = new ArrayList<>();
        Consumer<MAVLinkPacket> listener = packets::add;

        UartMavlinkPacketReaderWritter reader = new UartMavlinkPacketReaderWritter();

        // when
        byte[] previous = reader.processBytes(part1, part1.length, listener, null);
        byte[] remaining = reader.processBytes(part2, part2.length, listener, previous);

        // then
        assertEquals(1, packets.size(), "Powinien zostać sparsowany 1 pakiet z dwóch części");
        assertNull(remaining, "Nie powinno zostać nic w buforze po pełnym pakiecie");
    }

    @Test
    void shouldReturnRemainingBytesIfPacketIsIncomplete() {
        // given
        byte[] incomplete = new byte[] {(byte) 0xFD, 0x09}; // niepełny nagłówek
        List<MAVLinkPacket> packets = new ArrayList<>();
        Consumer<MAVLinkPacket> listener = packets::add;

        UartMavlinkPacketReaderWritter reader = new UartMavlinkPacketReaderWritter();

        // when
        byte[] remaining = reader.processBytes(incomplete, incomplete.length, listener, null);

        // then
        assertEquals(0, packets.size(), "Nie powinno być żadnych pakietów");
        assertNotNull(remaining, "Powinien zostać zwrócony nieprzetworzony bufor");
        assertArrayEquals(incomplete, remaining);
    }

    @Test
    void shouldNotIncludePreviousBufferInRemainingBytes() {
        // given
        byte[] previous = new byte[] {(byte) 0xFD}; // pierwszy bajt
        byte[] current = new byte[] {0x09, 0x00};   // fragment kontynuacji
        List<MAVLinkPacket> packets = new ArrayList<>();
        Consumer<MAVLinkPacket> listener = packets::add;

        UartMavlinkPacketReaderWritter reader = new UartMavlinkPacketReaderWritter();

        // when
        byte[] remaining = reader.processBytes(current, current.length,listener, previous);

        // then
        assertNotNull(remaining, "Powinno zwrócić końcówkę bajtów");
        assertArrayEquals(current, remaining, "Pozostałość powinna zawierać tylko bajty z bieżącego bufora");
        assertTrue(packets.isEmpty(), "Żaden pakiet nie powinien zostać sparsowany");
    }

    private byte[] createValidMavlinkPacket() {
        msg_heartbeat heartbeat = new msg_heartbeat();
        heartbeat.type = MAV_TYPE.MAV_TYPE_QUADROTOR;
        heartbeat.autopilot = MAV_AUTOPILOT.MAV_AUTOPILOT_GENERIC;
        heartbeat.base_mode = 0;
        heartbeat.custom_mode = 0;
        heartbeat.system_status = MAV_STATE.MAV_STATE_STANDBY;
        heartbeat.mavlink_version = 2;

        MAVLinkPacket packet = heartbeat.pack();
        packet.compatFlags = 0;
        packet.incompatFlags = 0;
        packet.isMavlink2 = true;

        return packet.encodePacket();
    }

    private byte[] createValidMavlinkPacketPart1() {
        byte[] full = createValidMavlinkPacket();
        return Arrays.copyOfRange(full, 0, 5); // np. tylko nagłówek
    }

    private byte[] createValidMavlinkPacketPart2() {
        byte[] full = createValidMavlinkPacket();
        return Arrays.copyOfRange(full, 5, full.length); // reszta pakietu
    }

}

