package cn.jack.suspensionwindow.ui;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import cn.jack.suspensionwindow.R;
import cn.jack.suspensionwindow.util.DisplayUtil;


/**
 * Created by manji
 * Date：2018/8/9 下午5:16
 * Desc：
 */
public class LoginActivity extends FragmentActivity {

    //    private Intent serviceIntent;
    private ImageView imageView;

    private ConstraintLayout.LayoutParams layoutParams;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        imageView = findViewById(R.id.al_iv_image);

//        imageView.post(new Runnable() {
//            @Override
//            public void run() {
//                Log.e("TAG", "left111--->" + imageView.getLeft());
//                Log.e("TAG", "right111--->" + imageView.getRight());
//                Log.e("TAG", "top111--->" + imageView.getTop());
//                Log.e("TAG", "bottom111--->" + imageView.getBottom());
//            }
//        });

        imageView.setOnTouchListener(new View.OnTouchListener() {
            int startX, startY;
            int l, r, t, b;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int endX = (int) event.getX();
                        int endY = (int) event.getY();

                        int dx = endX - startX;
                        int dy = endY - startY;

                        l = imageView.getLeft() + dx;
                        r = imageView.getRight() + dx;
                        t = imageView.getTop() + dy;
                        b = imageView.getBottom() + dy;

                        if (t <= 0) {
                            t = 0;
                            b = imageView.getHeight();
                        }
                        if (b >= DisplayUtil.getRealHeight(LoginActivity.this)) {
                            b = DisplayUtil.getRealHeight(LoginActivity.this);
                            t = b - imageView.getHeight();
                        }

                        if (l <= 0) {
                            l = 0;
                            r = imageView.getWidth();
                        }
                        if (r >= DisplayUtil.getScreenWidth(LoginActivity.this)) {
                            r = DisplayUtil.getScreenWidth(LoginActivity.this);
                            l = r - imageView.getWidth();
                        }
                        imageView.layout(l, t, r, b);
                        break;
                    case MotionEvent.ACTION_UP:
                        int finalX;
                        if (l > 0 && r < DisplayUtil.getScreenWidth(LoginActivity.this)) {
                            Log.e("TAG", "l-->" + l);
                            Log.e("TAG", "r-->" + r);
                            if (r - imageView.getWidth() / 2 >= DisplayUtil.getScreenWidth(LoginActivity.this) / 2) {
                                finalX = DisplayUtil.getScreenWidth(LoginActivity.this) - imageView.getWidth();
                            } else {
                                finalX = 0;
                            }
                            Log.e("TAG", "finalX-->" + finalX);
                            ValueAnimator animator = ValueAnimator.ofInt(l, finalX).setDuration(Math.abs(finalX - l));
                            animator.addUpdateListener(animation -> {
                                int curL = (int) animation.getAnimatedValue();
                                imageView.layout(curL, t, curL + imageView.getWidth(), b);
                            });
                            animator.start();
                        }
                        break;
                }
                return true;
            }
        });

//        findViewById(R.id.al_btn_start).setOnClickListener(v -> {
//            serviceIntent = new Intent(this, CustomService.class);
//            startService(serviceIntent);
//        });
//
//        findViewById(R.id.al_btn_stop).setOnClickListener(v -> {
//            if (null != serviceIntent) {
//                stopService(serviceIntent);
//            }
//        });
//
//        findViewById(R.id.al_btn_bind).setOnClickListener(v -> {
//
//        });
//
//        findViewById(R.id.al_btn_unbind).setOnClickListener(v -> {
//
//        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
