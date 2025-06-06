package pl.stalostech.drongarazowy.symulacja;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.enums.MAV_COMPONENT;
import lombok.extern.slf4j.Slf4j;
import pl.stalostech.drongarazowy.uklad.czytnik.MavlinkUdpSender;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public abstract class Simulation {

    public abstract MavlinkUdpSender getGCSSender();
    public abstract MavlinkUdpSender getAutopilotSender();

    private AtomicLong sequence = new AtomicLong(1);

    public void sendPacketWithDelay(MAVLinkMessage msg, long delayMs, String label) {
        try {
            getGCSSender().send(msg.pack());
            if (label != null) log.info(label);
            if (delayMs > 0) Thread.sleep(delayMs);
        } catch (Exception e) {
            log.error("Błąd podczas wysyłania pakietu MAVLink", e);
        }
    }

    public void sendPacketFromAutopilot(MAVLinkMessage msg) {
        try {
            MAVLinkPacket packet = msg.pack();
            packet.sysid = 1; // PX4 "autopilot"
            packet.compid = MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1;
            packet.seq = (int) sequence.getAndIncrement();

            getAutopilotSender().send(packet);
        } catch (Exception e) {
            log.error("Błąd podczas wysyłania pakietu MAVLink", e);
        }
    }

    public void sendPacketAsGcs(MAVLinkMessage msg) {
        try {
            MAVLinkPacket packet = msg.pack();
            packet.sysid = 255; // GCS
            packet.compid = MAV_COMPONENT.MAV_COMP_ID_MISSIONPLANNER;
            packet.seq = (int) sequence.getAndIncrement();

            getGCSSender().send(packet);
        } catch (Exception e) {
            log.error("❌ Błąd przy wysyłaniu jako GCS", e);
        }
    }


}
