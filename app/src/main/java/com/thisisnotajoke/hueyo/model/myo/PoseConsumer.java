package com.thisisnotajoke.hueyo.model.myo;

import android.os.Handler;
import android.util.Log;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thisisnotajoke.hueyo.base.EventBusUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class PoseConsumer {
    private static final String TAG = "PoseConsumer";
    private boolean mPower = true;

    private final PHHueSDK mHue;
    private final Observable<Pose> mPoseObservable;
    private final Observable<Quaternion> mOrientationObservable;

    private Subscription mOrientation;
    private Subscription mFist;

    public PoseConsumer(PHHueSDK hue, Observable<Pose> poseObservable, Observable<Quaternion> orientationObservable) {
        mHue = hue;
        mPoseObservable = poseObservable;
        mOrientationObservable = orientationObservable;
    }

    public void consumePoses(Handler handler) {
        mFist = mPoseObservable
                .subscribeOn(AndroidSchedulers.handlerThread(handler))
                .filter(it -> it == Pose.FIST)
                .subscribe(
                        pose -> {
                            Log.i(TAG, "Toggling power");
                            PHLightState state = new PHLightState();
                            state.setOn(mPower);
                            mPower = !mPower;
                            mHue.getSelectedBridge().setLightStateForDefaultGroup(state);
                        }
                );
    }

    public void consumeOrientation(Handler handler) {
        mOrientation = mOrientationObservable
                .subscribeOn(AndroidSchedulers.handlerThread(handler))
                .sample(800, TimeUnit.MILLISECONDS)
                .subscribe(
                        quat -> {
                            PHLightState state = new PHLightState();

                            double pitch = (Quaternion.pitch(quat) + 1.5) / 3.0;
                            state.setBrightness((int) (pitch * 255.0));

                            double yaw = Quaternion.yaw(quat);
                            if (yaw <= -1.5) {// -3.0...-1.5
                                yaw = 0.5 + (yaw / 3.0) * 0.5;
                            } else if (yaw >= 1.5) {// 1.5...3.0
                                yaw = 0.5 - (yaw / -3.0) * 0.5;
                            }
                            state.setHue((int) (yaw * 65535));

                            double saturation = (Quaternion.roll(quat) + 1.5) / 4.0;
                            state.setSaturation((int) (saturation * 255.0));

                            state.setColorMode(PHLight.PHLightColorMode.COLORMODE_HUE_SATURATION);

                            mHue.getSelectedBridge().setLightStateForDefaultGroup(state);
                        }
                )
        ;
    }

    public void disconnect() {
        if(mOrientation != null) mOrientation.unsubscribe();
        if(mFist != null) mFist.unsubscribe();
    }
}
