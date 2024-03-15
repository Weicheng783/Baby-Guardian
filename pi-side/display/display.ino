// #include <Adafruit_MLX90614.h>
// #include <Arduino.h>
// #include <U8g2lib.h>
// #include <ArduinoSTL.h> // Include this for Arduino-compatible vector
// #include <SPI.h>
// #include <Wire.h>
#include "Arduino_SensorKit.h"
// using namespace std;

// Uncomment the line below if using DHT20
#define Environment Environment_I2C
U8G2_ST7920_128X64_1_SW_SPI u8g2(U8G2_R0, 2, 3, 4);
int a = 0;
float temp = 0.0;
float humidity = 0.0;
int status_a = 0;

// Set up a new SoftwareSerial object
// U8X8_ST7920_128X64_SW_SPI u8x8(2,3,4);

// Adafruit_MLX90614 mlx = Adafruit_MLX90614();
// std::vector<std::string> aaa = {"1"};
// const unsigned char bitmap[] PROGMEM = {your_image_data_here};

static const unsigned char Size1_2bmp[] U8X8_PROGMEM  = {
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x80, 0x80, 0x80, 0xc0, 0xc0, 0xc0, 0xc0, 0xe0, 0xe0, 0xe0, 0xe0, 0xf0, 0xe0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xc0, 0xe0, 0xf0, 0xf0, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x80, 0xc0, 0xe0, 0xf0, 0xf0, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0e, 0x0f, 0x0f, 0x0f, 0x0f, 0x07, 0x07, 0x87, 0xe3, 0xfb, 0xff, 0xff, 0x3f, 0x0f, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xc0, 0xf0, 0xfc, 0xff, 0xff, 0x7f, 0x0f, 0x03, 0x00, 0x00, 0x00, 0x00, 0xe0, 0xf8, 0xfe, 0xff, 0xff, 0x3f, 0x07, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xc0, 0xf0, 0xfe, 0xff, 0xff, 0x3f, 0x07, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0e, 0x1f, 0x1f, 0x1f, 0x0f, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1f, 0x1f, 0x1f, 0x1f, 0x0f, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xc0, 0xf8, 0xfe, 0xff, 0xff, 0x3f, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xe0, 0xfc, 0xff, 0xff, 0xff, 0x3f, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x07, 0x07, 0x03, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
};

