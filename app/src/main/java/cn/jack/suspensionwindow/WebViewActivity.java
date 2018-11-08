package cn.jack.suspensionwindow;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import cn.jack.suspensionwindow.bean.ArticleBean;
import cn.jack.suspensionwindow.util.SPUtil;
import cn.jack.suspensionwindow.window.WindowShowService;
import cn.jack.suspensionwindow.window.rom.RomUtils;
import cn.jake.share.frdialog.dialog.FRDialog;

/**
 * Created by manji
 * Date：2018/8/9 下午4:40
 * Desc：
 */
public class WebViewActivity extends FragmentActivity {

    public static final String ARTICLE_ID = "article_id";
    public static final String ARTICLE_JUMP_URL = "article_jump_url";
    public static final String ARTICLE_IMAGE_URL = "article_image_url";
    private static final String ARTICLE_BEAN = "article_bean";
    private static final String URL = "url";

    public static final String BROAD_CAST_NAME = "window_broad_cast";

    private WebView mWebView;
    private TextView tvBack;
    private TextView tvRight;
    private MyReceiver receiver;

    private String mUrl;

    private ArticleBean mArticleBean;
    private boolean isShowWindow;

    public static void start(Context context, ArticleBean articleBean) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARTICLE_BEAN, articleBean);
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void start(Context context, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(URL, url);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mWebView = findViewById(R.id.aw_webview);
        tvBack = findViewById(R.id.aw_tv_back);
        tvRight = findViewById(R.id.aw_tv_right);

        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BROAD_CAST_NAME);//从系统底层获得这条广播
        registerReceiver(receiver, filter);//注册广播接收器

        mArticleBean = getIntent().getParcelableExtra(ARTICLE_BEAN);

        if (null != mArticleBean) {
            mUrl = mArticleBean.getJumpUrl();
        } else {
            mUrl = getIntent().getStringExtra(URL);
            stopService(new Intent(WebViewActivity.this, WindowShowService.class));
        }

        if (SPUtil.getIntDefault(ARTICLE_ID, -1) > 0) {
            //已经悬浮了
            isShowWindow = true;
        } else {
            isShowWindow = false;
        }

        initWebViewSetting();
        initListener();
    }

    private void initListener() {
        tvBack.setOnClickListener(v -> {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                finish();
            }
        });

        tvRight.setOnClickListener(v -> {
            new FRDialog.CommonBuilder(this)
                    .setContentView(R.layout.article_dialog_bottom)
                    .setFromBottom()
                    .setFullWidth()
                    .setHeight(400)
                    .setText(R.id.adb_tv_window, isShowWindow ? "关闭悬浮" : "开启悬浮")
                    .setOnClickListener(R.id.adb_tv_window, view -> {
                        if (isShowWindow) {
                            //需要关闭悬浮
                            isShowWindow = false;
                            SPUtil.setIntDefault(ARTICLE_ID, -1);
                            stopService(new Intent(WebViewActivity.this, WindowShowService.class));
                        } else {
                            //需要开启悬浮并退出WebView
                            if (RomUtils.checkFloatWindowPermission(WebViewActivity.this)) {
                                //有权限，直接保存文章信息
                                isShowWindow = true;
                                SPUtil.setIntDefault(ARTICLE_ID, mArticleBean.getId());
                                SPUtil.setStringDefault(ARTICLE_JUMP_URL, mArticleBean.getJumpUrl());
                                SPUtil.setStringDefault(ARTICLE_IMAGE_URL, mArticleBean.getImageUrl());
                                startService(new Intent(WebViewActivity.this, WindowShowService.class));
                                finish();
                            } else {
                                //无权限，直接启动，后面会通过广播进行通知
                                startService(new Intent(WebViewActivity.this, WindowShowService.class));
                            }
                        }
                        return true;
                    })
                    .show();
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebViewSetting() {
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        WebSettings webSetting = mWebView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(false);
        webSetting.setBuiltInZoomControls(false);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(false);
        webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        webSetting.setDatabaseEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setAppCachePath(getDir("appCache", Context.MODE_PRIVATE).getPath());
        webSetting.setDatabasePath(getDir("databases", Context.MODE_PRIVATE).getPath());
        webSetting.setGeolocationDatabasePath(getDir("geolocation", Context.MODE_PRIVATE).getPath());
        webSetting.setPluginState(WebSettings.PluginState.ON);
        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSetting.setTextSize(WebSettings.TextSize.NORMAL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSetting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        mWebView.loadUrl(mUrl);
    }

    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();//得到Service发送的广播
            if (BROAD_CAST_NAME.equals(action)) {
                boolean isSuccess = intent.getBooleanExtra("permission", false);
                if (isSuccess) {
                    isShowWindow = true;
                    SPUtil.setIntDefault(ARTICLE_ID, mArticleBean.getId());
                    SPUtil.setStringDefault(ARTICLE_JUMP_URL, mArticleBean.getJumpUrl());
                    SPUtil.setStringDefault(ARTICLE_IMAGE_URL, mArticleBean.getImageUrl());
                    finish();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mArticleBean == null && isShowWindow) {
            startService(new Intent(WebViewActivity.this, WindowShowService.class));
        }
    }
}