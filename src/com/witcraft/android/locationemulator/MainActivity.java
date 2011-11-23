package com.witcraft.android.locationemulator;

import android.content.Intent;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.android.maps.*;
import com.witcraft.android.locationemulator.lib.LocationExtensions;
import com.witcraft.android.locationemulator.lib.OnDragEndListener;
import com.witcraft.android.locationemulator.lib.OnDragOverlay;

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

    private OnDragEndListener onMapViewDragEnd = new OnDragEndListener() {
        @Override
        public void onDragEnd(MapView mapView, GeoPoint center) {

            Location loc = LocationExtensions.fromGeoPoint(center);

            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, loc);
            
            Log.d(TAG, "Mock location set");
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
        this.mapView.getOverlays().add(new OnDragOverlay(onMapViewDragEnd));

        this.mapController = mapView.getController();

        mapController.setZoom(12);

        this.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        this.locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false, false, true, true, true, 0, 5);

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

}

