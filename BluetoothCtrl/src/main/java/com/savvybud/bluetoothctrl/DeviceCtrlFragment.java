package com.savvybud.bluetoothctrl;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link DeviceCtrlFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class DeviceCtrlFragment extends Fragment implements Runnable {

    //private OnFragmentInteractionListener mListener;
    private BluetoothDevice btDevice;
    private UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket btSocket;
    OutputStream mmOutStream;
    public static final String TAG = "BT";
    Thread mBlutoothConnectThread;
    TextView receivedMsgs;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param device Parameter.
     * @return A new instance of fragment DeviceCtrlFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DeviceCtrlFragment newInstance(BluetoothDevice device) {
        DeviceCtrlFragment fragment = new DeviceCtrlFragment();
        /*
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        */
        fragment.btDevice = device;
        return fragment;
    }

    public DeviceCtrlFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_PARAM1);
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mBlutoothConnectThread = new Thread(this);
        mBlutoothConnectThread.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView =  inflater.inflate(R.layout.fragment_device_ctrl, container, false);
        Switch s = (Switch)myView.findViewById(R.id.ctrl_switch);
        receivedMsgs = (TextView)myView.findViewById(R.id.received_msgs);
        s.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //Toast.makeText(getActivity().getApplicationContext(), "Switch :"+isChecked
                  //      , Toast.LENGTH_LONG).show();
                if(isChecked){
                    sendDataToPairedDevice("0");
                }else {
                    sendDataToPairedDevice("1");
                }
            }
        });
        return myView;
    }

    private void sendDataToPairedDevice(String message){
        byte[] toSend = message.getBytes();
        try {
            if(btSocket != null && btSocket.isConnected()){
                mmOutStream = btSocket.getOutputStream();
                mmOutStream.write(toSend);
            }else{
                Log.e(TAG, "socket not connected");
            }
        } catch (IOException e) {
            Log.e("BT", "Exception during write", e);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mBlutoothConnectThread != null){
            mBlutoothConnectThread.interrupt();
            closeSocket(btSocket);
        }
    }

    public void run()
    {
        try
        {
            btSocket = btDevice.createRfcommSocketToServiceRecord(applicationUUID);
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            btSocket.connect();

            mHandler.sendEmptyMessage(0);
            /*
            InputStream is = btSocket.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(btSocket.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) {
                receivedMsgs.setText(line);
            }
            */
            BluetoothSocketListener bsl = new BluetoothSocketListener(btSocket, mHandler,receivedMsgs);
            Thread messageListener = new Thread(bsl);
            messageListener.start();
        }
        catch (IOException eConnectException)
        {
            Log.d(TAG, "CouldNotConnectToSocket", eConnectException);
            closeSocket(btSocket);
            return;
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


    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            //mBluetoothConnectProgressDialog.dismiss();
            Toast.makeText(getActivity(), "Device Connected", 5000).show();
        }
    };

    private class MessagePoster implements Runnable {
        private TextView textView;
        private String message;

        public MessagePoster(TextView textView, String message) {
            this.textView = textView;
            this.message = message;
        }

        public void run() {
            textView.setText("L"+message);
            //Toast.makeText(getActivity().getApplicationContext(),message,Toast.LENGTH_LONG).show();
        }
    }
    private class BluetoothSocketListener implements Runnable {

        private BluetoothSocket socket;
        private TextView textView;
        private Handler handler;
    public BluetoothSocketListener(BluetoothSocket socket,
                                   Handler handler, TextView textView) {
        this.socket = socket;
        this.textView = textView;
        this.handler = handler;
    }

    public void run() {
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        try {
            InputStream instream = socket.getInputStream();
            int bytesRead = -1;
            String message = "";
            StringBuilder sb = new StringBuilder();
            while (true) {
                message = "";
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
                    handler.post(new MessagePoster(textView, sb.toString()));
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
