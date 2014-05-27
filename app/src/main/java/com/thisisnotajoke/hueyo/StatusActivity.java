package com.thisisnotajoke.hueyo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;

public class StatusActivity extends SingleFragmentActivity {
    private static final String TAG = "SplashActivity";
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

    @Override
    protected Fragment getFragment() {
        return StatusFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    public void quit() {
        if(mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
        stopService(new Intent(this, HueyoService.class));
        finish();
    }
}
