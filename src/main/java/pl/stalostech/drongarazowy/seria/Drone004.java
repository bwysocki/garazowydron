package pl.stalostech.drongarazowy.seria;

import com.MAVLink.common.msg_set_position_target_local_ned;
import com.MAVLink.enums.MAV_FRAME;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import pl.stalostech.drongarazowy.symulacja.GazeboDrone;
import pl.stalostech.drongarazowy.symulacja.GazeboPlane;
import pl.stalostech.drongarazowy.symulacja.Jmavsim;
import pl.stalostech.drongarazowy.symulacja.kontrola.KeyboardInputLoop;


/**
 python -m pymavlink.tools.mavgen \
 --lang Java \
 --wire-protocol 2.0 \
 --output ./generated_java \
 ./common.xml
 */
@SpringBootApplication
@ComponentScan(
        basePackages = "pl.stalostech.drongarazowy.uklad.czytnik.notexist", //just to skip spring for now
        useDefaultFilters = false
)
@Slf4j
public class Drone004 {

    enum SIMULATION {jmavsim, gazebo_drone, gazebo_plane}

    private static final SIMULATION simulation = SIMULATION.gazebo_plane;

    private static ApplicationContext context;


    public static void main(String[] args) throws Exception {
        //make clean
        //make distclean
        //gz sim Tools/simulation/gz/worlds/baylands.sdf
        /*
            commander mode manual
            commander check
            commander status

       export GZ_GUI_CONFIG=/home/stalos/Projects/PX4-Autopilot/Tools/simulation/gz/gui/gui.config

       gz topic -t /gui/track -m gz.msgs.CameraTrack -p '
        track_mode: FOLLOW
        follow_target: { name: "advanced_plane_0" }
        follow_offset: { x: -1.7, y: 0.0, z: 1.3 }
        follow_pgain: 1.5
        track_pgain: 2.0
        '

        gz service -s /gui/track --reqtype gz.msgs.CameraTrack --reptype gz.msgs.Boolean --req 'track_mode: FOLLOW follow_target: { name: "advanced_plane_0" } follow_offset: { x: -1, y: 0, z: 0.5 } follow_pgain: 1.0 track_pgain: 2.0'


         */
        context = SpringApplication.run(Drone004.class, args);

        log.info("Czekam na drona...");
        if (simulation == SIMULATION.gazebo_plane) {
            //make px4_sitl gz_advanced_plane
            // listener manual_control_setpoint
            // param set COM_RC_LOSS_T 1000
            // param set FD_ESCS_EN 0
            GazeboPlane gazeboPlane = new GazeboPlane();

            gazeboPlane.sendHeartbeatLoop(); // -> mavlink status -> instance 1
            //gazeboPlane.receiveLoop();

            Thread.sleep(2000);

            KeyboardInputLoop loop = new KeyboardInputLoop(gazeboPlane::sendManualControlLoop);
            loop.start();

            Thread.sleep(2000);
            gazeboPlane.setFlightModeManual();
            Thread.sleep(1000);
            gazeboPlane.armDrone();
            Thread.sleep(1000);

            log.info("Arming...");

            Thread.sleep(30000 * 10);

            loop.stop();

        } else if (simulation == SIMULATION.gazebo_drone) {
            //make px4_sitl gz_x500
            GazeboDrone gazeboDrone = new GazeboDrone();

            gazeboDrone.sendHeartbeatLoop();
            gazeboDrone.receiveLoop();

            Thread.sleep(3000);

            // Krok 4: zacznij spamować SET_POSITION_TARGET_LOCAL_NED (velocity)
            new Thread(() -> {
                try {
                    long start = System.currentTimeMillis();
                    while (System.currentTimeMillis() - start < 10000) {
                        msg_set_position_target_local_ned msg = new msg_set_position_target_local_ned();
                        msg.time_boot_ms = (int)(System.nanoTime() / 1_000_000 % 4294967296L);
                        msg.coordinate_frame = MAV_FRAME.MAV_FRAME_LOCAL_NED;
                        msg.type_mask = 0b0000111111000111; // tylko vx, vy, vz
                        msg.target_system = 1;
                        msg.target_component = 1;
                        msg.vx = 0;
                        msg.vy = 0;
                        msg.vz = -1; // leć w górę
                        gazeboDrone.sendPacketFromAutopilot(msg);
                        Thread.sleep(100); // 10Hz
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, "offboard-vel-thread").start();


            Thread.sleep(2000); // poczekaj aż PX4 "poczuje" komendy
            gazeboDrone.setOffboardMode();
            Thread.sleep(500);

            gazeboDrone.armDrone();
            Thread.sleep(1000);

            gazeboDrone.takeoffToAltitude(10f);

        } else if (simulation == SIMULATION.jmavsim) {
            //git submodule update --init --recursive
            //./jmavsim_run.sh -u -p 4560 (~/Projects/PX4-Autopilot/Tools/simulation/jmavsim)

            Jmavsim jmavsim = new Jmavsim();

            jmavsim.sendHeartbeatContinuously();
            jmavsim.armDrone();
            jmavsim.throttleUp();
            jmavsim.move();
            jmavsim.landDrone();
            jmavsim.disarmDrone();

        } else {
            throw new IllegalStateException("Simulation should be one of jmavsim, gazebo");
        }

        log.info("Koniec lotu");
        System.exit(0);
    }


}
