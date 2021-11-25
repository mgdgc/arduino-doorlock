#include <SoftwareSerial.h>
#include <LiquidCrystal_I2C.h>
#include <Wire.h>
#include <Servo.h>
#include <Keypad.h>

const byte KEY_ROWS = 4;
const byte KEY_COLS = 4;
const char KEYS[KEY_ROWS][KEY_COLS] = {
  {'1', '2', '3', 'u'},
  {'4', '5', '6', 'd'},
  {'7', '8', '9', 'o'},
  {'*', '0', '#', 'x'}
};

const byte PIN_THERMAL = A2;
const byte PIN_PROXIMITY = A3
                           const byte PIN_BT_RX = 4;
const byte PIN_BT_TX = 5;
const byte PIN_KEY_ROWS[KEY_ROWS] = {13, 12, 11, 10};
const byte PIN_KEY_COLS[KEY_COLS] = {9, 8, 7, 6};

LiquidCrystal_I2C lcd(0x27, 16, 2);
SoftwareSerial BTSerial(PIN_BT_RX, PIN_BT_TX);
Keypad pad = Keypad(makeKeymap(KEYS),
                    PIN_KEY_ROWS, PIN_KEY_COLS,
                    KEY_ROWS, KEY_COLS);

byte password[12] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

void setup() {
  // put your setup code here, to run once:
  lcd.init();
  lcd.backlight();
  lcd.setCursor(2, 0);
  lcd.print("Set password");
}

void loop() {
  // put your main code here, to run repeatedly:

}
