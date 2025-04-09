#include <SoftwareSerial.h>

#define RX_PIN 10
#define TX_PIN 11
SoftwareSerial hc12(RX_PIN, TX_PIN);

void setup() {
  Serial.begin(115200);
  hc12.begin(9600);
  Serial.println("Start odbiornika MAVLink przez HC-12");
}

void loop() {
  while (hc12.available()) {
    Serial.write(hc12.read());  // Przekazuje surowe bajty 1:1
  }
}
