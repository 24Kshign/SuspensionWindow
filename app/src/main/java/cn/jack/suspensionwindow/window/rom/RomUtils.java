/*
 * Copyright (C) 2016 Facishare Technology Co., Ltd. All Rights Reserved.
 */
package cn.jack.suspensionwindow.window.rom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.Method;

import cn.jack.suspensionwindow.util.SPUtil;

/**
 * Description:
 *
 * @author zhaozp
 * @since 2016-05-23
 */
public class RomUtils {

    public static final int REQUEST_PERMISSION_CODE = 0x0110;

    public static OnSuspensionPermissionListener mOnSuspensionPermissionListener;

    public static boolean checkFloatWindowPermission(Context context) {
//        Boolean result = true;
//        if (Build.VERSION.SDK_INT >= 23) {
//            try {
//                Class clazz = Settings.class;
//                Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
//                result = (Boolean) canDrawOverlays.invoke(null, context);
//            } catch (Exception e) {
//                Log.e("TAG", Log.getStackTraceString(e));
//                result = false;
//            }
//        }
//        return result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    public static void applyPermission(Context context, OnSuspensionPermissionListener onSuspensionPermissionListener) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            ((Activity) context).startActivityForResult(intent, REQUEST_PERMISSION_CODE);
            mOnSuspensionPermissionListener = onSuspensionPermissionListener;
        }
    }

    public static void onActivityResult(Context context, int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (null != mOnSuspensionPermissionListener) {
                    mOnSuspensionPermissionListener.onPermissionGranted(Settings.canDrawOverlays(context));
                }
            }
        }
    }

    public interface OnSuspensionPermissionListener {

        /**
         * 当权限请求完毕后的回调
         *
         * @param granted 是否授权。true，已授权。false，未授权
         */
        void onPermissionGranted(boolean granted);
    }
}
