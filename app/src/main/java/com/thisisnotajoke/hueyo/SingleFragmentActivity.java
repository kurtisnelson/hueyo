package com.thisisnotajoke.hueyo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public abstract class SingleFragmentActivity extends FragmentActivity {
    protected abstract Fragment getFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_single_fragment_container);
        if (fragment == null) {
            fragment = getFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_single_fragment_container, fragment)
                    .commit();
        }
    }
}