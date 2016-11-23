package com.x7ff.mobilissu.event;

import android.os.AsyncTask;

import java.util.Map;

public abstract class EventBusAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    private final EventTaskResult.Id id;

    private final Map<String, Object> args;

    public EventBusAsyncTask(EventTaskResult.Id id, Map<String, Object> args) {
        this.id = id;
        this.args = args;
    }

    @Override
    protected void onPostExecute(Result result) {
        EventBus.getBus().post(new EventTaskResult<>(id, result, args));
    }

}
