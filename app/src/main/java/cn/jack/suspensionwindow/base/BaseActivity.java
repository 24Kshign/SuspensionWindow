package cn.jack.suspensionwindow.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

/**
 * Created by manji
 * Date：2018/11/19 3:45 PM
 * Desc：
 */
public abstract class BaseActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getLayoutRes() <= 0) {
            throw new IllegalArgumentException("xml should not be null");
        }
        setContentView(getLayoutRes());
    }

    protected abstract int getLayoutRes();

    protected void initView() {

    }
}
