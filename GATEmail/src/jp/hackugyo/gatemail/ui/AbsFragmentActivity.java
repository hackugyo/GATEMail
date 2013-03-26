package jp.hackugyo.gatemail.ui;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.flurry.android.FlurryAgent;
import com.ntt.appondemand.Defines;
import com.ntt.appondemand.R;
import com.ntt.appondemand.exception.CustomUncaughtExceptionHandler;
import com.ntt.appondemand.ui.activity.AbsWebViewActivity;
import com.ntt.appondemand.ui.activity.WebViewActivity;
import com.ntt.appondemand.util.AppUtils;
import com.ntt.appondemand.util.LogUtils;
import com.ntt.appondemand.util.ViewUtils;

/**
 * すべてのActivityの親となるべきクラスです．<br>
 * Fragmentに関連するメソッドは，正しく呼び出さないと問題が起きる場合があるので，<br>
 * このクラスの実装経由で利用することを推奨します．<br>
 * 
 * @author kwatanabe
 * 
 */
abstract public class AbsFragmentActivity extends SherlockFragmentActivity {
    protected FragmentManager mFragmentManager;
    private final AbsFragmentActivity self = this;

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

        // FragmentManagerを確保
        mFragmentManager = getSupportFragmentManager();

        // OSにキャプチャされないようにする．2.3を超えたやつに対してのみ適用
        // http://y-anz-m.blogspot.jp/2012/05/android_05.html
        // if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        // ホームボタンを有効にする．
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (AppUtils.isDebuggable()) {
            // LogCat設定
            FlurryAgent.setLogLevel(Log.DEBUG);
            FlurryAgent.setLogEnabled(true);
        }
        FlurryAgent.setReportLocation(false);
        FlurryAgent.setCaptureUncaughtExceptions(false);
        FlurryAgent.onStartSession(this, Defines.MY_FLURRY_API_KEY);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
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
        if (prev == null) {
            LogUtils.v("  not found: " + fragmentTag);
            return false;
        }
        LogUtils.v("  found: " + fragmentTag);

        if (prev instanceof DialogFragment) {
            final Dialog dialog = ((DialogFragment) prev).getDialog();

            if (dialog != null && dialog.isShowing()) {
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

        Fragment prev = mFragmentManager.findFragmentByTag(fragmentTag);
        if (prev == null) return false;

        if (prev instanceof DialogFragment) {
            final Dialog dialog = ((DialogFragment) prev).getDialog();
            if (dialog != null && dialog.isShowing()) {
                return true;
            }
        }
        return false;
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
                try {
                    fragment.show(mFragmentManager, tag);
                } catch (NullPointerException e) {
                    LogUtils.e(tag + ": cannot get SupportFragmentManager.");
                    LogUtils.e(self.getClass().getSimpleName() + e.getMessage());
                } catch (IllegalStateException e) {
                    LogUtils.e(tag + ": cannot show a dialog when this activity is in background.");
                    LogUtils.e(self.getClass().getSimpleName() + e.getMessage());
                }
                LogUtils.v("  no fragment of " + tag + " ? " + (mFragmentManager.findFragmentByTag(tag) == null));
            }
        });
    }

    /***********************************************
     * intent handling *
     **********************************************/

    /**
     * 外部ブラウザを選択させて表示します．<br>
     * Andorid4.0以降，外部ブラウザが端末にインストールされていない場合があるため，<br>
     * このメソッドを利用することを推奨します．<br>
     * 
     * @param url
     */
    public void launchExternalBrowser(String url) {
        selectBrowser(url);
    }

    protected void launchWebView(String url) {
        Intent i = new Intent(self, WebViewActivity.class).putExtra(AbsWebViewActivity.TARGET_URL_KEY, url).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
    }

    /***********************************************
     * ブラウザ起動*
     ***********************************************/
    protected static final int REQUEST_PICK_BROWSER = 0x1111;

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

    /***********************************************
     * Activity Result Handling *
     **********************************************/
    /**
     * アプリケーション（このactivityインスタンスを含んだタスク）全体をbackgroundに入れます．
     * 4.1(JellyBean)APIのfinishAffinity()の代わりです．
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
        } else {
            super.onActivityResult(requestCode, resultCode, fromToDataIntent);
        }
    }

    /**
     * {@link AbsFragmentActivity#onActivityResult(int, int, Intent)}
     * において，requestCodeが指定のものと同じかどうか判定します．
     * FragmentからstartActivityForResult()した場合， <br>
     * support packageを使う際は，requestCodeの下位16bitを除いて比較する必要があるため，このメソッドを使う必要があります．
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
        return ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                return onHomeIconPressed(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * ホームアイコン押下時の動作．
     */
    protected boolean onHomeIconPressed(MenuItem item) {
        finish();
        return true;
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
                // case Configuration.ORIENTATION_SQUARE:
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
                // case Configuration.ORIENTATION_SQUARE:
            case Configuration.ORIENTATION_UNDEFINED:
            default:
                return false;
        }
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
