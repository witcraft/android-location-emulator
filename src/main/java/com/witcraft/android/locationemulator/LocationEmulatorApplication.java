package com.witcraft.android.locationemulator;

import android.app.Application;

/**
 * Created by IntelliJ IDEA.
 * User: zakharov
 * Date: 11/22/11
 * Time: 5:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class LocationEmulatorApplication extends Application {
    private boolean isServiceEnabled;

    @Override
    public void onCreate() {
        isServiceEnabled = false;
        super.onCreate();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public boolean isServiceEnabled() {
        return isServiceEnabled;
    }

    public void setServiceEnabled(boolean enabled) {
        isServiceEnabled = enabled;
    }
}
