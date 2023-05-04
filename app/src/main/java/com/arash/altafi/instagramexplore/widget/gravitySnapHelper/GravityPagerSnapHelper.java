package com.arash.altafi.instagramexplore.widget.gravitySnapHelper;

import android.support.annotation.Nullable;

@Deprecated
public class GravityPagerSnapHelper extends GravitySnapHelper {

    public GravityPagerSnapHelper(int gravity) {
        this(gravity, null);
    }

    public GravityPagerSnapHelper(int gravity,
                                  @Nullable GravitySnapHelper.SnapListener snapListener) {
        super(gravity, false, snapListener);
        setMaxFlingSizeFraction(1.0f);
        setScrollMsPerInch(50f);
    }
}