static const unsigned char image_DolphinNice_bits[] U8X8_PROGMEM = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xfe,0x1f,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xe0,0x01,0xe0,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x18,0x00,0x00,0x0e,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x04,0x00,0x00,0x30,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x02,0x00,0x00,0x40,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x01,0x00,0x00,0x80,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x80,0x00,0x00,0x00,0x00,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x40,0x00,0x00,0x00,0x00,0x02,0x00,0x00,0x00,0x00,0x00,0x00,0x40,0x00,0x00,0x00,0x00,0x04,0x00,0x00,0x00,0x00,0x00,0x00,0x20,0x00,0x00,0x00,0x00,0x04,0x00,0x00,0x00,0x00,0x00,0x00,0x10,0x00,0x00,0x00,0x00,0x08,0x00,0x00,0x00,0x00,0x00,0x1f,0x30,0x00,0x00,0xf0,0x07,0x10,0x00,0x00,0x00,0x00,0xe0,0xe0,0x58,0x01,0x00,0x08,0x08,0x10,0x00,0x00,0x00,0x00,0x10,0x00,0xaf,0x02,0x00,0x04,0x10,0x20,0x00,0x00,0x00,0x00,0x08,0x00,0x78,0x05,0x00,0xf2,0x23,0x40,0x00,0x00,0x00,0x00,0x04,0x00,0xc0,0x03,0x00,0x3a,0x26,0x40,0x00,0x00,0x00,0x00,0x04,0x00,0x00,0x06,0x00,0x3a,0x27,0x40,0x00,0x00,0x00,0x00,0x02,0x00,0x00,0x00,0x00,0x3a,0x27,0x40,0x00,0x00,0x00,0x00,0x02,0x00,0x00,0x00,0x00,0xfa,0x27,0x80,0x00,0x00,0x00,0x00,0xe2,0x01,0x00,0x00,0x00,0xfa,0x27,0x80,0x00,0x00,0x00,0x00,0x12,0x06,0x00,0x00,0x00,0xf4,0x53,0x80,0x00,0x00,0x00,0x00,0x0a,0x38,0x00,0x00,0x00,0xf8,0xa9,0x80,0x00,0x00,0x00,0x00,0x04,0xc0,0x01,0x00,0x00,0x04,0x56,0x81,0x00,0x00,0x00,0x00,0x04,0x00,0x06,0x00,0x00,0x00,0xa8,0x80,0x00,0x00,0x00,0x00,0x04,0x00,0x18,0x00,0x00,0x00,0x50,0x81,0x00,0x00,0x18,0x00,0x04,0x00,0x60,0x00,0x00,0x00,0xb0,0x80,0x00,0x00,0x24,0x00,0x08,0x00,0x80,0x01,0x00,0x00,0x50,0x80,0x00,0x00,0x22,0x00,0x08,0x00,0x00,0x06,0x00,0x00,0x30,0x80,0x00,0xe0,0x21,0x00,0x10,0x00,0x00,0x18,0x00,0x10,0x10,0x80,0x00,0x18,0x22,0x00,0x20,0x00,0x00,0x60,0x00,0x0c,0x00,0x80,0x00,0x04,0x24,0x00,0x40,0x00,0x00,0x80,0x81,0x03,0x00,0x80,0x00,0x02,0x24,0x00,0x80,0x02,0x00,0x00,0x7e,0x00,0x00,0x80,0x00,0x01,0x28,0x00,0x00,0x15,0x00,0x00,0x00,0x00,0x00,0x80,0x00,0x01,0x48,0x00,0x00,0x2e,0x00,0x00,0x00,0x00,0x00,0x80,0x00,0x01,0x88,0x00,0x00,0x58,0x01,0x00,0x00,0x00,0x00,0x80,0x00,0x01,0x08,0x03,0x00,0xb0,0x02,0x00,0x00,0x00,0x00,0x80,0x00,0x01,0x04,0x0c,0x00,0x40,0x15,0x00,0x00,0x00,0x00,0x80,0x01,0x02,0x02,0x30,0x00,0x80,0xaa,0x02,0x00,0x00,0x00,0xc0,0x01,0xfc,0x01,0xc0,0x00,0x80,0x55,0x55,0x00,0x00,0x00,0xc0,0x03,0x08,0x00,0x00,0x07,0x80,0xab,0xaa,0x00,0x00,0x00,0xc0,0x07,0x10,0x00,0x00,0x38,0xf0,0x55,0x15,0x00,0x00,0x00,0xc0,0x07,0x20,0x00,0x00,0xc0,0xdf,0xaa,0x00,0x00,0x00,0x00,0xc0,0x0f,0x40,0x00,0x00,0x00,0x6a,0x00,0x00,0x00,0x00,0x00,0xc0,0x0f,0x80,0x00,0x00,0x80,0x54,0x00,0x00,0x00,0x00,0x00,0x40,0x1f,0x00,0x01,0x00,0x00,0x2a,0x00,0x00,0x00,0x00,0x00,0xc0,0x1e,0x00,0x02,0x00,0x00,0x35,0x00,0x00,0x00,0x00,0x00,0x40,0x3d,0x00,0x04,0x00,0x40,0x1a,0x00,0x00,0x00,0x00,0x00,0xa0,0x3a,0x00,0x08,0x00,0x00,0x15,0x00,0x00,0x00,0x00,0x00,0x60,0x7d,0x00,0x10,0x00,0x40,0x0a,0x00,0x00,0x00,0x00,0x00,0xa0,0x7a,0x00,0x20,0x00,0x00,0x0d,0x00,0x00,0x00,0x00,0x00,0x60,0xf5,0x00,0xc0,0x00,0x80,0x0a,0x00,0x00,0x00,0x00,0x00,0xa0,0xea,0x00,0x00,0x01,0x20,0x05,0x00,0x00,0x00,0x00,0x00,0x60,0xf5,0x00,0x00,0x06,0x80,0x06,0x00,0x00,0x00,0x00,0x00,0xa0,0xea,0x00,0x00,0x08,0x40,0x05,0x00,0x00,0x00,0x00,0x00,0x60,0xc5,0x00,0x00,0x30,0x90,0x02,0x00,0x00,0x00,0x00,0x00,0xa0,0xca,0x00,0x00,0xc0,0x40,0x03,0x00,0x00,0x00,0x00,0x00,0x60,0x85,0x00,0x00,0x00,0x87,0x02,0x00,0x00,0x00,0x00,0x00,0xa0,0x82,0x00,0x00,0x00,0x78,0x01,0x00,0x00,0x00,0x00,0x00,0x60,0x85,0x00,0x00,0x00,0xc0,0x01,0x00,0x00,0x00,0x00,0x00,0xa0,0x02};

