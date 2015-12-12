package com.savvybud.bluetoothctrl;

/**
 * Created by vivsriva on 12/12/15.
 */
public interface JoystickCtrlViewListener {
    void touching(int size, float x, float y);
    void release();
}
