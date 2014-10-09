package com.thisisnotajoke.hueyo;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;

import rx.Observable;
import rx.Observer;
import rx.subscriptions.Subscriptions;

public class ObservableFactory {

    public static Observable<Pose> fromPosesOf(final Hub hub) {
        return Observable.create((Observer<? super Pose> observer) -> {
            final DeviceListener listener = new AbstractDeviceListener() {
                @Override
                public void onPose(Myo myo, long timestamp, Pose pose) {
                    observer.onNext(pose);
                }
            };
            hub.addListener(listener);

            return Subscriptions.create(() -> hub.removeListener(listener));
        });
    }

    public static Observable<Quaternion> fromOrientationOf(final Hub hub) {
        return Observable.create((Observer<? super Quaternion> observer) -> {
            final DeviceListener listener = new AbstractDeviceListener() {
                @Override
                public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
                    observer.onNext(rotation);
                }
            };
            hub.addListener(listener);

            return Subscriptions.create(() -> hub.removeListener(listener));
        });
    }
}
