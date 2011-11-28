package com.witcraft.android.locationemulator.lib;

import com.google.android.maps.GeoPoint;


public class GeoPointExtensions {
    public static double getLatitude(GeoPoint point) {
        return point.getLatitudeE6() / 1000000.;
    }

    public static double getLongitude(GeoPoint point) {
        return point.getLongitudeE6() / 1000000.;
    }

    public static GeoPoint getNewCenter(GeoPoint... points) {
        int maxLat = points[0].getLatitudeE6();
        int maxLng = points[0].getLongitudeE6();
        int minLat = points[0].getLatitudeE6();
        int minLng = points[0].getLongitudeE6();

        for (GeoPoint point : points) {
            if (point.getLatitudeE6() > maxLat) {
                maxLat = point.getLatitudeE6();
            }

            if (point.getLongitudeE6() > maxLng) {
                maxLng = point.getLongitudeE6();
            }

            if (point.getLatitudeE6() < minLat) {
                minLat = point.getLatitudeE6();
            }

            if (point.getLongitudeE6() < minLng) {
                minLng = point.getLongitudeE6();
            }
        }

        return new GeoPoint((maxLat + minLat) / 2, (maxLng + minLng) / 2);
    }
}
