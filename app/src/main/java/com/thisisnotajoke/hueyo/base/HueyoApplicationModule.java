package com.thisisnotajoke.hueyo.base;

import android.content.Context;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.thalmic.myo.Hub;
import com.thisisnotajoke.hueyo.HueyoService;
import com.thisisnotajoke.hueyo.PreferenceUtil;
import com.thisisnotajoke.hueyo.StatusActivity;
import com.thisisnotajoke.hueyo.myo.PoseConsumer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = {
            StatusActivity.class,
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
    public PoseConsumer providesPoseConsumer(PHHueSDK hueSDK) {
        return new PoseConsumer(hueSDK);
    }
}