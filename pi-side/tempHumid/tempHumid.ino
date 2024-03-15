// #include <Adafruit_MLX90614.h>
// #include <Arduino.h>
// #include <U8g2lib.h>
// #include <SPI.h>
// #include <Wire.h>
#include "Arduino_SensorKit.h"

// Uncomment the line below if using DHT20
#define Environment Environment_I2C
// U8G2_ST7920_128X64_1_SW_SPI u8g2(U8G2_R0, 3, 4, 5);

// Adafruit_MLX90614 mlx = Adafruit_MLX90614();

void setup() {
  Serial.begin(9600); // Start serial communication at 9600 baud
  // u8g2.begin();

  // Initialize the Environment Sensor
  Environment.begin();

  // Initialize the MLX90614
  // mlx.begin();
}

void loop() {
  // Get the current time
  // unsigned long currentTime = millis();

  // // Reading from the environment sensor
  // Serial.print("Time: ");
  // Serial.print(currentTime / 1000); // Convert milliseconds to seconds
  Serial.print("Temperature = ");
  Serial.print(Environment.readTemperature()); // Print temperature from the environment sensor
  Serial.println(" C");

  Serial.print("Humidity = ");
  Serial.print(Environment.readHumidity()); // Print humidity
  Serial.println(" %");

  // u8g2.firstPage();
  // do {
  //   u8g2.setFont(u8g2_font_ncenB14_tr);
  //   u8g2.drawStr(0,20,"Hello World!");
  // } while ( u8g2.nextPage() );

  // Reading from the MLX90614 sensor
  // Serial.print("Ambient = ");
  // Serial.print(mlx.readAmbientTempC()); // Print ambient temperature
  // Serial.println(" C");

  // Serial.print("Object = ");
  // Serial.print(mlx.readObjectTempC()); // Print object temperature
  // Serial.println(" C");

  // Delay between readings
  delay(300);
}