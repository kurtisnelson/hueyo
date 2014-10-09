package com.thisisnotajoke.hueyo;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

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
            setImageResource(R.drawable.ic_connected);
        }else{
            setImageResource(R.drawable.ic_disconnected);
        }
    }

    public boolean isActive() {
        return mActive;
    }
}
