package com.savvybud.bluetoothctrl;

import android.app.ListFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link DeviceCtrlFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class DeviceCtrlFragment extends ListFragment implements Runnable {

    private BluetoothDevice btDevice;
    private UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket btSocket;
    OutputStream mmOutStream;
    public static final String TAG = "BT";
    Thread mBlutoothConnectThread;
    TextView receivedMsgs;
    DeviceCtrlAdapter mDeviceCtrlAdapter;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param device Parameter.
     * @return A new instance of fragment DeviceCtrlFragment.
     */
    public static DeviceCtrlFragment newInstance(BluetoothDevice device) {
        DeviceCtrlFragment fragment = new DeviceCtrlFragment();
        fragment.btDevice = device;
        return fragment;
    }

    public DeviceCtrlFragment() {
        btDevice = BTDeviceManager.getInstance().getBtDevice();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDeviceCtrlAdapter = new DeviceCtrlAdapter(getActivity().getApplicationContext());
        setListAdapter(mDeviceCtrlAdapter);
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
        View myView =  inflater.inflate(R.layout.fragment_device_ctrl, container, false);
        return myView;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Control c = mDeviceCtrlAdapter.controls.get(position);
        c.currentState = ! c.currentState;
        Toast.makeText(getActivity().getApplicationContext(),
                c.name, Toast.LENGTH_LONG).show();
        Log.e(TAG,"Sending message to "+c.name);
        sendDataToPairedDevice(c.currentState ? c.onCmd : c.offCmd);
        mDeviceCtrlAdapter.notifyDataSetChanged();
    }

    private void sendDataToPairedDevice(int message){
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
            //textView.setText("L"+message);
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

    private enum CtrlType {
        switch_ctrl,
        slider_ctrl
    }

    private class Control {
        int onCmd;
        int offCmd;
        String name;
        CtrlType ctrl_type;
        boolean currentState;
    }

    public class DeviceCtrlAdapter extends BaseAdapter {

        public static final String NAME="name";
        public static final String ONCMD="onCmd";
        public static final String OFFCMD="offCmd";

        ArrayList<Control> controls;


        public DeviceCtrlAdapter(Context context){
            controls = new ArrayList<Control>();
            XmlPullParser p = context.getResources().getXml(R.xml.controls);

            try {
                Log.e(TAG,"Parsing resource controls.xml");
                p.next();
                int eventType = p.getEventType();
                while(eventType != XmlPullParser.END_DOCUMENT){
                    if(eventType == XmlPullParser.START_TAG && p.getName().compareTo("control")==0)
                    {
                        Log.e(TAG,"START: "+p.getName());
                        AttributeSet as = Xml.asAttributeSet(p);
                        String name = as.getAttributeValue(null, DeviceCtrlAdapter.NAME);
                        Integer onCmd = as.getAttributeIntValue(null, DeviceCtrlAdapter.ONCMD, 1);
                        Integer offCmd = as.getAttributeIntValue(null,DeviceCtrlAdapter.OFFCMD,1);
                        Control c = new Control();
                        c.onCmd = onCmd;
                        c.offCmd = offCmd;
                        c.name = name;
                        c.ctrl_type = CtrlType.switch_ctrl;
                        controls.add(c);
                        Log.i(TAG,"Name: "+c.name+" on:"+c.onCmd+", off:"+c.offCmd);
                    }
                    else if(eventType == XmlPullParser.END_TAG)
                    {
                        //stringBuffer.append("\nEND_TAG: "+p.getName());
                        Log.e(TAG,"END:"+p.getName());
                    }
                    p.next();
                    eventType = p.getEventType();
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public int getCount() {
            return controls.size()+1;
        }

        @Override
        public Object getItem(int i) {
            return i<controls.size()?controls.get(i):null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public int getViewTypeCount (){
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return position>=controls.size()?1:0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View myView;
            if(view == null){
                LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if(i>=controls.size()){
                    myView =  inflater.inflate(
                            R.layout.listview_automode_ctrl, null);
                }else
                    myView =  inflater.inflate(
                        R.layout.listview_switch_ctrl, null);
            } else {
                myView = view;
            }
            if(i>=controls.size()) {
            }else{
                Switch s = (Switch)myView.findViewById(R.id.list_ctrl_switch);
                Control c= controls.get(i);
                s.setText(c.name);
                s.setChecked(c.currentState);
            }
            return myView;
        }
    }
}
