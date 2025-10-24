// com/devlabting/tucancha/util/PortraitLock.java
package com.devlabting.tucancha.util;

import android.app.Activity;
import android.content.pm.ActivityInfo;

public final class PortraitLock {
    private PortraitLock() {}

    /** Fuerza vertical; ignora sensores y auto-rotate del sistema. */
    public static void apply(Activity a) {
        a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
