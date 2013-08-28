package jp.hackugyo.gatemail.ui;

import jp.hackugyo.gatemail.R;
import jp.hackugyo.gatemail.exception.CustomUncaughtExceptionHandler;
import jp.hackugyo.gatemail.ui.activity.AbsWebViewActivity;
import jp.hackugyo.gatemail.ui.activity.WebViewActivity;
import jp.hackugyo.gatemail.util.FragmentUtils;
import jp.hackugyo.gatemail.util.LogUtils;
import jp.hackugyo.gatemail.util.ViewUtils;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

abstract public class AbsFragmentActivity extends SherlockFragmentActivity {
    private final AbsFragmentActivity self = this;
    protected FragmentManager mFragmentManager;
    /** Image Loading Manage */
    protected ImageLoader mImageLoader = ImageLoader.getInstance();

    /***********************************************
     * Life Cycle *
     ***********************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
        super.onCreate(savedInstanceState);

        // UncaughtExceptionHandlerを実装したクラスをセットする。
        CustomUncaughtExceptionHandler customUncaughtExceptionHandler;
        customUncaughtExceptionHandler = new CustomUncaughtExceptionHandler(getApplicationContext());
        Thread.setDefaultUncaughtExceptionHandler(customUncaughtExceptionHandler);

        mFragmentManager = getSupportFragmentManager();
        // OSにキャプチャされないようにする．2.3を超えたやつに対してのみ適用
        // http://y-anz-m.blogspot.jp/2012/05/android_05.html
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            // getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // ActionBarのホームボタンを有効にする．
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    /**
     * メモリ解放処理を行います．<br>
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            ViewUtils.cleanupView(getContentView());
            LogUtils.v("cleaned up this view: " + self.getClass().getSimpleName());
        } catch (Exception e) { // onDestroy時に落ちてほしくないので，お守り
            LogUtils.e(e.toString());
            e.printStackTrace();
        }
    }

    /***********************************************
     * Fragment Control *
     **********************************************/
    /**
     * タグで指定されたフラグメントを消去します
     * 
     * @param fragmentTag
     */
    protected boolean removeFragment(String fragmentTag) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        if (mFragmentManager == null) mFragmentManager = getSupportFragmentManager();
        if (mFragmentManager == null) {
            LogUtils.w("fragment manager not found.");
            return false;
        }
        Fragment prev = mFragmentManager.findFragmentByTag(fragmentTag);
        if (prev == null) return false;

