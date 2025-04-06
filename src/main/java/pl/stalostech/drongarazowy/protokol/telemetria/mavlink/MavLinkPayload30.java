package pl.stalostech.drongarazowy.protokol.telemetria.mavlink;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MavLinkPayload30 {
    private int messageId;
    private String messageType;
    private long timeBootMs;
    private float roll;
    private float pitch;
    private float yaw;
    private float rollSpeed;
    private float pitchSpeed;
    private float yawSpeed;
    private List<String> rawHexPayload;

    @Override
    public String toString() {
        return "Payload30{" +
                "yawSpeed=" + yawSpeed +
                ", pitchSpeed=" + pitchSpeed +
                ", rollSpeed=" + rollSpeed +
                ", yaw=" + yaw +
                ", pitch=" + pitch +
                ", roll=" + roll +
                ", timeBootMs=" + timeBootMs +
                ", messageId=" + messageId +
                ", messageType='" + messageType + '\'' +
                '}';
    }
}
