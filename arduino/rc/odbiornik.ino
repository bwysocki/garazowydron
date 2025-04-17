#include <Arduino.h>
#include <Servo.h>

#define FRAME_SIZE 4
#define START_BYTE 0xAA
#define STOP_BYTE  0x55

uint8_t buffer[FRAME_SIZE];
size_t bufferIndex = 0;

Servo esc;
Servo serwo1;
Servo serwo2;

int lastLp = -1;
int lastGd = -1;
int lastMotor = -1;

void setup() {
  Serial.begin(9600);

  esc.attach(9);
  esc.writeMicroseconds(2000);
  delay(3000);
  esc.writeMicroseconds(1000);

  delay(3000);

  serwo1.attach(5);
  serwo2.attach(6);

  serwo1.write(85);
  serwo2.write(123);
}

void loop() {
  while (Serial.available() > 0) {
    uint8_t byteRead = Serial.read();

    if (bufferIndex == 0 && byteRead != START_BYTE) {
      continue;
    }

    buffer[bufferIndex++] = byteRead;

    if (bufferIndex == FRAME_SIZE) {
      if (buffer[0] == START_BYTE && buffer[3] == STOP_BYTE) {
        decodeAndRun(buffer);
      }
      bufferIndex = 0;
    }
  }
}

void decodeAndRun(uint8_t* data) {
  uint8_t motorAndLp = data[1];
  uint8_t gd = data[2] & 0b111;

  uint8_t motor = (motorAndLp >> 3) & 0b111;
  uint8_t lp = motorAndLp & 0b111;

  if (motor != lastMotor) {
    int pwm = map(motor, 0, 7, 1000, 2000);
    esc.writeMicroseconds(pwm);
    lastMotor = motor;
  }

  if (lp != lastLp) {
    int lpAngle = map(lp, 0, 7, 0, 180);
    serwo1.write(lpAngle);
    lastLp = lp;
  }

  if (gd != lastGd) {
    int gdAngle = map(gd, 0, 7, 0, 180);
    serwo2.write(gdAngle);
    lastGd = gd;
  }
}