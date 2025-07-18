package pl.stalostech.drongarazowy.seria;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_manual_control;
import com.MAVLink.minimal.msg_heartbeat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import pl.stalostech.drongarazowy.uklad.czytnik.UartMavlinkPacketReader;

@SpringBootApplication
@ComponentScan({"pl.stalostech.drongarazowy.uklad", "pl.stalostech.drongarazowy.protokol.telemetria.mavlink"})
@Slf4j
public class Drone006 {

    private static ApplicationContext context;

    public static void main(String[] args) throws Exception {
        context = SpringApplication.run(Drone003.class, args);
        UartMavlinkPacketReader reader = context.getBean(UartMavlinkPacketReader.class);

        reader.listen((MAVLinkPacket mavLinkPacket) -> {
            MAVLinkMessage message = mavLinkPacket.unpack(); // lub .payload
            switch (message) {
                case msg_manual_control manual -> {
                    System.out.printf("STEROWANIE: x=%d, y=%d, z=%d, r=%d%n", manual.x, manual.y, manual.z, manual.r);
                }
                case msg_heartbeat heartbeat -> {
                    System.out.println("HEARTBEAT: system=" + heartbeat.sysid);
                }
                default -> {
                    System.out.println("Nieobsługiwany typ: " + message.getClass().getSimpleName());
                }
            }

        }).start();
    }

}
