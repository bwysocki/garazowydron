package pl.stalostech.drongarazowy.symulacja;

import com.MAVLink.common.msg_hil_actuator_controls;
import com.MAVLink.minimal.msg_heartbeat;
import lombok.extern.slf4j.Slf4j;
import pl.stalostech.drongarazowy.uklad.czytnik.MavlinkUdpSender;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

@Slf4j
public class Jmavsim extends Simulation {

    private final MavlinkUdpSender sender;

    public Jmavsim() throws SocketException {
        sender = new MavlinkUdpSender(new DatagramSocket(), new InetSocketAddress("127.0.0.1", 4560));
    }

    @Override
    public MavlinkUdpSender getGCSSender() {
        return sender;
    }

    @Override
    public MavlinkUdpSender getAutopilotSender() {
        return sender;
    }


    public void sendHeartbeatContinuously() {
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

    public void armDrone() {
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

    public void throttleUp() {
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

    public void move() throws InterruptedException {
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

    public void landDrone() {
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


    public void disarmDrone() {
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


    public void sendControl(float roll, float pitch, float yaw, float throttle) {
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
