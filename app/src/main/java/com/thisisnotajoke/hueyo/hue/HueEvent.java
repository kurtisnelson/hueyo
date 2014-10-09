package com.thisisnotajoke.hueyo.hue;

import com.philips.lighting.model.PHBridge;

public class HueEvent {
    private final boolean mConnected;
    private final String mName;

    public HueEvent(PHBridge bridge, boolean connected) {
        if(bridge != null) {
            mName = bridge.getResourceCache().getBridgeConfiguration().getIpAddress();
        }else{
            mName = null;
        }
        mConnected = connected;
    }

    public boolean isConnected() {
        return mConnected;
    }

    public String getName() {
        return mName;
    }
}
