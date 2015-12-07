package com.savvybud.bluetoothctrl;

import android.app.Application;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.logging.Logger;

public class MainCtrlActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new JoystickFragment())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        Logger.getAnonymousLogger().warning("onBackPressed :"+getFragmentManager().getBackStackEntryCount());
        if(getFragmentManager().getBackStackEntryCount()>0){
            getFragmentManager().popBackStack();
        }else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_ctrl, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_help:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new HelpFragment())
                        .addToBackStack("help")
                        .commit();
                break;
            case R.id.action_joystick:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new JoystickFragment())
                        .addToBackStack("joystick")
                        .commit();
                break;
            case R.id.action_devicectrl:
                //TODO
                Toast.makeText(this.getApplicationContext(),
                        "Stubbed currently",
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_bt:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new BTDevicesFragment())
                        .addToBackStack("bt")
                        .commit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    /*
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
*/
    private void createShortcut(){

        Intent shortcutIntent = new Intent(getApplicationContext(), MainCtrlActivity.class);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "BluetoothCtrl");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.ic_launcher));
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        getApplicationContext().sendBroadcast(addIntent);
    }

}
