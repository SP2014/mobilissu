package com.x7ff.mobilissu.event.impl;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.x7ff.mobilissu.event.EventBusAsyncTask;
import com.x7ff.mobilissu.event.EventTaskResult;
import com.x7ff.mobilissu.model.BusLocation;

import java.net.URL;
import java.util.List;
import java.util.Map;

public final class LineRouteFetchEvent extends EventBusAsyncTask<String, Void, List<String>> {
    public static final String BUS_LINE = "bus_line";

    public LineRouteFetchEvent(Map<String, Object> args) {
        super(EventTaskResult.Id.LINE_ROUTE, args);
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

    public static Map<String, Object> getArgs(String busLine) {
        Map<String, Object> args = Maps.newHashMap();
        args.put(BUS_LINE, busLine);
        return args;
    }

}
