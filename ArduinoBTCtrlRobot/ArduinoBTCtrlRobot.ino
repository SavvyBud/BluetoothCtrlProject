/**
 * 
 * This program uses the Adafruit Motor Shield V1
 * Ref: https://learn.adafruit.com/adafruit-motor-shield/overview
 * and HC-06 BT module. The HC-06 BT modules connects to A0 and A1 
 * pins on Arduino UNO R3
 * 
 * 1 byte message to control Robot
 * 
 * bit 7: Joystick or Buttons 0:joystick, 1 button
 * if joystick:
 *  bit 6: 0=FORWARD, 1=BACKWARD
 *  bit 5-4: y coords move straight
 *  bit 3-2: x coords speed decreese for left wheel
 *  bit 1-0: x coords speed decreese for right wheel
 * if button:
 *  bit 0: U/F
 *  bit 1: D/B
 *  bit 2: L
 *  bit 3: R  
 */
 
#include <AFMotor.h>
#include <SoftwareSerial.h>

#define LED_PIN 13
#define MAX_SPEED 255
#define MIN_SPEED 240
#define DECR 20

// Using channel 1 and 4
AF_DCMotor m1(1, MOTOR12_64KHZ);
AF_DCMotor m2(4, MOTOR12_64KHZ);

int state = HIGH;
int lastState = LOW; 
int inByte;
int m1Speed = 255;
int m2Speed = 255;

// Connect TX pin to A0, and RX pin to A1
SoftwareSerial bt(A0, A1);

void toggleState(){
    if (state == HIGH)
      state = LOW;
    else
      state = HIGH;  
}

void readBT(){
  if (bt.available() > 0) {
    inByte = bt.read ();
    Serial.print("BT data: ");
    Serial.println((char)inByte);
    toggleState();
    bt.print((char)inByte);
  }  
}

void moveMotor(AF_DCMotor m, int dir, int duration){
  m.run(dir);
  delay(duration);
  m.run(RELEASE);
}

void setup(){
  bt.begin(9600);
  Serial.begin(9600);   
  pinMode(LED_PIN, OUTPUT);
  Serial.println("Starting app...");
  
  pinMode(LED_PIN, OUTPUT);
  for(int i=0;i<3;i++){
    digitalWrite(LED_PIN, HIGH);
    delay(1000);
    digitalWrite(LED_PIN, LOW);
    delay(1000);
  }
  m1.setSpeed(m1Speed);
  m2.setSpeed(m2Speed);
  moveMotor(m1,FORWARD,1000);
  moveMotor(m1,BACKWARD,1000);
  moveMotor(m2,FORWARD,1000);
  moveMotor(m2,BACKWARD,1000);    
}

void loop() {
  readBT();
  if(state != lastState){ 
    Serial.print("Toggle pin: ");
    Serial.println(LED_PIN);
    digitalWrite(LED_PIN,state);
    Serial.print("Read byte: ");
    Serial.println(inByte);    
    lastState = state;
    if ((inByte & (1<<7)) == 0){
      // joystick
      int dir = ((inByte >> 6) & 0x1)?BACKWARD:FORWARD;
      int xSpeed = (inByte >> 5) & (0x03);
      int lWheelDecr = (inByte >> 3) & 0x03;
      int rWheelDecr = (inByte & 0x03);
      
      Serial.print("xSpeed: "); Serial.print(xSpeed);
      Serial.print(" lWheelDecr: "); Serial.print(lWheelDecr);
      Serial.print(" rWheelDecr: "); Serial.println(rWheelDecr);
      
      if (xSpeed == 0 && lWheelDecr == 0 && rWheelDecr == 0){
        m1.run(RELEASE);
        m2.run(RELEASE);
      } else{
        int m1s = MAX_SPEED - (MAX_SPEED-MIN_SPEED)/4*xSpeed;
        int m2s = m1s;
        m1s -= lWheelDecr*DECR;
        m2s -= rWheelDecr*DECR;
        m1.setSpeed(m1s);
        m2.setSpeed(m2s);
        Serial.print("Motor-1 Speed: "); Serial.print(m1s);
        Serial.print(" Motor-2 Speed: "); Serial.print(m2s);
        m1.run(dir);
        m2.run(dir);
      }      
    }else{
      /* if button:
       *  bit 0: U/F
       *  bit 1: D/B
       *  bit 2: L
       *  bit 3: R  
       */
       Serial.println("Button pressed");
       m1.setSpeed(MAX_SPEED);
       m2.setSpeed(MAX_SPEED);
               
       if (inByte & 0x01) {
          m1.run(FORWARD);
          m2.run(FORWARD);
       }
       if(inByte & 0x2){
          m1.run(BACKWARD);
          m2.run(BACKWARD);        
       }
       if(inByte & 0x4){
          m2.run(FORWARD);
       }
       if(inByte & 0x8){
          m1.run(FORWARD);
       }       
       delay(500);
       m1.run(RELEASE);
       m2.run(RELEASE);            
    }
  }
}

