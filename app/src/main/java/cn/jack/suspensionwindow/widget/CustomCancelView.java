package cn.jack.suspensionwindow.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import cn.jack.suspensionwindow.R;
import cn.jack.suspensionwindow.base.SimpleCallback;
import cn.jack.suspensionwindow.util.DisplayUtil;

/**
 * Created by manji
 * Date：2018/11/16 1:41 PM
 * Desc：
 */
public class CustomCancelView extends View {

    private static final int STATE_NORMAL = 0x1000;
    private static final int STATE_PROCESSING = 0x2000;

    private static final int OPEN_MASK = 0x0001;
    private static final int CLOSE_MASK = OPEN_MASK << 1;


    private int mState = STATE_NORMAL;


    private Paint mPaint;
    private float mRadius;
    private float mTouchExpand = 20;
    private boolean isInSide = false;
    private boolean isInit = true;
    private int defaultSize = 150;

    public CustomCancelView(Context context) {
        this(context, null);
    }

    public CustomCancelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomCancelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CustomCancelView, defStyleAttr, 0);
        int mColor = array.getColor(R.styleable.CustomCancelView_view_color, Color.RED);
        mRadius = array.getDimension(R.styleable.CustomCancelView_view_radius, 0);
        array.recycle();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setColor(mColor);
        setVisibility(GONE);
    }


    public void startAnimate(boolean isOpen) {
        startAnimate(isOpen, null);
    }

    public void startAnimate(final boolean isOpen, final SimpleCallback<Void> callback) {
        if (isInit) {
            setTranslationX(getMeasuredWidth());
            setTranslationY(getMeasuredHeight());
            isInit = false;
        }
        if (isOpen && (mState & OPEN_MASK) != 0) return;
        if (!isOpen && (mState & CLOSE_MASK) != 0) return;

        animate().translationX(isOpen ? 0 : getMeasuredWidth())
                .translationY(isOpen ? 0 : getMeasuredHeight())
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (isOpen) {
                            if (getVisibility() != View.VISIBLE) {
                                setVisibility(VISIBLE);
                            }
                        }
                        mState = STATE_PROCESSING | (isOpen ? OPEN_MASK : CLOSE_MASK);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!isOpen) {
                            if (callback != null) {
                                callback.onCall(null);
                            }
                        }
                        mState = STATE_NORMAL | (isOpen ? OPEN_MASK : CLOSE_MASK);
                    }
                })
                .setStartDelay(500)
                .start();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(getWidth(), getHeight());
        canvas.drawCircle(0, 0, mRadius + (isInSide ? mTouchExpand : 0), mPaint);
    }


    public void isInSide(boolean isInSide) {
        this.isInSide = isInSide;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width, height;
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            width = size;
        } else {
            width = DisplayUtil.dip2px(defaultSize);
        }

        mode = MeasureSpec.getMode(heightMeasureSpec);
        size = MeasureSpec.getSize(heightMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            height = size;
        } else {
            height = DisplayUtil.dip2px(defaultSize);
        }
        setMeasuredDimension(width, height);
        mRadius = Math.min(width, height) - mTouchExpand;
    }
}