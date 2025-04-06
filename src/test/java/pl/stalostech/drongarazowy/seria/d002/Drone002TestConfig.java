package pl.stalostech.drongarazowy.seria.d002;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import pl.stalostech.drongarazowy.protokol.telemetria.mavlink.MavLinkService;
import pl.stalostech.drongarazowy.uklad.czytnik.UartMavlinkAttitudeReader;

@TestConfiguration
public class Drone002TestConfig {

    @Bean
    public MavLinkService mavLinkService() {
        return new MavLinkService();
    }

    @Bean
    public UartMavlinkAttitudeReader uartMavlinkAttitudeReader(MavLinkService mavLinkService) {
        return new UartMavlinkAttitudeReader(mavLinkService);
    }

}
