package com.thisisnotajoke.hueyo.controller;


import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.thisisnotajoke.hueyo.base.BaseActivity;
import com.thisisnotajoke.hueyo.base.EventBusUtils;
import com.thisisnotajoke.hueyo.R;
import com.thisisnotajoke.hueyo.model.hue.HueEvent;

import javax.inject.Inject;

public class PHPushlinkActivity extends BaseActivity {
    private ProgressBar pbar;
    private static final int MAX_TIME=30;

    private boolean isDialogShowing;

    @Inject
    protected PHHueSDK mHueSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pushlink);
        setTitle(R.string.hue_auth_required);
        isDialogShowing=false;

        pbar = (ProgressBar) findViewById(R.id.countdownPB);
        pbar.setMax(MAX_TIME);
        
        mHueSDK.getNotificationManager().registerSDKListener(listener);
        EventBusUtils.register(this);
    }

    @Override
    protected void onStop(){
        super.onStop();
        EventBusUtils.unregister(this);
        mHueSDK.getNotificationManager().unregisterSDKListener(listener);
    }

    public void onEvent(HueEvent e){
        if(e.isConnected()){
            finish();
        }
    }

    public void incrementProgress() {
        pbar.incrementProgressBy(1);
    }
    
    private PHSDKListener listener = new PHSDKListener() {

        @Override
        public void onAccessPointsFound(List<PHAccessPoint> arg0) {}

        @Override
        public void onAuthenticationRequired(PHAccessPoint arg0) {}

        @Override
        public void onBridgeConnected(PHBridge arg0) {}

        @Override
        public void onCacheUpdated(int arg0, PHBridge arg1) {}

        @Override
        public void onConnectionLost(PHAccessPoint arg0) {}

        @Override
        public void onConnectionResumed(PHBridge arg0) {}

        @Override
        public void onError(int code, final String message) {
            if (code == PHMessageType.PUSHLINK_BUTTON_NOT_PRESSED) {
                incrementProgress();
            }
            else if (code == PHMessageType.PUSHLINK_AUTHENTICATION_FAILED) {
 //               PHWizardAlertDialog.getInstance().closeProgressDialog();
                incrementProgress();

                if (!isDialogShowing) {
                    isDialogShowing=true;
                    PHPushlinkActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(PHPushlinkActivity.this);
                            builder.setMessage(message).setNeutralButton(R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            finish();
                                        }
                                    });

                            builder.create();
                            builder.show();
                        }
                    });
                }
                
            }

        } // End of On Error
    };
    
}

