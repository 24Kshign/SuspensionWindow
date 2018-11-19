package cn.jack.suspensionwindow.window;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import cn.jack.suspensionwindow.R;
import cn.jack.suspensionwindow.ui.WebViewActivity;
import cn.jack.suspensionwindow.util.DisplayUtil;
import cn.jack.suspensionwindow.util.SPUtil;
import cn.jack.suspensionwindow.widget.CustomCancelView;
import cn.jack.suspensionwindow.window.rom.RomUtils;

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

    private View mCancelView;
    private CustomCancelView mCustomCancelView;

    private boolean isShowCancel;

    private WindowManager.LayoutParams mCancelViewLayoutParams;

    private static final int mViewWidth = 100;

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
        if (RomUtils.checkFloatWindowPermission(context)) {
            showWindow(context);
        } else {
            onPermissionListener.showPermissionDialog();
        }
    }

    @SuppressLint("CheckResult")
    private void showWindow(Context context) {
        if (null == mWindowManager && null == mView && null == mCancelView) {
            mWindowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
            mView = LayoutInflater.from(context).inflate(R.layout.article_window, null);
            mCancelView = LayoutInflater.from(context).inflate(R.layout.activity_test, null);
            mCustomCancelView = mCancelView.findViewById(R.id.at_cancel_view);

            ImageView ivImage = mView.findViewById(R.id.aw_iv_image);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ivImage.getLayoutParams();
            lp.width = DisplayUtil.dip2px(mViewWidth - 20);
            lp.height = DisplayUtil.dip2px(mViewWidth - 20);
            ivImage.setLayoutParams(lp);
            String imageUrl = SPUtil.getStringDefault(WebViewActivity.ARTICLE_IMAGE_URL, "");
            RequestOptions requestOptions = RequestOptions.circleCropTransform();
            requestOptions.placeholder(R.mipmap.ic_launcher_round).error(R.mipmap.ic_launcher_round);
            Glide.with(context).load(imageUrl).apply(requestOptions).into(ivImage);

            initListener(context);

            mLayoutParams = new WindowManager.LayoutParams();
            mCancelViewLayoutParams = new WindowManager.LayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                mCancelViewLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                mCancelViewLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }

            mCancelViewLayoutParams.format = PixelFormat.RGBA_8888;   //窗口透明
            mCancelViewLayoutParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;  //窗口位置
            mCancelViewLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            mCancelViewLayoutParams.width = DisplayUtil.dip2px(2 * mViewWidth);
            mCancelViewLayoutParams.height = DisplayUtil.dip2px(2 * mViewWidth);
            mWindowManager.addView(mCancelView, mCancelViewLayoutParams);

            mLayoutParams.format = PixelFormat.RGBA_8888;   //窗口透明
            mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;  //窗口位置
            mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            mLayoutParams.width = DisplayUtil.dip2px(mViewWidth);
            mLayoutParams.height = DisplayUtil.dip2px(mViewWidth);
            mWindowManager.addView(mView, mLayoutParams);
        }
    }

    public void dismissWindow() {
        if (null != mCustomCancelView) {
            mCustomCancelView.destroy();
        }
        if (mWindowManager != null && mView != null) {
            mWindowManager.removeViewImmediate(mView);
            mWindowManager.removeViewImmediate(mCancelView);
            mWindowManager = null;
            mCancelView = null;
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

            int mCancelX = mWindowManager.getDefaultDisplay().getWidth();
            int mCancelY = mWindowManager.getDefaultDisplay().getHeight();

            boolean isRemove;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
                        if (resourceId > 0) {
                            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
                        }
                        startTime = System.currentTimeMillis();
                        isMove = false;
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        mLayoutParams.x = (int) (event.getRawX() - startX);
                        //这里修复了刚开始移动的时候，悬浮窗的y坐标是不正确的，要减去状态栏的高度，可以将这个去掉运行体验一下
                        mLayoutParams.y = (int) (event.getRawY() - startY - statusBarHeight);
                        updateViewLayout();   //更新mView 的位置
                        isMove = true;

                        if (!isShowCancel) {
                            isShowCancel = true;
                            if (null != mCustomCancelView) {
                                mCustomCancelView.startAnim(true);
                            }
                        }
                        isRemove = isRemoveAllView(mLayoutParams.x, mLayoutParams.y, mCancelX, mCancelY);
                        return true;
                    case MotionEvent.ACTION_UP:
                        //判断mView是在Window中的位置，以中间为界
                        if (mLayoutParams.x + mView.getMeasuredWidth() / 2 >= mWindowManager.getDefaultDisplay().getWidth() / 2) {
                            finalMoveX = mWindowManager.getDefaultDisplay().getWidth() - mView.getMeasuredWidth();
                        } else {
                            finalMoveX = 0;
                        }

                        if (isRemove) {
                            SPUtil.setIntDefault(WebViewActivity.ARTICLE_ID, -1);
                            SPUtil.setStringDefault(WebViewActivity.ARTICLE_JUMP_URL, "");
                            SPUtil.setStringDefault(WebViewActivity.ARTICLE_IMAGE_URL, "");
                            dismissWindow();
                        } else {
                            if (isShowCancel) {
                                isShowCancel = false;
                                if (null != mCustomCancelView) {
                                    mCustomCancelView.startAnim(false);
                                }
                            }

                            //使用动画移动mView
                            ValueAnimator animator = ValueAnimator.ofInt(mLayoutParams.x, finalMoveX).setDuration(Math.abs(mLayoutParams.x - finalMoveX));
                            animator.setInterpolator(new AccelerateDecelerateInterpolator());
                            animator.addUpdateListener((ValueAnimator animation) -> {
                                mLayoutParams.x = (int) animation.getAnimatedValue();
                                updateViewLayout();
                            });
                            animator.start();
                        }
                        return isMove;
                }
                return false;
            }
        });
    }

    private boolean isRemoveAllView(int x1, int y1, int x2, int y2) {
        //利用勾股定理计算出两个圆心（悬浮窗，右下角的圆弧）的距离，然后判断两者是否重合
        double radius = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        return radius <= DisplayUtil.dip2px((float) (100 * Math.sqrt(2) + 200));
    }

    private void updateViewLayout() {
        if (null != mView && null != mLayoutParams) {
            mWindowManager.updateViewLayout(mView, mLayoutParams);
        }
    }

    interface OnPermissionListener {
        void showPermissionDialog();
    }
}