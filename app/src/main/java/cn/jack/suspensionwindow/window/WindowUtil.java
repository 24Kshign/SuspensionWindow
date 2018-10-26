package cn.jack.suspensionwindow.window;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

import cn.jack.suspensionwindow.R;
import cn.jack.suspensionwindow.WebViewActivity;
import cn.jack.suspensionwindow.util.SPUtil;
import cn.jack.suspensionwindow.window.rom.HuaweiUtils;
import cn.jack.suspensionwindow.window.rom.MeizuUtils;
import cn.jack.suspensionwindow.window.rom.MiuiUtils;
import cn.jack.suspensionwindow.window.rom.OppoUtils;
import cn.jack.suspensionwindow.window.rom.QikuUtils;
import cn.jack.suspensionwindow.window.rom.RomUtils;
import cn.jake.share.frdialog.dialog.FRDialog;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by manji
 * Date：2018/9/29 下午4:29
 * Desc：
 */
public class WindowUtil {


    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private View mView;

    private static final String ROM360 = "rom360";
    private static final String HUAWEI = "huawei";
    private static final String MEIZU = "meizu";
    private static final String MIUI = "miui";
    private static final String OPPO = "oppo";
    private static final String COMMON_ROM = "common_rom";

    private OnPermissionListener mOnPermissionListener;


    private WindowUtil() {

    }

    private static class SingletonInstance {
        @SuppressLint("StaticFieldLeak")
        private static final WindowUtil INSTANCE = new WindowUtil();
    }

    public static WindowUtil getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public void showPermissionWindow(Context context, OnPermissionListener onPermissionListener) {
        if (checkPermission(context)) {
            showWindow(context);
        } else {
            SPUtil.setIntDefault(WebViewActivity.ARTICLE_ID, -1);
            SPUtil.setStringDefault(WebViewActivity.ARTICLE_JUMP_URL, "");
            SPUtil.setStringDefault(WebViewActivity.ARTICLE_IMAGE_URL, "");
            mOnPermissionListener = onPermissionListener;
            applyPermission(context);
        }
    }

    @SuppressLint("CheckResult")
    private void showWindow(Context context) {
        mWindowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        mView = LayoutInflater.from(context).inflate(R.layout.article_window, null);

        ImageView ivImage = mView.findViewById(R.id.aw_iv_image);
        String imageUrl = SPUtil.getStringDefault(WebViewActivity.ARTICLE_IMAGE_URL, "");
        RequestOptions requestOptions = RequestOptions.circleCropTransform();
        requestOptions.placeholder(R.mipmap.ic_launcher_round).error(R.mipmap.ic_launcher_round);
        Glide.with(context).load(imageUrl).apply(requestOptions).into(ivImage);

        initListener(context);

        mLayoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        mLayoutParams.format = PixelFormat.RGBA_8888;   //窗口透明
        mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;  //窗口位置
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.width = 200;
        mLayoutParams.height = 200;
        mLayoutParams.x = mWindowManager.getDefaultDisplay().getWidth() - 200;
        mLayoutParams.y = 0;
        mWindowManager.addView(mView, mLayoutParams);
    }

    public void dismissWindow() {
        if (mWindowManager != null && mView != null) {
            mWindowManager.removeViewImmediate(mView);
            mWindowManager = null;
            mView = null;
        }
    }

