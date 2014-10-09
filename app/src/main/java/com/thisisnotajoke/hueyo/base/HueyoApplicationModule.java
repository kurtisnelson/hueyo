package com.thisisnotajoke.hueyo.base;

import android.content.Context;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thisisnotajoke.hueyo.controller.HueyoService;
import com.thisisnotajoke.hueyo.controller.PHPushlinkActivity;
import com.thisisnotajoke.hueyo.model.ObservableFactory;
import com.thisisnotajoke.hueyo.controller.StatusActivity;
import com.thisisnotajoke.hueyo.model.PreferenceUtil;
import com.thisisnotajoke.hueyo.model.myo.PoseConsumer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.Observable;

@Module(
        injects = {
            StatusActivity.class,
            PHPushlinkActivity.class,
            HueyoService.class,
        }
        , complete = true)
public class HueyoApplicationModule {

    private final HueyoApplication mApplication;
    protected Context mContext;

    public HueyoApplicationModule(HueyoApplication application) {
        mApplication = application;
        mContext = application.getApplicationContext();
    }

    @Provides
    public Context provideContext() {
        return mContext;
    }

    @Provides
    @Singleton
    public PreferenceUtil providesPreferenceUtil(Context context) {
        return new PreferenceUtil(context);
    }

    @Provides
    @Singleton
    public PHHueSDK providesHueSdk() {
        return PHHueSDK.getInstance();
    }

    @Provides
    @Singleton
    public Hub providesMyoHub() {
        return Hub.getInstance();
    }

    @Provides
    public Observable<Pose> providesPoseObservable(Hub hub) {
        return ObservableFactory.fromPosesOf(hub);
    }

    @Provides
    public Observable<Quaternion> providesOrientationObservable(Hub hub) {
        return ObservableFactory.fromOrientationOf(hub);
    }

    @Provides
    public PoseConsumer providesPoseConsumer(PHHueSDK hueSDK, Observable<Pose> poseObservable, Observable<Quaternion> orientationObservable) {
        return new PoseConsumer(hueSDK, poseObservable, orientationObservable);
    }
}