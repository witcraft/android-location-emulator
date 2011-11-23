package com.witcraft.android.locationemulator.lib;

import android.graphics.Canvas;
import android.view.MotionEvent;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;


public class OnDragOverlay extends Overlay {

    private GeoPoint lastLatLng = new GeoPoint(0, 0);
    private GeoPoint currentLatLng;

    protected boolean isMapMoving = false;
    private OnDragEndListener dragEndListener;

    public OnDragOverlay(OnDragEndListener dragEndListener) {
        this.dragEndListener = dragEndListener;
    }


    @Override
    public boolean onTouchEvent(MotionEvent motionEvent, MapView mapView) {
        super.onTouchEvent(motionEvent, mapView);

        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            // Added to example to make more complete
            isMapMoving = true;
        }

        return false;
    }


    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (!shadow) {
            if (isMapMoving) {
                currentLatLng = mapView.getProjection().fromPixels(0, 0);
                if (currentLatLng.equals(lastLatLng)) {
                    isMapMoving = false;
                    dragEndListener.onDragEnd(mapView, mapView.getMapCenter());
                } else {
                    lastLatLng = currentLatLng;
                }
            }
        }
    }
}