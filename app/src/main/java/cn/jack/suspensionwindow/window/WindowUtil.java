package cn.jack.suspensionwindow.window;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
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
    private CustomCancelView mCustomCancelView;

    private Point point = new Point();
    private Rect mDeleteRect = new Rect();
    private static final int mViewWidth = 100;
    private static final int mCancelViewSize = 200;
    private int statusBarHeight = 0;

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
        if (null == mWindowManager && null == mView && null == mCustomCancelView) {
            mWindowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
            mView = LayoutInflater.from(context).inflate(R.layout.article_window, null);
            mCustomCancelView = (CustomCancelView) LayoutInflater.from(context).inflate(R.layout.activity_test, null);
            mWindowManager.getDefaultDisplay().getSize(point);

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
            WindowManager.LayoutParams mCancelViewLayoutParams = new WindowManager.LayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                mCancelViewLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                mCancelViewLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }

            mCancelViewLayoutParams.format = PixelFormat.RGBA_8888;   //窗口透明
            mCancelViewLayoutParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;  //窗口位置
            mCancelViewLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            mCancelViewLayoutParams.width = DisplayUtil.dip2px(mCancelViewSize);
            mCancelViewLayoutParams.height = DisplayUtil.dip2px(mCancelViewSize);
            mWindowManager.addView(mCustomCancelView, mCancelViewLayoutParams);

            mLayoutParams.format = PixelFormat.RGBA_8888;   //窗口透明
            mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;  //窗口位置
            mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            mLayoutParams.width = DisplayUtil.dip2px(mViewWidth);
            mLayoutParams.height = DisplayUtil.dip2px(mViewWidth);
            // 可以修改View的初始位置
//            mLayoutParams.x = 0;
//            mLayoutParams.y = 0;
            mWindowManager.addView(mView, mLayoutParams);
        }
    }

    public void dismissWindow() {
        if (mWindowManager != null && mView != null) {
            mWindowManager.removeViewImmediate(mView);
            mCustomCancelView.startAnimate(false, data -> {
                mWindowManager.removeViewImmediate(mCustomCancelView);
                mWindowManager = null;
                mCustomCancelView = null;
                mView = null;
            });
        }
    }

    private void initListener(final Context context) {
        mView.setOnClickListener(v -> {
            String jumpUrl = SPUtil.getStringDefault(WebViewActivity.ARTICLE_JUMP_URL, "");
            if (!jumpUrl.isEmpty()) {
                WebViewActivity.start(context, jumpUrl);
            }
        });

        final int mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        //设置触摸滑动事件
        mView.setOnTouchListener(new View.OnTouchListener() {
            int startX, startY;  //起始点
            boolean isPerformClick;  //是否点击
            int finalMoveX;  //最后通过动画将mView的X轴坐标移动到finalMoveX

            boolean isRemove;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mDeleteRect.isEmpty()) {
                    mDeleteRect.set(point.x - mCustomCancelView.getMeasuredWidth(),
                            point.y - mCustomCancelView.getMeasuredHeight(),
                            point.x,
                            point.y);
                    mDeleteRect.left += mView.getWidth() / 2;
                    mDeleteRect.top += mView.getHeight() / 2;
                }
                Log.d("click", "onTouch: " + event.getAction());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        isPerformClick = true;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //判断是CLICK还是MOVE
                        //只要移动过，就认为不是点击
                        if (Math.abs(startX - event.getX()) >= mTouchSlop || Math.abs(startY - event.getY()) >= mTouchSlop) {
                            isPerformClick = false;
                        }

                        mLayoutParams.x = (int) (event.getRawX() - startX);
                        //这里修复了刚开始移动的时候，悬浮窗的y坐标是不正确的，要减去状态栏的高度，可以将这个去掉运行体验一下
                        mLayoutParams.y = (int) (event.getRawY() - startY - statusBarHeight);
                        Log.e("TAG", "x---->" + mLayoutParams.x);
                        Log.e("TAG", "y---->" + mLayoutParams.y);
                        updateViewLayout();   //更新mView 的位置

                        mCustomCancelView.startAnimate(true);
                        mCustomCancelView.isInSide(isRemove(mLayoutParams.x + (mView.getMeasuredWidth() >> 1),
                                mLayoutParams.y + ((mView.getMeasuredHeight() >> 1))));
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (isPerformClick) {
                            mView.performClick();
                        }
                        isRemove = isRemove(mLayoutParams.x + (mView.getMeasuredWidth() >> 1),
                                mLayoutParams.y + ((mView.getMeasuredHeight() >> 1)));

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
                            mCustomCancelView.startAnimate(false);
                            stickToSide();
                        }
                        return !isPerformClick;
                }
                return false;
            }

            private void stickToSide() {
                ValueAnimator animator = ValueAnimator.ofInt(mLayoutParams.x, finalMoveX).setDuration(Math.abs(mLayoutParams.x - finalMoveX));
                animator.setInterpolator(new BounceInterpolator());
                animator.addUpdateListener(animation -> {
                    mLayoutParams.x = (int) animation.getAnimatedValue();
                    updateViewLayout();
                });
                animator.start();
            }
        });
    }

    private boolean isRemove(int centerX, int centrY) {
        return mDeleteRect.contains(centerX, centrY);
    }

    private void updateViewLayout() {
        if (null != mView && null != mLayoutParams) {
            mWindowManager.updateViewLayout(mView, mLayoutParams);
        }
    }

    public void hideWindow() {
        if (mView != null) {
            mView.setVisibility(View.GONE);
        }
        if (mCustomCancelView != null) {
            mCustomCancelView.setVisibility(View.GONE);
        }
    }

    public void visibleWindow() {
        if (mView != null) {
            mView.setVisibility(View.VISIBLE);
        }
        if (mCustomCancelView != null) {
            mCustomCancelView.setVisibility(View.VISIBLE);
        }
    }

    interface OnPermissionListener {
        void showPermissionDialog();
    }
}