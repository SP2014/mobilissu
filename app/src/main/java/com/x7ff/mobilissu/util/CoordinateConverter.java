package com.x7ff.mobilissu.util;

import com.google.android.gms.maps.model.LatLng;

import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;

public final class CoordinateConverter {

    private static final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
    private static final CRSFactory csFactory = new CRSFactory();
    private static final CoordinateReferenceSystem sourceCRS = csFactory.createFromName("EPSG:2393");
    private static final String WGS84_PARAM = "+title=long/lat:WGS84 +proj=longlat +ellps=WGS84 +datum=WGS84 +units=degrees";
    private static final CoordinateReferenceSystem WGS84 = csFactory.createFromParameters("WGS84", WGS84_PARAM);
    private static final CoordinateTransform transform = ctFactory.createTransform(sourceCRS, WGS84);

    private static ProjCoordinate decodeEPSG2393(String point, boolean reversed) {
        String[] parts = point.split(",");
        double x = Double.parseDouble(parts[reversed ? 0 : 1]);
        double y = Double.parseDouble(parts[reversed ? 1 : 0]);
        try {
            ProjCoordinate sourceCoordinate = new ProjCoordinate(x, y);
            ProjCoordinate destCoordinate = new ProjCoordinate();

            transform.transform(sourceCoordinate, destCoordinate);
            return destCoordinate;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ProjCoordinate getProj4JCoordinates(String point) {
        return decodeEPSG2393(point, false);
    }

    public static ProjCoordinate getProj4JCoordinates(String point, boolean reversed) {
        return decodeEPSG2393(point, reversed);
    }

    public static LatLng getGoogleMapCoordinates(String point) {
        return getGoogleMapCoordinates(point, false);
    }

    public static LatLng getGoogleMapCoordinates(String point, boolean reversed) {
        ProjCoordinate coordinate = getProj4JCoordinates(point, reversed);
        if (coordinate == null) {
            return null;
        }
        return new LatLng(coordinate.y, coordinate.x);
    }

    private CoordinateConverter() {

    }

}