        if (prev instanceof DialogFragment) {
            final Dialog dialog = ((DialogFragment) prev).getDialog();

            if (dialog != null && dialog.isShowing()) {
                // 最新のソースだと，dialogそのものをdismissする前にフラグを見て抜けてしまうので，
                // dialog自体は別途dismiss()してやるのが確実．
                dialog.dismiss(); // http://blog.zaq.ne.jp/oboe2uran/article/876/
                // prev.dismiss()を呼んではだめ． http://memory.empressia.jp/article/44110106.html
                ((DialogFragment) prev).onDismiss(dialog); // DialogFragmentの場合は閉じる処理も追加
            }
        }

        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.remove(prev);
        // ft.commit();
        ft.commitAllowingStateLoss();
        ft = null;
        getSupportFragmentManager().executePendingTransactions();
        return true;
    }

    protected boolean isShowingSameDialogFragment(String fragmentTag) {
        return FragmentUtils.isShowingSameDialogFragment(mFragmentManager, fragmentTag);
    }

    /**
     * onLoadFinished()の中などからでも，安全にDialogFragmentをshow()します．
     * 
     * @param fragment
     */
    protected void showDialogFragment(final DialogFragment fragment, final String tag) {
        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                removeFragment(tag);
                if (mFragmentManager == null) mFragmentManager = getSupportFragmentManager();
                if (mFragmentManager == null) return;
                try {
                    fragment.show(mFragmentManager, tag);
                } catch (IllegalStateException e) {
                    LogUtils.e(tag + ": cannot show a dialog when this activity is in background.");
                    LogUtils.e(self.getClass().getSimpleName() + e.getMessage());
                    // 表示のタイミングでバックグラウンドにいた場合など，
                    // show()だとIllegalStateExceptionで落ちてしまう
                    // http://stackoverflow.com/a/16206036/2338047
                    // ただし，show()を使わないと内部的なフラグが動かないので，
                    // まずshow()を使ってフラグを立て，
                    // 失敗したときのみFragmet#commit()のかわりにFragment#commitAllowingStateLoss()を呼ぶ．
                    removeFragment(tag);
                    FragmentTransaction ft = mFragmentManager.beginTransaction();
                    ft.add(fragment, tag);
                    ft.commitAllowingStateLoss();
                    mFragmentManager.executePendingTransactions();
                }
            }
        });
    }

    /***********************************************
     * intent handling *
     **********************************************/

    public void launchExternalBrowser(String url) {
        // launchBrowser(url, true);
        selectBrowser(url);
    }

    protected static final int REQUEST_PICK_BROWSER = 0x1111;
    protected static final int REQUEST_PICK_BROWSER_TO_DOWNLOAD_THIS_APP = 0x1112; // TODO あとで適切な場所に移動し一覧化する

    /**
     * urlを処理できるアプリ（ブラウザアプリ）の一覧を表示するchooserを出します．
     * {@link #onActivityResult(int, int, Intent)}で，選択されたアプリを起動します．
     * 
     * @param url
     */
    private void selectBrowser(String url) {
        selectBrowser(url, REQUEST_PICK_BROWSER);
    }

    private void selectBrowser(String url, int requestId) {
        if (url == null) url = "";
        Intent mainIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        Intent chooserIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, mainIntent); // ブラウザ選択
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "アプリケーションを選択");
        try {
            startActivityForResult(chooserIntent, requestId);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(self, "ブラウザアプリがインストールされていません。", Toast.LENGTH_LONG).show();
            LogUtils.e("browser activity cannot found.");
        }
    }

    /**
     * 内部WebViewで表示するためのインテントを取得します． デフォルトでは，{@link WebViewActivity}を使います．
     * {@link Intent#FLAG_ACTIVITY_CLEAR_TOP}や
     * {@link Intent#FLAG_ACTIVITY_SINGLE_TOP}などのフラグは未設定なので，呼び出し時に付加してください．
     * 
     * @param url
     */
    public Intent getInternalWebViewIntent(String url) {
        Intent i = new Intent(self, WebViewActivity.class).putExtra(AbsWebViewActivity.TARGET_URL_KEY, url);
        return i;
    }

    /***********************************************
     * Activity Result Handling *
     **********************************************/
    /**
     * アプリケーション（このactivityインスタンスを含んだタスク）全体をbackgroundに入れる
     * 4.1(JellyBean)APIのfinishAffinity()は全体をfinishさせますが，<br>
     * これはfinishはしません．<br>
     */
    protected void finishApplication() {
        moveTaskToBack(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent fromToDataIntent) {
        if (isSameRequestCode(requestCode, REQUEST_PICK_BROWSER)) {
            if (fromToDataIntent == null) return;
            fromToDataIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(fromToDataIntent);
        } else if (isSameRequestCode(requestCode, REQUEST_PICK_BROWSER_TO_DOWNLOAD_THIS_APP)) {
            if (fromToDataIntent == null) return;
            fromToDataIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(fromToDataIntent);
            finishApplication();
        } else {
            super.onActivityResult(requestCode, resultCode, fromToDataIntent);
        }
    }

    /**
     * {@link AbsFragmentActivity#onActivityResult(int, int, Intent)}
     * において，requestCodeが指定のものと同じかどうか判定します．
     * FragmentからstartActivityForResult()した場合， support
     * packageを使う際は，requestCodeの下位16bitを除いて比較する必要があるため，このメソッドを使う必要があります．
     * 
     * @see <a
     *      href="http://y-anz-m.blogspot.jp/2012/05/support-package-fragment.html">http://y-anz-m.blogspot.jp/2012/05/support-package-fragment.html</a>
     * 
     * @param requestCode
     * @param targetCode
     */
    protected boolean isSameRequestCode(int requestCode, int targetCode) {
        int requestCodeFromFragment = requestCode & 0xffff;
        return (requestCode == targetCode || requestCodeFromFragment == targetCode);
    }

    /***********************************************
     * View *
     **********************************************/

    /**
     * setContentView(id)したViewを取得します．
     * 
     */
    public View getContentView() {
        return ViewUtils.getContentView(self);
    }

    /***********************************************
     * orientation *
     **********************************************/

    /**
     * 画面が横向きかどうか
     * 
     */
    protected boolean isLandScape() {
        int currentOrientation = getResources().getConfiguration().orientation;
        switch (currentOrientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                return true;
            case Configuration.ORIENTATION_PORTRAIT:
            case Configuration.ORIENTATION_SQUARE:
            case Configuration.ORIENTATION_UNDEFINED:
            default:
                return false;
        }
    }

    /**
     * 画面が縦向きかどうか
     * 
     */
    protected boolean isPortrait() {
        int currentOrientation = getResources().getConfiguration().orientation;
        switch (currentOrientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                return true;
            case Configuration.ORIENTATION_LANDSCAPE:
            case Configuration.ORIENTATION_SQUARE:
            case Configuration.ORIENTATION_UNDEFINED:
            default:
                return false;
        }
    }

    /***********************************************
     * MenuButton Handle *
     **********************************************/

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            onClickMenuButton();
        }

        return super.dispatchKeyEvent(event);
    }

    /**
     * Menuボタンが押されたときのアクションを定義します。 デフォルトでは空動作。必要に応じて上書きする。
     */
    @Deprecated
    protected void onClickMenuButton() {
    }

    /***********************************************
     * Toast *
     **********************************************/
    private Toast mToast;

    /**
     * Activity内で消し忘れがないよう，単一のToastインスタンスを使い回します．
     * 
     * @param text
     * @param length
     */
    protected void showSingleToast(String text, int length) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(self, text, length);
        mToast.show();
    }

    /**
     * Activity内で消し忘れがないよう，単一のToastインスタンスを使い回します．
     * 
     * @param resId
     * @param length
     */
    protected void showSingleToast(int resId, int length) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(self, resId, length);
        mToast.show();
    }

    /**
     * 使い回している単一のToastインスタンスを破棄します．
     * 
     */
    protected void removeSingleToast() {
        if (mToast != null) mToast.cancel();
        mToast = null;
    }

}
