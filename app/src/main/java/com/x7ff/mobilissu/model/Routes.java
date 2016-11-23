package com.x7ff.mobilissu.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public final class Routes {

    private final Route route;

    private final Route returnRoute;

    public Routes(Route route, Route returnRoute) {
        this.route = route;
        this.returnRoute = returnRoute;
    }

    public Route getRoute() {
        return route;
    }

    public Route getReturnRoute() {
        return returnRoute;
    }

    public boolean identicalRoutes() {
        Iterator<LatLng> routeIterator = route.points.iterator();
        ListIterator<LatLng> returnRouteIterator = returnRoute.points.listIterator(returnRoute.points.size());

        boolean identical = true;

        while (routeIterator.hasNext() && returnRouteIterator.hasPrevious()) {
            if (routeIterator.next().equals(returnRouteIterator.previous())) {
                identical = false;
            }
        }

        return identical;
    }

    public static class Route {

        private final List<LatLng> points;

        private Polyline polyline;

        public Route(List<LatLng> points) {
            this.points = points;
        }

        public List<LatLng> getPoints() {
            return points;
        }

        public Polyline getPolyline() {
            return polyline;
        }

        public void setPolyline(Polyline polyline) {
            this.polyline = polyline;
        }

    }

}
