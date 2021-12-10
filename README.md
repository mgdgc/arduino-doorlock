# 아두이노 도어락

> 2021년 2학기 오픈소스 하드웨어 텀프로젝트
> 아두이노를 활용한 블루투스 도어락 프로젝트


**목차**
- [아두이노 도어락](#아두이노-도어락)
  - [구성](#구성)
  - [doorlock.ino](#doorlockino)
    - [Libraries](#libraries)

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
```C++
#include <SoftwareSerial.h>
#include <LiquidCrystal_I2C.h>
#include <Wire.h>
#include <Servo.h>
#include <Keypad.h>
```

* I2C LCD 디스플레이를 사용하기 위해 [LiquidCrystal_I2C](https://github.com/johnrickman/LiquidCrystal_I2C) 라이브러리를 포함해야 합니다.
* 간단하게 4x4(or 3x4) 키패드를 사용하기 위해 [Keypad](https://playground.arduino.cc/Code/Keypad/) 라이브러리를 추가합니다.