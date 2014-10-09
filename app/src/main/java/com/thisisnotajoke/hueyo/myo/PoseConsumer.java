package com.thisisnotajoke.hueyo.myo;

import android.os.Handler;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thisisnotajoke.hueyo.base.EventBusUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class PoseConsumer {
    private boolean mEnabled = false;
    private boolean mPower = true;

    private final PHHueSDK mHue;
    private final Observable<Pose> mPoseObservable;
    private final Observable<Quaternion> mOrientationObservable;

    private Subscription mOrientation;
    private Subscription mFist;
    private Subscription mLock;

    public PoseConsumer(PHHueSDK hue, Observable<Pose> poseObservable, Observable<Quaternion> orientationObservable) {
        mHue = hue;
        Handler handler = new Handler();
        mPoseObservable = poseObservable.subscribeOn(AndroidSchedulers.handlerThread(handler));
        mOrientationObservable = orientationObservable.subscribeOn(AndroidSchedulers.handlerThread(handler));
    }

    public void consumePoses() {
        mFist = mPoseObservable
                .filter(it -> it == Pose.FIST)
                .skipWhile(it -> !mEnabled)
                .subscribe(
                        pose -> {
                            PHLightState state = new PHLightState();
                            state.setOn(mPower);
                            mPower = !mPower;
                            EventBusUtils.post(Myo.VibrationType.SHORT);
                            mHue.getSelectedBridge().setLightStateForDefaultGroup(state);
                        }
                );

        mLock = mPoseObservable
                .filter(it -> it == Pose.THUMB_TO_PINKY)
                .subscribe(
                        pose -> {
                            if(!mEnabled) {
                                PHLightState state = new PHLightState();
                                state.setAlertMode(PHLight.PHLightAlertMode.ALERT_SELECT);
                                EventBusUtils.post(Myo.VibrationType.LONG);
                                mEnabled = true;
                                mHue.getSelectedBridge().setLightStateForDefaultGroup(state);
                            }else{
                                EventBusUtils.post(Myo.VibrationType.LONG);
                                mEnabled = false;
                            }
                        }
                );
    }

    public void consumeOrientation() {
        mOrientation = mOrientationObservable
                .sample(500, TimeUnit.MILLISECONDS)
                .skipWhile(quat -> !mEnabled)
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
        if(mLock != null) mLock.unsubscribe();
    }
}
