#include <Wire.h>
#include <MPU6050.h>

MPU6050 mpu;

void setup() {
    Serial.begin(115200); 
    Wire.begin();

    if (!mpu.testConnection()) {
        Serial.println("Nie wykryto MPU6050!");
        while (1);
    }

    Serial.println("MPU6050 podłączony!");
    mpu.initialize();
    mpu.setDLPFMode(6); //ustawia filtr dolnoprzepustowy na 5 Hz, co skutkuje wygładzonymi danymi z MPU6050.
}

void loop() {
    int16_t ax, ay, az;
    int16_t gx, gy, gz;
    
    // Pobranie danych
    mpu.getAcceleration(&ax, &ay, &az);
    mpu.getRotation(&gx, &gy, &gz);
    
    // Wysyłanie wartości przez port szeregowy
    Serial.print(ax); Serial.print(",");
    Serial.print(ay); Serial.print(",");
    Serial.print(az); Serial.print(",");
    Serial.print(gx); Serial.print(",");
    Serial.print(gy); Serial.print(",");
    Serial.println(gz); 
    delay(200); 
}
