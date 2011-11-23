package com.witcraft.android.locationemulator;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.google.android.maps.*;
import com.witcraft.android.locationemulator.lib.LocationExtensions;
import com.witcraft.android.locationemulator.lib.OnDragEndListener;
import com.witcraft.android.locationemulator.lib.OnDragOverlay;

public class MainActivity extends MapActivity {
    private static final String TAG = "MainActivity";
    private static final String ACTION_APPLICATION_DEVELOPMENT_SETTINGS = "com.android.settings.APPLICATION_DEVELOPMENT_SETTINGS";
    private MapView mapView;
    private MapController mapController;
    //    private LocationManager locationManager;
    private ToggleButton toggleEmulationBtn;
    private boolean mIsBound;
    private MockService mBoundService;
    private EditText speedText;
    private EditText headingText;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((MockService.LocalBinder) service).getService();

            Toast.makeText(MainActivity.this, R.string.mock_service_connected,
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            Toast.makeText(MainActivity.this, R.string.mock_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };

    private CompoundButton.OnCheckedChangeListener onToggleEmulationBtnCheckedChanged = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean cheked) {
            if (cheked) {
                Log.d(TAG, "Service starting...");

                Intent service = new Intent(MainActivity.this, MockService.class);
                service.putExtra("location", LocationExtensions.fromGeoPoint(mapView.getMapCenter()));
                startService(service);

                doBindService();
            } else {
                Log.d(TAG, "Service stopping...");

                doUnbindService();

                stopService(new Intent(MainActivity.this, MockService.class));
            }
        }
    };

    private OnDragEndListener onMapViewDragEnd = new OnDragEndListener() {
        @Override
        public void onDragEnd(MapView mapView, GeoPoint center) {
            if (mBoundService != null) {
                Location mockLocation = getCurrentLocation();

                mBoundService.setNewMockLocation(mockLocation);
            }
        }
    };

    private Location getCurrentLocation() {
        Location mockLocation = LocationExtensions.fromGeoPoint(mapView.getMapCenter());
        mockLocation.setAccuracy(10);

        try {
            mockLocation.setSpeed((float) speedFromKMH(Integer.parseInt(speedText.getText().toString())));
        } catch (NumberFormatException e) {
            mockLocation.setSpeed(20);
        }

        try {
            mockLocation.setBearing((float) Integer.parseInt(headingText.getText().toString()));
        } catch (NumberFormatException e) {
            //  Do nothing
        }

        return mockLocation;
    }

    public void apply(View view) {
        Location mockLocation = getCurrentLocation();

        mBoundService.setNewMockLocation(mockLocation);
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        this.mapView = (MapView) findViewById(R.id.map_view);
        this.mapView.getOverlays().add(new OnDragOverlay(onMapViewDragEnd));

        this.mapController = mapView.getController();

        mapController.setZoom(12);

        this.toggleEmulationBtn = (ToggleButton) findViewById(R.id.toggle_emulation_btn);
        this.toggleEmulationBtn.setOnCheckedChangeListener(onToggleEmulationBtnCheckedChanged);

        speedText = (EditText) findViewById(R.id.speed);
        headingText = (EditText) findViewById(R.id.heading);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkMockSettings();

        if (toggleEmulationBtn.isChecked()) {
            doBindService();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        doUnbindService();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    private void doBindService() {
        bindService(new Intent(MainActivity.this, MockService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void doUnbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    private double speedFromKMH(int speed) {
        return speed / 3.6;
    }

    private void checkMockSettings() {
        if (Settings.Secure.getString(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0")) {
            Intent settings = new Intent(ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
            startActivity(settings);

            Toast.makeText(this, "Please enable mock locations", Toast.LENGTH_LONG).show();
        }
    }

}

