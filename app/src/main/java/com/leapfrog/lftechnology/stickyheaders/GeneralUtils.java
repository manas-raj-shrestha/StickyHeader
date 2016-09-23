package com.leapfrog.lftechnology.stickyheaders;

import android.content.Context;

public class GeneralUtils {
    public static float pxFromDp(Context context, int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return dp * scale;
    }
}