static const unsigned char image_paint_1_bits[] U8X8_PROGMEM = {0x00,0x00,0x00,0x00,0xf0,0x1f,0x00,0x00,0x00,0x00,0x00,0x00,0x18,0xe0,0x03,0x00,0x00,0x00,0x00,0x00,0x08,0x00,0x1c,0x00,0x00,0x00,0x00,0x00,0x08,0x00,0x70,0x00,0x00,0x00,0x00,0x00,0x18,0x00,0xc0,0x00,0x00,0x00,0x00,0x00,0x30,0x00,0x00,0x01,0x00,0x00,0x00,0x00,0x20,0x00,0x00,0x03,0x00,0x00,0x00,0x00,0x20,0x00,0x00,0x02,0x80,0x3f,0x00,0x00,0x60,0x00,0x00,0x06,0xc0,0x60,0x00,0x00,0xc0,0x00,0x00,0x04,0x60,0x40,0x00,0x00,0x80,0x00,0x00,0x0c,0x30,0x40,0x00,0x00,0x80,0x01,0x00,0x08,0x10,0x40,0x00,0x00,0x00,0x01,0x00,0x18,0x18,0x60,0x00,0x00,0x00,0x03,0x00,0x10,0x08,0x38,0x00,0x00,0x00,0x02,0x00,0x10,0x0c,0x18,0x00,0x00,0x00,0x06,0x00,0x10,0xfc,0x03,0x00,0xfc,0xff,0xff,0xff,0x1f,0x08,0xfc,0xff,0x03,0x00,0x00,0x00,0x10,0x08,0x00,0x00,0x00,0x00,0x00,0x00,0x10,0x08,0x00,0x00,0x00,0x00,0x00,0x00,0x10,0x08,0x00,0x00,0x00,0x00,0x00,0x00,0x10,0x08,0x00,0x00,0x00,0x00,0x00,0x00,0x10,0x08,0x00,0x00,0x00,0x00,0x00,0x00,0x10,0x18,0x00,0x00,0x00,0x00,0x00,0x00,0x10,0x10,0x00,0x00,0x00,0x00,0x00,0x00,0x10,0x10,0x00,0x00,0x00,0x00,0x00,0x00,0x18,0x30,0x00,0x00,0x00,0x00,0x00,0x00,0x0c,0x20,0x00,0x00,0x00,0x00,0x00,0x00,0x04,0x60,0x00,0x00,0x00,0x00,0x00,0x00,0x06,0xc0,0x00,0x00,0x00,0x00,0x00,0x00,0x03,0x80,0x01,0x00,0x00,0x00,0x00,0x80,0x01,0x00,0x03,0x00,0x00,0x00,0x00,0xc0,0x00,0x00,0x06,0x00,0x00,0x00,0x00,0x60,0x00,0x00,0x18,0x00,0x00,0x00,0x00,0x38,0x00,0x00,0xe0,0x00,0x00,0x00,0x00,0x0e,0x00,0x00,0x80,0x03,0x00,0x00,0x80,0x03,0x00,0x00,0x00,0x3e,0x00,0x00,0xf0,0x00,0x00,0x00,0x00,0xf0,0xff,0xff,0x3f,0x00,0x00,0x00,0x00,0x1c,0x00,0x00,0x60,0x00,0x00,0x00,0x00,0x06,0x00,0x00,0xc0,0x00,0x00,0x00,0x00,0x02,0x00,0x00,0x80,0x00,0x00,0x00,0x00,0x03,0x00,0x00,0x00,0x01,0x00,0x00,0x80,0x01,0x00,0x00,0x00,0x01,0x00,0x00,0x80,0x00,0x00,0x00,0x00,0x01,0x00,0x00,0xbe,0x00,0x00,0x00,0x00,0x01,0x00,0xff,0xe1,0xff,0xff,0xff,0xff,0xff,0xff};

