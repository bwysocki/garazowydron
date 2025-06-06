package pl.stalostech.drongarazowy.uklad.czytnik;

import com.MAVLink.MAVLinkPacket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MavlinkUdpSenderTest {

    private DatagramSocket socket;
    private InetSocketAddress targetAddress;
    private MavlinkUdpSender sender;

    @BeforeEach
    void setup() throws Exception {
        socket = mock(DatagramSocket.class);
        targetAddress = new InetSocketAddress("127.0.0.1", 4560);
        sender = new MavlinkUdpSender(socket, targetAddress);
    }

    @Test
    void shouldSendEncodedPacketViaSocket() throws IOException {
        // given
        MAVLinkPacket packet = mock(MAVLinkPacket.class);
        byte[] encoded = new byte[]{0x01, 0x02, 0x03};
        when(packet.encodePacket()).thenReturn(encoded);

        // when
        sender.send(packet);

        // then
        ArgumentCaptor<DatagramPacket> captor = ArgumentCaptor.forClass(DatagramPacket.class);
        verify(socket).send(captor.capture());

        DatagramPacket sentPacket = captor.getValue();
        assertNotNull(sentPacket);
        assertEquals(encoded.length, sentPacket.getLength());
        assertEquals(targetAddress.getAddress(), sentPacket.getAddress());
        assertEquals(targetAddress.getPort(), sentPacket.getPort());
        assertArrayEquals(encoded, sentPacket.getData());
    }

}
