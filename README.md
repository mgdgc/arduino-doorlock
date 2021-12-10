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
```c++
#include <SoftwareSerial.h>
#include <LiquidCrystal_I2C.h>
#include <Wire.h>
#include <Servo.h>
#include <Keypad.h>
```

* I2C LCD 디스플레이를 사용하기 위해 [LiquidCrystal_I2C](https://github.com/johnrickman/LiquidCrystal_I2C) 라이브러리를 포함해야 합니다.
* 간단하게 4x4(or 3x4) 키패드를 사용하기 위해 [Keypad](https://playground.arduino.cc/Code/Keypad/) 라이브러리를 추가합니다.

### 전역변수
```c++
char password[6] = {'0', '0', '0', '0', '0', '0'};
char input[6] = {'n', 'n', 'n', 'n', 'n', 'n'};
byte inputCursor = 0;

byte mode = 0;
boolean locked = false;
boolean pwChecked = false;
```
* `char password[]`: 도어락의 비밀번호를 저장하기 위한 변수
  * 초기 비밀번호는 `000000`
* `char input[]`: 사용자의 입력을 저장하기 위한 변수
  * 빈 입력을 표시하기 위해 'n' 사용
* `byte inputCursor`: 사용자의 입력 위치를 저장하기 위한 변수
* `byte mode`: 도어락의 모드를 저장하기 위한 변수
  * 0: 메인 메뉴
  * 1: 잠금 해제 모드
  * 2: 비밀번호 변경 모드
* `boolean locked`: 현재 잠금 상태를 저장하기 위한 변수
* `boolean pwChecked`: 비밀번호 변경 등 비밀번호 확인이 필요한 메뉴에서 인증이 되었는지 여부를 판별하기 위한 변수

### 상수 및 기타 변수
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