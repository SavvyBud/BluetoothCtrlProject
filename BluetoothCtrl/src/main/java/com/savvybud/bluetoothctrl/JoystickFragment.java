package com.savvybud.bluetoothctrl;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.logging.Logger;

/**
 * Created by vivsriva on 1/13/14.
 */
public class JoystickFragment extends Fragment implements JoystickCtrlViewListener{

    Logger log = Logger.getLogger(this.getClass().getSimpleName());

    BTDeviceManager btDeviceManager;
    int lastByteSent = 0 ;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_content_joystick,container,false);
        btDeviceManager = BTDeviceManager.getInstance();
        JoystickCtrlView js = (JoystickCtrlView) v.findViewById(R.id.joystick);
        js.registerListener(this);

        Button buttonTop = (Button) v.findViewById(R.id.buttonTop);
        buttonTop.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                btDeviceManager.sendDataToPairedDevice(0x80|0x1);
            }
        });

        Button buttonBottom = (Button) v.findViewById(R.id.buttonBottom);
        buttonBottom.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                btDeviceManager.sendDataToPairedDevice(0x80|0x2);
            }
        });

        Button buttonLeft = (Button) v.findViewById(R.id.buttonLeft);
        buttonLeft.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                btDeviceManager.sendDataToPairedDevice(0x80|0x4);
            }
        });

        Button buttonRight = (Button) v.findViewById(R.id.buttonRight);
        buttonRight.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                btDeviceManager.sendDataToPairedDevice(0x80|0x8);
            }
        });

        return v;
    }

    /*
     * bit 7: Joystick or Buttons 0:joystick, 1 button
     * if joystick:
     *  bit 6: 0=FORWARD, 1=BACKWARD
     *  bit 5-4: y coords move straight
     *  bit 3-2: x coords speed decrease for left wheel
     *  bit 1-0: x coords speed decrease for right wheel
     * if button:
     *  bit 0: U/F
     *  bit 1: D/B
     *  bit 2: L
     *  bit 3: R
     */
    @Override
    public void touching(int size, float x, float y) {
        x = Math.min(size, x);
        x = Math.max(0, x);
        y = Math.min(size, y);
        y = Math.max(0,y);
        int tmp = size/2;
        int dir = y>tmp?1:0;
        int speed = Math.round(Math.abs(y-tmp)/(tmp/3));

        int shift = x>tmp?0:2;
        int turn =  Math.round(Math.abs(x - tmp) / (tmp / 3));
        int wheelDecr = turn << shift;
        int b = (dir << 6) & 0x40 | (speed << 4) & 0x30 | (turn << shift) & 0xF;
        if (b != lastByteSent) {
            log.info("size: "+size+ " x: "+x+" y: "+y);
            log.info("dir: " + dir + " speed: " + speed + " turn: " + turn + " shift: " + shift + " byte: " + b);
            btDeviceManager.sendDataToPairedDevice(b);
            lastByteSent = b;
        }
    }

    @Override
    public void release() {
        log.info("released");
    }
}
