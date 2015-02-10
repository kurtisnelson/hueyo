package com.thisisnotajoke.hueyo.controller;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueParsingError;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.scanner.ScanActivity;
import com.thisisnotajoke.hueyo.model.PreferenceUtil;
import com.thisisnotajoke.hueyo.R;
import com.thisisnotajoke.hueyo.base.EventBusUtils;
import com.thisisnotajoke.hueyo.base.HueyoApplication;
import com.thisisnotajoke.hueyo.model.hue.HueAuthEvent;
import com.thisisnotajoke.hueyo.model.hue.HueEvent;
import com.thisisnotajoke.hueyo.model.myo.MyoEvent;
import com.thisisnotajoke.hueyo.model.myo.PoseConsumer;
import com.thisisnotajoke.hueyo.model.myo.PoseEvent;

import java.util.List;

import javax.inject.Inject;

public class HueyoService extends Service {
    private static final String TAG = "HueyoService";
	private static final int ONGOING_NOTIFICATION_ID = 101;
    private final IBinder mBinder = new LocalBinder();

	private static final Class<?>[] mSetForegroundSignature = new Class[] {
	    boolean.class};
	private static final Class<?>[] mStartForegroundSignature = new Class[] {
	    int.class, Notification.class};
	private static final Class<?>[] mStopForegroundSignature = new Class[] {
	    boolean.class};
		
	private NotificationManager mNM;
	private Method mSetForeground;
	private Method mStartForeground;
	private Method mStopForeground;
	private Object[] mSetForegroundArgs = new Object[1];
	private Object[] mStartForegroundArgs = new Object[2];
	private Object[] mStopForegroundArgs = new Object[1];
		
    @Inject
    protected PreferenceUtil mPrefUtils;

    @Inject
    protected PHHueSDK mHue;

    @Inject
    protected Hub mHub;

    @Inject
    protected PoseConsumer mPoseConsumer;
    private Handler mHandler;

    public class LocalBinder extends Binder {
        HueyoService getService() {
            return HueyoService.this;
        }
    }

	void invokeMethod(Method method, Object[] args) {
	    try {
	        method.invoke(this, args);
	    } catch (InvocationTargetException e) {
	        // Should not happen.
	        Log.w(TAG, "Unable to invoke method", e);
	    } catch (IllegalAccessException e) {
	        // Should not happen.
	        Log.w(TAG, "Unable to invoke method", e);
	    }
	}
	
	/**
	 * This is a wrapper around the new startForeground method, using the older
	 * APIs if it is not available.
	 */
	void startForegroundCompat(int id, Notification notification) {
	    // If we have the new startForeground API, then use it.
	    if (mStartForeground != null) {
	        mStartForegroundArgs[0] = Integer.valueOf(id);
	        mStartForegroundArgs[1] = notification;
	        invokeMethod(mStartForeground, mStartForegroundArgs);
	        return;
	    }

	    // Fall back on the old API.
	    mSetForegroundArgs[0] = Boolean.TRUE;
	    invokeMethod(mSetForeground, mSetForegroundArgs);
	    mNM.notify(id, notification);
	}
	
	/**
	 * This is a wrapper around the new stopForeground method, using the older
	 * APIs if it is not available.
	 */
	void stopForegroundCompat(int id) {
	    // If we have the new stopForeground API, then use it.
	    if (mStopForeground != null) {
	        mStopForegroundArgs[0] = Boolean.TRUE;
	        invokeMethod(mStopForeground, mStopForegroundArgs);
	        return;
	    }

	    // Fall back on the old API.  Note to cancel BEFORE changing the
	    // foreground state, since we could be killed at that point.
	    mNM.cancel(id);
	    mSetForegroundArgs[0] = Boolean.FALSE;
	    invokeMethod(mSetForeground, mSetForegroundArgs);
	}
	
    @Override
    public void onCreate() {
        super.onCreate();
		
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	    try {
	        mStartForeground = getClass().getMethod("startForeground",
	                mStartForegroundSignature);
	        mStopForeground = getClass().getMethod("stopForeground",
	                mStopForegroundSignature);
	        return;
	    } catch (NoSuchMethodException e) {
	        // Running on an older platform.
	        mStartForeground = mStopForeground = null;
	    }
	    try {
	        mSetForeground = getClass().getMethod("setForeground",
	                mSetForegroundSignature);
	    } catch (NoSuchMethodException e) {
	        throw new IllegalStateException(
	                "OS doesn't have Service.startForeground OR Service.setForeground!");
	    }
		
        HandlerThread thread = new HandlerThread("HueyoService");
        thread.start();
        mHandler = new Handler(thread.getLooper());

        HueyoApplication.get(HueyoService.this).inject(HueyoService.this);
        EventBusUtils.register(HueyoService.this);

        createMyo();
        createHue();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		
		Notification notification = new Notification(R.drawable.ic_launcher, getText(R.string.ticker_text),
		        System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, HueyoService.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(this, getText(R.string.notification_title),
		        getText(R.string.notification_message), pendingIntent);
		startForegroundCompat(ONGOING_NOTIFICATION_ID, notification);
		
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        EventBusUtils.unregister(this);

		// Make sure our notification is gone.
		stopForegroundCompat(ONGOING_NOTIFICATION_ID);
		
        destroyMyo();
        destroyHue();
        mPoseConsumer = null;
        mHandler.getLooper().quit();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void pair() {
        mHub.attachToAdjacentMyo();
    }

    public void pairActivity(Context context) {
        context.startActivity(new Intent(context, ScanActivity.class));
    }

    public void onEvent(Myo.VibrationType event) {
        for (Myo device : mHub.getConnectedDevices()) {
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
        mPoseConsumer.disconnect();
        mHub.removeListener(mMyoListener);
        mHub.shutdown();
        mHub = null;
    }

    private DeviceListener mMyoListener = new AbstractDeviceListener() {
        @Override
        public void onConnect(Myo myo, long timestamp) {
            EventBusUtils.postSticky(new MyoEvent(myo));
            mPoseConsumer.disconnect();
            mPoseConsumer.consumePoses(mHandler);
            mPoseConsumer.consumeOrientation(mHandler);
        }

        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            EventBusUtils.postSticky(new MyoEvent(myo));
            mPoseConsumer.disconnect();
        }

        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            EventBusUtils.post(new PoseEvent(myo, pose));
        }
    };

    public PHSDKListener mHueListener = new PHSDKListener() {
        @Override
        public void onCacheUpdated(List<Integer> integers, PHBridge phBridge) {

        }

        @Override
        public void onParsingErrors(List<PHHueParsingError> phHueParsingErrors) {

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
        }

        @Override
        public void onAuthenticationRequired(PHAccessPoint accessPoint) {
            Log.w(TAG, "Authentication Required.");
            mHue.startPushlinkAuthentication(accessPoint);
            EventBusUtils.post(new HueAuthEvent());
        }

        @Override
        public void onAccessPointsFound(List<PHAccessPoint> accessPoints) {
            if (accessPoints == null) return;
            Log.w(TAG, "Access Points Found. " + accessPoints.size());

            if (accessPoints.size() > 0) {
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
        }

        @Override
        public void onConnectionLost(PHAccessPoint accessPoints) {
            EventBusUtils.postSticky(new HueEvent(null, false));
        }

    };
}
