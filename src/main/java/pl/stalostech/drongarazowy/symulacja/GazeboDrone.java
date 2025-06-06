package pl.stalostech.drongarazowy.symulacja;

import com.MAVLink.common.msg_command_long;
import com.MAVLink.common.msg_manual_control;
import com.MAVLink.common.msg_set_position_target_local_ned;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_COMPONENT;
import com.MAVLink.enums.MAV_FRAME;
import com.MAVLink.enums.MAV_MODE_FLAG;
import lombok.extern.slf4j.Slf4j;
import pl.stalostech.drongarazowy.protokol.rc.KeyboardControl;
import pl.stalostech.drongarazowy.symulacja.kontrola.ControlMapper;

@Slf4j
public class GazeboDrone extends Gazebo {


    public void setOffboardMode() {
        msg_command_long cmd = new msg_command_long();
        cmd.target_system = 1;
        cmd.target_component = MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1;
        cmd.command = MAV_CMD.MAV_CMD_DO_SET_MODE;
        cmd.param1 = MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED |
                MAV_MODE_FLAG.MAV_MODE_FLAG_GUIDED_ENABLED;
        cmd.param2 = 6; // OFFBOARD mode
        sendPacketAsGcs(cmd);
        log.info(">>> Wysłano MAV_CMD_DO_SET_MODE (OFFBOARD)");
    }


    public void takeoffToAltitude(float targetZ) {
        for (int i = 0; i < 50; i++) {
            msg_set_position_target_local_ned msg = new msg_set_position_target_local_ned();
            msg.coordinate_frame = MAV_FRAME.MAV_FRAME_LOCAL_NED;
            msg.type_mask = 0b0000111111111000; // tylko pozycja X/Y/Z
            msg.time_boot_ms = (int) (System.nanoTime() / 1_000_000 % 4294967296L);
            msg.target_system = 1;
            msg.target_component = 1;
            msg.x = 0;
            msg.y = 0;
            msg.z = -targetZ; // ujemne Z = w górę w NED
            msg.vx = 0;
            msg.vy = 0;
            msg.vz = 0;
            sendPacketWithDelay(msg, 200, null);
        }
    }


}
