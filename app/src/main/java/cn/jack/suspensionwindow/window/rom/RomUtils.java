/*
 * Copyright (C) 2016 Facishare Technology Co., Ltd. All Rights Reserved.
 */
package cn.jack.suspensionwindow.window.rom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

/**
 * Description:
 *
 * @author zhaozp
 * @since 2016-05-23
 */
public class RomUtils {
    private static final String TAG = "RomUtils";

    private static IRomCompat IMPL;

    static {
        IMPL = getImpl();
    }

    private static IRomCompat getImpl() {
        if (IMPL == null) {
            IMPL = RomCompatFactory.getRomCompatImpl();
            Log.d(TAG, "获得rom适配： " + IMPL.getClass().getSimpleName());
        }
        return IMPL;
    }

    public static void applyPermission(Context context) {
        getImpl().applyPermission(context);
    }

    public static boolean checkFloatWindowPermission(Context context) {
        return getImpl().checkFloatWindowPermission(context);
    }
}
