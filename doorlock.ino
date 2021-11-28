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
SoftwareSerial bt(PIN_BT_TX, PIN_BT_RX);
Keypad pad = Keypad(makeKeymap(KEYS),
                    PIN_KEY_ROWS, PIN_KEY_COLS,
                    KEY_ROWS, KEY_COLS);
Servo servo;

char password[6] = {'0', '0', '0', '0', '0', '0'};
char input[6] = {'n', 'n', 'n', 'n', 'n', 'n'};
byte inputCursor = 0;

byte mode = 0;
boolean locked = false;
boolean pwChecked = false;

void printMenu();
void clearInput();
boolean checkPw();

void setup() {
  // put your setup code here, to run once:

  Serial.begin(9600);

  // Initialize servo
  servo.attach(PIN_SERVO);
  servo.write(0);

  // Initiailize bluetooth
  bt.begin(9600);

  // Initialize button (proximity replacement)
  pinMode(PIN_PROXIMITY, INPUT);

  lcd.init();
  lcd.backlight();
  lcd.setCursor(3, 0);
  lcd.print("PW: 000000");
  lcd.setCursor(0, 1);
  lcd.print("Press any button");

  char key;
  while (!(key = pad.getKey())) {
    delay(100);
  }

  printMenu();

}

void loop() {
  // put your main code here, to run repeatedly:

  // Detect door closing
  //int proximity = analogRead(PIN_PROXIMITY);
  //if (proximity > 800) {
  //  servo.write(90);
  //}
  if (digitalRead(PIN_PROXIMITY) && !locked) {
    locked = true;
    servo.write(90);

    lcd.init();
    lcd.setCursor(5, 0);
    lcd.print("Locked");

    delay(1000);
    printMenu();
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

      if (pass) {
        servo.write(0);

        lcd.init();
        lcd.setCursor(3, 0);
        lcd.print("Bluetooth");
        lcd.setCursor(4, 1);
        lcd.print("Unlocked");

        delay(1000);
        printMenu();

      }

    } else if (data == 'l') {
      servo.write(90);

      lcd.init();
      lcd.setCursor(3, 0);
      lcd.print("Bluetooth");
      lcd.setCursor(5, 1);
      lcd.print("Locked");

      delay(1000);
      printMenu();
    }
  }


  char key = pad.getKey();

  if (!key) {
    delay(50);
    return;
  }

  switch (mode) {
    case 0: // Main menu mode
      if (key == '1') {
        mode = 1;
        printMenu();
      } else if (key == '2') {
        mode = 2;
        printMenu();
      }
      break;

    case 1: // Unlock mode

      input[inputCursor++] = key;
      lcd.setCursor(4 + inputCursor, 1);
      lcd.print(key);

      if (inputCursor >= 6) {
        if (checkPw()) {
          clearInput();

          locked = false;
          servo.write(0);

          lcd.init();
          lcd.setCursor(4, 0);
          lcd.print("Welcome!");
          lcd.setCursor(4, 1);
          lcd.print("Unlocked");

          delay(1000);

        } else {

          lcd.init();
          lcd.setCursor(5, 0);
          lcd.print("Wrong");
          lcd.setCursor(4, 1);
          lcd.print("Password");

          delay(1000);
        }

        // Clear input
        mode = 0;
        printMenu();

      }
      break;

    case 2: // Settings mode
      input[inputCursor++] = key;
      lcd.setCursor(4 + inputCursor, 1);
      lcd.print(key);

      if (inputCursor >= 6) {
        if (pwChecked) {
          pwChecked = false;

          for (int i = 0; i < 6; i++) {
            password[i] = input[i];
          }

          lcd.init();
          lcd.setCursor(4, 0);
          lcd.print("Password");
          lcd.setCursor(4, 1);
          lcd.print("Changed!");

          delay(1000);

          mode = 0;
          printMenu();

        } else {
          if (checkPw()) {
            pwChecked = true;
            printMenu();
          } else {
            pwChecked = false;

            lcd.init();
            lcd.setCursor(5, 0);
            lcd.print("Wrong");
            lcd.setCursor(4, 1);
            lcd.print("Password");

            delay(1000);

            mode = 0;
            printMenu();
          }
        }
        clearInput();
      }
      break;
  }

  delay(50);
}

void printMenu() {
  lcd.init();
  switch (mode) {
    case 0: // Main menu mode
      lcd.setCursor(1, 0);
      lcd.print("1. Unlock");
      lcd.setCursor(1, 1);
      lcd.print("2. Change PW");
      break;

    case 1: // Unlock mode
      lcd.setCursor(4, 0);
      lcd.print("Password");
      break;

    case 2: // Settings mode
      if (pwChecked) {
        lcd.setCursor(2, 0);
        lcd.print("Enter new pw");
      } else {
        lcd.setCursor(4, 0);
        lcd.print("Enter pw");
      }
      break;
  }
}

void clearInput() {
  for (int i = 0; i < 6; i++) {
    input[i] = 'n';
  }
  inputCursor = 0;
}

boolean checkPw() {
  boolean correct = true;
  for (int i = 0; i < 6; i++) {
    if (password[i] != input[i]) {
      correct = false;
      break;
    }
  }

  return correct;
}
