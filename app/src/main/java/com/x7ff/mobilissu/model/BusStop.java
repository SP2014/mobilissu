package com.x7ff.mobilissu.model;

import com.google.android.gms.maps.model.LatLng;

public final class BusStop {

    private final LatLng coordinates;

    private final String title;

    private final String stopCode;

    private final String zone;

    public BusStop(LatLng coordinates, String title, String stopCode, String zone) {
        this.coordinates = coordinates;
        this.title = title;
        this.stopCode = stopCode;
        this.zone = zone;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public String getTitle() {
        return title;
    }

    public String getStopCode() {
        return stopCode;
    }

    public String getZone() {
        return zone;
    }

}
