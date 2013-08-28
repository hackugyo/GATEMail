package jp.hackugyo.gatemail.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ErrorDialogFragment extends DialogFragment {
    @SuppressWarnings("unused")
    private final ErrorDialogFragment self = this;

    private Dialog mDialog;

    public ErrorDialogFragment() {
        super();
        mDialog = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return mDialog;
    }

    public void setDialog(Dialog dialog) {
        mDialog = dialog;
    }
}
