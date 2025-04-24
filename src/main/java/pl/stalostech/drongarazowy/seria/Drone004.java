package pl.stalostech.drongarazowy.seria;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;
import com.MAVLink.common.msg_hil_actuator_controls;
import com.MAVLink.enums.MAV_AUTOPILOT;
import com.MAVLink.enums.MAV_COMPONENT;
import com.MAVLink.enums.MAV_STATE;
import com.MAVLink.enums.MAV_TYPE;
import com.MAVLink.minimal.msg_heartbeat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import pl.stalostech.drongarazowy.uklad.czytnik.MavlinkUdpSender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;


/**
 * pip install pymavlink
 * git clone https://github.com/mavlink/mavlink.git
 * python3 mavgenerate.py
 * ./jmavsim_run.sh -u -p 4560 (~/Projects/PX4-Autopilot/Tools/simulation/jmavsim)
 */
@SpringBootApplication
@ComponentScan(
        basePackages = "pl.stalostech.drongarazowy.uklad.czytnik",
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MavlinkUdpSender.class),
        useDefaultFilters = false
)
@Slf4j
public class Drone004 {

    private static ApplicationContext context;
    private static MavlinkUdpSender sender;

    public static void main(String[] args) throws Exception {
        context = SpringApplication.run(Drone004.class, args);
        sender = context.getBean(MavlinkUdpSender.class);

        //java
        log.info("Czekam na drona...");

        // Start heartbeat sender
        new Thread(Drone004::sendHeartbeatLoop, "heartbeat-thread").start();

        // Start packet receiver
        new Thread(Drone004::receiveLoop, "receiver-thread").start();


        /*sendHeartbeatContinuously();
        armDrone();
        throttleUp();
        move();
        landDrone();
        disarmDrone();*/
        log.info("Koniec lotu");
    }

