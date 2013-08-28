package jp.hackugyo.gatemail.util;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * 文字入力を表示・非表示します．
 * 
 * @author kwatanabe
 * 
 */
public class ImeUtils {

    /**
     * 文字入力を開きます．
     * 
     * @see <a
     *      href="http://stackoverflow.com/questions/2403632/android-show-soft-keyboard-automatically-when-focus-is-on-an-edittext">参考ページ</a>
     */
    public static void openIme(View view, Context context, Dialog dialog) {
        // view.setFocusableInTouchMode(true);
        view.requestFocus();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE//
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        // inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        // inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT, null);

        // http://stackoverflow.com/questions/4761741/show-soft-keyboard-when-the-device-is-landscape-mode
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);

        // inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        // inputMethodManager.showSoftInputFromInputMethod(view.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT);
        // view.requestFocus();

        // カーソルを末尾に移動
        if (view instanceof EditText) {
            EditText et = (EditText) view;
            et.setSelection(et.getText().length());
        }
    }

    /**
     * 文字入力を閉じます．
     * 
     * @param view
     * @param context
     */
    public static void closeIme(View view, Context context) {
        if (context == null) {
            LogUtils.w("Trying closing IME, but context is null.");
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);

        // hideSoftInputFromInputMethodは使わない
        // http://stackoverflow.com/questions/3858362/hide-soft-keyboard
        // inputMethodManager.hideSoftInputFromInputMethod(view.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
