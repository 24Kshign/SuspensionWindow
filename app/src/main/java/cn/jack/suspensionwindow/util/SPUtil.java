package cn.jack.suspensionwindow.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

import cn.jack.suspensionwindow.App;

/**
 * Created by manji
 * Date：2018/9/29 上午11:02
 * Desc：
 */
public class SPUtil {

    public static final String DEFALUT_NAME = "defalut_name";

    private SPUtil() {
    }

    private static SharedPreferences getSharedPreferences(String name) {
        return App.getInstance().getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static String getString(String name, String key, String defValue) {
        return getSharedPreferences(name).getString(key, defValue);
    }

    public static String getStringDefault(String key, String defValue) {
        return getSharedPreferences(DEFALUT_NAME).getString(key, defValue);
    }

    public static void clearSharePreference(String name) {
        getSharedPreferences(name).edit().clear().apply();
    }

    public static boolean setString(String name, String key, String value) {
        return getSharedPreferences(name).edit().putString(key, value).commit();
    }

    public static boolean setStringDefault(String key, String value) {
        return getSharedPreferences(DEFALUT_NAME).edit().putString(key, value).commit();
    }

    public static int getInt(String name, String key, int defValue) {
        return getSharedPreferences(name).getInt(key, defValue);
    }

    public static int getIntDefault(String key, int defValue) {
        return getSharedPreferences(DEFALUT_NAME).getInt(key, defValue);
    }

    public static boolean setInt(String name, String key, int value) {
        return getSharedPreferences(name).edit().putInt(key, value).commit();
    }

    public static boolean setIntDefault(String key, int value) {
        return getSharedPreferences(DEFALUT_NAME).edit().putInt(key, value).commit();
    }

    public static long getLong(String name, String key, long defValue) {
        return getSharedPreferences(name).getLong(key, defValue);
    }

    public static boolean setLong(String name, String key, long value) {
        return getSharedPreferences(name).edit().putLong(key, value).commit();
    }

    public static boolean getBoolean(String name, String key, boolean defValue) {
        return getSharedPreferences(name).getBoolean(key, defValue);
    }

    public static boolean getBooleanDefault(String key, boolean defValue) {
        return getSharedPreferences(DEFALUT_NAME).getBoolean(key, defValue);
    }

    public static boolean setBoolean(String name, String key, boolean value) {
        return getSharedPreferences(name).edit().putBoolean(key, value).commit();
    }

    public static boolean setBooleanDefault(String key, boolean value) {
        return getSharedPreferences(DEFALUT_NAME).edit().putBoolean(key, value).commit();
    }

    public static boolean setStringSet(String name, String key, Set<String> value) {
        return getSharedPreferences(name).edit().putStringSet(key, value).commit();
    }

    public static Set<String> getStringSet(String name, String key, Set<String> value) {
        return getSharedPreferences(name).getStringSet(key, value);
    }
}
