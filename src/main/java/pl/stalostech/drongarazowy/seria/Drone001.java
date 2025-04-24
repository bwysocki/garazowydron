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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import pl.stalostech.drongarazowy.uklad.czytnik.MPU6050RecordReader;
import pl.stalostech.drongarazowy.uklad.model.MPU6050Record;

@SpringBootApplication
@ComponentScan("pl.stalostech.drongarazowy.uklad")
public class Drone001 extends Application {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;

    private final Rotate rotateX = new Rotate(10, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    private static ApplicationContext context;

    public static void main(String[] args) {
        context = SpringApplication.run(Drone001.class, args);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        MPU6050RecordReader reader = context.getBean(MPU6050RecordReader.class);

        MPU6050Record.Yaw yaw = new MPU6050Record.Yaw();

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

        primaryStage.setTitle("MPU6050 Visualization");
        primaryStage.setScene(scene);
        primaryStage.show();

        reader.listen((MPU6050Record record) -> {
            yaw.setCurrentYaw(record.getYaw(yaw, System.nanoTime()));
            yaw.setLastTimeNano(System.nanoTime());

            rotateX.setAngle(-80 + -1 * record.getPitch());
            rotateY.setAngle(record.getRoll());
            rotateZ.setAngle(yaw.getCurrentYaw());
        }).start();
    }

    private static void addLight(int v, int v1, int x, Group root) {
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(v);
        light.setTranslateY(v1);
        light.setTranslateZ(-x);
        root.getChildren().add(light);
    }
}
