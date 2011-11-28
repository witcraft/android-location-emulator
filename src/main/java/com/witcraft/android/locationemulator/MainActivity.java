package com.witcraft.android.locationemulator;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.*;
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
    private boolean mIsBound;
    private MockService mBoundService;
    private EditText accuracyText;
    private EditText speedText;
    private EditText headingText;
    private ImageView pointer;
    private boolean headingMode;

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

    private OnDragEndListener onMapViewDragEnd = new OnDragEndListener() {
        @Override
        public void onDragEnd(MapView mapView, GeoPoint center) {
            if (mBoundService != null) {
                Location mockLocation = getCurrentLocation();

                mBoundService.setNewMockLocation(mockLocation);
            }
        }
    };

    private LocationEmulatorApplication getApp() {
        return (LocationEmulatorApplication) getApplication();
    }

    private Location getCurrentLocation() {
        Location mockLocation = LocationExtensions.fromGeoPoint(mapView.getMapCenter());
        mockLocation.setAccuracy(10);

        try {
            mockLocation.setAccuracy((float) Integer.parseInt(accuracyText.getText().toString()));
        } catch (NumberFormatException e) {
            mockLocation.setAccuracy(10);
        }

        try {
            mockLocation.setSpeed((float) speedFromKMH(Integer.parseInt(speedText.getText().toString())));
        } catch (NumberFormatException e) {
            mockLocation.setSpeed(20);
        }

        try {
            mockLocation.setBearing((float) Integer.parseInt(headingText.getText().toString()));
        } catch (NumberFormatException e) {
            mockLocation.setBearing(0);
        }

        return mockLocation;
    }

    public void apply(View view) {
        if (getApp().isServiceEnabled()) {
            Location mockLocation = getCurrentLocation();

            if (mBoundService != null) {
                mBoundService.setNewMockLocation(mockLocation);
            }
        } else {
            Toast.makeText(this, "Enable service first", Toast.LENGTH_LONG).show();
        }
    }

    public void clear(View view) {
        if (mBoundService != null) {
            mBoundService.clearList();
        }
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        this.mapView = (MapView) findViewById(R.id.map_view);
        this.mapView.getOverlays().add(new OnDragOverlay(onMapViewDragEnd));

        this.mapController = mapView.getController();

        mapController.setZoom(12);
        mapView.setBuiltInZoomControls(true);

        pointer = (ImageView) findViewById(R.id.pointer);
        pointer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "Long click happened");
                headingMode = true;

                Log.d(TAG, "" + (view.getRight() + view.getLeft()) / 2 + " - " + (view.getTop() + view.getBottom()) / 2);

                return false;
            }
        });

        pointer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                    headingMode = false;
                }

                if (headingMode) {
                    double xCenter = (view.getRight() + view.getLeft()) / 2;
                    double yCenter = (view.getTop() + view.getBottom()) / 2;
                    double x1End = 0;
                    double y1End = -100;
                    double x2End = view.getLeft() + motionEvent.getX() - xCenter;
                    double y2End = view.getTop() + motionEvent.getY() - yCenter;

                    Log.d(TAG, "" + x2End + " - " + y2End);

//                    double cos = () / ();
                }

                return false;
            }
        });

        accuracyText = (EditText) findViewById(R.id.accuracy);
        speedText = (EditText) findViewById(R.id.speed);
        headingText = (EditText) findViewById(R.id.heading);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkMockSettings();

        if (getApp().isServiceEnabled()) {
            doBindService();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        doUnbindService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.enable:
                if (!getApp().isServiceEnabled()) {
                    Log.d(TAG, "Service starting...");

                    Intent service = new Intent(MainActivity.this, MockService.class);
                    service.putExtra("location", getCurrentLocation());
                    startService(service);

                    doBindService();

                    getApp().setServiceEnabled(true);
                }
                return true;
            case R.id.disable:
                if (getApp().isServiceEnabled()) {
                    Log.d(TAG, "Service stopping...");

                    doUnbindService();

                    stopService(new Intent(MainActivity.this, MockService.class));

                    getApp().setServiceEnabled(false);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

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

