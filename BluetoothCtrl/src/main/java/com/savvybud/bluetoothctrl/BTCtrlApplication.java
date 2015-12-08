package com.savvybud.bluetoothctrl;

import android.app.Application;
import android.content.Context;

/**
 * Created by vivsriva on 12/7/15.
 */
public class BTCtrlApplication extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        BTCtrlApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return BTCtrlApplication.context;
    }
}
