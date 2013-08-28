package jp.hackugyo.gatemail.util;

import jp.hackugyo.gatemail.Defines;
import android.util.Log;

/**
 * Log出力クラス. android:debuggable が false の場合はログを出力しない
 * 基本的に、アプリケーションでのログ出力はこのクラスを使う
 * 
 * @author User
 */
public final class LogUtils {

    /**
     * タグを指定してデバッグログを出力します.
     * 
     * @param tag
     *            タグ
     * @param msg
     *            デバッグログ
     */
    public static void d(String tag, CharSequence msg) {
        if (!AppUtils.isDebuggable()) return;
        Log.d(tag, getLogForm(Thread.currentThread().getStackTrace()) + msg);
    }

    /**
     * デバッグログを出力します. タグは SMBCDirect になります
     * 
     * @param msg
     *            デバッグログ
     */
    public static void d(CharSequence msg) {
        if (!AppUtils.isDebuggable()) return;
        Log.d(Defines.LOG_TAG, getLogForm(Thread.currentThread().getStackTrace()) + msg);
    }

    public static void i(CharSequence msg) {
        if (!AppUtils.isDebuggable()) return;
        Log.i(Defines.LOG_TAG, getLogForm(Thread.currentThread().getStackTrace()) + msg);
    }

    /**
     * エラーログを出力します. タグは SMBCDirect になります
     * 
     * @param msg
     *            エラーログ
     */
    public static void e(CharSequence msg) {
        Log.e(Defines.LOG_TAG, getLogForm(Thread.currentThread().getStackTrace()) + msg);
    }

    /**
     * エラーログを出力します. タグは SMBCDirect になります
     * 
     * @param tag
     *            タグ
     * @param msg
     *            エラーログ
     */
    public static void e(String tag, CharSequence msg) {
        Log.e(tag, getLogForm(Thread.currentThread().getStackTrace()) + msg);
    }

    /**
     * エラーログを出力します. タグは SMBCDirect になります
     * 
     * @param msg
     *            エラーログ
     * @param e
     *            例外
     */
    public static void e(CharSequence msg, Throwable e) {
        Log.e(Defines.LOG_TAG, getLogForm(Thread.currentThread().getStackTrace()) + msg, e);
    }

    /**
     * エラーログを出力します. タグは SMBCDirect になります
     * 
     * @param tag
     *            タグ
     * @param msg
     *            エラーログ
     * @param e
     *            例外
     */
    public static void e(String tag, CharSequence msg, Throwable e) {
        Log.e(tag, getLogForm(Thread.currentThread().getStackTrace()) + msg, e);
    }

    /**
     * タグを指定してワーニングログを出力します.
     * 
     * @param tag
     *            タグ
     * @param msg
     *            ワーニングログ
     */
    public static void w(String tag, CharSequence msg) {
        if (!AppUtils.isDebuggable()) return;
        Log.w(tag, getLogForm(Thread.currentThread().getStackTrace()) + msg);
    }

    /**
     * ワーニングログを出力します. タグは SMBCDirect になります
     * 
     * @param msg
     *            ワーニングログ
     */
    public static void w(CharSequence msg) {
        if (!AppUtils.isDebuggable()) return;
        Log.w(Defines.LOG_TAG, getLogForm(Thread.currentThread().getStackTrace()) + msg);
    }

    /**
     * タグを指定してVerboseログを出力します.
     * 
     * @param tag
     *            タグ
     * @param msg
     *            Verboseログ
     */
    public static void v(String tag, CharSequence msg) {
        if (!AppUtils.isDebuggable()) return;
        Log.v(tag, getLogForm(Thread.currentThread().getStackTrace()) + msg);
    }

    /**
     * Verboseログを出力します. タグは SMBCDirect になります
     * 
     * @param msg
     *            Verboseログ
     */
    public static void v(CharSequence msg) {
        if (!AppUtils.isDebuggable()) return;
        Log.v(Defines.LOG_TAG, getLogForm(Thread.currentThread().getStackTrace()) + msg);
    }

    /**
     * ログのヘッダ情報を整形します.
     * 
     * @param elements
     *            実行中のメソッド情報
     * @return ヘッダ情報
     */
    private static String getLogForm(StackTraceElement[] elements) {
        StringBuilder sb = new StringBuilder();
        try {

            String file = elements[3].getFileName();
            String method = elements[3].getMethodName();
            int line = elements[3].getLineNumber();

            sb.append(StringUtils.ellipsizeMiddle(file.replace(".java", ""), 25, true));
            sb.append("#").append(StringUtils.ellipsize(method, 18, true));
            sb.append("() [").append(String.format("%1$04d", line)).append("] : ");
        } catch (NullPointerException ignore) {
            // ignore. return blank string.
            // リリースビルドでは，elements[3]がnullになるようなので，ここで握りつぶしておく．
        }
        return sb.toString();
    }
}
