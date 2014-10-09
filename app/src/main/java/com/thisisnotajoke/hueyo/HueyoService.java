package com.thisisnotajoke.hueyo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.scanner.ScanActivity;

import java.util.List;

import javax.inject.Inject;

public class HueyoService extends Service {
    private static final String TAG = "HueyoService";
    private final IBinder mBinder = new LocalBinder();

    private PoseConsumer mPoseConsumer;
    private int mSelectedLight;

    @Inject
    protected PreferenceUtil mPrefUtils;

    @Inject
    protected PHHueSDK mHue;

    @Inject
    protected Hub mHub;

    public class LocalBinder extends Binder {
        HueyoService getService() {
            return HueyoService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HueyoApplication.get(this).inject(this);
        EventBusUtils.register(this);

        createMyo();
        createHue();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        EventBusUtils.unregister(this);

        destroyMyo();
        destroyHue();
        mPoseConsumer = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void pair() {
       mHub.pairWithAnyMyo();
    }

    public void pairActivity(Context context) {
        context.startActivity(new Intent(context, ScanActivity.class));
    }

    public boolean isHueConnected() {
        if(mHue != null){
            return mHue.getSelectedBridge() != null;
        }
        return false;
    }

    public boolean isMyoConnected() {
        if(mHub != null){
            return !mHub.getConnectedDevices().isEmpty();
        }
        return false;
    }

    public void onEvent(Myo.VibrationType event) {
        for(Myo device : mHub.getConnectedDevices()){
            device.vibrate(event);
        }
    }

    private void createHue() {
        mHue.setDeviceName(getString(R.string.app_name));
        mHue.getNotificationManager().registerSDKListener(mHueListener);

        // Try to automatically connect to the last known bridge.
        String lastIpAddress = mPrefUtils.getLastConnectedBridgeAddress();
        String lastUsername = mPrefUtils.getHueUsername();

        // Automatically try to connect to the last connected IP Address.  For multiple bridge support a different implementation is required.
        if (lastIpAddress != null && !lastIpAddress.equals("")) {
            final PHAccessPoint lastAccessPoint = new PHAccessPoint();
            lastAccessPoint.setIpAddress(lastIpAddress);
            lastAccessPoint.setUsername(lastUsername);

            if (!mHue.isAccessPointConnected(lastAccessPoint)) {
                mHue.connect(lastAccessPoint);
            }
        } else {
            PHBridgeSearchManager sm = (PHBridgeSearchManager) mHue.getSDKService(PHHueSDK.SEARCH_BRIDGE);
            sm.search(true, true);
        }
    }

    private void destroyHue() {
        mHue.destroySDK();
        mHue = null;
    }

    private void createMyo() {
        if (!mHub.init(this)) {
            Log.e(TAG, "Could not initialize the Hub.");
            stopSelf();
            return;
        }
        mHub.addListener(mMyoListener);
        pair();
    }

    private void destroyMyo() {
        mHub.removeListener(mMyoListener);
        mHub = null;
    }


    private DeviceListener mMyoListener = new AbstractDeviceListener() {
        public long mLastOrientationTimestamp;

        @Override
        public void onConnect(Myo myo, long timestamp) {
            EventBusUtils.postSticky(new MyoEvent(myo));
        }

        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            EventBusUtils.postSticky(new MyoEvent(myo));
        }

        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            EventBusUtils.post(new PoseEvent(myo, pose));
            if(mPoseConsumer != null)
                mPoseConsumer.eat(pose);
        }

        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            if(timestamp > mLastOrientationTimestamp + 750) {
                mLastOrientationTimestamp = timestamp;
                if (mPoseConsumer != null)
                    mPoseConsumer.eat(rotation);
            }
        }
    };

    public PHSDKListener mHueListener = new PHSDKListener() {

        @Override
        public void onCacheUpdated(int flags, PHBridge bridge) {
        }

        @Override
        public void onBridgeConnected(PHBridge b) {
            mHue.setSelectedBridge(b);
            mHue.enableHeartbeat(b, PHHueSDK.HB_INTERVAL);
            mHue.getLastHeartbeat().put(b.getResourceCache().getBridgeConfiguration().getIpAddress(), System.currentTimeMillis());
            mPrefUtils.setLastConnectedBridgeAddress(b.getResourceCache().getBridgeConfiguration().getIpAddress());
            mPrefUtils.setHueUsername(b.getResourceCache().getBridgeConfiguration().getUsername());
            Log.i(TAG, "Hue bridge connected");
            EventBusUtils.postSticky(new HueEvent(b, true));
            loadSelectedLight(b);
            mPoseConsumer = new PoseConsumer(mHue, mSelectedLight);
        }

        @Override
        public void onAuthenticationRequired(PHAccessPoint accessPoint) {
            Log.w(TAG, "Authentication Required.");
            mHue.startPushlinkAuthentication(accessPoint);
            EventBusUtils.post(new HueAuthEvent());
        }

        @Override
        public void onAccessPointsFound(List<PHAccessPoint> accessPoints) {
            Log.w(TAG, "Access Points Found. " + accessPoints.size());

            if (accessPoints != null && accessPoints.size() > 0) {
                mHue.getAccessPointsFound().clear();
                mHue.getAccessPointsFound().addAll(accessPoints);
                PHAccessPoint accessPoint = accessPoints.get(0);
                accessPoint.setUsername(mPrefUtils.getHueUsername());
                mHue.connect(accessPoint);
            } else {
                // FallBack Mechanism.  If a UPNP Search returns no results then perform an IP Scan.
                PHBridgeSearchManager sm = (PHBridgeSearchManager) mHue.getSDKService(PHHueSDK.SEARCH_BRIDGE);
                // Start the IP Scan Search if the UPNP and NPNP return 0 results.
                sm.search(false, false, true);
            }

        }

        @Override
        public void onError(int code, final String message) {
            switch (code) {
                case PHMessageType.PUSHLINK_BUTTON_NOT_PRESSED:
                    break;
                default:
                    Log.e(TAG, "on Error Called : " + code + ":" + message);
            }
        }


        @Override
        public void onConnectionResumed(PHBridge bridge) {
            EventBusUtils.postSticky(new HueEvent(bridge, true));
            loadSelectedLight(bridge);
        }

        @Override
        public void onConnectionLost(PHAccessPoint accessPoints) {
            EventBusUtils.postSticky(new HueEvent(null, false));
        }

    };

    private void loadSelectedLight(PHBridge bridge) {
        mSelectedLight = mPrefUtils.getSelectedLight();
        if(mSelectedLight >= bridge.getResourceCache().getAllLights().size()){
            mSelectedLight = 0;
            mPrefUtils.setSelectedLight(0);
        }
    }
}
