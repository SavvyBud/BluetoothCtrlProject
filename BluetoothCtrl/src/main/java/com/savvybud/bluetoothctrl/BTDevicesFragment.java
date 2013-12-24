package com.savvybud.bluetoothctrl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by gsrivastav on 12/22/13.
 */
public class BTDevicesFragment extends Fragment {
    BluetoothAdapter btAdapter;
    Logger out = Logger.getAnonymousLogger();
    ArrayAdapter<BluetoothDevice> adapter;
    BTDeviceAdapter btDataAdapter;
    public BTDevicesFragment(){
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        //now you must initialize your list view
        ListView listview =(ListView)rootView.findViewById(R.id.btListView);
        enableBT();
        btDataAdapter = new BTDeviceAdapter(this.getActivity().getApplicationContext());
        btDataAdapter.devices.addAll(btAdapter.getBondedDevices());
        listview.setAdapter(btDataAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getActivity().getApplicationContext(),
                        "Opening Control for "+btDataAdapter.devices.get(position).getName(), Toast.LENGTH_LONG).show();
                openDeviceControlFragment(btDataAdapter.devices.get(position));
            }
        });
        return rootView;
    }

    private void openDeviceControlFragment(BluetoothDevice device){
        // Create new fragment and transaction
        Fragment newFragment = DeviceCtrlFragment.newInstance(device);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    protected void enableBT(){
        //Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_UUID);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(ActionFoundReceiver, filter); // Don't forget to unregister during onDestroy

        if (!btAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(this.getActivity().getApplicationContext(), "Bluetooth turned on"
                    , Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this.getActivity().getApplicationContext(),"Bluetooth already on",
                    Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        if (btAdapter != null) {
            btAdapter.cancelDiscovery();
        }
        getActivity().unregisterReceiver(ActionFoundReceiver);
    }

    private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                out.info("\n  Device: " + device.getName() + ", " + device);
                btDataAdapter.devices.add(device);
            } else {
                if(BluetoothDevice.ACTION_UUID.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                    for (int i=0; i<uuidExtra.length; i++) {
                        out.info("\n  Device: " + device.getName() + ", " + device + ", Service: " + uuidExtra[i].toString());
                    }
                    btDataAdapter.devices.add(device);
                } else {
                    if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                        out.info("\nDiscovery Started...");
                    } else {
                        if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                            out.info("\nDiscovery Finished");
                            Iterator<BluetoothDevice> itr = btDataAdapter.devices.iterator();
                            while (itr.hasNext()) {
                                // Get Services for paired devices
                                BluetoothDevice device = itr.next();
                                out.info("\nGetting Services for " + device.getName() + ", " + device);
                                if(!device.fetchUuidsWithSdp()) {
                                    out.info("\nSDP Failed for " + device.getName());
                                }
                                //adapter.add(device);
                            }
                        }
                    }
                }
            }
            btDataAdapter.notifyDataSetChanged();
        }
    };

    class BTDeviceAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<BluetoothDevice> devices;

        public BTDeviceAdapter(Context context) {
            this.context = context;
            devices = new ArrayList<BluetoothDevice>();
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View myView;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                myView =  inflater.inflate(
                        R.layout.device_ctrl, null);
            } else {
                myView = convertView;
            }
            TextView tv1 = (TextView)myView.findViewById(R.id.dc_text1);
            TextView tv2 = (TextView)myView.findViewById(R.id.dc_text2);
            BluetoothDevice d = devices.get(position);
            tv1.setText(d.getName());
            tv2.setText(d.getAddress());
            return myView;
        }
    }
}