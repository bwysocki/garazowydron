package pl.stalostech.drongarazowy.symulacja;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Parser;
import com.MAVLink.common.msg_command_ack;
import com.MAVLink.common.msg_command_long;
import com.MAVLink.common.msg_manual_control;
import com.MAVLink.common.msg_statustext;
import com.MAVLink.enums.*;
import com.MAVLink.minimal.msg_heartbeat;
import lombok.extern.slf4j.Slf4j;
import pl.stalostech.drongarazowy.uklad.czytnik.MavlinkUdpSender;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

@Slf4j
public class Gazebo extends Simulation {

    public static final int DRONE_ID = 1;

    protected MavlinkUdpSender gcsSender;
    protected MavlinkUdpSender autopilotSender;
    protected final Parser parser = new Parser();

    public Gazebo() {
        try {
            gcsSender = new MavlinkUdpSender(new DatagramSocket(14550), new InetSocketAddress("127.0.0.1", 18570));
            autopilotSender = new MavlinkUdpSender(new DatagramSocket(14540), new InetSocketAddress("127.0.0.1", 14580));
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MavlinkUdpSender getGCSSender() {
        return gcsSender;
    }

    @Override
    public MavlinkUdpSender getAutopilotSender() {
        return autopilotSender;
    }

    /**
     * Starts a background thread that sends a MAVLink HEARTBEAT message every second.
     *
     * This heartbeat identifies the sender as a Ground Control Station (GCS),
     * and signals that it is active and in GUIDED and MANUAL mode. It is essential
     * to maintain a valid MAVLink connection with PX4, especially when operating
     * in OFFBOARD mode. If this heartbeat is not sent regularly, PX4 may assume
     * that the control link has been lost and exit OFFBOARD mode.
     */
    public void sendHeartbeatLoop() {
        Thread heartbeatThread = new Thread(() -> {
            try {
                while (true) {
                    msg_heartbeat hb = new msg_heartbeat();
                    hb.sysid = 255; // Ground Control Station system ID
                    hb.compid = MAV_COMPONENT.MAV_COMP_ID_MISSIONPLANNER; // GCS component
                    hb.type = MAV_TYPE.MAV_TYPE_GCS; // This is a GCS
                    hb.autopilot = MAV_AUTOPILOT.MAV_AUTOPILOT_GENERIC; // Generic autopilot
                    hb.base_mode = MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED |
                            MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED |
                            MAV_MODE_FLAG.MAV_MODE_FLAG_GUIDED_ENABLED;
                    hb.system_status = MAV_STATE.MAV_STATE_ACTIVE; // GCS is active
                    hb.mavlink_version = 3;
                    hb.isMavlink2 = true;

                    sendPacketAsGcs(hb);
                    Thread.sleep(100); // send heartbeat every 1 second
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "heartbeat-thread");

        heartbeatThread.start();
    }

    public void receiveLoop() {
        new Thread(() -> {
            try {
                byte[] buffer = new byte[512];
                DatagramSocket socket = getGCSSender().getSocket();

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    for (int i = 0; i < packet.getLength(); i++) {
                        MAVLinkPacket mavPacket = parser.mavlink_parse_char(buffer[i]);
                        if (mavPacket != null) {
                            MAVLinkMessage msg = mavPacket.unpack();
                            if (msg instanceof msg_heartbeat hb) {
                                System.out.println("➡️ HEARTBEAT from sysid: " + hb.sysid + ", compid: " + hb.compid);
                            }else if (msg instanceof msg_command_ack ack) {
                                log.info("⬅️  COMMAND_ACK: cmd=" + ack.command +
                                        ", result=" + ack.result + " (" + interpretAckResult(ack.result) + ")");
                            }else if (msg instanceof msg_statustext statusText) {
                                String message = new String(statusText.text).trim();
                                log.warn("📢 STATUSTEXT [" + statusText.severity + "]: " + message);
                            } else {
                                //log.info(msg.name());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Błąd w receiveLoop", e);
            }}, "receiveloop-thread").start();
    }


    /**
     * Wysyła cyklicznie wiadomości MAVLink typu MANUAL_CONTROL, symulując sygnał z aparatury RC.
     *
     * Funkcja działa w osobnym wątku i co 100 ms wysyła do PX4 zestaw komend: pitch, roll, yaw i throttle,
     * w zakresie [-1000, 1000] — tak jak w standardowym kontrolerze.
     *
     * PX4 traktuje te dane jak wejścia z pilota RC, o ile:
     * - znajduje się w trybie MANUAL, STABILIZED, ACRO lub (opcjonalnie) OFFBOARD,
     * - `COM_RC_IN_MODE = 1` (jeśli nie ma fizycznego radia),
     * - wiadomości są wysyłane regularnie (min co 500 ms – najlepiej co 100 ms).
     *
     * @param pitch     Wartość pitch [-1000, 1000] – ujemna = nos w dół
     * @param roll      Wartość roll [-1000, 1000] – ujemna = przechył w lewo
     * @param yaw       Wartość yaw [-1000, 1000] – ujemna = obrót w lewo
     * @param throttle  Wartość throttle [0, 1000] – 0 = min gaz, 1000 = max gaz
     */
    public void sendManualControlLoop(short pitch, short roll, short yaw, short throttle) {
        new Thread(() -> {
            try {
                long start = System.currentTimeMillis();
                while (true) { // przez 10 sekund
                    msg_manual_control msg = new msg_manual_control();
                    msg.target = DRONE_ID;          // ID drona/systemu
                    msg.x = pitch;           // kanał pitch
                    msg.y = roll;            // kanał roll
                    msg.r = yaw;             // kanał yaw (kierunek)
                    msg.z = throttle;        // kanał throttle
                    msg.buttons = 0;         // brak przycisków

                    sendPacketAsGcs(msg);         // wyślij przez MAVLink\

                    Thread.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "manual-control-thread").start();
    }


    private String interpretAckResult(int result) {
        return switch (result) {
            case MAV_RESULT.MAV_RESULT_ACCEPTED -> "ACCEPTED ✅";
            case MAV_RESULT.MAV_RESULT_TEMPORARILY_REJECTED -> "TEMP REJECTED";
            case MAV_RESULT.MAV_RESULT_DENIED -> "DENIED ❌";
            case MAV_RESULT.MAV_RESULT_UNSUPPORTED -> "UNSUPPORTED";
            case MAV_RESULT.MAV_RESULT_FAILED -> "FAILED";
            default -> "UNKNOWN";
        };
    }

    /**
     * Arms the drone by sending a MAVLink COMMAND_LONG message with
     * the MAV_CMD_COMPONENT_ARM_DISARM command and param1 set to 1 (arm).
     *
     * This command tells the flight controller to arm the motors,
     * provided all pre-arm checks pass (e.g. GPS lock, safety switch, etc.).
     *
     */
    public void armDrone() {
        msg_command_long cmd = new msg_command_long();
        cmd.target_system = 1; // ID of the target system (usually 1 for single-drone setups)
        cmd.target_component = 1; // ID of the target component (usually 1 = autopilot)
        cmd.command = MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM; // Command to arm/disarm the drone
        cmd.param1 = 1; // 1 = arm, 0 = disarm
        cmd.confirmation = 0; // 0 = no confirmation, 1+ = request retransmission
        sendPacketAsGcs(cmd); // Send the command via MAVLink
    }



}
