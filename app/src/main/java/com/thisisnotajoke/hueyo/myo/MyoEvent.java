package com.thisisnotajoke.hueyo.myo;

import com.thalmic.myo.Myo;

public class MyoEvent {
    private final String mAddress;
    private final Myo.ConnectionState mState;

    public MyoEvent(Myo myo) {
        mAddress = myo.getMacAddress();
        mState = myo.getConnectionState();
    }

    public Myo.ConnectionState getState() {
        return mState;
    }

    public String getAddress() {
        return mAddress;
    }
}
