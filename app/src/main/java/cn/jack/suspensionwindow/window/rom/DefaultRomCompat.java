package cn.jack.suspensionwindow.window.rom;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by 大灯泡 on 2018/11/7.
 */
public class DefaultRomCompat extends BaseRomCompatImpl {

    @Override
    public boolean checkFloatWindowPermission(Context context) {
        Boolean result = true;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class clazz = Settings.class;
                Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                result = (Boolean) canDrawOverlays.invoke(null, context);
            } catch (Exception e) {
                Log.e("TAG", Log.getStackTraceString(e));
                result = false;
            }
        }
        return result;
    }

    @Override
    public boolean applyPermission(Context context) {
        return applyCommonPermission(context);
    }

    @Override
    public boolean checkRom() {
        return true;
    }
}
