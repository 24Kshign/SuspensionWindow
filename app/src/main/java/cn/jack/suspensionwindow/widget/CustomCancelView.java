package cn.jack.suspensionwindow.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import cn.jack.suspensionwindow.R;
import cn.jack.suspensionwindow.util.DisplayUtil;

/**
 * Created by manji
 * Date：2018/11/16 1:41 PM
 * Desc：
 */
public class CustomCancelView extends View {

    private Paint mPaint;

    private float mRadius;

    private ValueAnimator mAnimator;

    private float mCurrentRadius = 0;

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
        mRadius = array.getDimension(R.styleable.CustomCancelView_view_radius, DisplayUtil.dip2px(200));
        array.recycle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(mColor);
    }


    public void startAnim(boolean isOpen) {
        float start = isOpen ? 0 : mCurrentRadius;
        float end = isOpen ? mRadius : 0;
        mAnimator = ValueAnimator.ofFloat(start, end).setDuration(400);
        mAnimator.addUpdateListener(animation -> {
            mCurrentRadius = (float) animation.getAnimatedValue();
            if (!isOpen && mCurrentRadius == 0) {
                setVisibility(View.GONE);
            } else {
                setVisibility(View.VISIBLE);
            }
            postInvalidate();
        });
        if (isOpen) {
            mAnimator.setStartDelay(100);
        }
        mAnimator.start();
    }

    public void destroy() {
        if (null != mAnimator) {
            mAnimator.cancel();
            mAnimator = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(getWidth() + mRadius - mCurrentRadius, getHeight() + mRadius - mCurrentRadius, mRadius, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width, height;
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            width = size;
        } else {
            width = (int) (2 * mRadius);
        }

        mode = MeasureSpec.getMode(heightMeasureSpec);
        size = MeasureSpec.getSize(heightMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            height = size;
        } else {
            height = (int) (2 * mRadius);
        }
        setMeasuredDimension(width, height);
    }
}