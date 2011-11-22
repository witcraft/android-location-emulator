package com.witcraft.android.locationemulator;

import android.content.Intent;
import android.graphics.Canvas;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.android.maps.*;

public class MainActivity extends MapActivity {
    private static final String TAG = "MainActivity";
    private static final String ACTION_APPLICATION_DEVELOPMENT_SETTINGS = "com.android.settings.APPLICATION_DEVELOPMENT_SETTINGS";
    private MapView mapView;
    private MapController mapController;
    private LocationManager locationManager;
    private ToggleButton toggleEmulationBtn;

    private CompoundButton.OnCheckedChangeListener onToggleEmulationBtnCheckedChanged = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean cheked) {
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, cheked);
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        this.mapView = (MapView) findViewById(R.id.map_view);
        this.mapController = mapView.getController();

        mapController.setZoom(12);

        this.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        this.toggleEmulationBtn = (ToggleButton) findViewById(R.id.toggle_emulation_btn);
        this.toggleEmulationBtn.setOnCheckedChangeListener(onToggleEmulationBtnCheckedChanged);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkMockSettings();


    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    private void checkMockSettings() {
        if (Settings.Secure.getString(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0")) {
            Intent settings = new Intent(ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
            startActivity(settings);

            Toast.makeText(this, "Please enable mock locations", Toast.LENGTH_LONG).show();

        }
    }

//    private class OnMoveOverlay extends Overlay {
//        private GeoPoint location = new GeoPoint(0, 0);
//        private GeoPoint currentLocation;
//
//        protected boolean isMapMoving = false;
//
//        public OnMoveOverlay() {
//            super();
//        }
//
//        @Override
//        public boolean onTouchEvent(MotionEvent motionEvent, com.google.android.maps.MapView mapView) {
//
//            if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
//                // Added to example to make more complete
//                isMapMoving = true;
//            }
//
//            return false;
//        }
//
//        @Override
//        public boolean draw(Canvas canvas, com.google.android.maps.MapView mapView, boolean shadow, long l) {
//            if (!shadow) {
//                if (isMapMoving) {
//
//
//                    currentLocation = mapView.getMapCenter();
//                    if (currentLocation.equals(location)) {
//                        isMapMoving = false;
//
//                        locationBtn.setChecked(false);
//
//                        new GeocodeFromLocationTask(currentLocation).execute();
//                    } else {
//                        location = currentLocation;
//                    }
//                }
//            }
//
//            return super.draw(canvas, mapView, shadow, l);
//        }
//    }

    private class OnMoveOverlay extends Overlay
    {

        private  GeoPoint lastLatLng = new GeoPoint(0, 0);
        private  GeoPoint currentLatLng;

        protected boolean isMapMoving = false;

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent, MapView mapView) {
            super.onTouchEvent(motionEvent, mapView);

            if (motionEvent.getAction() == MotionEvent.ACTION_UP)
            {
                // Added to example to make more complete
                isMapMoving = true;
            }

            return false;
        }


        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow)
        {
            if (!shadow)
            {
                if (isMapMoving)
                {
                    currentLatLng = mapView.getProjection().fromPixels(0, 0);
                    if (currentLatLng.equals(lastLatLng))
                    {
                        isMapMoving = false;
                        eventListener.mapMovingFinishedEvent();
                    }
                    else
                    {
                        lastLatLng = currentLatLng;
                    }
                }
            }
        }
    }

}

