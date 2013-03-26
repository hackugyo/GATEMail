package jp.hackugyo.gatemail.ui;

import jp.hackugyo.gatemail.CustomApplication;
import jp.hackugyo.gatemail.R;
import jp.hackugyo.gatemail.util.LogUtils;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;


/**
 * AlertDialogを管理するFragmentです．<br>
 * 実装の際は， {@link FragmentActivity}または{@link Fragment}に{@link Callbacks}
 * をimplementsしてください．<br>
 * コールバックが不要（単なる確認ダイアログなど）の場合，Callbacksをimplementしなくても使えます．<br>
 * 呼び出し時は，ファクトリーメソッドで作成したインスタンスを{@link #show(FragmentManager, String)}で表示できますが，<br>
 * 表示時に {@link Runnable}を使う必要がある場合があるので，<br>
 * {@link AbsFragmentActivity#showDialogFragment(DialogFragment, String)}
 * を利用することを推奨します．<br>
 * 表示を外部から閉じる場合も，Dialogの閉じかたに注意すべき点があるため，<br>
 * {@link AbsFragmentActivity#removeFragment(String)}を推奨します．
 * 
 * @author kwatanabe
 * 
 */
abstract public class AbsCustomAlertDialogFragment extends DialogFragment {
    public static final String ICON = "icon";
    public static final String TITLE = "title";
    public static final String MESSAGE = "message";
    public static final String POSITIVE_TEXT = "positive_text";
    public static final String NEGATIVE_TEXT = "negative_text";
    public static final String ALERTDIALOG_VIEW = "alert_dialog_view";
    /**
     * trueにしてargsに渡すと，キャンセル不可のダイアログを作ります．<br>
     * {@link #NEGATIVE_TEXT}をセットした場合， ボタンでのキャンセルは可能になります．
     */
    public static final String IS_CANCELABLE = "IS_CANCELABLE";
    @SuppressWarnings("unused")
    private final AbsCustomAlertDialogFragment self = this;

    /** コールバック. */
    private Callbacks mCallbacks;

