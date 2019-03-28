package cn.jack.suspensionwindow.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import cn.jack.suspensionwindow.R;
import cn.jack.suspensionwindow.bean.ArticleBean;
import cn.jack.suspensionwindow.util.SPUtil;
import cn.jack.suspensionwindow.window.WindowShowService;
import cn.jack.suspensionwindow.window.WindowUtil;
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

    private String mUrl;

    private ArticleBean mArticleBean;
    private boolean isShowWindow;

    private MyReceiver receiver;


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
            WindowUtil.getInstance().hideWindow();
        }

        isShowWindow = isShow();

        initWebViewSetting();
        initListener();
    }

    private boolean isShow() {
        return SPUtil.getIntDefault(ARTICLE_ID, -1) > 0;
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
            isShowWindow = isShow();
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
                                setSpDate(mArticleBean.getId(), mArticleBean.getJumpUrl(), mArticleBean.getImageUrl());
                            } else {
                                isShowWindow = false;
                                setSpDate(-1, "", "");
                            }
                        }
                        return true;
                    })
                    .show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        RomUtils.onActivityResult(this, requestCode, resultCode, data);
    }

    private void setSpDate(int id, String jumpUrl, String imageUrl) {
        SPUtil.setIntDefault(ARTICLE_ID, id);
        SPUtil.setStringDefault(ARTICLE_JUMP_URL, jumpUrl);
        SPUtil.setStringDefault(ARTICLE_IMAGE_URL, imageUrl);
        startService(new Intent(WebViewActivity.this, WindowShowService.class));
        if (isShowWindow) {
            finish();
        }
    }

    private void showDialog() {
        FRDialog dialog = new FRDialog.MDBuilder(this)
                .setTitle("悬浮窗权限")
                .setMessage("您的手机没有授予悬浮窗权限，请开启后再试")
                .setPositiveContentAndListener("现在去开启", view -> {
                    RomUtils.applyPermission(this, () -> {
                        new Handler().postDelayed(() -> {
                            if (!RomUtils.checkFloatWindowPermission(this)) {
                                // 授权失败
                                showDialog();
                            } else {
                                //授权成功
                                isShowWindow = true;
                                setSpDate(mArticleBean.getId(), mArticleBean.getJumpUrl(), mArticleBean.getImageUrl());
                            }
                        }, 500);
                    });
                    return true;
                }).setNegativeContentAndListener("暂不开启", view -> true).create();
        dialog.show();
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();//得到Service发送的广播
            if (BROAD_CAST_NAME.equals(action)) {
                showDialog();
            }
        }
    }

    @Override
    protected void onDestroy() {
        WindowUtil.getInstance().visibleWindow();
        super.onDestroy();
        unregisterReceiver(receiver);
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

    @Override
    protected void onStop() {
        super.onStop();
        //点击悬浮窗进来之后是不会有弹窗的，返回之后才有，这里判断一下进来的场景
        if (mArticleBean == null && isShowWindow) {
            startService(new Intent(WebViewActivity.this, WindowShowService.class));
        }
    }
}