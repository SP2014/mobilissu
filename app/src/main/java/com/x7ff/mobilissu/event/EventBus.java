package com.x7ff.mobilissu.event;

import com.squareup.otto.Bus;

public final class EventBus {

    private static final Bus BUS = new Bus();

    public static Bus getBus() {
        return BUS;
    }

    private EventBus() {

    }

}
