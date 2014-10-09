package com.thisisnotajoke.hueyo;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;

public class PoseConsumer {
    private final PHHueSDK mHue;
    private final int mSelectedLight;
    private boolean mEnabled = false;
    private boolean mPower = true;
    private boolean mEffects = false;

    public PoseConsumer(PHHueSDK hue, int selectedLight) {
        mHue = hue;
        mSelectedLight = selectedLight;
    }

    public void eat(Pose pose) {
        PHLightState state = new PHLightState();
        switch (pose) {
            case FIST:
                if(mEnabled) {
                    state.setOn(mPower);
                    mPower = !mPower;
                    EventBusUtils.post(Myo.VibrationType.SHORT);
                }
                break;
            case THUMB_TO_PINKY:
                if(!mEnabled) {
                    state.setAlertMode(PHLight.PHLightAlertMode.ALERT_SELECT);
                    EventBusUtils.post(Myo.VibrationType.LONG);
                    mEnabled = true;
                }else{
                    EventBusUtils.post(Myo.VibrationType.LONG);
                    mEnabled = false;
                }
                break;
            default:
                return;
        }
        mHue.getSelectedBridge().setLightStateForDefaultGroup(state);
    }

    public void eat(Quaternion quat) {
        if (mEnabled) {
            PHLightState state = new PHLightState();

            double pitch = (Quaternion.pitch(quat) + 1.5) / 3.0;
            state.setBrightness((int)(pitch * 255.0));

            double yaw = Quaternion.yaw(quat);
            if(yaw <= -1.5){// -3.0...-1.5
                yaw = 0.5 + (yaw / 3.0)*0.5;
            }else if(yaw >= 1.5){// 1.5...3.0
                yaw = 0.5 - (yaw / -3.0)*0.5;
            }
            state.setHue((int) (yaw * 65535));

            double saturation = (Quaternion.roll(quat) + 1.5) / 4.0;
            state.setSaturation((int) (saturation * 255.0));

            state.setColorMode(PHLight.PHLightColorMode.COLORMODE_HUE_SATURATION);

            mHue.getSelectedBridge().setLightStateForDefaultGroup(state);
        }
    }
}
