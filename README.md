# 아두이노 도어락

> 2021년 2학기 오픈소스 하드웨어 텀프로젝트
> 아두이노를 활용한 블루투스 도어락 프로젝트


**목차**
- [아두이노 도어락](#아두이노-도어락)
  - [구성](#구성)
  - [doorlock.ino](#doorlockino)
    - [Libraries](#libraries)
    - [전역변수](#전역변수)
    - [상수 및 기타 변수](#상수-및-기타-변수)
    - [함수](#함수)
      - [`void PrintMenu()`](#void-printmenu)
      - [`void clearInput()`](#void-clearinput)
      - [`bool checkPw()`](#bool-checkpw)
    - [`setup()`](#setup)

---

## 구성
아래 부품이 각각 한 개씩 필요합니다.
>  * 아두이노 우노
>  * I2C LCD
>  * HM-10 블루투스 모듈
>  * 4x4(or 3x4) Keypad
>  * NTC Thermister
>  * 마그네틱 도어 센서
>  * 서보 모터
>  * 220Ω 저항
>  * 10KΩ 저항

---

## doorlock.ino
### Libraries

<details>
<summary>코드 보기</summary>

```c++
#include <SoftwareSerial.h>
#include <LiquidCrystal_I2C.h>
#include <Wire.h>
#include <Servo.h>
#include <Keypad.h>
```

</details>

* I2C LCD 디스플레이를 사용하기 위해 [LiquidCrystal_I2C](https://github.com/johnrickman/LiquidCrystal_I2C) 라이브러리를 포함해야 합니다.
* 간단하게 4x4(or 3x4) 키패드를 사용하기 위해 [Keypad](https://playground.arduino.cc/Code/Keypad/) 라이브러리를 추가합니다.


### 전역변수

<details>
<summary>코드 보기</summary>

```c++
char password[6] = {'0', '0', '0', '0', '0', '0'};
char input[6] = {'n', 'n', 'n', 'n', 'n', 'n'};
byte inputCursor = 0;

byte mode = 0;
bool locked = false;
bool pwChecked = false;
```

</details>

* `char password[]`: 도어락의 비밀번호를 저장하기 위한 변수
  * 초기 비밀번호는 `000000`
* `char input[]`: 사용자의 입력을 저장하기 위한 변수
  * 빈 입력을 표시하기 위해 'n' 사용
* `byte inputCursor`: 사용자의 입력 위치를 저장하기 위한 변수
* `byte mode`: 도어락의 모드를 저장하기 위한 변수
  * 0: 메인 메뉴
  * 1: 잠금 해제 모드
  * 2: 비밀번호 변경 모드
* `bool locked`: 현재 잠금 상태를 저장하기 위한 변수
* `bool pwChecked`: 비밀번호 변경 등 비밀번호 확인이 필요한 메뉴에서 인증이 되었는지 여부를 판별하기 위한 변수


### 상수 및 기타 변수

<details>
<summary>코드 보기</summary>

```c++
const int THERMAL_LIMIT = 500; // 화재 인식 범위

const byte KEY_ROWS = 4; // 키패드 열
const byte KEY_COLS = 4; // 키패드 행
// 키패드 배열
const char KEYS[KEY_ROWS][KEY_COLS] = {
  {'1', '2', '3', 'u'},
  {'4', '5', '6', 'd'},
  {'7', '8', '9', 'o'},
  {'*', '0', '#', 'x'}
};

// 핀 위치
const byte PIN_TEMPERATURE = A2;
const byte PIN_DOOR_SENSOR = 2;
const byte PIN_BT_RX = 4;
const byte PIN_BT_TX = 5;
const byte PIN_SERVO = 3;
const byte PIN_KEY_ROWS[KEY_ROWS] = {13, 12, 11, 10};
const byte PIN_KEY_COLS[KEY_COLS] = {9, 8, 7, 6};

// I2C LCD 모듈 
LiquidCrystal_I2C lcd(0x27, 16, 2);
// 블루투스 모듈
SoftwareSerial bt(PIN_BT_TX, PIN_BT_RX);
// 키패드
Keypad pad = Keypad(makeKeymap(KEYS),
                    PIN_KEY_ROWS, PIN_KEY_COLS,
                    KEY_ROWS, KEY_COLS);
// 서보모터
Servo servo;
```

</details>


### 함수
```c++
void printMenu();
void clearInput();
bool checkPw();
```

#### `void PrintMenu()`

<details>
<summary>코드 보기</summary>

```c++
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
```

</details>

* 전역 변수 `mode`에 따라 I2C LCD에 화면을 출력합니다.
* `mode 0`: 메인 메뉴로, 1번을 눌러 잠금 해제하거나 2번을 눌러 비밀번호를 변경할 수 있습니다.
* `mode 1`: 설정된 비밀번호를 입력하면 잠금 해제합니다.
* `mode 2`: 전역 변수 `pwChecked`에 따라 비밀번호 확인 절차를 거치고 새 비밀번호를 입력하도록 안내합니다.

#### `void clearInput()`

<details>
<summary>코드 보기</summary>

```c++
void clearInput() {
  for (int i = 0; i < 6; i++) {
    input[i] = 'n';
  }
  inputCursor = 0;
}
```

</details>

* 사용자의 입력은 전역변수 `input`에 저장됩니다.
* 입력이 완료되었거나 취소되면 이 함수에 의해 `input` 변수에 저장된 값이 'n'으로 초기화됩니다.

#### `bool checkPw()`

<details>
<summary>코드 보기</summary>

```c++
bool checkPw() {
  bool correct = true;
  for (int i = 0; i < 6; i++) {
    if (password[i] != input[i]) {
      correct = false;
      break;
    }
  }

  return correct;
}
```

</details>

* 사용자의 입력 `input`과 설정된 비밀번호 `password`를 비교하여 비밀번호가 일치하는지 확인합니다.


### `setup()`

<details>
<summary>코드 보기</summary>

```c++
void setup() {
  // put your setup code here, to run once:

  Serial.begin(9600);

  // Initialize servo
  servo.attach(PIN_SERVO);
  servo.write(0);

  // Initiailize bluetooth
  bt.begin(9600);

  // Initialize button (proximity replacement)
  pinMode(PIN_DOOR_SENSOR, INPUT);

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
```

</details>