package com.thisisnotajoke.hueyo.base;

import android.app.Activity;
import android.os.Bundle;

public abstract class BaseActivity extends Activity {

    public String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HueyoApplication.get(this).inject(this);
    }

}