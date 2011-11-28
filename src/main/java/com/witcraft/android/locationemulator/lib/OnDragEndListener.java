package com.witcraft.android.locationemulator.lib;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

/**
 * Created by IntelliJ IDEA.
 * User: zakharov
 * Date: 11/23/11
 * Time: 8:08 AM
 * To change this template use File | Settings | File Templates.
 */
public interface OnDragEndListener {
    public void onDragEnd(MapView mapView, GeoPoint center);
}
