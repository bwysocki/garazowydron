#include <Arduino.h>
#include <SoftwareSerial.h>

#define HC12_RX 10
#define HC12_TX 11
#define FRAME_SIZE 4
#define START_BYTE 0xAA
#define STOP_BYTE  0x55

SoftwareSerial hc12(HC12_RX, HC12_TX);

uint8_t buffer[FRAME_SIZE];

void setup() {
  Serial.begin(9600);
  hc12.begin(9600);
  Serial.println("TX READY - czekam na ramki 0xAA XX XX 0x55");
}

void loop() {
  if (Serial.available() >= FRAME_SIZE) {
    // Szukaj startu ramki
    if (Serial.read() != START_BYTE) {
      return; // Śmieć - ignoruj
    }

    buffer[0] = START_BYTE;

    // Zbierz resztę ramki
    for (size_t i = 1; i < FRAME_SIZE; i++) {
      buffer[i] = Serial.read();
    }

    if (buffer[FRAME_SIZE - 1] != STOP_BYTE) {
      Serial.println("Błąd: zły STOP_BYTE");
      return; // Nie wysyłaj - ramka uszkodzona
    }

    Serial.print("FRAME TX: ");
    for (size_t i = 0; i < FRAME_SIZE; i++) {
      Serial.print(buffer[i], HEX);
      Serial.print(" ");
    }
    Serial.println();

    hc12.write(buffer, FRAME_SIZE);
  }
}
