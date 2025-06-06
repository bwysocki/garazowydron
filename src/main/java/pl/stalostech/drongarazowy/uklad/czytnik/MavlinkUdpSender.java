package pl.stalostech.drongarazowy.uklad.czytnik;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

@Slf4j
@Getter
public class MavlinkUdpSender {

    private final DatagramSocket socket;
    private final InetSocketAddress targetAddress;

    public MavlinkUdpSender(DatagramSocket socket, InetSocketAddress targetAddress) throws SocketException {
        this.socket = socket;
        this.targetAddress = targetAddress;
    }

    public void send(com.MAVLink.MAVLinkPacket packet) throws IOException {
        byte[] raw = packet.encodePacket();
        DatagramPacket udpPacket = new DatagramPacket(raw, raw.length, targetAddress);
        socket.send(udpPacket);
    }

}
