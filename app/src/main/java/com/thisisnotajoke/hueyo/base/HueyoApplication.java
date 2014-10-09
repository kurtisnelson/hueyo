package com.thisisnotajoke.hueyo.base;

import android.app.Application;
import android.content.Context;

import com.crittercism.app.Crittercism;
import com.thisisnotajoke.hueyo.BuildConfig;

import dagger.ObjectGraph;

public class HueyoApplication extends Application {

    private ObjectGraph mObjectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        if(!BuildConfig.DEBUG)
            Crittercism.initialize(getApplicationContext(), "5436d09eb573f1582b000006");
        mObjectGraph = ObjectGraph.create(new HueyoApplicationModule(this));
    }

    public static HueyoApplication get(Context context) {
        return (HueyoApplication) context.getApplicationContext();
    }


    public final void inject(Object object) {
        mObjectGraph.inject(object);
    }

}