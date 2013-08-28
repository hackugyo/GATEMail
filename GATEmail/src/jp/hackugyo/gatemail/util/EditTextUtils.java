package jp.hackugyo.gatemail.util;

import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;

public class EditTextUtils {
    @SuppressWarnings("unused")
    private final EditTextUtils self = this;

    /**
     * 
     * EditTextを，数値以外入力できないようにして返します．<br>
     * 003SH（iWnn IME SH edition)のように，キャレット移動のボタン（→）が 半角スペース挿入 になっている場合，<br>
     * ここで弾いてしまいますが，<br>
     * IME側がおかしいものと考えておきます．<br>
     * 
     * @see <a
     *      href="http://bbs.kakaku.com/bbs/J0000005221/SortID=13154334/">参考ページ</a>
     * 
     * @see <a
     *      href="http://dev.classmethod.jp/smartphone/android/android-tips-20-edittext-inputtype/">http://dev.classmethod.jp/smartphone/android/android-tips-20-edittext-inputtype/</a>
     * 
     * @param editText
     * @param maxLength
     * @return EditText
     */
    public static EditText getNumberEditText(EditText editText, int maxLength) {
        InputFilter inputFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source.toString().matches("^[0-9]+$")) {
                    return source;
                } else {
                    return "";
                }
            }
        };
        // android:maxLengthの設定を上書きしてしまうので，改めて設定しておきます
        InputFilter lengthFilter = new InputFilter.LengthFilter(maxLength);
        // フィルターの配列を作成
        InputFilter[] filters = new InputFilter[] { inputFilter, lengthFilter };
        editText.setFilters(filters);
        return editText;
    }

    /**
     * 
     * EditTextを，半角英数字以外入力できないようにして返します．<br>
     * 003SH（iWnn IME SH edition)のように，キャレット移動のボタン（→）が 半角スペース挿入 になっている場合，<br>
     * ここで弾いてしまいますが，<br>
     * IME側がおかしいものと考えておきます．<br>
     * 
     * @see <a
     *      href="http://bbs.kakaku.com/bbs/J0000005221/SortID=13154334/">参考ページ</a>
     * 
     * @see <a
     *      href="http://dev.classmethod.jp/smartphone/android/android-tips-20-edittext-inputtype/">http://dev.classmethod.jp/smartphone/android/android-tips-20-edittext-inputtype/</a>
     * 
     * @param editText
     * @param maxLength
     * @return EditText
     */
    public static EditText getAlphanumericEditText(EditText editText, int maxLength) {
        InputFilter inputFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source.toString().matches("^[a-zA-Z0-9]+$")) {
                    return source;
                } else {
                    return "";
                }
            }
        };
        // android:maxLengthの設定を上書きしてしまうので，改めて設定しておきます
        InputFilter lengthFilter = new InputFilter.LengthFilter(maxLength);
        // フィルターの配列を作成
        InputFilter[] filters = new InputFilter[] { inputFilter, lengthFilter };
        editText.setFilters(filters);
        return editText;
    }

    /**
     * 
     * EditTextを，数値以外入力できないようにし，かつ途中への挿入・編集ができないようにして返します．<br>
     * 
     * @see <a
     *      href="http://stackoverflow.com/a/911254/2338047">http://stackoverflow.com/a/911254/2338047</a>
     * 
     * @param editText
     * @param maxLength
     * @return EditText
     */
    public static EditText getUnInsertableNumberEditText(final EditText editText, int maxLength) {
        InputFilter inputFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (isInserting(dend)) {

                    return dest.subSequence(dstart, dend); // 文字を変えない
                } else if (source.toString().matches("^[0-9]+$")) {
                    return source;
                } else {
                    return "";
                }
            }

            /**
             * 末尾の文字以外を編集しているかどうか
             * 
             * @param dend
             * @return true: 挿入または編集<br>
             *         false: 末尾の文字を編集している
             */
            private boolean isInserting(int dend) {
                final int textLength = editText.getText().length();
                return dend < textLength;
            }
        };
        InputFilter lengthFilter = new InputFilter.LengthFilter(maxLength);
        // フィルターの配列を作成
        InputFilter[] filters = new InputFilter[] { inputFilter, lengthFilter };
        editText.setFilters(filters);
        return editText;
    }
}
