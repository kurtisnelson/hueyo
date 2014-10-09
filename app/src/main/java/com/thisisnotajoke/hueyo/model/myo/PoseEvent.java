package com.thisisnotajoke.hueyo.model.myo;

import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;

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