static const unsigned char image_paint_0_bits[] U8X8_PROGMEM = {0x00,0x00,0xe0,0x01,0x02,0x00,0x70,0x00,0x00,0x80,0x08,0x00,0x00,0x00,0x00,0x00,0x00,0x40,0x02,0x02,0x00,0x88,0x00,0x00,0x80,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x40,0x72,0x9a,0x08,0x08,0x22,0xa7,0xb1,0x0c,0xa7,0x01,0x00,0x00,0x00,0x00,0xc0,0x81,0xa6,0x08,0x08,0x22,0x68,0xca,0x08,0x68,0x02,0x00,0x00,0x00,0x00,0x40,0xf2,0xa2,0x0c,0xc8,0x22,0x2f,0x88,0x08,0x2f,0x02,0x00,0x00,0x00,0x00,0x40,0x8a,0x26,0x0b,0x88,0xb2,0x28,0xc8,0x88,0x28,0x02,0x00,0x00,0x00,0x00,0xe0,0xf1,0x1a,0x08,0x70,0x2c,0x2f,0xb0,0x1c,0x2f,0x02,0x00,0x00,0x00,0x00,0x00,0x00,0x80,0x08,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x07,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xf0,0xff,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x0e,0x00,0x03,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x02,0x00,0x04,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x01,0x00,0x04,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x04,0x00,0x00,0x00,0x00,0x01,0x00,0x08,0xff,0x03,0x00,0x00,0x00,0x00,0x00,0x04,0x00,0x00,0x00,0x00,0x01,0x00,0x08,0x0e,0x0c,0x00,0x00,0x00,0x7c,0x00,0x04,0x01,0x00,0x00,0x00,0x03,0x00,0x08,0x01,0xf0,0x03,0x00,0x00,0xc3,0x00,0x04,0x01,0x00,0x00,0x00,0x0c,0x00,0x88,0x00,0x00,0x0c,0x00,0xc0,0x80,0x00,0x04,0x01,0x00,0x00,0x00,0x78,0x00,0x48,0x00,0x00,0x30,0x00,0x30,0x00,0x01,0x04,0x01,0x00,0x00,0x00,0x80,0x03,0x28,0x00,0x00,0xc0,0x01,0x0c,0x00,0x01,0x84,0x00,0x00,0x00,0x00,0x00,0xfc,0x3f,0xc0,0x07,0x00,0x06,0x02,0x00,0x01,0x84,0x00,0x00,0x00,0x00,0x00,0x00,0x20,0x38,0x00,0x00,0x18,0x01,0x00,0x01,0x84,0x00,0x00,0x00,0x00,0x00,0x00,0x20,0x00,0x00,0xf8,0x21,0x00,0x00,0x01,0x84,0x00,0x00,0x00,0x00,0x00,0x00,0x20,0x00,0x02,0x06,0x27,0x00,0x00,0x01,0x00,0x20,0x00,0x00,0x00,0x00,0x00,0x20,0x00,0x02,0x00,0xc4,0x00,0x00,0x01,0x00,0x20,0x00,0x00,0x00,0x00,0x00,0x20,0x00,0x02,0x00,0x04,0x01,0x00,0x81,0x00,0x20,0x0f,0x00,0x00,0x00,0x00,0x20,0x00,0x02,0x04,0x00,0x02,0x00,0x01,0x01,0x20,0x1b,0x00,0x00,0x00,0x00,0x20,0x00,0x02,0x04,0x00,0x04,0x80,0x00,0x01,0x20,0x10,0xfe,0x03,0x00,0x00,0x20,0x00,0x02,0x04,0x00,0x04,0xc0,0x00,0x0e,0x20,0x10,0x82,0x03,0x00,0x00,0x20,0x00,0x06,0x04,0x00,0x04,0x3f,0x00,0x10,0x18,0x10,0x82,0x03,0x00,0x00,0x20,0x00,0x08,0x04,0x00,0xe4,0x00,0x00,0xe0,0x07,0x10,0x82,0x03,0x00,0x00,0x20,0x00,0x08,0x04,0x00,0x04,0x00,0x00,0x00,0x00,0x18,0x82,0x03,0x00,0x00,0x40,0x00,0x00,0x04,0x00,0x02,0x00,0x00,0x00,0x00,0xfe,0xfe,0x03,0x00,0x00,0x40,0x00,0x04,0x00,0x00,0x02,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x40,0x00,0x08,0x00,0x00,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x40,0x00,0x18,0x80,0x80,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x80,0x00,0xf0,0x6f,0x40,0x00,0x00,0x00,0x00,0x00,0x00,0x60,0x00,0x00,0x00,0x00,0x23,0x00,0x18,0x40,0x00,0x00,0x00,0x00,0x00,0x38,0x60,0x00,0x00,0x00,0x00,0x1c,0x00,0x00,0x30,0x00,0x00,0x00,0x00,0x00,0x44,0x50,0x00,0x00,0x00,0x00,0xf6,0x00,0x00,0x18,0x01,0x00,0x00,0x00,0x00,0x44,0x48,0x00,0x00,0x00,0x00,0x01,0x07,0x00,0x0e,0x06,0x00,0x00,0x00,0x00,0x40,0x44,0x00,0x00,0x00,0xc0,0x00,0xf8,0xff,0x09,0x38,0x00,0x00,0x00,0x00,0x40,0xc4,0x07,0x00,0x00,0x20,0x00,0x00,0x00,0x08,0x20,0x00,0x00,0x00,0x00,0x60,0x78,0x00,0x00,0x00,0x20,0x00,0x02,0x00,0x08,0x20,0x00,0x00,0x00,0x00,0x18,0x40,0x00,0x00,0x00,0x10,0x00,0x02,0x00,0x08,0x20,0x00,0x00,0x00,0x00,0xff,0x41,0x00,0x00,0x00,0x10,0x00,0x02,0x00,0x08,0x20,0x00,0x00,0x00,0x00,0x00,0x40,0x00,0x00,0x00,0x18,0x00,0x02,0x00,0x08,0x20,0x00,0x00,0x00,0x00,0x00,0x40,0x00,0x00,0x00,0x08,0x00,0x02,0x00,0x08,0x20,0x00,0x00,0x00,0x00,0x00,0x40,0x00,0x00,0x00,0x08,0x00,0x02,0x00,0x08,0x20,0x00,0x00,0x00,0x00,0x00,0x20,0x00,0x00,0x00,0x08,0x00,0x02,0x00,0x08,0x20,0x00,0x00,0x00,0x00,0x00,0x20,0x00,0x00,0x00,0x10,0x00,0x01,0x00,0x08,0x20,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xe0,0x80,0x00,0x00,0x18,0x10,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x7f,0x00,0x00,0x60,0x10,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x80,0x10,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x17,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x18,0x00,0x00,0x00,0x00};

