package com.savvybud.bluetoothctrl;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by vivsriva on 12/7/15.
 */
public class BTDeviceManager {

    private static BTDeviceManager ourInstance = new BTDeviceManager();
    private BluetoothDevice btDevice;
    private UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket btSocket;
    OutputStream mmOutStream;
    public static final String TAG = "BT";
    Thread mBlutoothConnectThread;

    public static BTDeviceManager getInstance() {
        return ourInstance;
    }

    private BTDeviceManager() {
    }

    public void setBtDevice(BluetoothDevice device){
        btDevice = device;
    }

    public BluetoothDevice getBtDevice(){
        return btDevice;
    }
}
