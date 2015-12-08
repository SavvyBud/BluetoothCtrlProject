BluetoothCtrlProject
====================

Bluetooth SPP based control app

The application uses HC-06 or HC-05 BT module and communicates with Arduino.
There are two different UIs of communicating with Arduino:

* Arduino Ctrl
* Joystick

![design](design.png)

## Arduino Ctrl Mode
In Arduino Ctrl, we simply send a byte of data to Arduino. This may be a simplistic
way to send commands to Arduino.

![ctrl](arduino_ctrl.png)

## Joystick Mode
In second Joystick mode, the coordinates of a virtual joystick is sent to Arduino. This will
help us build an Arduino controlled robot. Just like a joystick, there are 4 buttons.
The intent of these 4 buttons is to control 2 servo motors.


![joystick](joystick.png)
