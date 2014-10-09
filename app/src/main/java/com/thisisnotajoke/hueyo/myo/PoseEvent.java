package com.thisisnotajoke.hueyo.myo;

import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thisisnotajoke.hueyo.myo.MyoEvent;

public class PoseEvent extends MyoEvent {
    private final Pose mPose;

    public PoseEvent(Myo myo, Pose pose) {
        super(myo);
        mPose = pose;
    }

    public Pose getPose() {
        return mPose;
    }
}
