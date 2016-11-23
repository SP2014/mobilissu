package com.x7ff.mobilissu.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;
import com.x7ff.mobilissu.activity.MapActivity;
import com.x7ff.mobilissu.model.BusLocation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class BusLocationService extends JobService {
    private static final String TAG = BusLocationService.class.getName();

    private final String API_BASE_URL = "http://lissu.tampere.fi/ajax_servers/busLocations.php?ts=";

    private MapActivity callbackActivity;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Messenger callback = intent.getParcelableExtra("messenger");
        Message message = Message.obtain();
        message.what = MapActivity.MSG_SERVICE_OBJ;
        message.obj = this;
        try {
            callback.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        jobHandler.sendMessage(Message.obtain(jobHandler, 1, jobParameters));
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "onStopJob: bus location update");
        return false;
    }

    private Handler jobHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            AsyncTask<String, Void, List<BusLocation>> asyncLocationTask = new AsyncTask<String, Void, List<BusLocation>>() {
                @Override
                protected List<BusLocation> doInBackground(String... strings) {
                    try {
                        return getLocations();
                    } catch (IOException e) {
                        return new ArrayList<>();
                    }
                }
            }.execute();

            List<BusLocation> locations = new ArrayList<>();
            try {
                locations.addAll(asyncLocationTask.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            if (callbackActivity != null) {
                callbackActivity.handleBusLocations(locations);
            }

            jobFinished((JobParameters) message.obj, false);
            return true;
        }
    });

    private List<BusLocation> getLocations() throws IOException {
        long time = System.currentTimeMillis() / 1000;
        URL url = new URL(API_BASE_URL + time);
        InputStreamReader reader = new InputStreamReader(url.openStream());
        BusLocation[] locations = new Gson().fromJson(reader, BusLocation[].class);
        return Arrays.asList(locations);
    }

    public void setCallbackActivity(MapActivity callbackActivity) {
        Log.d(TAG, "setCallbackActivity: " + callbackActivity);
        this.callbackActivity = callbackActivity;
    }



}