static const unsigned char image_wifi_not_connected_bits[] U8X8_PROGMEM = {0x84,0x0f,0x00,0x68,0x30,0x00,0x10,0xc0,0x00,0xa4,0x0f,0x01,0x42,0x30,0x02,0x91,0x40,0x04,0x08,0x85,0x00,0xc4,0x1a,0x01,0x20,0x24,0x00,0x10,0x4a,0x00,0x80,0x15,0x00,0x40,0x20,0x00,0x00,0x42,0x00,0x00,0x85,0x00,0x00,0x02,0x01,0x00,0x00,0x00};
static const unsigned char image_wifi_full_bits[] U8X8_PROGMEM = {0x80,0x0f,0x00,0xe0,0x3f,0x00,0x78,0xf0,0x00,0x9c,0xcf,0x01,0xee,0xbf,0x03,0xf7,0x78,0x07,0x3a,0xe7,0x02,0xdc,0xdf,0x01,0xe8,0xb8,0x00,0x70,0x77,0x00,0xa0,0x2f,0x00,0xc0,0x1d,0x00,0x80,0x0a,0x00,0x00,0x07,0x00,0x00,0x02,0x00,0x00,0x00,0x00};
static const unsigned char image_usb_cable_connected_bits[] U8X8_PROGMEM = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x03,0xc0,0x07,0x20,0x03,0x10,0x20,0x13,0x60,0xff,0xff,0x43,0x60,0x40,0x20,0x80,0x0c,0x00,0x1f,0x00,0x0c,0x00,0x00,0x00,0x00};
static const unsigned char image_operation_warning_bits[] U8X8_PROGMEM = {0x00,0x00,0x80,0x01,0x40,0x02,0x40,0x02,0x20,0x04,0x90,0x09,0x90,0x09,0x88,0x11,0x88,0x11,0x84,0x21,0x02,0x40,0x82,0x41,0x81,0x81,0x01,0x80,0xfe,0x7f,0x00,0x00};
static const unsigned char image_music_bits[] U8X8_PROGMEM = {0x00,0x3e,0xf0,0x21,0x10,0x20,0x10,0x3e,0xf0,0x23,0x10,0x20,0x10,0x20,0x10,0x20,0x10,0x20,0x10,0x20,0x10,0x1c,0x0e,0x22,0x11,0x22,0x11,0x1c,0x0e,0x00,0x00,0x00};
static const unsigned char image_hour_glass_75_bits[] U8X8_PROGMEM = {0xff,0x07,0x02,0x02,0x02,0x02,0x8a,0x02,0xfa,0x02,0x74,0x01,0xa8,0x00,0x50,0x00,0x50,0x00,0x88,0x00,0x24,0x01,0x22,0x02,0x72,0x02,0xfa,0x02,0xfe,0x03,0xff,0x07};
static const unsigned char image_checked_bits[] U8X8_PROGMEM = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x20,0x00,0x30,0x00,0x38,0x01,0x1c,0x03,0x0e,0x07,0x07,0x8e,0x03,0xdc,0x01,0xf8,0x00,0x70,0x00,0x20,0x00,0x00,0x00,0x00,0x00};
static const unsigned char image_light_bits[] U8X8_PROGMEM = {0x04,0x20,0xc8,0x13,0x20,0x04,0x10,0x08,0x95,0xa8,0x90,0x09,0x90,0x08,0x24,0x24,0x42,0x42,0x80,0x00,0xc0,0x03,0x00,0x00,0xc0,0x03,0x00,0x00,0x80,0x01,0x00,0x00};
static const unsigned char image_date_day_bits[] U8X8_PROGMEM = {0x00,0x00,0x90,0x04,0xfe,0x3f,0x93,0x64,0x01,0x40,0x31,0x44,0x49,0x46,0x49,0x45,0x41,0x44,0x21,0x44,0x11,0x44,0x09,0x44,0x79,0x4f,0x03,0x60,0xfe,0x3f,0x00,0x00};
static const unsigned char image_weather_humidity_white_bits[] U8X8_PROGMEM = {0x20,0x00,0x20,0x00,0x30,0x00,0x50,0x00,0x48,0x00,0x88,0x00,0x04,0x01,0x04,0x01,0x82,0x02,0x02,0x03,0x01,0x05,0x01,0x04,0x02,0x02,0x02,0x02,0x0c,0x01,0xf0,0x00};

