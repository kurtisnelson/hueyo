package com.thisisnotajoke.hueyo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.scanner.ScanActivity;
import com.thalmic.myo.trainer.TrainActivity;

public class HueyoService extends Service{
    private static final String TAG = "HueyoService";
    private Hub mHub;
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        HueyoService getService() {
            return HueyoService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //EventBusUtils.register(this);
        mHub = Hub.getInstance();
        if (!mHub.init(this)) {
            Log.e(TAG, "Could not initialize the Hub.");
            stopSelf();
            return;
        }
        mHub.addListener(mListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mHub.removeListener(mListener);
        mHub = null;
        //EventBusUtils.unregister(this);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void trainAll(Context context) {
        for(Myo device : mHub.getConnectedDevices()){
            if(device.isConnected())
                train(context, device.getMacAddress());
        }
    }

    public void pair() {
        mHub.pairWithAdjacentMyo();
    }

    public void pairActivity(Context context) {
        context.startActivity(new Intent(context, ScanActivity.class));
    }

    public static void train(Context context, String device) {
        Intent intent = new Intent(context, TrainActivity.class);
        intent.putExtra(TrainActivity.EXTRA_ADDRESS, device);
        context.startActivity(intent);
    }

    private DeviceListener mListener = new AbstractDeviceListener() {
        @Override
        public void onConnect(Myo myo, long timestamp) {
            EventBusUtils.post(new MyoEvent(myo));
        }

        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            EventBusUtils.post(new MyoEvent(myo));
        }

        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            EventBusUtils.post(new PoseEvent(myo, pose));
        }
    };
}
