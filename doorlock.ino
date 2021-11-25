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
//const byte PIN_PROXIMITY = A3;
const byte PIN_PROXIMITY = 2;
const byte PIN_BT_RX = 4;
const byte PIN_BT_TX = 5;
const byte PIN_SERVO = 3;
const byte PIN_KEY_ROWS[KEY_ROWS] = {13, 12, 11, 10};
const byte PIN_KEY_COLS[KEY_COLS] = {9, 8, 7, 6};

LiquidCrystal_I2C lcd(0x27, 16, 2);
SoftwareSerial bt(PIN_BT_RX, PIN_BT_TX);
Keypad pad = Keypad(makeKeymap(KEYS),
                    PIN_KEY_ROWS, PIN_KEY_COLS,
                    KEY_ROWS, KEY_COLS);
Servo servo;

char password[6] = {'0', '0', '0', '0', '0', '0'};

byte mode = 0;

void setup() {
  // put your setup code here, to run once:

  // Initialize servo
  servo.attach(PIN_SERVO);
  servo.write(0);

  // Initiailize bluetooth
  bt.write("AT+NAMEDoorlock");
  bt.write("AT+PIN000000");

  // Initialize button (proximity replacement)
  pinMode(PIN_PROXIMITY, INPUT);
  
  lcd.init();
  lcd.backlight();
  lcd.setCursor(0, 0);
  lcd.print("Initial password");
  lcd.setCursor(1, 1);
  lcd.print("000000");

  char key;
  while (!(key = pad.getKey())) {
    delay(100);
  }

}

void loop() {
  // put your main code here, to run repeatedly:

  // Detect door closing
  //int proximity = analogRead(PIN_PROXIMITY);
  //if (proximity > 800) {
  //  servo.write(90);
  //}
  if (digitalRead(PIN_PROXIMITY) == HIGH) {
    servo.write(90);
  }

  // Waiting for bluetooth
  if (bt.available()) {
    byte data = bt.read();
    if (data == 'u') {
      bool pass = true;
      for (int i = 0; bt.available() && i < 6; i++) {
        if (bt.read() != password[i]) {
          pass = false;
          break;
        }
      }
      
    } else if (data == 'l') {
      servo.write(90);
    }
  }
  
  switch (mode) {
    case 0: // Main menu mode
      lcd.init();
      lcd.setCursor(1, 0);
      lcd.print("1. Unlock");
      lcd.setCursor(1, 1);
      lcd.print("2. Settings");

      char key = pad.getKey();

      if (key == 1) {
        mode = 1;
      }
      break;

    case 1: // Unlock mode
      break;

    case 2: // Settings mode
      break;
  }

  delay(50);
}
