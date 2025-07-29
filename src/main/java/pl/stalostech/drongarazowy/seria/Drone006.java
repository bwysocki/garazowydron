package pl.stalostech.drongarazowy.seria;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_command_ack;
import com.MAVLink.common.msg_command_long;
import com.MAVLink.common.msg_manual_control;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_RESULT;
import com.MAVLink.minimal.msg_heartbeat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import pl.stalostech.drongarazowy.symulacja.GazeboPlane;
import pl.stalostech.drongarazowy.symulacja.kontrola.ControlMapper;
import pl.stalostech.drongarazowy.uklad.czytnik.UartMavlinkPacketReaderWritter;

@SpringBootApplication
@ComponentScan({"pl.stalostech.drongarazowy.uklad", "pl.stalostech.drongarazowy.protokol.telemetria.mavlink"})
@Slf4j
public class Drone006 {

    private static ApplicationContext context;

    public static void main(String[] args) throws Exception {
        context = SpringApplication.run(Drone003.class, args);
        UartMavlinkPacketReaderWritter serial = context.getBean(UartMavlinkPacketReaderWritter.class);

        // make px4_sitl gz_advanced_plane
        // listener manual_control_setpoint
        // param set COM_RC_LOSS_T 1000
        // param set FD_ESCS_EN 0
        /*
        gz topic -t /gui/track -m gz.msgs.CameraTrack -p '
        track_mode: FOLLOW
        follow_target: { name: "advanced_plane_0" }
        follow_offset: { x: -1.7, y: 0.0, z: 1.3 }
        follow_pgain: 1.5
        track_pgain: 2.0
        '
         */
        final GazeboPlane gazeboPlane = new GazeboPlane();

        serial.listen((MAVLinkPacket mavLinkPacket) -> {
            MAVLinkMessage message = mavLinkPacket.unpack(); // lub .payload
            switch (message) {
                case msg_manual_control manual -> {
                    System.out.printf("STEROWANIE: x=%d, y=%d, z=%d, r=%d%n", manual.x, manual.y, manual.z, manual.r);
                    msg_manual_control msg = new msg_manual_control();
                    msg.target = 1;

                    msg.x = clampTo200By5(-1 * manual.y);
                    msg.y = clampTo200By5( -1 * manual.x);
                    msg.r = clampTo200(manual.r);
                    msg.z = manual.z;
                    msg.buttons = 0;


                    gazeboPlane.sendPacketAsGcs(msg);
                }
                case msg_heartbeat heartbeat -> {
                    System.out.println("HEARTBEAT: system=" + heartbeat.sysid);
                    gazeboPlane.sendHeartbeatLoop();
                }
                case msg_command_long cmd -> {
                    System.out.printf(
                            "COMMAND_LONG: system=%d, component=%d, command=%d, param1=%.2f, param2=%.2f%n",
                            cmd.sysid, cmd.compid, cmd.command, cmd.param1, cmd.param2
                    );

                    msg_command_ack ack = new msg_command_ack();
                    ack.command = MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM;
                    ack.result = MAV_RESULT.MAV_RESULT_ACCEPTED;
                    ack.target_system = 5;
                    ack.target_component = 1;

                    serial.send(ack);

                    gazeboPlane.setFlightModeManual();
                    //Thread.sleep(1000);
                    gazeboPlane.armDrone();

                }
                default -> {
                    System.out.println("Nieobsługiwany typ: " + message.getClass().getSimpleName());
                }
            }

        }).start();
    }

    private static short clampTo200(int value) {
        return (short) Math.max(-200, Math.min(200, value));
    }

    private static short clampTo200By5(int value) {
        return (short) Math.max(-200, Math.min(200, value/20));
    }

}
