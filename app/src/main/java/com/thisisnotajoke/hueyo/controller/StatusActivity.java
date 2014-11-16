package com.thisisnotajoke.hueyo.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.thisisnotajoke.hueyo.R;
import com.thisisnotajoke.hueyo.base.BaseActivity;
import com.thisisnotajoke.hueyo.base.EventBusUtils;
import com.thisisnotajoke.hueyo.model.hue.HueEvent;

public class StatusActivity extends BaseActivity {
    private static final String STATE_LIGHTS = "LightsEnabled";
    private boolean mIsBound;
    private HueyoService mBoundService;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((HueyoService.LocalBinder)service).getService();
            mBoundService.pair();
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
        }
    };
    private FragmentPageAdapter mAdapter;
    private ViewPager mPager;
    private boolean mLightsEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        EventBusUtils.register(this);

        if(savedInstanceState != null){
            mLightsEnabled = savedInstanceState.getBoolean(STATE_LIGHTS);
        }
        mAdapter = new FragmentPageAdapter(getSupportFragmentManager());

        mPager = (ViewPager) findViewById(R.id.activity_status_pager);
        mPager.setAdapter(mAdapter);

        startService(new Intent(this, HueyoService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(this, HueyoService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBusUtils.unregister(this);
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_LIGHTS, mLightsEnabled);
    }

    public void onEventMainThread(HueEvent e){
        if(e.isConnected()) {
            mLightsEnabled = true;
        } else {
            mLightsEnabled = false;
        }
        mAdapter.notifyDataSetChanged();
    }

    public void quit() {
        if(mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
        stopService(new Intent(this, HueyoService.class));
        finish();
    }

    private class FragmentPageAdapter extends FragmentStatePagerAdapter {
        public FragmentPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            if(mLightsEnabled){
                return 2;
            }else{
                return 1;
            }
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return StatusFragment.newInstance();
                case 1:
                    return LightsFragment.newInstance();
            }
            return null;
        }
    }
}
