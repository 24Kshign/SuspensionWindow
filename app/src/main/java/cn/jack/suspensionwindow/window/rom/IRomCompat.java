package cn.jack.suspensionwindow.window.rom;

import android.content.Context;

/**
 * Created by 大灯泡 on 2018/11/7.
 */
public interface IRomCompat {

    boolean checkFloatWindowPermission(Context context);

    boolean applyPermission(Context context);

    boolean checkRom();
}
