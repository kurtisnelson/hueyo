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
    private static final String STATE_MAC = "Mac";
    private String mMyoMac;
    private TextView mMacTextView;
    private Button mTrainButton;

    @Override
    public void onStart() {
        super.onStart();
        EventBusUtils.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status, container, false);
        mMacTextView = (TextView) view.findViewById(R.id.fragment_status_mac);
        mTrainButton = (Button) view.findViewById(R.id.fragment_status_train);
        mTrainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HueyoService.train(getActivity(), mMyoMac);
            }
        });
        if(savedInstanceState != null) {
            setMyoMac(savedInstanceState.getString(STATE_MAC, null));
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_MAC, mMyoMac);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBusUtils.unregister(this);
    }

    public static StatusFragment newInstance() {
        return new StatusFragment();
    }

    public void onEvent(PoseEvent poseEvent){
        Log.d(TAG, "PoseEvent: " + poseEvent.getPose());
        Toast.makeText(getActivity(), poseEvent.getPose().toString(), Toast.LENGTH_SHORT).show();
    }

    public void onEvent(MyoEvent myoEvent) {
        Log.d(TAG, "MyoEvent: " + myoEvent.getAddress() + " " + myoEvent.getState());
        switch (myoEvent.getState()) {
            case CONNECTED:
                setMyoMac(myoEvent.getAddress());
                break;
            case DISCONNECTED:
                setMyoMac(null);
                break;
        }
    }

    public void setMyoMac(String myoMac) {
        this.mMyoMac = myoMac;
        if(mMyoMac != null) {
            mTrainButton.setEnabled(true);
            mMacTextView.setText(mMyoMac);
        }else {
            mTrainButton.setEnabled(false);
            mMacTextView.setText(R.string.no_paired_myo);
        }
    }
}