    private static void sendHeartbeatLoop() {
        try {
            while (true) {
                msg_heartbeat hb = new msg_heartbeat();
                hb.sysid = 255;
                hb.compid = MAV_COMPONENT.MAV_COMP_ID_MISSIONPLANNER;
                hb.type = MAV_TYPE.MAV_TYPE_GCS;
                hb.autopilot = MAV_AUTOPILOT.MAV_AUTOPILOT_INVALID;
                hb.base_mode = 0;
                hb.system_status = MAV_STATE.MAV_STATE_ACTIVE;
                hb.mavlink_version = 3;
                hb.isMavlink2 = true;

                MAVLinkPacket packet = hb.pack();
                sender.send(packet);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void receiveLoop() {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        Parser parser = new Parser();

        while (true) {
            try {
                sender.getSocket().receive(packet);
                for (byte b : Arrays.copyOf(packet.getData(), packet.getLength())) {
                    MAVLinkPacket mavPacket = parser.mavlink_parse_char(b);
                    if (mavPacket != null) {
                        System.out.println("<< Received: " + mavPacket.unpack().getClass().getName());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static void sendHeartbeatContinuously() {
        new Thread(() -> {
            try {
                msg_heartbeat hb = new msg_heartbeat();
                hb.sysid = -1;
                hb.compid = 51;
                hb.type = 6; // GCS
                hb.autopilot = 8; // INVALID
                hb.base_mode = 0;
                hb.system_status = 0;

                while (true) {
                    sender.send(hb.pack());
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                log.error("Błąd podczas wysyłania heartbeat: ", e);
            }
        }, "heartbeat-thread").start();
    }

    private static void armDrone() {
        msg_hil_actuator_controls arm = new msg_hil_actuator_controls();
        arm.sysid = 255;
        arm.compid = 0;
        arm.time_usec = System.nanoTime() / 1000;
        arm.controls = new float[8];
        arm.mode = 128;  // ARM
        arm.flags = 0;
        arm.isMavlink2 = true;

        sendPacketWithDelay(arm, 2000, "ARM");
    }

    private static void throttleUp() {
        log.info("THROTTLE UP");
        for (int i = 0; i < 20; i++) {
            msg_hil_actuator_controls ctrl = new msg_hil_actuator_controls();
            ctrl.sysid = 255;
            ctrl.compid = 0;
            ctrl.time_usec = System.nanoTime() / 1000;
            ctrl.controls = new float[]{0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f};
            ctrl.mode = 128;
            ctrl.flags = 0;
            ctrl.isMavlink2 = true;

            sendPacketWithDelay(ctrl, 100, null);
        }
    }

    private static void move() throws InterruptedException {
        log.info("przelec (niewielki przechył i wyrównanie)");

        // krok 1 — bardzo delikatny przechył w prawo
        for (int i = 0; i < 5; i++) {
            sendControl(0.501f, 0.5f, 0.5f, 0.5f);
            Thread.sleep(100);
        }

        // krok 2 — powrót do równowagi
        for (int i = 0; i < 20; i++) {
            sendControl(0.5f, 0.5f, 0.5f, 0.5f);
            Thread.sleep(100);
        }

        // krok 3 — bardzo delikatny przechył w lewo
        for (int i = 0; i < 5; i++) {
            sendControl(0.499f, 0.5f, 0.5f, 0.5f);
            Thread.sleep(100);
        }

        // krok 4 — powrót do równowagi
        for (int i = 0; i < 20; i++) {
            sendControl(0.5f, 0.5f, 0.5f, 0.5f);
            Thread.sleep(100);
        }
    }

    private static void landDrone() {
        log.info("LĄDOWANIE (landing sequence)");

        for (int i = 0; i < 20; i++) {
            msg_hil_actuator_controls ctrl = new msg_hil_actuator_controls();
            ctrl.sysid = 255;
            ctrl.compid = 0;
            ctrl.time_usec = System.nanoTime() / 1000;

            float spped = 0.5f - i * (0.5f / 20); // schodzenie w 20 krokach

            ctrl.controls = new float[]{
                    spped,  // roll neutral
                    spped,  // pitch neutral
                    spped,  // yaw neutral
                    spped,  // tylko throttle się zmienia
                    0.0f, 0.0f, 0.0f, 0.0f
            };

            ctrl.mode = 128;
            ctrl.flags = 0;
            ctrl.isMavlink2 = true;

            sendPacketWithDelay(ctrl, 300, null);
        }
    }



    private static void disarmDrone() {
        msg_hil_actuator_controls disarm = new msg_hil_actuator_controls();
        disarm.sysid = 255;
        disarm.compid = 0;
        disarm.time_usec = System.nanoTime() / 1000;
        disarm.controls = new float[8];
        disarm.mode = 0;  // DISARM
        disarm.flags = 0;
        disarm.isMavlink2 = true;

        sendPacketWithDelay(disarm, 0, "DISARM");
    }

    private static void sendPacketWithDelay(msg_hil_actuator_controls msg, long delayMs, String label) {
        try {
            sender.send(msg.pack());
            if (label != null) log.info(label);
            if (delayMs > 0) Thread.sleep(delayMs);
        } catch (Exception e) {
            log.error("Błąd podczas wysyłania pakietu MAVLink", e);
        }
    }

    private static void sendControl(float roll, float pitch, float yaw, float throttle) {
        msg_hil_actuator_controls ctrl = new msg_hil_actuator_controls();
        ctrl.sysid = 255;
        ctrl.compid = 0;
        ctrl.time_usec = System.nanoTime() / 1000;
        ctrl.controls = new float[]{
                roll,
                pitch,
                yaw,
                throttle,
                0.0f, 0.0f, 0.0f, 0.0f
        };
        ctrl.mode = 128;
        ctrl.flags = 0;
        ctrl.isMavlink2 = true;

        sendPacketWithDelay(ctrl, 0, null);
    }
}
