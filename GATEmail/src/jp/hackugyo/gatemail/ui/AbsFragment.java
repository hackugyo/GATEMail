package jp.hackugyo.gatemail.ui;

import jp.hackugyo.gatemail.CustomApplication;
import jp.hackugyo.gatemail.R;
import jp.hackugyo.gatemail.ui.fragment.ProgressDialogFragment;
import jp.hackugyo.gatemail.util.FragmentUtils;
import jp.hackugyo.gatemail.util.LogUtils;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class AbsFragment extends Fragment {
    private final AbsFragment self = this;

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
                // 最新のソースだと，dialogそのものをdismissする前にフラグを見て抜けてしまうので，
                // dialog自体は別途dismiss()してやるのが確実．
                dialog.dismiss(); // http://blog.zaq.ne.jp/oboe2uran/article/876/
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

    protected AbsFragmentActivity getActivitySafely() {
        FragmentActivity result = getActivity();
        if (result == null) {
            LogUtils.w("cannot getActivity()");
            throw new NullPointerException("cannot getActivity() at " + getClass().getSimpleName());
        }
        return (AbsFragmentActivity) result;
    }

    /**
     * {@link AbsBaseActivity#isShowingSameDialogFragment(String)}と同様
     * 
     * @param fragmentTag
     */
    protected boolean isShowingSameDialogFragment(String fragmentTag) {
        return FragmentUtils.isShowingSameDialogFragment(getFragmentManager(), fragmentTag);
    }

    protected void showDialogFragment(final DialogFragment fragment, final String tag) {
        // onLoadFinished()で呼ぶ際，単にshowしてしまうとCan not perform this action inside of onLoadFinishedが出るので，
        // Handlerを使う．
        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                fragment.setTargetFragment(self, 0);
                try {
                    fragment.show(getFragmentManager(), tag);
                } catch (NullPointerException e) {
                    LogUtils.e("cannot get SupportFragmentManager.");
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
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.add(fragment, tag);
                    ft.commitAllowingStateLoss();
                    getFragmentManager().executePendingTransactions();
                }
            }
        });
    }

    /**
     * {@link AbsBaseActivity#showProgressDialog(int, String, boolean)}と同様
     * 
     * @param loaderId
     * @param message
     * @param isCancelable
     */
    protected boolean showProgressDialog(int loaderId, String message, boolean isCancelable) {
        if (isShowingSameDialogFragment(ProgressDialogFragment.TAG)) return false; // すでに同じものが出ていた場合，何もしない

        Bundle argsForProgressDialog = new Bundle();
        argsForProgressDialog.putInt(ProgressDialogFragment.TARGET_LOADER_ID, loaderId);
        argsForProgressDialog.putBoolean(ProgressDialogFragment.IS_CANCELABLE, isCancelable);
        if (isCancelable) {
            argsForProgressDialog.putString(ProgressDialogFragment.NEGATIVE_TEXT, CustomApplication.getStringById(R.string.Dialog_Button_Label_Negative));
        } else {
            argsForProgressDialog.putString(ProgressDialogFragment.NEGATIVE_TEXT, null); //キャンセル不可の場合，ボタンもなし
        }
        ProgressDialogFragment dialogFragment = ProgressDialogFragment.createProgressDialog(argsForProgressDialog, null, null);
        dialogFragment.setTargetFragment(self, 0);
        dialogFragment.show(getFragmentManager(), ProgressDialogFragment.TAG);

        return true;
    }
}
