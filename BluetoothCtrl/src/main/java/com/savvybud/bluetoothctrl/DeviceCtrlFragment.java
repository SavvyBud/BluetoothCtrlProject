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
 * create an instance of this fragment.
 *
 */
public class DeviceCtrlFragment extends ListFragment {

    public static final String TAG = "BT";
    DeviceCtrlAdapter mDeviceCtrlAdapter;
    BTDeviceManager btDeviceManager;

    public DeviceCtrlFragment() {
        btDeviceManager = BTDeviceManager.getInstance();
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

        btDeviceManager.sendDataToPairedDevice(c.currentState ? c.onCmd : c.offCmd);
        mDeviceCtrlAdapter.notifyDataSetChanged();
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
