package jp.hackugyo.gatemail.util;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class FragmentUtils {
    /**
     * すでに同じダイアログが表示されているかどうかを返します．
     * 
     * @param fragmentManger
     * @param fragmentTag
     * @return true 表示されている false 表示されていない
     */
    public static boolean isShowingSameDialogFragment(FragmentManager fragmentManger, String fragmentTag) {

        Fragment prev = fragmentManger.findFragmentByTag(fragmentTag);
        if (prev == null) return false;

        if (prev instanceof DialogFragment) {
            final Dialog dialog = ((DialogFragment) prev).getDialog();
            if (dialog != null && dialog.isShowing()) {
                return true;
            }
        }
        return false;
    }
}
