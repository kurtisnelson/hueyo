package com.thisisnotajoke.hueyo;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class PreferenceUtil {
    private static final String PREFS_NAME = "Hueyo.PreferenceUtil";
    private static final String BRIDGE_ADDRESS = "HueBridgeAddress";
    private static final String HUE_USERNAME = "HueUsername";
    private static final String SELECTED_LIGHT = "HueSelectedLight";
    private final SharedPreferences mPreferences;

    public PreferenceUtil(Context applicationContext) {
        mPreferences = applicationContext.getSharedPreferences(PREFS_NAME, 0);
    }

    public static PreferenceUtil newInstance(Context applicationContext) {
        return new PreferenceUtil(applicationContext);
    }

    public int getSelectedLight() {
        return mPreferences.getInt(SELECTED_LIGHT, 0);
    }

    public String getLastConnectedBridgeAddress() {
        return mPreferences.getString(BRIDGE_ADDRESS, null);
    }

    public void setLastConnectedBridgeAddress(String address){
        mPreferences.edit().putString(BRIDGE_ADDRESS, address).commit();
    }

    public String getHueUsername() {
        return mPreferences.getString(HUE_USERNAME, UUID.randomUUID().toString());
    }

    public void setHueUsername(String name){
        mPreferences.edit().putString(HUE_USERNAME, name).commit();
    }

    public void setSelectedLight(int selectedLight) {
        mPreferences.edit().putInt(SELECTED_LIGHT, selectedLight).commit();
    }
}
