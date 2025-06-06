package pl.stalostech.drongarazowy.seria;


import com.MAVLink.common.msg_attitude_quaternion;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import pl.stalostech.drongarazowy.uklad.czytnik.MPU6050RecordReader;
import pl.stalostech.drongarazowy.uklad.model.MPU6050Record;
import pl.stalostech.drongarazowy.websocket.DroneWebsocketHandler;


@SpringBootApplication
@ComponentScan(basePackages = {"pl.stalostech.drongarazowy.websocket", "pl.stalostech.drongarazowy.uklad", "pl.stalostech.drongarazowy.protokol.telemetria.mavlink"})
public class Drone005 {

    private static ApplicationContext context;

    public static void main(String[] args) throws InterruptedException {
        context = SpringApplication.run(Drone005.class, args);

        DroneWebsocketHandler droneWebsocketHandler = context.getBean(DroneWebsocketHandler.class);
        MPU6050RecordReader reader = context.getBean(MPU6050RecordReader.class);

        MPU6050Record.Yaw yaw = new MPU6050Record.Yaw();
        reader.listen((MPU6050Record record) -> {
            float dt = (float) ((System.nanoTime() - yaw.getLastTimeNano()) / 1_000_000_000.0); // czas w sekundach
            yaw.setLastTimeNano(System.nanoTime());
            droneWebsocketHandler.broadcast(createMsgFromGyro(record, dt));
        }).start();
    }

    public static msg_attitude_quaternion createMsgFromGyro(MPU6050Record record, float dt) {
        int gx = record.gx();
        int gy = record.gy();
        int gz = record.gz();

        // Przekształcenie do rad/s (zakres ±250°/s → 131 LSB/°/s)
        float wx = gx / 131.0f * (float) Math.PI / 180f;
        float wy = gy / 131.0f * (float) Math.PI / 180f;
        float wz = gz / 131.0f * (float) Math.PI / 180f;

        // Oblicz długość wektora omega (moduł)
        float omega = (float) Math.sqrt(wx * wx + wy * wy + wz * wz);
        if (omega < 1e-6f) {
            return new msg_attitude_quaternion(
                    System.currentTimeMillis(),
                    1, 0, 0, 0, // brak obrotu
                    0.0f, 0.0f, 0.0f,
                    new float[]{0, 0, 0, 0}
            );
        }

        // Kąt obrotu
        float theta = omega * dt;

        // Jednostkowy wektor osi obrotu
        float ux = wx / omega;
        float uy = wy / omega;
        float uz = wz / omega;

        // Oblicz kwaternion
        float halfTheta = theta / 2f;
        float sinHalf = (float) Math.sin(halfTheta);
        float cosHalf = (float) Math.cos(halfTheta);

        float w = cosHalf;
        float x = ux * sinHalf;
        float y = uy * sinHalf;
        float z = uz * sinHalf;

        return new msg_attitude_quaternion(
                System.currentTimeMillis(),
                w, x, y, z,
                wx, wy, wz,
                new float[]{0, 0, 0, 0}
        );
    }


}
