package com.webchecker.mm;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.widget.FrameLayout;
import android.view.ViewGroup.LayoutParams;
import android.content.ActivityNotFoundException;
import java.net.URISyntaxException;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private static final String PREFS_NAME = "UrlPrefs";
    private static final String URL_KEY = "savedUrl";
    private static final String AUTO_PROTOCOL_KEY = "autoProtocol";
    private boolean isPageLoaded = false; // 新增标志位，跟踪页面是否已成功加载
    private View customView; // 用于全屏显示的视图
    private WebChromeClient.CustomViewCallback customViewCallback; // 全屏回调
    private FrameLayout fullscreenContainer; // 全屏容器
    private ViewGroup webViewParent; // 保存WebView的父容器
    private LayoutParams webViewLayoutParams; // 保存WebView的布局参数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置沉浸模式，隐藏状态栏和导航栏
        hideSystemUI();

        // 初始化WebView
        webView = findViewById(R.id.webView);

        // 配置WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // 启用JavaScript
        webSettings.setDomStorageEnabled(true); // 启用DOM存储
        webSettings.setAllowFileAccess(true); // 允许文件访问
        webSettings.setAllowContentAccess(true); // 允许内容访问
        webSettings.setLoadWithOverviewMode(true); // 缩放至适合屏幕
        webSettings.setUseWideViewPort(true); // 支持宽视图
        webSettings.setSupportZoom(false); // 禁用缩放
        webSettings.setBuiltInZoomControls(false); // 禁用内置缩放控件
        webSettings.setMediaPlaybackRequiresUserGesture(false); // 允许自动播放媒体
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT); // 启用缓存

        // 设置WebChromeClient以支持全屏视频
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                // 进入全屏模式
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }

                customView = view;
                customViewCallback = callback;

                // 保存WebView的父容器和布局参数
                webViewParent = (ViewGroup) webView.getParent();
                webViewLayoutParams = webView.getLayoutParams();
                webViewParent.removeView(webView);

                // 创建全屏容器
                fullscreenContainer = new FrameLayout(MainActivity.this);
                fullscreenContainer.addView(view, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                setContentView(fullscreenContainer);

                // 设置系统UI为全屏
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );                
            }

            @Override
            public void onHideCustomView() {
                super.onHideCustomView();
                // 退出全屏模式
                if (customView == null) {
                    return;
                }

                // 恢复系统UI并重新设置沉浸模式
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                // 延迟重新设置沉浸模式，确保UI完全恢复后再隐藏导航栏
                getWindow().getDecorView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideSystemUI();
                    }
                }, 100);

                // 移除全屏视图
                ViewGroup parent = (ViewGroup) customView.getParent();
                parent.removeView(customView);
                customView = null;

                // 移除全屏容器
                if (fullscreenContainer != null) {
                    setContentView(webViewParent);
                    fullscreenContainer = null;
                }

                // 将WebView放回原始位置
                if (webViewParent != null && webViewLayoutParams != null) {
                    webViewParent.addView(webView, webViewLayoutParams);
                }

                // 调用回调
                if (customViewCallback != null) {
                    customViewCallback.onCustomViewHidden();
                    customViewCallback = null;
                }
            }
        });

        // 设置WebViewClient以在WebView中打开链接和处理错误
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                
                // 处理特殊的URL scheme
                if (url.startsWith("intent://") || url.startsWith("market://")) {
                    // 检查URL是否包含下载相关关键词
                    if (url.contains("download") || url.contains("app") || url.contains("install") || url.contains("apk")) {
                        // 屏蔽下载链接，显示提示信息
                        Toast.makeText(MainActivity.this, "已阻止应用下载链接", Toast.LENGTH_SHORT).show();
                        return true;
                    } else {
                        try {
                            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                            if (intent != null) {
                                // 检查是否有应用可以处理这个Intent
                                if (getPackageManager().resolveActivity(intent, 0) != null) {
                                    startActivity(intent);
                                    return true;
                                }
                            }
                        } catch (URISyntaxException e) {
                            // 处理异常
                            e.printStackTrace();
                        }
                        // 如果无法处理intent，尝试在浏览器中打开
                        String fallbackUrl = request.getUrl().getQueryParameter("fallback_url");
                        if (fallbackUrl != null) {
                            view.loadUrl(fallbackUrl);
                            return true;
                        }
                    }
                } else if (url.startsWith("weixin://") || url.startsWith("alipays://")) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        if (intent != null) {
                            // 检查是否有应用可以处理这个Intent
                            if (getPackageManager().resolveActivity(intent, 0) != null) {
                                startActivity(intent);
                                return true;
                            }
                        }
                    } catch (URISyntaxException e) {
                        // 处理异常
                        e.printStackTrace();
                    }
                    // 如果无法处理intent，尝试在浏览器中打开
                    String fallbackUrl = request.getUrl().getQueryParameter("fallback_url");
                    if (fallbackUrl != null) {
                        view.loadUrl(fallbackUrl);
                        return true;
                    }
                }
                
                // 对于http/https链接，允许在WebView中加载
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    view.loadUrl(url);
                    return true;
                }
                
                // 处理未知协议，尝试转换为http/https
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    try {
                        // 检查是否是常见的协议前缀
                        if (url.contains("://")) {
                            // 提取域名部分并添加https前缀
                            String domain = url.substring(url.indexOf("://") + 3);
                            if (!domain.isEmpty()) {
                                String httpsUrl = "https://" + domain;
                                view.loadUrl(httpsUrl);
                                return true;
                            }
                        } else {
                            // 如果没有协议前缀，直接添加https
                            String httpsUrl = "https://" + url;
                            view.loadUrl(httpsUrl);
                            return true;
                        }
                    } catch (Exception e) {
                        // 转换失败，尝试系统浏览器
                        e.printStackTrace();
                    }
                }
                
                // 对于其他不支持的scheme，尝试用系统默认浏览器打开
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                        return true;
                    } else {
                        // 没有应用可以处理，显示更友好的错误提示
                        Toast.makeText(MainActivity.this, "无法识别的链接格式: " + url, Toast.LENGTH_LONG).show();
                        return true;
                    }
                } catch (ActivityNotFoundException e) {
                    // 如果没有应用可以处理这个URL，显示错误信息
                    Toast.makeText(MainActivity.this, "无法打开链接: " + url, Toast.LENGTH_SHORT).show();
                    return false;
                } catch (Exception e) {
                    // 其他异常，显示错误信息
                    Toast.makeText(MainActivity.this, "链接格式错误: " + url, Toast.LENGTH_SHORT).show();
                    return false;
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                isPageLoaded = true; // 页面加载完成，设置标志位
                // 页面加载完成后再次设置沉浸模式，确保内容完全填满屏幕
                hideSystemUI();
                // 确保WebView没有内边距
                view.setPadding(0, 0, 0, 0);
                view.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                String url = request.getUrl().toString();
                boolean isAutoProtocol = getAutoProtocolFlag();

                // 只有当主文档请求失败且页面尚未成功加载时，才处理错误
                if (!isPageLoaded && request.isForMainFrame()) {
                    // 获取错误码
                    int errorCode = 0;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        errorCode = error.getErrorCode();
                    }
                    
                    // 处理未知协议错误
                    if (errorCode == WebViewClient.ERROR_UNSUPPORTED_SCHEME) {
                        Toast.makeText(MainActivity.this, "不支持的链接格式，已尝试转换为标准网页格式", Toast.LENGTH_LONG).show();
                        // 尝试将URL转换为标准格式
                        String correctedUrl = url;
                        
                        // 处理常见的URL格式问题
                        if (url.startsWith("https://url=https//")) {
                            correctedUrl = url.replace("https://url=https//", "https://");
                        } else if (url.startsWith("https://utils/")) {
                            correctedUrl = url.replace("https://utils/", "https://");
                        } else if (url.contains("://")) {
                            // 提取域名部分并添加https前缀
                            String domain = url.substring(url.indexOf("://") + 3);
                            if (!domain.isEmpty()) {
                                correctedUrl = "https://" + domain;
                            }
                        } else {
                            // 添加默认https协议
                            correctedUrl = "https://" + url;
                        }
                        
                        if (!correctedUrl.equals(url)) {
                            saveUrl(correctedUrl, false);
                            webView.loadUrl(correctedUrl);
                            return;
                        }
                    }
                    
                    // 如果是DNS解析错误，尝试修复URL并重新加载
                    if (errorCode == WebViewClient.ERROR_HOST_LOOKUP) {
                        // 检查网络连接状态
                        if (!isNetworkAvailable()) {
                            Toast.makeText(MainActivity.this, "无网络连接，请检查网络设置", Toast.LENGTH_LONG).show();
                        } else {
                            // 检查URL是否包含格式错误
                            if (url != null) {
                                // 尝试使用addProtocolPrefix方法标准化URL
                                String correctedUrl = addProtocolPrefix(url);

                                // 如果URL发生了变化，尝试重新加载
                                if (!correctedUrl.equals(url)) {
                                    Toast.makeText(MainActivity.this, "URL格式可能有误，已尝试修复", Toast.LENGTH_LONG).show();
                                    saveUrl(correctedUrl, false);
                                    webView.loadUrl(correctedUrl);
                                    return;
                                }

                                // 特殊格式URL处理
                                if (url.startsWith("https://url=https//")) {
                                    correctedUrl = url.replace("https://url=https//", "https://");
                                    Toast.makeText(MainActivity.this, "URL格式错误，已尝试修复", Toast.LENGTH_LONG).show();
                                    saveUrl(correctedUrl, false);
                                    webView.loadUrl(correctedUrl);
                                    return;
                                }
                                if (url.startsWith("https://utils/")) {
                                    correctedUrl = url.replace("https://utils/", "https://");
                                    Toast.makeText(MainActivity.this, "URL格式错误，已尝试修复", Toast.LENGTH_LONG).show();
                                    saveUrl(correctedUrl, false);
                                    webView.loadUrl(correctedUrl);
                                    return;
                                }
                            }
                            Toast.makeText(MainActivity.this, "DNS解析失败，可能是网络问题或URL格式错误", Toast.LENGTH_LONG).show();
                        }
                        clearSavedUrl();
                        showUrlInputDialog();
                        return;
                    }
                    
                    // 如果是连接超时或网络错误，提示用户
                    if (errorCode == WebViewClient.ERROR_TIMEOUT || errorCode == WebViewClient.ERROR_CONNECT 
                            || errorCode == WebViewClient.ERROR_IO) {
                        Toast.makeText(MainActivity.this, "网络连接超时，请检查网络设置", Toast.LENGTH_LONG).show();
                        clearSavedUrl();
                        showUrlInputDialog();
                        return;
                    }
                    
                    // 如果是自动添加的HTTPS失败，尝试HTTP
                    if (url.startsWith("https://") && isAutoProtocol) {
                        String httpUrl = url.replace("https://", "http://");
                        Toast.makeText(MainActivity.this, "HTTPS访问失败，尝试HTTP", Toast.LENGTH_SHORT).show();
                        // 更新为非自动协议标记
                        saveUrl(httpUrl, false);
                        webView.loadUrl(httpUrl);
                    } else {
                        // URL加载失败，提示用户重新输入
                        Toast.makeText(MainActivity.this, "URL无效，请重新输入", Toast.LENGTH_SHORT).show();
                        clearSavedUrl();
                        showUrlInputDialog();
                    }
                }
            }
        });

        // 检查网络连接状态
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "无网络连接，请检查网络设置", Toast.LENGTH_LONG).show();
            showUrlInputDialog();
        } else {
            // 检查是否有保存的URL
            String savedUrl = getSavedUrl();
            if (savedUrl != null && !savedUrl.isEmpty()) {
                // 加载保存的URL
                webView.loadUrl(savedUrl);
            } else {
                // 没有保存的URL，显示输入对话框
                showUrlInputDialog();
            }
        }
        
        // 生产环境关掉调试
        WebView.setWebContentsDebuggingEnabled(false);
        
        // 允许 WebView 走系统代理，兼容公司/校园网
        if (androidx.webkit.WebViewFeature.isFeatureSupported(androidx.webkit.WebViewFeature.PROXY_OVERRIDE)) {
            androidx.webkit.ProxyController.getInstance().setProxyOverride(new androidx.webkit.ProxyConfig.Builder().build(), Runnable::run, () -> {});
        }
    }

    // 设置沉浸模式，隐藏状态栏和导航栏
    private void hideSystemUI() {
        // 全屏显示
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 允许应用内容延伸到系统装饰区域
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
        } else {
            // 旧版本设置SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN等标志已经隐含了这个行为
        }

        // 针对不同Android版本设置沉浸式模式
        View decorView = window.getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上使用新API
            WindowInsetsController controller = decorView.getWindowInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            // 旧版本兼容
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    // 处理屏幕旋转事件
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        // 重新设置沉浸模式
        hideSystemUI();
    }

    // 在窗口获得焦点时保持沉浸模式
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    // 显示URL输入对话框
    private void showUrlInputDialog() {
        // 如果页面已经加载成功，不再显示对话框
        if (isPageLoaded) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请输入网址");

        // 设置输入框
        final EditText input = new EditText(this);
        builder.setView(input);

        // 设置确定按钮
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String url = input.getText().toString().trim();
                if (!url.isEmpty()) {
                    // 检查是否已包含协议
                    boolean hasProtocol = url.startsWith("http://") || url.startsWith("https://");
                    // 自动添加协议前缀
                    url = addProtocolPrefix(url);
                    // 保存URL和协议标记
                    saveUrl(url, !hasProtocol);
                    // 加载URL
                    webView.loadUrl(url);
                } else {
                    Toast.makeText(MainActivity.this, "网址不能为空", Toast.LENGTH_SHORT).show();
                    showUrlInputDialog(); // 重新显示对话框
                }
            }
        });

        // 设置取消按钮
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish(); // 关闭应用
            }
        });

        // 显示对话框
        builder.show();
    }

    // 为URL添加协议前缀
    private String addProtocolPrefix(String url) {
        // 处理格式错误的URL，如 url=https//...
        if (url.startsWith("url=")) {
            url = url.substring(4); // 移除 "url=" 前缀
        }
        
        // 处理特殊格式URL，如 https://utils/...
        if (url.startsWith("https://utils/")) {
            url = url.replace("https://utils/", "https://");
        }
        
        // 处理没有协议前缀的URL
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            // 处理未知协议
            if (url.contains("://")) {
                // 提取域名部分并添加https前缀
                String domain = url.substring(url.indexOf("://") + 3);
                if (!domain.isEmpty()) {
                    return "https://" + domain;
                }
            }
            // 对于国内搜索引擎和常见网站，优先使用HTTPS
            return "https://" + url;
        }
        return url;
    }

    // 保存URL到SharedPreferences
    private void saveUrl(String url, boolean isAutoProtocol) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(URL_KEY, url);
        editor.putBoolean(AUTO_PROTOCOL_KEY, isAutoProtocol);
        editor.apply();
    }

    // 从SharedPreferences获取保存的URL
    private String getSavedUrl() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(URL_KEY, "");
    }

    // 获取自动协议标记
    private boolean getAutoProtocolFlag() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(AUTO_PROTOCOL_KEY, false);
    }

    // 清除保存的URL
    private void clearSavedUrl() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(URL_KEY);
        editor.remove(AUTO_PROTOCOL_KEY);
        editor.apply();
    }

    // 检查网络连接状态
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnected();
            } else {
                // 旧版本API
                NetworkInfo[] networks = connectivityManager.getAllNetworkInfo();
                for (NetworkInfo network : networks) {
                    if (network != null && network.isConnected()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // 处理返回键
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}