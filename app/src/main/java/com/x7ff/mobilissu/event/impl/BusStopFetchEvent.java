package com.x7ff.mobilissu.event.impl;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.x7ff.mobilissu.event.EventBusAsyncTask;
import com.x7ff.mobilissu.event.EventTaskResult;

import java.net.URL;
import java.util.List;

public final class BusStopFetchEvent extends EventBusAsyncTask<String, Void, List<String>> {

    public BusStopFetchEvent() {
        super(EventTaskResult.Id.BUS_STOPS, Maps.newHashMap());
    }

    @Override
    protected List<String> doInBackground(String... strings) {
        try {
            URL url = new URL(strings[0]);
            return Resources.readLines(url, Charsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
