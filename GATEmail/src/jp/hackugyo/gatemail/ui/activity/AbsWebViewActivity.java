package jp.hackugyo.gatemail.ui.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Timer;
import java.util.TimerTask;

import jp.hackugyo.gatemail.R;
import jp.hackugyo.gatemail.ui.AbsCustomAlertDialogFragment;
import jp.hackugyo.gatemail.ui.AbsFragmentActivity;
import jp.hackugyo.gatemail.ui.fragment.PlainAlertDialogFragment;
import jp.hackugyo.gatemail.util.LogUtils;
import jp.hackugyo.gatemail.util.StringUtils;

import org.apache.http.protocol.HTTP;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.ViewConfiguration;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

/**
 * WebViewの親abstractクラス
 * 
 * @author kwatanabe
 * 
 */
abstract public class AbsWebViewActivity extends AbsFragmentActivity implements AbsCustomAlertDialogFragment.Callbacks {
    public static final String TARGET_URL_KEY = "target_url";
    /**
     * ActionBarの見た目情報受け渡し用のExtraのKey
     */
    public static final String INTENT_EXTRA_KEY_SHOW_HOME_BUTTON = "INTENT_EXTRA_KEY_SHOW_HOME_BUTTON";
    /** メニュー指定用のKey */
    public static final String INTENT_EXTRA_KEY_SHOW_MENU_OF_PRODUCT_LINEUP = "INTENT_EXTRA_KEY_SHOW_MENU_OF_PRODUCT_LINEUP";
    /** 表示方法指定 */
    public static final boolean WITH_HOME = true;
    public static final boolean NO_HOME = false;

    private final AbsWebViewActivity self = this;
    private WebView mWebView;
    private String mInitialTargetUrl;
    /** 戻る，進む，更新，中止のボタン */
    ImageButton mBackController, mNextController, mReloadController, mStopController;

    /***********************************************
     * Life Cycle *
     ***********************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFinishing()) {
            return;
        }
        setContentView(R.layout.activity_webview);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setView();
        setWebView();
        if (mWebView.getOriginalUrl() == null) {
            // バックグラウンドから復帰させ，onStartが再度呼ばれても，最初のURLを再読み込みしないよう修正
            mWebView.loadUrl(mInitialTargetUrl);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // gifアニメが中にあった場合，停止させる
        // @see http://starzero.hatenablog.com/entry/20120716/1342421720
        mWebView.resumeTimers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.pauseTimers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // WebViewを廃棄
        mWebView.stopLoading();
        mWebView.clearCache(true);
        unregisterForContextMenu(mWebView);

        // ここで，WebViewを即座に廃棄してしまうと，ZoomControlのfadingのバグが発生する．
        // http://stackoverflow.com/questions/5267639/how-to-safely-turn-webview-zooming-on-and-off-as-needed
        long timeout = ViewConfiguration.getZoomControlsTimeout();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mWebView.destroy();
                mWebView = null;
            }
        }, timeout + 100l);

        // クッキーを削除しない
        // CookieManager cookieManager = CookieManager.getInstance();
        // cookieManager.removeAllCookie();
    }

    /***********************************************
     * View *
     ***********************************************/

    protected void setWebView() {

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setWebChromeClient(mWebChromeClient);
        // mWebView.setBackgroundColor(0); // 背景を透明に

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setGeolocationEnabled(true);
        webSettings.setJavaScriptEnabled(false);
        webSettings.setSaveFormData(false);
        webSettings.setSavePassword(false);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        mWebView.setVerticalScrollbarOverlay(true);
        mWebView.setHorizontalScrollbarOverlay(true);

        // WebViewで使うcookieの準備
        CookieSyncManager.createInstance(self);
        CookieSyncManager.getInstance().startSync();
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().removeExpiredCookie();

        mInitialTargetUrl = getIntent().getStringExtra(TARGET_URL_KEY);
    }

    protected void setView() {
        setActionBar();
    }

    protected abstract void setActionBar();