const int MAX_LENGTH = 300;
char* long_saying = new char[MAX_LENGTH];
char* long_saying_original = "[A] Welcome to Baby Guardian, enjoy your time with your baby :) Please Check Connection (both Internet and Pi)";
char* ptr;
bool show_date = false;
bool show_note = false;
bool wifi_connect = false;
bool pi_connect = false;
bool music_play = false;
bool humidifier = false;
bool need_warning = true;
bool refresh = true;
bool must_warn = false;
long int pos_counter = -10;
bool test_mode = true;
char* thres = "No Data";

void setup() {
  Serial.begin(9600); // Start serial communication at 9600 baud
  u8g2.begin();
  // u8x8.begin();

  // Initialize the Environment Sensor
  Environment.begin();
  long_saying = "[A] Welcome to Baby Guardian, enjoy your time with your baby :) Please Check Connection (both Internet and Pi)";

  // Initialize the MLX90614
  // mlx.begin();
}

char* drawing(){
  return "Baby Guardian (Uno)";
}

char buffer[10]; // Adjust the buffer size as needed
char bufferA[10]; // Adjust the buffer size as needed

char* drawingA() {
  dtostrf(temp, 6, 2, buffer);
  return buffer;
}

char* drawingB() {
  dtostrf(humidity, 6, 2, bufferA);
  return bufferA;
}

