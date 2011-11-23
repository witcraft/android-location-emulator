package com.witcraft.android.locationemulator.lib;


import android.location.Location;
import android.location.LocationManager;
import com.google.android.maps.GeoPoint;

public class LocationExtensions {

    private static final float HIGH_ACCURACY = 400;
    private static final int SIGNIFICANT_PAUSE = 1000 * 60 * 2;

    /**
     * Determines whether one LatLng reading is better than the current LatLng fix
     *
     * @param currentBestLocation The current LatLng fix, to which you want to compare the new one
     * @param location            The new LatLng that you want to evaluate
     */
    public static boolean isBetterLocation(Location currentBestLocation, Location location) {
        if (location == null) {
            // An old is always better than no location
            return false;
        }

        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > SIGNIFICANT_PAUSE;
        boolean isSignificantlyOlder = timeDelta < -SIGNIFICANT_PAUSE;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta <= 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > HIGH_ACCURACY;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    public static boolean isHighAccuracy(Location location) {
        if (location.getAccuracy() < HIGH_ACCURACY && location.getAccuracy() > 0) {
            return true;
        }

        return false;
    }

    public static GeoPoint getGeoPoint(Location location) {
        return new GeoPoint(
                (int) (location.getLatitude() * 1000000),
                (int) (location.getLongitude() * 1000000));
    }

    public static Location fromGeoPoint(GeoPoint geoPoint) {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(GeoPointExtensions.getLatitude(geoPoint));
        location.setLongitude(GeoPointExtensions.getLongitude(geoPoint));
        return location;
    }

    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

}
