package com.savvybud.bluetoothctrl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * This class needs some work. The handler and the MessagePoster is a kluge.
 * Created by vivsriva on 12/7/15.
 */
public class BTDeviceManager implements Runnable {

    private static BTDeviceManager ourInstance = new BTDeviceManager();
    private BluetoothDevice btDevice;
    private UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket btSocket;
    OutputStream mmOutStream;
    public static final String TAG = "BT";
    Thread mBluetoothConnectThread;
    Handler mHandler;

    public static BTDeviceManager getInstance() {
        return ourInstance;
    }

    private BTDeviceManager() {
        mHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                Toast.makeText(BTCtrlApplication.getAppContext(), "Device Connected", Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void setBtDevice(BluetoothDevice device){
        if(mBluetoothConnectThread != null){
            destroy();
        }
        btDevice = device;
        mBluetoothConnectThread = new Thread(this);
        mBluetoothConnectThread.start();
    }

    public BluetoothDevice getBtDevice(){
        return btDevice;
    }

    public void run()
    {
        try
        {
            btSocket = btDevice.createRfcommSocketToServiceRecord(applicationUUID);
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            btSocket.connect();

            mHandler.sendEmptyMessage(0);
            BluetoothSocketListener bsl = new BluetoothSocketListener(btSocket, mHandler);
            Thread messageListener = new Thread(bsl);
            messageListener.start();
        }
        catch (IOException eConnectException)
        {
            Log.d(TAG, "CouldNotConnectToSocket", eConnectException);
            closeSocket(btSocket);
        }
    }

    public void sendDataToPairedDevice(int message){
        Log.i(TAG,"Sending: "+message);
        try {
            if(btSocket != null && btSocket.isConnected()){
                mmOutStream = btSocket.getOutputStream();
                mmOutStream.write((byte)message);
            }else{
                Log.e(TAG, "socket not connected");
            }
        } catch (IOException e) {
            Log.e("BT", "Exception during write", e);
        }
    }

    public void destroy(){
        if(mBluetoothConnectThread != null){
            mBluetoothConnectThread.interrupt();
            closeSocket(btSocket);
        }
    }

    private void closeSocket(BluetoothSocket nOpenSocket)
    {
        try
        {
            btSocket.close();
            Log.d(TAG, "SocketClosed");
        }
        catch (IOException ex)
        {
            Log.d(TAG, "CouldNotCloseSocket");
        }
    }

    private class MessagePoster implements Runnable {
        private String message;

        public MessagePoster(String message) {
            this.message = message;
        }

        public void run() {
            Toast.makeText(BTCtrlApplication.getAppContext(),message,Toast.LENGTH_LONG).show();
        }
    }

    private class BluetoothSocketListener implements Runnable {

        private BluetoothSocket socket;
        private TextView textView;
        private Handler handler;

        public BluetoothSocketListener(BluetoothSocket socket,
                                       Handler handler) {
            this.socket = socket;
            this.handler = handler;
        }

        public void run() {
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            try {
                InputStream instream = socket.getInputStream();
                int bytesRead = -1;
                StringBuilder sb = new StringBuilder();
                while (true) {
                    bytesRead = instream.read(buffer);
                    if (bytesRead != -1) {
                        /*
                        while ((bytesRead==bufferSize)&&(buffer[bufferSize-1] != 0)) {
                            message = message + new String(buffer, 0, bytesRead);
                            bytesRead = instream.read(buffer);
                        }
                        message = message + new String(buffer, 0, bytesRead - 1);
                        */
                        sb.append(new String(buffer,0,bytesRead-1));
                        handler.post(new MessagePoster(sb.toString()));
                        socket.getInputStream();
                        sb.setLength(0);
                    }
                }
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }
}
