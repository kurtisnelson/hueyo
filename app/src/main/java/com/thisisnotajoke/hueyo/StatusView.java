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
            setImageResource(android.R.drawable.presence_online);
        }else{
            setImageResource(android.R.drawable.presence_offline);
        }
    }

    public boolean isActive() {
        return mActive;
    }
}
