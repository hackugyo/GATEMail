package jp.hackugyo.gatemail.ui.fragment;

import jp.hackugyo.gatemail.CustomApplication;
import jp.hackugyo.gatemail.R;
import jp.hackugyo.gatemail.ui.AbsCustomAlertDialogFragment;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.os.Bundle;


/**
 * OKボタン（閉じる動作）だけの単純なAlertDialogFragmentを作成します．
 * 
 * @author kwatanabe
 * 
 */
public class PlainAlertDialogFragment extends AbsCustomAlertDialogFragment {
    /**
     * ファクトリーメソッド、キャンセル可能なダイアログを表示します
     * 
     * @param title
     *            : nullの場合タイトルを表示しません
     * @param message
     */
    public static PlainAlertDialogFragment newInstance(String title, String message) {
        return newInstance(title, message, true);
    }

    public static PlainAlertDialogFragment newInstanceWithYesNo(Integer title, int message) {
        return newInstanceWithYesNo(title, message, null);
    }

    public static PlainAlertDialogFragment newInstanceWithYesNo(Integer title, int message, Bundle args) {
        String t = null;
        if (title != null) t = CustomApplication.getStringById(title);
        String m = CustomApplication.getStringById(message);
        return newInstanceWithYesNo(t, m, args);
    }

    public static PlainAlertDialogFragment newInstanceWithYesNo(String title, String message) {
        return newInstance(title, message, null);
    }

    public static PlainAlertDialogFragment newInstanceWithYesNo(String title, String message, Bundle args) {
        if (args == null) args = new Bundle();
        if (!args.containsKey(NEGATIVE_TEXT)) {
            args.putString(NEGATIVE_TEXT, CustomApplication.getStringById(R.string.Dialog_Button_Label_Negative));
        }
        PlainAlertDialogFragment fragment = PlainAlertDialogFragment.newInstance(null, message, args);
        return fragment;
    }

    public static PlainAlertDialogFragment newInstance(String title, String message, boolean isCancelable) {
        Bundle args = new Bundle();
        if (!args.containsKey(AbsCustomAlertDialogFragment.IS_CANCELABLE)) {
            args.putBoolean(AbsCustomAlertDialogFragment.IS_CANCELABLE, isCancelable);
        }
        return newInstance(title, message, args);
    }

    public static PlainAlertDialogFragment newInstance(String title, String message, Bundle args) {
        PlainAlertDialogFragment fragment = new PlainAlertDialogFragment();
        if (!args.containsKey(NEGATIVE_TEXT)) args.putString(NEGATIVE_TEXT, null); // initializeより先にセットすること
        args = initializeSettings(args, title, message, null);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Builder customizeBuilder(Builder builder, Bundle args) {
        return builder;
    }

    @Override
    public Dialog cutomizeDialog(Dialog dialog, Bundle args) {
        // ダイアログの外側をタッチされても、勝手に閉じないようにした
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}