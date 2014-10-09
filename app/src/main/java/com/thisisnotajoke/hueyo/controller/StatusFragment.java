package com.thisisnotajoke.hueyo.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thisisnotajoke.hueyo.R;
import com.thisisnotajoke.hueyo.view.StatusView;
import com.thisisnotajoke.hueyo.base.EventBusUtils;
import com.thisisnotajoke.hueyo.model.hue.HueAuthEvent;
import com.thisisnotajoke.hueyo.model.hue.HueEvent;
import com.thisisnotajoke.hueyo.model.myo.MyoEvent;
import com.thisisnotajoke.hueyo.model.myo.PoseEvent;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class StatusFragment extends Fragment {

    private static final String TAG = "StatusFragment";
    private static final String STATE_MYO_MAC = "MyoMac";
    private static final String STATE_MYO_ACTIVE = "MyoActive";
    private static final String STATE_HUE_ACTIVE = "HueActive";
    private static final String STATE_HUE_NAME = "HueName";
    private String mMyoMac;

    @InjectView(R.id.fragment_status_myo_address)
    protected TextView mMyoAddressView;
    @InjectView(R.id.fragment_status_myo_status)
    protected StatusView mMyoStatus;
    @InjectView(R.id.fragment_status_hue_status)
    protected StatusView mHueStatus;
    @InjectView(R.id.fragment_status_hue_name)
    protected TextView mHueName;
    @InjectView(R.id.fragment_status_last_command)
    protected TextView mLastCommandView;

    @Override
    public void onStart() {
        super.onStart();
        EventBusUtils.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status, container, false);
        ButterKnife.inject(this, view);

        if(savedInstanceState != null) {
            setMyoMac(savedInstanceState.getString(STATE_MYO_MAC, null));
            setHueName(savedInstanceState.getString(STATE_HUE_NAME, null));
            mMyoStatus.setActive(savedInstanceState.getBoolean(STATE_MYO_ACTIVE, false));
            mHueStatus.setActive(savedInstanceState.getBoolean(STATE_HUE_ACTIVE, false));
        }
        onEventMainThread((MyoEvent) EventBusUtils.getSticky(MyoEvent.class));
        onEventMainThread((HueEvent) EventBusUtils.getSticky(HueEvent.class));
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_MYO_MAC, mMyoMac);
        outState.putString(STATE_HUE_NAME, mHueName.getText().toString());
        outState.putBoolean(STATE_MYO_ACTIVE, mMyoStatus.isActive());
        outState.putBoolean(STATE_HUE_ACTIVE, mHueStatus.isActive());
    }

    @OnClick(R.id.fragment_status_stop)
    public void quit() {
        ((StatusActivity) getActivity()).quit();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBusUtils.unregister(this);
    }

    public static StatusFragment newInstance() {
        return new StatusFragment();
    }

    public void onEventMainThread(PoseEvent poseEvent){
        mLastCommandView.setText(poseEvent.getPose().toString());
    }

    public void onEventMainThread(MyoEvent myoEvent) {
        if(myoEvent == null)
            return;
        Log.d(TAG, "MyoEvent: " + myoEvent.getAddress() + " " + myoEvent.getState());
        switch (myoEvent.getState()) {
            case CONNECTED:
                mMyoStatus.setActive(true);
                setMyoMac(myoEvent.getAddress());
                break;
            case DISCONNECTED:
                mMyoStatus.setActive(false);
                setMyoMac(null);
                break;
        }
    }

    public void onEventMainThread(HueAuthEvent event) {
        startActivity(new Intent(getActivity(), PHPushlinkActivity.class));
    }

    public void onEventMainThread(HueEvent event) {
        if(event == null)
            return;
        if(event.isConnected()){
            mHueStatus.setActive(true);
            setHueName(event.getName());
        } else{
            mHueStatus.setActive(false);
            setHueName(null);
        }

    }

    private void setMyoMac(String myoMac) {
        this.mMyoMac = myoMac;
        if(mMyoMac != null) {
            mMyoAddressView.setText(mMyoMac);
        }else {
            mMyoAddressView.setText(R.string.no_paired_myo);
        }
    }

    private void setHueName(String hueName) {
       if(hueName != null) {
           mHueName.setText(hueName);
       }else {
           mHueName.setText(R.string.no_paired_hue);
       }
    }
}
