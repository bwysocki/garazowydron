#include <SoftwareSerial.h>

#define RX_PIN 10
#define TX_PIN 9
SoftwareSerial hc12(RX_PIN, TX_PIN);

void setup() {
  Serial.begin(9600);
  hc12.begin(9600);
}

void loop() {
  // Odbiór z HC-12 i przekazanie do komputera
  while (hc12.available()) {
    Serial.write(hc12.read());
  }
    // Odbiór z HC-12 i przekazanie do komputera
  int byteIndex = 0;
/*
while (hc12.available()) {
  int byteVal = hc12.read();
  Serial.print("BYTE ");
  Serial.print(byteIndex++);
  Serial.print(" -> RECV: ");
  Serial.print(byteVal);         // wartość dziesiętna
  Serial.print(" (0x");
  if (byteVal < 16) Serial.print("0"); // dla ładnego formatu np. 0x0A
  Serial.print(byteVal, HEX);    // wartość heksadecymalna
  Serial.println(")");
}*/

  // Odbiór z komputera i przekazanie do HC-12
  while (Serial.available()) {
    //int byteVal = Serial.read();
  //Serial.print(byteVal);  // log na monitor szeregowy
  //Serial.print(" ");
  //hc12.write(byteVal);    // wysyłka przez HC-12
    
    hc12.write(Serial.read());
  }
}
