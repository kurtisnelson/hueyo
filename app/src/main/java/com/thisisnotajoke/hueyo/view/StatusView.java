package com.thisisnotajoke.hueyo.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.thisisnotajoke.hueyo.R;

public class StatusView extends ImageView{
    private boolean mActive;

    public StatusView(Context context) {
        this(context, null);
    }

    public StatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setActive(false);
    }

    public void setActive(boolean active) {
        mActive = active;
        if(active){
            setImageResource(R.drawable.ic_check_black_36dp);
        }else{
            setImageResource(R.drawable.ic_error_red_36dp);
        }
    }

    public boolean isActive() {
        return mActive;
    }
}
