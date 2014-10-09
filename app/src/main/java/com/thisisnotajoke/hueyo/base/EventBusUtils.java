package com.thisisnotajoke.hueyo.base;

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

    public static void post(Object event) {
        EventBus.getDefault().post(event);
    }

    public static void postSticky(Object event) {
        EventBus.getDefault().postSticky(event);
    }

    public static Object getSticky(Class theClass) {
        return EventBus.getDefault().getStickyEvent(theClass);
    }
}
