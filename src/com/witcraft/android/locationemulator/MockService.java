package com.witcraft.android.locationemulator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;

import java.util.ArrayList;

public class MockService extends Service {
    private NotificationManager mNM;

    private static final String TAG = "MockService";
    private int NOTIFICATION = R.string.mock_service_started;
    private Handler handler;
    private LocationManager locationManager;
    private boolean serviceEnabled;
    private ArrayList<Location> mockLocationList;
    private int pointer = 0;

    public class LocalBinder extends Binder {
        MockService getService() {
            return MockService.this;
        }
    }

    private Runnable setLocationWorker = new Runnable() {
        @Override
        public void run() {
            if (serviceEnabled) {
                handler.postDelayed(this, 5000);
            }

            if (!mockLocationList.isEmpty()) {
                Location currentLocation = mockLocationList.get(pointer);
                currentLocation.setTime(System.currentTimeMillis());

                locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocationList.get(pointer));

                if (pointer < (mockLocationList.size() - 1)) {
                    pointer++;
                } else {
                    pointer = 0;
                }
            }
        }
    };
    
    @Override
    public void onCreate() {
        mockLocationList = new ArrayList<Location>();
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        showNotification();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false, false, false, false, false, 1, 1);

        handler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceEnabled = true;

        mockLocationList.add((Location) intent.getParcelableExtra("location"));

        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        handler.post(setLocationWorker);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mNM.cancel(NOTIFICATION);

        serviceEnabled = false;

        Toast.makeText(this, R.string.mock_service_stopped, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();

    public void setNewMockLocation(Location location) {
        mockLocationList.add(location);
    }

    public void clearList() {
        mockLocationList.clear();
    }

    private void showNotification() {
        CharSequence text = getText(R.string.mock_service_started);

        Notification notification = new Notification(R.drawable.location, text,
                System.currentTimeMillis());

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        notification.setLatestEventInfo(this, getText(R.string.mock_service_label),
                       text, contentIntent);

        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        mNM.notify(NOTIFICATION, notification);
    }
}
