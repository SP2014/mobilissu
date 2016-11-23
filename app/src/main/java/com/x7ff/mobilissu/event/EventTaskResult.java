package com.x7ff.mobilissu.event;

import java.util.Map;

public class EventTaskResult<R> {
    public enum Id {
        BUS_STOPS, LINE_ROUTE
    }

    private final Id id;
    private final R result;
    private final Map<String, Object> args;

    EventTaskResult(Id id, R result, Map<String, Object> args) {
        this.id = id;
        this.result = result;
        this.args = args;
    }

    public Id getId() {
        return id;
    }

    public R getResult() {
        return result;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    @SuppressWarnings("unchecked")
    public <T> T getArg(String key) {
        return (T) args.get(key);
    }

}
