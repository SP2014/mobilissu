package com.x7ff.mobilissu.activity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.squareup.otto.Subscribe;
import com.x7ff.mobilissu.R;
import com.x7ff.mobilissu.event.EventBus;
import com.x7ff.mobilissu.event.EventTaskResult;
import com.x7ff.mobilissu.event.impl.BusStopFetchEvent;
import com.x7ff.mobilissu.event.impl.LineRouteFetchEvent;
import com.x7ff.mobilissu.model.BusLocation;
import com.x7ff.mobilissu.model.BusMarker;
import com.x7ff.mobilissu.model.BusStop;
import com.x7ff.mobilissu.model.Routes;
import com.x7ff.mobilissu.service.BusLocationService;
import com.x7ff.mobilissu.util.CoordinateConverter;

import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback {
    private static final String TAG = MapActivity.class.getName();
    public static final int MSG_SERVICE_OBJ = 2;

    private static final String BUS_STOP_DATA_URL = "http://lissu.tampere.fi/ajax_servers/getStopData.php";
    private static final String LINE_ROUTE_URL = "http://lissu.tampere.fi/ajax_servers/routeCoords.php?line=";

    private BusLocationService locationService;

    private GoogleMap googleMap;
    private List<BusStop> busStops;
    private final Map<String, BusMarker> markers = Maps.newHashMap();
    private final Map<String, Routes> routeLines = Maps.newHashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment_map);
        mapFragment.getMapAsync(this);

        Intent locationService = new Intent(this, BusLocationService.class);
        locationService.putExtra("messenger", new Messenger(handler));
        startService(locationService);

        EventBus.getBus().register(this);
    }

    @Override
    protected void onDestroy() {
        EventBus.getBus().unregister(this);
        super.onDestroy();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SERVICE_OBJ:
                    locationService = (BusLocationService) msg.obj;
                    locationService.setCallbackActivity(MapActivity.this);
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("followedLine", followedLine);
        outState.putString("lastFollowedLine", lastFollowedLine);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        followedLine = savedInstanceState.getString("followedLine", null);
        if (followedLine != null) {
            showRoute(followedLine);
        }
        lastFollowedLine = savedInstanceState.getString("lastFollowedLine", null);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        this.googleMap = googleMap;
        scheduleBusLocationUpdate();

        new BusStopFetchEvent().execute(BUS_STOP_DATA_URL);

        googleMap.setOnMapClickListener(latLng -> {
            if (followedLine != null) {
                removeLines(followedLine);
            }
            lastFollowedLine = followedLine;
            followedLine = null;
        });

        googleMap.setOnMarkerClickListener(mapMarker -> {
            BusMarker marker = findBusMarker(mapMarker);
            if (marker == null) {
                return false;
            }

            BusLocation busLocation = marker.getBusLocation();
            removeLines(lastFollowedLine);
            followedLine = busLocation.getLineCode();
            lastFollowedLine = followedLine;

            // todo: aint working
            if (mapMarker.isInfoWindowShown()) {
                mapMarker.hideInfoWindow();
                removeLines(followedLine);
                return true;
            }

            mapMarker.showInfoWindow();
            showRoute(followedLine);
            return true;
        });
    }

    private String followedLine;
    private String lastFollowedLine;

    private BusMarker findBusMarker(Marker marker) {
        for (Map.Entry<String, BusMarker> entry : markers.entrySet()) {
            BusMarker busMarker = entry.getValue();
            if (busMarker.getMarker().equals(marker)) {
                return busMarker;
            }
        }
        return null;
    }

    private void scheduleBusLocationUpdate() {
        scheduleBusLocationUpdate(0);
    }

    private void scheduleBusLocationUpdate(int count) {
        if (count >= 10) {
            Log.e(TAG, "Failed to schedule bus location update 10 times, auto updating won't be available");
            // todo: enable manual updating feature here (refresh in actionbar)
            return;
        }

        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(getPackageName(), BusLocationService.class.getName()));
        builder.setPeriodic(3000);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        if (jobScheduler.schedule(builder.build()) <= 0) {
            Log.d("NOPE", "schedule failed");
            SystemClock.sleep(2000);
            scheduleBusLocationUpdate(++count);
        }
    }

    public void handleBusLocations(List<BusLocation> locations) {
        for (BusLocation busLocation : locations) {
            BusMarker busMarker = markers.get(busLocation.getJourneyId());
            Marker marker = null;
            if (busMarker != null) {
                marker = busMarker.getMarker();
            }
            LatLng position = new LatLng(busLocation.getY(), busLocation.getX());
            if (busMarker == null) {
                marker = googleMap.addMarker(new MarkerOptions()
                        .position(position)
                        .flat(true)
                        .icon(busIcon(R.drawable.bus_marker, busLocation.getLineCode()))
                        .title(busLocation.getLineCode()));
                Log.d(TAG, "add marker to position: " + position);
            }
            marker.setPosition(position);
            markers.put(busLocation.getJourneyId(), new BusMarker(busLocation, marker));
        }
        if (followedLine == null) {
            removeLines(lastFollowedLine);
        }
    }

    public BitmapDescriptor busIcon(int drawableId, String text) {
        Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(25);

        float textWidth = paint.measureText(text);
        float height = bm.getHeight() / 2;

        Canvas canvas = new Canvas(bm);
        canvas.drawText(text, (bm.getWidth() / 2) - (textWidth / 2), height + (height / 6), paint);

        return BitmapDescriptorFactory.fromBitmap(bm);
    }

    @Subscribe
    public void onEventTaskResult(EventTaskResult<List<String>> result) {
        switch (result.getId()) {
            case BUS_STOPS:
                parseStops(result.getResult());
                break;
            case LINE_ROUTE:
                parseRoutes(result);
                break;
        }
    }

    private void parseRoutes(EventTaskResult<List<String>> result) {
        String busLine = result.getArg(LineRouteFetchEvent.BUS_LINE);
        List<String> lines = result.getResult();
        if (lines.isEmpty()) {
            return;
        }
        String line = lines.get(0);
        String[] routes = line.split("\\|");

        Routes.Route lineRoute = null;
        Routes.Route lineReturningRoute = null;
        for (int i = 0; i < 2; i++) {
            String route = routes[i];
            String[] points = route.split(" ");
            boolean returning = i == 1;

            List<LatLng> pts = Lists.newArrayList();
            for (String point : points) {
                pts.add(CoordinateConverter.getGoogleMapCoordinates(point, true));
            }

            Routes.Route r = new Routes.Route(pts);
            if (!returning) {
                lineReturningRoute = r;
            } else {
                lineRoute = r;
            }
        }
        routeLines.put(busLine, new Routes(lineRoute, lineReturningRoute));
        showRoute(busLine);
    }

    private void showRoute(String line) {
        Routes routes = routeLines.get(line);
        if (routes == null) {
            new LineRouteFetchEvent(LineRouteFetchEvent.getArgs(line)).execute(LINE_ROUTE_URL + line);
            return;
        }
        drawRoute(routes.getRoute(), false);
        if (!routes.identicalRoutes()) {
            drawRoute(routes.getReturnRoute(), true);
        }
    }

    private void drawRoute(Routes.Route route, boolean returning) {
        PolylineOptions rectLine = new PolylineOptions()
                .width(10)
                .color(returning ? Color.MAGENTA : Color.BLUE);

        for (LatLng point : route.getPoints()) {
            rectLine.add(point);
        }

        Polyline polyline = googleMap.addPolyline(rectLine);
        route.setPolyline(polyline);
    }

    private void removeLines(String line) {
        Routes routes = routeLines.get(line);
        if (routes == null) {
            return;
        }
        Polyline polyline = routes.getRoute().getPolyline();
        if (polyline != null) {
            polyline.remove();
        }
        polyline = routes.getReturnRoute().getPolyline();
        if (polyline != null) {
            polyline.remove();
        }
    }

    private void parseStops(List<String> lines) {
        if (lines.isEmpty() || lines.size() <= 1) {
            return;
        }
        lines.remove(0); // instruction line
        // point	title	lines	icon	iconSize	iconOffset	stopCode	zone
        busStops = Lists.newArrayList();
        for (String line : lines) {
            String[] parts = line.split("\t");
            int idx = 0;
            String point = parts[idx++];
            String title = parts[idx++];
            String busLines = parts[idx++];
            String icon = parts[idx++];
            String iconSize = parts[idx++];
            String iconOffset = parts[idx++];
            String stopCode = parts[idx++];
            String zone = parts[idx];

            LatLng coordinates = CoordinateConverter.getGoogleMapCoordinates(point);
            if (coordinates == null) {
                continue;
            }

            busStops.add(new BusStop(coordinates, title, stopCode, zone));
        }
    }

}