    /***********************************************
     * Life Cycle *
     ***********************************************/
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        AlertDialog.Builder builder = getBuilder();
        setBuilderDefaultSettings(builder, args);
        builder = customizeBuilder(builder, args);// この位置で，builderに追加設定
        if (builder == null) throw new NullPointerException("AbsCustomAlertDialogFragment#customizeBuilder(builder, args) is not implemented,");// org.apache.commons.lang.NotImplementedException();
        Dialog dialog = builder.create();
        dialog = cutomizeDialog(dialog, args); // この位置で，Windowに追加設定
        if (dialog == null) throw new NullPointerException("AbsCustomAlertDialogFragment#cutomizeDialog(dialog, args) is not implemented,");// org.apache.commons.lang.NotImplementedException();

        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mCallbacks.onAlertDialogCancelled(getTag(), getArguments());
    }

    /***********************************************
     * Builder *
     ***********************************************/

    /**
     * ビルダークラスを取得します．
     * 
     * @return {@link android.app.AlertDialog.Builder}またはその子クラス
     */
    protected AlertDialog.Builder getBuilder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder;
    }

    /**
     * デフォルトで利用する情報をargsから取得してビルドします．
     * 
     * @param builder
     * @param args
     */
    protected AlertDialog.Builder setBuilderDefaultSettings(AlertDialog.Builder builder, Bundle args) {
        if (args.containsKey(ICON)) builder.setIcon(args.getInt(ICON));
        if (args.containsKey(TITLE)) builder.setTitle(args.getString(TITLE));
        if (args.containsKey(MESSAGE)) builder.setMessage(args.getString(MESSAGE));
        mCallbacks = setCallbacks();
        // OK/キャンセルボタンセット．ただし，ボタン表示名に明示的にnullを指定されていた場合，そのボタンは表示しない
        if (args.containsKey(POSITIVE_TEXT) && args.getString(POSITIVE_TEXT) != null) {
            builder.setPositiveButton(args.getString(POSITIVE_TEXT), new DialogInterface.OnClickListener() {
                // ダイアログのボタンを押された時のリスナを定義する.
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mCallbacks.onAlertDialogClicked(getTag(), getArguments(), which);
                }
            });
        }
        if (args.containsKey(NEGATIVE_TEXT) && args.getString(NEGATIVE_TEXT) != null) {
            builder.setNegativeButton(args.getString(NEGATIVE_TEXT), new DialogInterface.OnClickListener() {
                // ダイアログのボタンを押された時のリスナを定義する.
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // onCancelでCallbacksも呼んでいる．
                    onCancel(dialog);
                }
            });
        }
        if (!args.getBoolean(IS_CANCELABLE, true)) {
            builder.setCancelable(false); // 戻るボタンでのキャンセルを不可にする
            builder.setOnKeyListener(new OnKeyListener() { // KEYCODE_SEARCHも無効にする
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK) return true; // ignore
                    return false;
                }
            });
        }
        return builder;
    }

    /**
     * TargetFragment - Activityの順でコールバックが実装されているかどうかをチェックし, 使用する.<br>
     * されていないならば何もしない.<br>
     * （負担になるので, コールバック実装の強制はしない事にする）
     */
    protected Callbacks setCallbacks() {
        if (getTargetFragment() != null && getTargetFragment() instanceof Callbacks) {
            mCallbacks = (Callbacks) getTargetFragment();
        } else if (getActivity() instanceof Callbacks) {
            mCallbacks = (Callbacks) getActivity();
        } else {
            mCallbacks = new AbsCustomAlertDialogFragment.Callbacks() {
                @Override
                public void onAlertDialogClicked(String tag, Bundle args, int which) {
                    // do nothing.
                }

                @Override
                public void onAlertDialogCancelled(String tag, Bundle args) {
                    // do nothing.
                }
            };
        }
        return mCallbacks;
    }

    /**
     * 基本的な設定を受け取り，argsにまとめて返します．
     * 
     * @param args
     * @param title
     * @param message
     * @param layoutId
     */
    protected static Bundle initializeSettings(Bundle args, String title, String message, Integer layoutId) {
        if (args == null) args = new Bundle();

        if (title != null) args.putString(TITLE, title);
        if (message != null) args.putString(MESSAGE, message);
        if (layoutId != null) args.putInt(ALERTDIALOG_VIEW, layoutId);
        if (!args.containsKey(POSITIVE_TEXT)) args.putString(POSITIVE_TEXT, CustomApplication.getStringById(R.string.Dialog_Button_Label_Positive));
        if (!args.containsKey(NEGATIVE_TEXT)) args.putString(NEGATIVE_TEXT, CustomApplication.getStringById(R.string.Dialog_Button_Label_Negative));
        return args;
    }

    /**
     * コールバックを取得します．
     * 
     * @return コールバック
     */
    protected Callbacks getCallbacks() {
        return mCallbacks;
    }

    /**
     * 独自のコールバックをセットします．
     * 
     * @param callbacksInstance
     */
    public void setCallbacks(Callbacks callbacksInstance) {
        mCallbacks = callbacksInstance;
    }

    /**
     * {@link android.app.AlertDialog.Builder}を拡張したビルダーに，ここで設定を行います．
     * 
     * @param builder
     * @param args
     * @return ビルダー
     */
    abstract public AlertDialog.Builder customizeBuilder(AlertDialog.Builder builder, Bundle args);

    /**
     * {@link android.app.AlertDialog.Builder#create()}したダイアログに，<br>
     * Windowサイズ調整などの修正を行います．
     * 
     * @param dialog
     * @param args
     * @return Dialog
     */
    abstract public Dialog cutomizeDialog(Dialog dialog, Bundle args);

    /***********************************************
     * Callbacks interface *
     ***********************************************/
    /**
     * ダイアログの各ボタンを押下した際のコールバックインタフェース.
     */
    public static interface Callbacks {

        /**
         * ダイアログのボタン及びリストを押下した際のイベント処理.
         * 
         * @param tag
         *            Fragmentにつけたタグ
         * @param args
         *            setParamsで渡されたパラメータ
         * @param which
         *            DialogInterfaceのID
         */
        void onAlertDialogClicked(String tag, Bundle args, int which);

        /**
         * ダイアログがキャンセルされた際のイベント処理.
         * 
         * @param tag
         *            Fragmentのタグ
         * @param args
         *            setParamsで渡されたパラメータ
         */
        void onAlertDialogCancelled(String tag, Bundle args);
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
        Fragment prev = getFragmentManager().findFragmentByTag(fragmentTag);

        if (prev == null) return false;

        if (prev instanceof DialogFragment) {
            final Dialog dialog = ((DialogFragment) prev).getDialog();

            if (dialog != null && dialog.isShowing()) {
                // prev.dismiss()を呼んではだめ． http://memory.empressia.jp/article/44110106.html
                ((DialogFragment) prev).onDismiss(dialog); // DialogFragmentの場合は閉じる処理も追加
            }
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.remove(prev);
        ft.commitAllowingStateLoss();
        ft = null;
        getFragmentManager().executePendingTransactions();
        return true;
    }

    /**
     * onLoadFinished()の中などからでも，安全にDialogFragmentをshow()します．
     * 
     * @param fragment
     */
    protected void showDialogFragment(final FragmentManager fragmentManager, final DialogFragment fragment, final String tag) {
        Handler h = new Handler(Looper.getMainLooper());

        h.post(new Runnable() {
            @Override
            public void run() {
                removeFragment(tag);
                try {
                    fragment.show(fragmentManager, tag);
                } catch (NullPointerException e) {
                    LogUtils.e("cannot get SupportFragmentManager.");
                }
            }
        });
    }
}