package com.thisisnotajoke.hueyo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class StatusFragment extends Fragment {

    private static final String TAG = "StatusFragment";
    private static final String STATE_MYO_MAC = "MyoMac";
    private static final String STATE_MYO_ACTIVE = "MyoActive";
    private static final String STATE_HUE_ACTIVE = "HueActive";
    private static final String STATE_HUE_NAME = "HueName";
    private String mMyoMac;
    private TextView mMyoAddressView;
    private Button mTrainButton;
    private StatusView mMyoStatus;
    private StatusView mHueStatus;
    private TextView mHueName;

    @Override
    public void onStart() {
        super.onStart();
        EventBusUtils.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status, container, false);
        mMyoStatus = (StatusView) view.findViewById(R.id.fragment_status_myo_status);
        mMyoAddressView = (TextView) view.findViewById(R.id.fragment_status_myo_address);
        mTrainButton = (Button) view.findViewById(R.id.fragment_status_train);
        mTrainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HueyoService.train(getActivity(), mMyoMac);
            }
        });
        mHueStatus = (StatusView) view.findViewById(R.id.fragment_status_hue_status);
        mHueName = (TextView) view.findViewById(R.id.fragment_status_hue_name);

        if(savedInstanceState != null) {
            setMyoMac(savedInstanceState.getString(STATE_MYO_MAC, null));
            setHueName(savedInstanceState.getString(STATE_HUE_NAME, null));
            mMyoStatus.setActive(savedInstanceState.getBoolean(STATE_MYO_ACTIVE, false));
            mHueStatus.setActive(savedInstanceState.getBoolean(STATE_HUE_ACTIVE, false));
        }
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

    @Override
    public void onStop() {
        super.onStop();
        EventBusUtils.unregister(this);
    }

    public static StatusFragment newInstance() {
        return new StatusFragment();
    }

    public void onEventMainThread(PoseEvent poseEvent){
        Log.d(TAG, "PoseEvent: " + poseEvent.getPose());
        Toast.makeText(getActivity(), poseEvent.getPose().toString(), Toast.LENGTH_SHORT).show();
    }

    public void onEventMainThread(MyoEvent myoEvent) {
        Log.d(TAG, "MyoEvent: " + myoEvent.getAddress() + " " + myoEvent.getState());
        switch (myoEvent.getState()) {
            case CONNECTED:
                mMyoStatus.setActive(true);
                mTrainButton.setEnabled(true);
                setMyoMac(myoEvent.getAddress());
                break;
            case DISCONNECTED:
                mMyoStatus.setActive(false);
                mTrainButton.setEnabled(false);
                setMyoMac(null);
                break;
        }
    }

    public void onEventMainThread(HueAuthEvent event) {
        Toast.makeText(getActivity(), R.string.hue_push_link, Toast.LENGTH_LONG).show();
    }

    public void onEventMainThread(HueEvent event) {
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
