BluetoothCtrlProject
====================

Bluetooth SPP based control app

The application uses HC-06 or HC-05 BT module and communicates to Arduino.
There are two different UI of communicating with Arduino:

* Arduino Ctrl
* Joystick

## Arduino Ctrl Mode
In Arduino Ctrl, we simply send a byte of data to Arduino. This may be a simplistic
way to send commands to Arduino.


## Joystick Mode
In second Joystick mode, the coordinates of a virtual joystick is sent to Arduino. This will
help us build a Arduino controlled robot. Just like a joystick, there is additionally 4 buttons.
The intent of these 4 buttons is to control 2 servo motors.


![joystick](joystick.png)
