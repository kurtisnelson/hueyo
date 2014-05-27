package com.thisisnotajoke.hueyo;

import android.util.Log;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Vector3;

public class PoseConsumer {
    private final PHHueSDK mHue;
    private final PHBridge mBridge;
    private final int mSelectedLight;
    private boolean mEnabled = false;
    private long mLastChange = 0;

    public PoseConsumer(PHHueSDK hue, int selectedLight) {
        mHue = hue;
        mBridge = mHue.getSelectedBridge();
        mSelectedLight = selectedLight;
    }

    public void eat(Pose pose) {
        PHLight light = mBridge.getResourceCache().getAllLights().get(mSelectedLight);
        PHLightState lightState = light.getLastKnownLightState();
        lightState.setAlertMode(PHLight.PHLightAlertMode.ALERT_NONE);
        switch (pose.getType()) {
            case WAVE_IN:
                if(mEnabled) {
                    lightState.setOn(!lightState.isOn());
                    EventBusUtils.post(Myo.VibrationType.SHORT);
                }
                break;
            case FINGERS_SPREAD:
                if(mEnabled) {
                    switch (lightState.getEffectMode()) {
                        case EFFECT_COLORLOOP:
                            lightState.setEffectMode(PHLight.PHLightEffectMode.EFFECT_NONE);
                            break;
                        case EFFECT_NONE:
                        case EFFECT_UNKNOWN:
                            lightState.setEffectMode(PHLight.PHLightEffectMode.EFFECT_COLORLOOP);
                            break;
                    }
                    EventBusUtils.post(Myo.VibrationType.SHORT);
                }
                break;
            case FIST:
                if(!mEnabled) {
                    lightState.setAlertMode(PHLight.PHLightAlertMode.ALERT_SELECT);
                    EventBusUtils.post(Myo.VibrationType.LONG);
                    mEnabled = true;
                }else{
                    lightState.setAlertMode(PHLight.PHLightAlertMode.ALERT_NONE);
                    EventBusUtils.post(Myo.VibrationType.MEDIUM);
                    mEnabled = false;
                }
                break;
            case WAVE_OUT:
            case TWIST_IN:
            case NONE:
                return;
        }
        mBridge.setLightStateForDefaultGroup(lightState);
    }

    public void eat(Vector3 vector) {
        if (mEnabled) {
            if(mLastChange > System.currentTimeMillis() - 500)
                return;
            boolean changed = false;
            PHLight light = mBridge.getResourceCache().getAllLights().get(mSelectedLight);
            PHLightState lightState = light.getLastKnownLightState();
            //X -> twist Y -> up, down Z -> left, right
            if(Math.abs(vector.y()) > 10){
                Log.d("Myo", "up/down: " + vector.y());
                changed = true;
                if(vector.y() < 0) {
                    lightState.setBrightness(lightState.getBrightness() + 40);
                }else{
                    lightState.setBrightness(lightState.getBrightness() - 40);
                }
            }
            if(Math.abs(vector.z()) > 10){
                Log.d("Myo", "left/right: " + vector.z());
                changed = true;
                lightState.setColorMode(PHLight.PHLightColorMode.COLORMODE_HUE_SATURATION);
                if(vector.z() < 0) {
                    lightState.setHue(lightState.getHue() + 1000);
                }else{
                    lightState.setHue(lightState.getHue() - 1000);
                }
            }
            if(Math.abs(vector.x()) > 10){
                Log.d("Myo", "twist: " + vector.x());
                changed = true;
                lightState.setColorMode(PHLight.PHLightColorMode.COLORMODE_HUE_SATURATION);
                if(vector.z() < 0) {
                    lightState.setSaturation(lightState.getSaturation() + 30);
                }else{
                    lightState.setSaturation(lightState.getSaturation() - 30);
                }
            }
            if(changed){
                Log.d("Myo", "changing light state");
                mLastChange = System.currentTimeMillis();
                mBridge.setLightStateForDefaultGroup(lightState);
            }
        }
    }
}