void loop() {
  // u8x8.setFont(u8x8_font_chroma48medium8_r);
  // u8x8.setFlipMode(0);
  // // drawCentered(1, "Hello World!");
  // u8x8.drawString(0, 2, "<-right");
  // // drawLeft(3, "left->");
  // u8x8.drawString(0, 6, "counter =");
  // // Get the current time
  // unsigned long currentTime = millis();

  // // // Reading from the environment sensor
  // Serial.print("Time: ");
  // Serial.print(currentTime / 1000); // Convert milliseconds to seconds
  Serial.print("Temperature = ");
  temp = Environment.readTemperature();
  Serial.print(temp); // Print temperature from the environment sensor
  Serial.println(" °C");

  Serial.print("Humidity = ");
  humidity = Environment.readHumidity();
  Serial.print(humidity); // Print humidity
  
  Serial.println(" %");

  String tempString = String(drawingA()) + char(176) +"C";
  String humidityString = String(drawingB()) + " %";

  // u8g2.clearBuffer();
  // u8g2.setFont(u8g2_font_ncenB14_tr);
  // u8g2.drawStr(0,20,"Hello World!");
  // u8g2.sendBuffer();

  u8g2.firstPage();
  do {
    if(status_a == 2){
      u8g2.drawStr(0, 40, tempString.c_str());
      u8g2.drawStr(0, 60, humidityString.c_str());
      // u8g2.setFont(u8g2_font_ncenB14_tr);
      // u8g2.setFont(u8g2_font_8x13B_tf);
      u8g2.setFont(u8g2_font_7x14_tf);
      u8g2.drawStr(0,20,drawing());
    }else if(status_a == 0){
      u8g2.clearBuffer();
      u8g2.setFont(u8g2_font_7x14_tf);
      // u8g2.drawXBMP(50, 0, 7, 80, image_wifi_full_bits);
      // u8g2.drawXBMP(7, 2, 118, 57, image_paint_0_bits);
      // u8g2.drawXBMP(0, 0, 19, 16, image_wifi_full_bits);

      // u8g2.setFont(u8g2_font_5x8);
      u8g2.drawStr(5, 10, "Baby Guardian");
      u8g2.drawXBMP(10, 14, 64, 46, image_paint_1_bits);
      u8g2.drawEllipse(30, 16, 2, 2);
      u8g2.drawStr(85, 45, "230313");
      u8g2.drawStr(90, 60, "beta");

    }else if(status_a == 1){
      u8g2.clearBuffer();
      // u8g2.drawXBMP(50, 0, 7, 80, image_wifi_full_bits);
      u8g2.drawXBMP(7, 2, 118, 57, image_paint_0_bits);
      u8g2.drawXBMP(0, 0, 19, 16, image_wifi_full_bits);

    }else if(status_a == 3){
      String receivedData = Serial.readStringUntil('\n');
      u8g2.clearBuffer();
      u8g2.drawStr(90, 60, "beta");
      u8g2.drawStr(0, 20, String(receivedData).c_str());
      u8g2.drawXBMP(0, 0, 19, 16, image_wifi_full_bits);
    }else if(status_a == 4){
      // u8g2.drawXBMP(14, 1, 96, 59, image_DolphinNice_bits);
      // bool wifi_connect = false;
      // bool pi_connect = false;
      // bool music_play = false;
      // bool humidifier = false;
      if(!wifi_connect){
        u8g2.drawXBMP(22, 0, 19, 16, image_wifi_not_connected_bits);
      }else{
        u8g2.drawXBMP(1, 0, 19, 16, image_wifi_full_bits);
      }
      if(pi_connect){
        u8g2.drawXBMP(43, 0, 16, 16, image_usb_cable_connected_bits);
      }
      if(music_play){
        u8g2.drawXBMP(62, 0, 14, 16, image_music_bits);
      }
      if(need_warning){
        u8g2.drawXBMP(97, -1, 16, 16, image_operation_warning_bits);
      }else{
        u8g2.drawXBMP(114, -1, 14, 16, image_checked_bits);
      }
      if(humidifier){
        u8g2.drawXBMP(82, 0, 11, 16, image_weather_humidity_white_bits);
      }

      // u8g2.drawXBMP(10, 40, 16, 16, image_light_bits);
      if(show_date){
        u8g2.drawXBMP(5-pos_counter, 45, 15, 16, image_date_day_bits);
      }else if(show_note){
        u8g2.drawXBMP(5-pos_counter, 45, 16, 16, image_light_bits);
      }
      
      // u8g2.drawXBMP(113, 40, 11, 16, image_weather_humidity_white_bits);

      u8g2.setFont(u8g2_font_t0_12_tf);
      u8g2.drawStr(-5, 40, tempString.c_str());
      u8g2.drawStr(40, 40, humidityString.c_str());
      u8g2.drawStr(0, 27, drawing());
      u8g2.drawStr(20-pos_counter, 58, long_saying);

      u8g2.setFont(u8g2_font_timB08_tf);
      u8g2.drawStr(90, 40, thres);
      // u8g2.setFont(u8g2_font_ncenB14_tr);
      // u8g2.setFont(u8g2_font_8x13B_tf);
      // u8g2.setFont(u8g2_font_7x14_tf);
    }

  } while ( u8g2.nextPage() );
    // u8g2.drawPixel(30,50);
    // u8g2.clearDisplay();
    // u8g2.setBitmapMode(false /* solid */);
    // u8g2.drawBitmap(0, 0, 64, 64, Size1_2bmp);
    // u8g2.drawStr(0,40,drawingA()+"°C");
    // u8g2.drawStr(0,60,drawingB()+"%");
    // u8g2.setColorIndex(0);

  if (Serial.available() > 0) {
    // Serial.read();
    // Read the data from the serial port
    // char long_saying[MAX_LENGTH];
    // long_saying = Serial.readStringUntil('\n').c_str();
    // Serial.println(long_saying);
    Serial.readString().toCharArray(long_saying, MAX_LENGTH);
    pos_counter = 0;
    ptr = nullptr;
    refresh = true;
    // Serial.println(Serial.readStringUntil('\n'));
  }

  if(refresh){
    ptr = strstr(long_saying, "[A]");
    if(long_saying == ptr){
      show_note = true;
    }else if(long_saying == strstr(long_saying, "[B]")){
      String(long_saying+3).toCharArray(thres, MAX_LENGTH);
    }else if(long_saying == strstr(long_saying, "[C]")){
      humidifier = false;
      // long_saying = long_saying_original;
    }else if(long_saying == strstr(long_saying, "[D]")){
      wifi_connect = true;
    }else if(long_saying == strstr(long_saying, "[E]")){
      wifi_connect = false;
    }else if(long_saying == strstr(long_saying, "[F]")){
      humidifier = true;
    }else if(long_saying == strstr(long_saying, "[G]")){
      pi_connect = true;
    }else if(long_saying == strstr(long_saying, "[H]")){
      music_play = true;
    }else if(long_saying == strstr(long_saying, "[I]")){
      music_play = false;
    }else if(long_saying == strstr(long_saying, "[W]")){
      must_warn = true;
    }else if(long_saying == strstr(long_saying, "[J]")){
      must_warn = false;
    }else{
      show_note = true;
    }
    long_saying = long_saying + 3;
    if(must_warn || !pi_connect || !wifi_connect){
      need_warning = true;
    }else{
      need_warning = false;
    }
    refresh = false;
  }

  if(pos_counter >= 16+(strlen(long_saying)*6)){
    pos_counter = 0;
  }else{
    pos_counter = pos_counter+5;
  }

  // Serial.print(16+(strlen(long_saying)*6));
  // Serial.println("");
  // Serial.print(pos_counter);

  if(a==0){
    a=1;
  }else if(a==1){
    a=2;
  }else if(a==2){
    a=0;
  }

  if(status_a == 0){
    delay(2000);
    status_a = 1;
  }else if(status_a == 1){
    delay(2000);
    status_a = 4;
  }else if(status_a == 2){
    delay(10000);
    status_a = 4;
  }else if(status_a == 4){
    // delay(3000);
    // status_a = 2;
  }
  // else if(status_a == 3){
  //   delay(5000);
  //   status_a = 2;
  // }

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