#include <SoftwareSerial.h>
#include <MAVLink.h>
#include <MPU6050.h>
#include <Crypto.h>
#include <SHA256.h>

#define RX_PIN 10
#define TX_PIN 11

SoftwareSerial hc12(RX_PIN, TX_PIN);

MPU6050 mpu;

// Tajny klucz do podpisywania wiadomości (32 bajty)
const uint8_t secretKey[32] = {
  0x01, 0x23, 0x45, 0x67, 0x89, 0xAB, 0xCD, 0xEF,
  0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0xDE, 0xF0,
  0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88,
  0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0xFF, 0x00
};

#define MAVLINK_SIGNATURE_LEN 13

float yaw = 0.0f; // obliczany przez całkowanie gz
unsigned long lastUpdateMicros = 0;

void setup() {
  Serial.begin(115200);
  hc12.begin(9600);
  Wire.begin();

  mpu.initialize();
  mpu.setDLPFMode(6); // filtr dolnoprzepustowy 5 Hz

  if (!mpu.testConnection()) {
    Serial.println("Nie wykryto MPU6050!");
    while (1);
  }

  Serial.println("MAVLink ATTITUDE Sender");
  lastUpdateMicros = micros();
}

void loop() {
  int16_t ax_raw, ay_raw, az_raw;
  int16_t gx_raw, gy_raw, gz_raw;
  mpu.getAcceleration(&ax_raw, &ay_raw, &az_raw);
  mpu.getRotation(&gx_raw, &gy_raw, &gz_raw);

  // Rzutowanie na float, żeby uniknąć błędów przepełnienia
  float ax = (float)ax_raw;
  float ay = (float)ay_raw;
  float az = (float)az_raw;

  float gx = (float)gx_raw;
  float gy = (float)gy_raw;
  float gz = (float)gz_raw;

  // Przelicz żyroskop na rad/s - nie potrzebujemy tego w projekcie - zostawiam dla ciekawskich
  const float gyroScale = 131.0f; // LSB / (°/s)
  float rollspeed  = 0.1; // gx / gyroScale * DEG_TO_RAD;
  float pitchspeed = 0.1; // gy / gyroScale * DEG_TO_RAD;
  float yawspeed   = gz / gyroScale * DEG_TO_RAD;

  // Oblicz roll i pitch z akcelerometru
  float denomRoll  = sqrt(ax * ax + az * az);
  if (denomRoll == 0.0f) denomRoll = 0.0001f;
  float roll = atan2(ay, denomRoll);

  float denomPitch = sqrt(ay * ay + az * az);
  if (denomPitch == 0.0f) denomPitch = 0.0001f;
  float pitch = atan2(-ax, denomPitch);

  // Całkowanie Yaw (z żyroskopu)
  unsigned long now = micros();
  float dt = (now - lastUpdateMicros) / 1e6; // sekundy
  lastUpdateMicros = now;

  if (dt > 0.00001f && dt < 1.0f) { // zabezpieczenie przed skokami czasu
    yaw += yawspeed * dt;
  }

  //debug
  //Serial.print("Roll: "); Serial.println(roll, 6);
  //Serial.print("Pitch: "); Serial.println(pitch, 6);
  //Serial.print("Yaw: "); Serial.println(yaw, 6);

  // Prześlij przez MAVLink
  sendAttitude(roll, pitch, yaw, rollspeed, pitchspeed, yawspeed);
  delay(200);
}

void sendAttitude(float roll, float pitch, float yaw, float rollspeed, float pitchspeed, float yawspeed) {
  mavlink_message_t msg;
  uint8_t buffer[MAVLINK_MAX_PACKET_LEN];

  uint32_t time_boot_ms = millis();

  // Spakowanie wiadomości ATTITUDE
  mavlink_msg_attitude_pack(
    1, MAV_COMP_ID_AUTOPILOT1, &msg,
    time_boot_ms, roll, pitch, yaw,
    rollspeed, pitchspeed, yawspeed
  );

  // Ustawienie flagi wymuszającej podpis
  msg.incompat_flags |= MAVLINK_IFLAG_SIGNED;

  // Finalizacja wiadomości i obliczenie długości
  uint16_t len = mavlink_finalize_message_chan(&msg, 1, MAV_COMP_ID_AUTOPILOT1, 0, msg.len, msg.len, MAVLINK_MSG_ID_ATTITUDE);

  // Generowanie podpisu
  uint8_t signature[MAVLINK_SIGNATURE_LEN];
  generateSignature((uint8_t*)&msg, len, signature);

  // Kopiowanie wiadomości i podpisu do bufora
  uint8_t* ptr = buffer;

  // 1. STX
  *ptr++ = MAVLINK_STX;

  // 2. Header (9 bajtów)
  *ptr++ = msg.len;
  *ptr++ = msg.incompat_flags;
  *ptr++ = msg.compat_flags;
  *ptr++ = msg.seq;
  *ptr++ = msg.sysid;
  *ptr++ = msg.compid;

  // 3. Message ID (3 bajty, LSB -> MSB)
  *ptr++ = msg.msgid & 0xFF;
  *ptr++ = (msg.msgid >> 8) & 0xFF;
  *ptr++ = (msg.msgid >> 16) & 0xFF;

  // 4. Payload
  memcpy(ptr, _MAV_PAYLOAD(&msg), msg.len);
  ptr += msg.len;

  // 5. Checksum (2 bajty, LSB -> MSB)
  *ptr++ = (uint8_t)(msg.checksum & 0xFF);
  *ptr++ = (uint8_t)((msg.checksum >> 8) & 0xFF);

  // 6. Signature
  //memcpy(ptr, signature, MAVLINK_SIGNATURE_LEN);
  //ptr += MAVLINK_SIGNATURE_LEN;

  // 7. Finalna długość wiadomości
  len = ptr - buffer;

  // Debug:
      Serial.print("ATTITUDE (len ");
  Serial.print(len);
  Serial.print("): ");
  for (int i = 0; i < len; i++) {
    if (buffer[i] < 0x10) Serial.print("0");
    Serial.print(buffer[i], HEX);
    Serial.print(" ");
  }
  Serial.println();

  // Wysyłka przez HC-12
  hc12.write(buffer, len);
}

void generateSignature(uint8_t* message, uint16_t length, uint8_t* signature) {
  SHA256 hasher;
  hasher.reset();
  hasher.update(message, length);             // Dane wiadomości
  hasher.update(secretKey, sizeof(secretKey)); // Klucz

  uint8_t hash[32];
  hasher.finalize(hash, sizeof(hash));
  memcpy(signature, hash, MAVLINK_SIGNATURE_LEN);
}