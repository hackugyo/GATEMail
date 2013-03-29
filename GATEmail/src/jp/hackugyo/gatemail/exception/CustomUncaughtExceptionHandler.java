package jp.hackugyo.gatemail.exception;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;


/**
 * キャッチされなかった例外を処理する
 * 
 * @author kwatanabe
 * 
 */
public class CustomUncaughtExceptionHandler implements UncaughtExceptionHandler {
	@SuppressWarnings("unused")
	private final CustomUncaughtExceptionHandler self = this;

	private UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;

	public CustomUncaughtExceptionHandler(Context context) {
		// デフォルト例外ハンドラを保持する。
		mDefaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		ex.printStackTrace();

		// デフォルト例外ハンドラを実行し、強制終了します。
		mDefaultUncaughtExceptionHandler.uncaughtException(thread, ex);
	}
}
