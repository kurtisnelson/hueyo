package com.thisisnotajoke.hueyo;

import de.greenrobot.event.EventBus;

public class EventBusUtils {
    public static void register(Object object) {
        if(!EventBus.getDefault().isRegistered(object))
            EventBus.getDefault().register(object);
    }

    public static void unregister(Object object) {
        if(EventBus.getDefault().isRegistered(object))
            EventBus.getDefault().unregister(object);
    }

    public static void post(MyoEvent myoEvent) {
        EventBus.getDefault().post(myoEvent);
    }
}