    /**
     * 「戻る」動作」．メニューが出ていない状態でのバックキー押下時など
     * バックキー押下時に「戻る」を許さずActivityを終了させたい場合は，このメソッドをoverrideしてください．
     */
    protected boolean goBack() {
        mWebView.stopLoading();
        mWebView.goBack();
        return true;
    }

    /***********************************************
     * WebViewClient *
     **********************************************/

    /**
     * カスタムwebViewClient 読み込み進捗のプログレスダイアログを表示します． メールリンク・電話番号リンクを正しくハンドリングします．
     * 
     */
    private final WebViewClient mWebViewClient = new WebViewUnlimitedClient() {
        private boolean mIsLoadingFinished = true;
        private boolean mIsRedirected = false;

        @Override
        public void onPageStarted(WebView webView, String url, Bitmap favicon) {
            mIsLoadingFinished = false;
            super.onPageStarted(webView, url, favicon);

            webView.clearView();
        }

        /**
         * {@link WebViewClientWithCookie#onPageFinished(WebView, String)}
         * をoverrideした．
         * GingerBreadやそれ以前では，リダイレクト後にonPageFinished()が呼ばれないバグがあるため，
         * リダイレクトは一律に新規読み込みとしている． そのフラグ処理の箇所は，HoneyComb以降では必ずスキップできるようにしている．
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            if (!checkUrl(url)) {
                view.stopLoading();
                view.clearView();
            }

            super.onPageFinished(view, url);
            if (!mIsRedirected) mIsLoadingFinished = true;
            if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) && mIsRedirected) {
                mIsRedirected = false;
            } else {
                //HIDE LOADING. IT HAS FINISHED.
                getSupportActionBar().setTitle(view.getTitle());
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        /**
         * {@link WebViewClient#shouldOverrideUrlLoading(WebView, String)} の実装．
         * ホワイトリストをチェックする． また，MailTo,
         * Dialに反応しないWebViewが機種により存在するので，こちらでハンドリングする．
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (!checkUrl(url)) {
                // これ以上何もせずに終了
                return true;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                // 素通しする
                return super.shouldOverrideUrlLoading(view, url);
            }

            // 以下の処理は，GingerBreadまたはそれ以前で，shouldOverrideLoading()がリダイレクト時に呼ばれないことを前提とした処理．
            if (!mIsLoadingFinished) mIsRedirected = true;
            mIsLoadingFinished = false;

            LogUtils.d("redirected to: " + url);
            mWebView.loadUrl(url); // リダイレクトのときは再読み込み．
            return true;
        }
    };

    /**
     * その他の事情で特定URLの検知時に反応すべきであれば，このメソッドをoverrideして対応します．
     * 
     * @param url
     * @return true: 読み込み停止 false: 読み込み続行
     */
    protected boolean shouldOverride(String url) {
        return false;
    }

    /***********************************************
     * WebChromeClient *
     **********************************************/
    private final WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
            callback.invoke(origin, true, false);
        }
    };

    /***********************************************
     * Protected Method *
     **********************************************/
    /**
     * 読み込みを停止します．
     */
    protected void stop() {
        if (mWebView != null) mWebView.stopLoading();
    }

    /**
     * URLをホワイトリストその他と照合し，読み込みを続行するかどうか判定する
     * 
     * @param url
     * @return true: 通常どおり読み込みを続行 false: これ以上の処理は必要ない
     */
    protected boolean checkUrl(String url) {
        if (url.contains(WebView.SCHEME_MAILTO)) {
            self.pushMailToAction(url);
            return false;
        } else if (url.contains(WebView.SCHEME_TEL)) {
            self.pushDialAction(url);
            return false;
        } else if (shouldOverride(url)) {
            return false;
        }
        return true;
    }

    private void pushMailToAction(String url) {
        self.stop();
        url = url.replace(WebView.SCHEME_MAILTO, "");
        String[] str = url.split("\\?");
        String email = str[0];
        String subject = "";
        try {
            if (str.length > 1) subject = URLDecoder.decode(str[1].replace("subject=", ""), HTTP.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { email });
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        startActivity(Intent.createChooser(intent, "アプリケーションを選択")); // TODO メールアプリ選択
    }

    private void pushDialAction(String url) {
        self.stop();
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
        startActivity(intent);
    }

    /***********************************************
     * Dialog fragment *
     **********************************************/
    public static class BackAlertDialogFragment extends AbsCustomAlertDialogFragment {
        public static final String TAG = "tag_back_alert_dialog";

        /** フラグメントのファクトリーメソッド． */
        public static BackAlertDialogFragment newInstance() {
            BackAlertDialogFragment fragment = new BackAlertDialogFragment();
            Bundle args = initializeSettings(null, null, "前の画面に戻りますか？", null);
            args.putBoolean(IS_CANCELABLE, true);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public Builder customizeBuilder(Builder builder, Bundle args) {
            return builder;
        }

        @Override
        public Dialog cutomizeDialog(Dialog dialog, Bundle args) {
            return dialog;
        }
    }

    @Override
    public void onAlertDialogClicked(String tag, Bundle args, int which) {
        if (StringUtils.isSame(tag, BackAlertDialogFragment.TAG)) {
            // 前の画面に戻ってOK
            if (which == DialogInterface.BUTTON_POSITIVE) self.finish();
        }
    }

    @Override
    public void onAlertDialogCancelled(String tag, Bundle args) {
        if (StringUtils.isSame(tag, BackAlertDialogFragment.TAG)) {
            // nothing to do.
        }
    }

    /***********************************************
     * public method *
     **********************************************/
    public WebView getWebView() {
        return mWebView;
    }

    /***********************************************
     * {@link WebViewClient} *
     **********************************************/
    public abstract class WebViewClientWithCookie extends WebViewUnlimitedClient {

        private String mLoginCookie = "";

        @Override
        public void onLoadResource(WebView view, String url) {
            CookieManager cMgr = CookieManager.getInstance();
            mLoginCookie = cMgr.getCookie(url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            CookieManager cMgr = CookieManager.getInstance();
            cMgr.setCookie(url, mLoginCookie);
            LogUtils.v("onPageFinished: " + url + " / cookie: " + (mLoginCookie == null ? "no cookie." : mLoginCookie.toString()));
            LogUtils.v(new StringBuilder("onPageFinished: ").append(url).append(" : ").append(CookieManager.getInstance().getCookie(url)).toString());
        }
    }

    public abstract class WebViewUnlimitedClient extends WebViewClient {

        /**
         * SSL通信で問題があるとエラーダイアログを表示し、接続を中止する
         */
        @Override
        public void onReceivedSslError(WebView webview, SslErrorHandler handler, SslError error) {
            showSslErrorDialog(error);
            handler.cancel();
        }
    }

    /***********************************************
     * private method *
     **********************************************/
    private void showSslErrorDialog(SslError error) {
        DialogFragment fragment = PlainAlertDialogFragment.newInstance("SSL接続エラー", createErrorMessage(error));
        showDialogFragment(fragment, "tag_ssl_error_alert_dialog");
    }

    private String createErrorMessage(SslError error) {
        SslCertificate cert = error.getCertificate();
        StringBuilder result = new StringBuilder().append("サイトのセキュリティ証明書が信頼できません。接続を終了しました。\n\nエラーの原因\n");
        switch (error.getPrimaryError()) {
            case SslError.SSL_EXPIRED:
                result.append("証明書の有効期限が切れています。\n\n終了時刻=").append(cert.getValidNotAfterDate());
                return result.toString();
            case SslError.SSL_IDMISMATCH:
                result.append("ホスト名が一致しません。\n\nCN=").append(cert.getIssuedTo().getCName());
                return result.toString();
            case SslError.SSL_NOTYETVALID:
                result.append("証明書はまだ有効ではありません\n\n開始時刻=").append(cert.getValidNotBeforeDate());
                return result.toString();
            case SslError.SSL_UNTRUSTED:
                result.append("証明書を発行した認証局が信頼できません\n\n認証局\n").append(cert.getIssuedBy().getDName());
                return result.toString();
            default:
                result.append("原因不明のエラーが発生しました");
                return result.toString();
        }
    }

}
