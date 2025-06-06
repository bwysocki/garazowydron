package pl.stalostech.drongarazowy.symulacja;

import com.MAVLink.common.msg_command_long;
import com.MAVLink.common.msg_manual_control;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_COMPONENT;
import com.MAVLink.enums.MAV_MODE;
import lombok.extern.slf4j.Slf4j;
import pl.stalostech.drongarazowy.protokol.rc.KeyboardControl;
import pl.stalostech.drongarazowy.symulacja.kontrola.ControlMapper;

@Slf4j
public class GazeboPlane extends Gazebo {

    public void setFlightModeManual() {
        msg_command_long msg = new msg_command_long();
        msg.target_system = DRONE_ID;
        msg.target_component = MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1;
        msg.command = MAV_CMD.MAV_CMD_DO_SET_MODE;
        msg.confirmation = 1;

        msg.param1 = MAV_MODE.MAV_MODE_MANUAL_DISARMED;
        msg.param2 = 0;
        msg.param3 = 0;
        msg.param4 = 0;
        msg.param5 = 0;
        msg.param6 = 0;
        msg.param7 = 0;

        sendPacketAsGcs(msg);
    }

    public void sendManualControlLoop(KeyboardControl control) {
        msg_manual_control msg = new msg_manual_control();
        msg.target = DRONE_ID;

        msg.x = ControlMapper.mapGd(control.getGd());
        msg.y = ControlMapper.mapLp(control.getLp());
        msg.r = ControlMapper.mapLp(control.getLp());
        msg.z = ControlMapper.mapMotor(control.getMotor());
        msg.buttons = 0;

        System.out.printf("%s-%s-%s-%s\r\t\n", msg.x, msg.y, msg.r, msg.z);

        sendPacketAsGcs(msg);

    }

}