    private void initListener(final Context context) {
        mView.setOnClickListener(v -> {
            String jumpUrl = SPUtil.getStringDefault(WebViewActivity.ARTICLE_JUMP_URL, "");
            if (!jumpUrl.isEmpty()) {
                WebViewActivity.start(context, jumpUrl);
            }
        });

        //设置触摸滑动事件
        mView.setOnTouchListener(new View.OnTouchListener() {
            int startX, startY;  //起始点
            boolean isMove;  //是否在移动
            long startTime;
            int finalMoveX;  //最后通过动画将mView的X轴坐标移动到finalMoveX
            int statusBarHeight;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
                        if (resourceId > 0) {
                            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
                            Log.e("TAG", "statusBarHeight---->" + statusBarHeight);
                        }
                        startTime = System.currentTimeMillis();
                        isMove = false;
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        mLayoutParams.x = (int) (event.getRawX() - startX);
                        //这里修复了刚开始移动的时候，悬浮窗的y坐标是不正确的，要减去状态栏的高度，可以将这个去掉运行体验一下
                        mLayoutParams.y = (int) (event.getRawY() - startY - statusBarHeight);
                        Log.e("TAG", "y---->" + mLayoutParams.y);
                        updateViewLayout();   //更新mView 的位置
                        return true;
                    case MotionEvent.ACTION_UP:
                        long curTime = System.currentTimeMillis();
                        isMove = curTime - startTime > 100;

                        //判断mView是在Window中的位置，以中间为界
                        if (mLayoutParams.x + mView.getMeasuredWidth() / 2 >= mWindowManager.getDefaultDisplay().getWidth() / 2) {
                            finalMoveX = mWindowManager.getDefaultDisplay().getWidth() - mView.getMeasuredWidth();
                        } else {
                            finalMoveX = 0;
                        }

                        //使用动画移动mView
                        ValueAnimator animator = ValueAnimator.ofInt(mLayoutParams.x, finalMoveX).setDuration(Math.abs(mLayoutParams.x - finalMoveX));
                        animator.addUpdateListener((ValueAnimator animation) -> {
                            mLayoutParams.x = (int) animation.getAnimatedValue();
                            updateViewLayout();
                        });
                        animator.start();

                        return isMove;
                }
                return false;
            }
        });
    }

    private void updateViewLayout() {
        if (null != mView && null != mLayoutParams) {
            mWindowManager.updateViewLayout(mView, mLayoutParams);
        }
    }

    public boolean checkPermission(Context context) {
        //6.0 版本之后由于 google 增加了对悬浮窗权限的管理，所以方式就统一了
        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                return miuiPermissionCheck(context);
            } else if (RomUtils.checkIsMeizuRom()) {
                return meizuPermissionCheck(context);
            } else if (RomUtils.checkIsHuaweiRom()) {
                return huaweiPermissionCheck(context);
            } else if (RomUtils.checkIs360Rom()) {
                return qikuPermissionCheck(context);
            } else if (RomUtils.checkIsOppoRom()) {
                return oppoROMPermissionCheck(context);
            }
        }
        return commonROMPermissionCheck(context);
    }

    private boolean huaweiPermissionCheck(Context context) {
        return HuaweiUtils.checkFloatWindowPermission(context);
    }

    private boolean miuiPermissionCheck(Context context) {
        return MiuiUtils.checkFloatWindowPermission(context);
    }

    private boolean meizuPermissionCheck(Context context) {
        return MeizuUtils.checkFloatWindowPermission(context);
    }

    private boolean qikuPermissionCheck(Context context) {
        return QikuUtils.checkFloatWindowPermission(context);
    }

    private boolean oppoROMPermissionCheck(Context context) {
        return OppoUtils.checkFloatWindowPermission(context);
    }

    private boolean commonROMPermissionCheck(Context context) {
        //最新发现魅族6.0的系统这种方式不好用，天杀的，只有你是奇葩，没办法，单独适配一下
        if (RomUtils.checkIsMeizuRom()) {
            return meizuPermissionCheck(context);
        } else {
            Boolean result = true;
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    Class clazz = Settings.class;
                    Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                    result = (Boolean) canDrawOverlays.invoke(null, context);
                } catch (Exception e) {
                    Log.e("TAG", Log.getStackTraceString(e));
                }
            }
            return result;
        }
    }

    private void applyPermission(Context context) {
        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                miuiROMPermissionApply(context);
            } else if (RomUtils.checkIsMeizuRom()) {
                meizuROMPermissionApply(context);
            } else if (RomUtils.checkIsHuaweiRom()) {
                huaweiROMPermissionApply(context);
            } else if (RomUtils.checkIs360Rom()) {
                ROM360PermissionApply(context);
            } else if (RomUtils.checkIsOppoRom()) {
                oppoROMPermissionApply(context);
            }
        } else {
            commonROMPermissionApply(context);
        }
    }

    private void ROM360PermissionApply(final Context context) {
        showDialog(context, ROM360);
    }

    private void huaweiROMPermissionApply(final Context context) {
        showDialog(context, HUAWEI);
    }

    private void meizuROMPermissionApply(final Context context) {
        showDialog(context, MEIZU);
    }

    private void miuiROMPermissionApply(final Context context) {
        showDialog(context, MIUI);
    }

    private void oppoROMPermissionApply(final Context context) {
        showDialog(context, OPPO);
    }

    /**
     * 通用 rom 权限申请
     */
    private void commonROMPermissionApply(final Context context) {
        //这里也一样，魅族系统需要单独适配
        if (RomUtils.checkIsMeizuRom()) {
            meizuROMPermissionApply(context);
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                showDialog(context, COMMON_ROM);
            }
        }
    }

    private void showDialog(Context context, String flag) {
        FRDialog dialog = new FRDialog.MDBuilder(context)
                .setTitle("悬浮窗权限")
                .setMessage("您的手机没有授予悬浮窗权限，请开启后再试")
                .setPositiveContentAndListener("现在去开启", view -> {
                    switch (flag) {
                        case ROM360:
                            QikuUtils.applyPermission(context);
                            mOnPermissionListener.result(true);
                            break;
                        case HUAWEI:
                            HuaweiUtils.applyPermission(context);
                            mOnPermissionListener.result(true);
                            break;
                        case MEIZU:
                            MeizuUtils.applyPermission(context);
                            mOnPermissionListener.result(true);
                            break;
                        case MIUI:
                            MiuiUtils.applyMiuiPermission(context);
                            mOnPermissionListener.result(true);
                            break;
                        case OPPO:
                            OppoUtils.applyOppoPermission(context);
                            mOnPermissionListener.result(true);
                            break;
                        case COMMON_ROM:
                            try {
                                commonROMPermissionApplyInternal(context);
                                mOnPermissionListener.result(true);
                            } catch (Exception e) {
                                e.printStackTrace();
                                mOnPermissionListener.result(false);
                            }
                            break;
                    }
                    return true;
                }).setNegativeContentAndListener("暂不开启", view -> {
                    if (null != mOnPermissionListener) {
                        mOnPermissionListener.result(false);
                    }
                    return true;
                }).create();
        //在service中弹dialog会有问题，设置一下dialog的类型，和android版本也有关系，在这里判断一下
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Objects.requireNonNull(dialog.getWindow()).setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY - 1);
        } else {
            Objects.requireNonNull(dialog.getWindow()).setType(WindowManager.LayoutParams.TYPE_TOAST);
        }
        dialog.show();
    }

    public void commonROMPermissionApplyInternal(Context context) throws NoSuchFieldException, IllegalAccessException {
        Class clazz = Settings.class;
        Field field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");

        Intent intent = new Intent(field.get(null).toString());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

    interface OnPermissionListener {
        void result(boolean isSuccess);
    }
}