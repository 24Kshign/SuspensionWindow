/*
 * Copyright (C) 2016 Facishare Technology Co., Ltd. All Rights Reserved.
 */
package cn.jack.suspensionwindow.window.rom;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

    /**
     * 获取 emui 版本号
     *
     * @return
     */
    private static double getEmuiVersion() {
        try {
            String emuiVersion = getSystemProperty("ro.build.version.emui");
            String version = emuiVersion.substring(emuiVersion.indexOf("_") + 1);
            return Double.parseDouble(version);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 4.0;
    }

    private static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            Log.e("TAG", "Unable to read sysprop " + propName, ex);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e("TAG", "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }

    /**
     * 去华为权限申请页面
     */
    private static void applyHuaWeiPermission(Context context) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity");//悬浮窗管理页面
            intent.setComponent(comp);
            if (getEmuiVersion() == 3.1) {
                //emui 3.1 的适配
                ((Activity) context).startActivityForResult(intent, REQUEST_PERMISSION_CODE);
            } else {
                //emui 3.0 的适配
                comp = new ComponentName("com.huawei.systemmanager", "com.huawei.notificationmanager.ui.NotificationManagmentActivity");//悬浮窗管理页面
                intent.setComponent(comp);
                ((Activity) context).startActivityForResult(intent, REQUEST_PERMISSION_CODE);
            }
        } catch (SecurityException e) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager",
                    "com.huawei.permissionmanager.ui.MainActivity");//华为权限管理，跳转到本app的权限管理页面,这个需要华为接口权限，未解决
            intent.setComponent(comp);
            ((Activity) context).startActivityForResult(intent, REQUEST_PERMISSION_CODE);
            Log.e("TAG", Log.getStackTraceString(e));
        } catch (ActivityNotFoundException e) {
            /**
             * 手机管家版本较低 HUAWEI SC-UL10
             */
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.Android.settings", "com.android.settings.permission.TabItem");//权限管理页面 android4.4
            intent.setComponent(comp);
            ((Activity) context).startActivityForResult(intent, REQUEST_PERMISSION_CODE);
            e.printStackTrace();
            Log.e("TAG", Log.getStackTraceString(e));
        } catch (Exception e) {
            //抛出异常时提示信息
            Toast.makeText(context, "进入设置页面失败，请手动设置", Toast.LENGTH_LONG).show();
            Log.e("TAG", Log.getStackTraceString(e));
        }
    }

    public static boolean checkIsHuaWeiRom() {
        return Build.MANUFACTURER.contains("HUAWEI");
    }

    public static void onActivityResult(Context context, int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (null != mOnSuspensionPermissionListener) {
                    mOnSuspensionPermissionListener.onPermissionGranted();
                }
            }
        }
    }

    public interface OnSuspensionPermissionListener {

        /**
         * 当权限请求完毕后的回调
         */
        void onPermissionGranted();
    }
}
