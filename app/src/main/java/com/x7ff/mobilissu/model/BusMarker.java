package com.x7ff.mobilissu.model;

import com.google.android.gms.maps.model.Marker;

public final class BusMarker {

    private final BusLocation busLocation;

    private final Marker marker;

    public BusMarker(BusLocation busLocation, Marker marker) {
        this.busLocation = busLocation;
        this.marker = marker;
    }

    public BusLocation getBusLocation() {
        return busLocation;
    }

    public Marker getMarker() {
        return marker;
    }

}
