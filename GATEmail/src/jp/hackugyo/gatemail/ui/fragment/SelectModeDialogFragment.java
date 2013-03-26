package jp.hackugyo.gatemail.ui.fragment;

import java.util.ArrayList;

import jp.hackugyo.gatemail.CustomApplication;
import jp.hackugyo.gatemail.R;
import jp.hackugyo.gatemail.ui.AbsCustomAlertDialogFragment;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class SelectModeDialogFragment extends AbsCustomAlertDialogFragment {

    public static final String WAYS = "ways";
    /** キャンセル用の選択肢を追加したい場合，これをtrueでargsにセットしてください（デフォルトはfalse） */
    public static final String WITH_CANCEL_ITEM = "WITH_CANCEL_ITEM";
    public static final String CHOICE_MODE_MULTIPLE = "CHOICE_MODE_MULTIPLE";
    public static final String CHOICE_MODE_SINGLE = "CHOICE_MODE_SINGLE";
    public static final String CHOSEN = "CHOSEN";

    public static SelectModeDialogFragment newInstance(ArrayList<String> ways, String title) {
        return newInstance(null, ways, title);
    }

    public static SelectModeDialogFragment newInstance(Bundle args, ArrayList<String> ways, String title) {
        ArrayList<String> choicesDisplay;
        if (ways == null) {
            choicesDisplay = new ArrayList<String>();
        } else {
            choicesDisplay = new ArrayList<String>(ways);
        }
        if (args != null && args.getBoolean(CHOICE_MODE_MULTIPLE)) {
            args.putBoolean(WITH_CANCEL_ITEM, false);
        }
        if (args != null && args.getBoolean(WITH_CANCEL_ITEM, false)) {
            choicesDisplay.add(CustomApplication.getStringById(R.string.Dialog_Button_Label_Negative));
        }

        SelectModeDialogFragment fragment = new SelectModeDialogFragment();
        if (args == null) args = new Bundle();
        args = initializeSettings(args, title, null, null);
        if (!args.getBoolean(CHOICE_MODE_MULTIPLE, false) && !args.getBoolean(CHOICE_MODE_SINGLE, false)) {
            args.putString(POSITIVE_TEXT, null);
            args.putString(NEGATIVE_TEXT, null);
        }
        args.putStringArray(WAYS, choicesDisplay.toArray(new String[] {}));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Builder customizeBuilder(Builder builder, Bundle args) {
        // リスナーを設定

        // 選択モード設定
        String[] choices = args.getStringArray(WAYS);
        if (args.getBoolean(CHOICE_MODE_MULTIPLE, false)) {
            builder = setMutlipleChoiceMode(builder, choices);
        } else if (args.getBoolean(CHOICE_MODE_SINGLE, false)) {
            builder = setSingleChoiceMode(builder, choices);
        } else {
            builder.setItems(choices, getOnClickListener());
        }
        return builder;
    }

    @Override
    public Dialog cutomizeDialog(Dialog dialog, Bundle args) {
        return dialog;
    }

    private Builder setMutlipleChoiceMode(Builder builder, String[] choices) {
        // 選択された選択肢を保存しておく配列を作成し，argumentsに入れておく
        // 生成時に受け取っている場合は不要
        Bundle args = getArguments();
        if (!args.containsKey(CHOSEN) || args.getBooleanArray(CHOSEN) == null) {
            boolean[] checked = new boolean[choices.length];
            args.putBooleanArray(CHOSEN, checked);
        }

        builder.setMultiChoiceItems(choices, args.getBooleanArray(CHOSEN), new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                Bundle args = getArguments();
                boolean[] checked = args.getBooleanArray(CHOSEN);
                checked[which] = isChecked;
                args.putBooleanArray(CHOSEN, checked);
            }
        });

        return builder;
    }

    private Builder setSingleChoiceMode(Builder builder, String[] choices) {
        // 選択された選択肢を保存しておく配列を作成し，argumentsに入れておく
        // 生成時に受け取っている場合は不要
        Bundle args = getArguments();
        if (!args.containsKey(CHOSEN) || args.getBooleanArray(CHOSEN) == null) {
            boolean[] checked = new boolean[choices.length];
            args.putBooleanArray(CHOSEN, checked);
        }
        boolean[] checked = args.getBooleanArray(CHOSEN);
        int chosenItem;
        for (chosenItem = 0; chosenItem < checked.length; chosenItem++) {
            if (checked[chosenItem]) break;
        }

        builder.setSingleChoiceItems(choices, chosenItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle args = getArguments();
                boolean[] checked = args.getBooleanArray(CHOSEN);
                for (int i = 0; i < checked.length; i++) {
                    checked[i] = (i == which); // 選ばれたもの以外すべてfalseにする
                }
                args.putBooleanArray(CHOSEN, checked);
            }

        });

        return builder;
    }

    private DialogInterface.OnClickListener getOnClickListener() {
        DialogInterface.OnClickListener onClickWayToLocateListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle args = getArguments();
                if (which == args.getStringArray(WAYS).length - 1 && args.getBoolean(WITH_CANCEL_ITEM, false)) {
                    // 最後のものであれば，キャンセルとする．
                    getCallbacks().onAlertDialogCancelled(getTag(), args);
                } else {
                    getCallbacks().onAlertDialogClicked(getTag(), args, which);
                }
                dialog.dismiss();
            }
        };
        return onClickWayToLocateListener;
    }
}
