package com.thisisnotajoke.hueyo;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;

public class PoseConsumer {
    private final PHHueSDK mHue;
    private boolean mEnabled = false;

    public PoseConsumer(PHHueSDK hue) {
        mHue = hue;
    }

    public void eat(int lightId, Pose pose) {
        PHBridge bridge = mHue.getSelectedBridge();
        PHLight light = bridge.getResourceCache().getAllLights().get(lightId);
        PHLightState lightState = light.getLastKnownLightState();
        switch (pose.getType()){
            case WAVE_IN:
                lightState = new PHLightState();
                lightState.setOn(false);
                break;
            case WAVE_OUT:
                lightState = new PHLightState();
                lightState.setOn(true);
                break;
            case FINGERS_SPREAD:
                mEnabled = true;
                lightState.setAlertMode(PHLight.PHLightAlertMode.ALERT_SELECT);
                break;
            case FIST:
                if(mEnabled) {
                    lightState.setEffectMode(PHLight.PHLightEffectMode.EFFECT_NONE);
                    lightState.setAlertMode(PHLight.PHLightAlertMode.ALERT_SELECT);
                }
                mEnabled = false;
                break;
            case TWIST_IN:
                lightState.setEffectMode(PHLight.PHLightEffectMode.EFFECT_COLORLOOP);
            case NONE:
                return;
        }
        bridge.updateLightState(light, lightState);
    }

    public void eat(int lightId, Quaternion quat){
    }
}
