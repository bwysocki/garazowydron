package pl.stalostech.drongarazowy.seria;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import pl.stalostech.drongarazowy.protokol.telemetria.mavlink.MavLinkMessage;
import pl.stalostech.drongarazowy.protokol.telemetria.mavlink.MavLinkPayload30;
import pl.stalostech.drongarazowy.protokol.telemetria.mavlink.MavLinkService;
import pl.stalostech.drongarazowy.uklad.czytnik.UartMavlinkAttitudeReader;

@SpringBootApplication
@ComponentScan({"pl.stalostech.drongarazowy.uklad", "pl.stalostech.drongarazowy.protokol.telemetria.mavlink"})
@Slf4j
public class Drone002 extends Application {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;

    private final Rotate rotateX = new Rotate(10, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    private static ApplicationContext context;

    public static void main(String[] args) {
        context = SpringApplication.run(Drone002.class, args);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        UartMavlinkAttitudeReader reader = context.getBean(UartMavlinkAttitudeReader.class);
        MavLinkService mavLinkService = context.getBean(MavLinkService.class);

        Box arduino = new Box(40, 30, 10);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.BLUE);
        arduino.setMaterial(material);

        Box przod = new Box(40, 50, 10);
        PhongMaterial przoMdaterial = new PhongMaterial();
        przoMdaterial.setDiffuseColor(Color.BLACK);
        przod.setMaterial(przoMdaterial);
        przod.setTranslateY(-40);

        Box tyl = new Box(40, 90, 10);
        PhongMaterial tylMaterial = new PhongMaterial();
        tylMaterial.setDiffuseColor(Color.ORANGE);
        tyl.setMaterial(tylMaterial);
        tyl.setTranslateY(60);

        Box leweskrzydlo = new Box(80, 30, 10);
        PhongMaterial leweSkrzydloMaterial = new PhongMaterial();
        leweSkrzydloMaterial.setDiffuseColor(Color.RED);
        leweskrzydlo.setMaterial(leweSkrzydloMaterial);
        leweskrzydlo.setTranslateX(-60);

        Box praweskrzydlo = new Box(80, 30, 10);
        PhongMaterial praweskrzydloMaterial = new PhongMaterial();
        praweskrzydloMaterial.setDiffuseColor(Color.TOMATO);
        praweskrzydlo.setMaterial(praweskrzydloMaterial);
        praweskrzydlo.setTranslateX(60);

        Group root = new Group(arduino, przod, tyl, leweskrzydlo, praweskrzydlo);
        root.getTransforms().addAll(rotateX, rotateY, rotateZ);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-600);
        camera.setNearClip(0.1);
        camera.setFarClip(2000);


        Scene scene = new Scene(root, WIDTH, HEIGHT, true);
        scene.setFill(Color.GREEN);
        scene.setCamera(camera);

        addLight(100, -100, 500, root);
        addLight(-100, 100, 300, root);

        primaryStage.setTitle("Mavlink Visualization");
        primaryStage.setScene(scene);
        primaryStage.show();

        reader.listen((MavLinkMessage mavLinkMessage) -> {
            MavLinkPayload30 mavLinkPayload30 = mavLinkService.getPayload30(mavLinkMessage);
            safeSetAngle(rotateX, mavLinkPayload30.getPitch(), "pitch");
            safeSetAngle(rotateY, mavLinkPayload30.getRoll(), "roll");
            safeSetAngle(rotateZ, mavLinkPayload30.getYaw(), "yaw");
        }).start();
    }

    private void safeSetAngle(Rotate rotate, double angleRad, String axisName) {
        double angleDeg = Math.toDegrees(angleRad);
        if (Double.isFinite(angleDeg) && Math.abs(angleDeg) >= 0.01 && Math.abs(angleDeg) <= 360) {
            rotate.setAngle(angleDeg);
        }
    }

    private static void addLight(int v, int v1, int x, Group root) {
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(v);
        light.setTranslateY(v1);
        light.setTranslateZ(-x);
        root.getChildren().add(light);
    }
}
