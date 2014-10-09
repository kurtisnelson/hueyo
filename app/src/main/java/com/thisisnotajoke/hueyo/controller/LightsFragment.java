package com.thisisnotajoke.hueyo.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thisisnotajoke.hueyo.R;

public class LightsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lights, container, false);
        return v;
    }

    public static LightsFragment newInstance() {
        return new LightsFragment();
    }
}
