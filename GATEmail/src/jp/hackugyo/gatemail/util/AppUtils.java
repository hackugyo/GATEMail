package jp.hackugyo.gatemail.util;

import jp.hackugyo.gatemail.CustomApplication;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class AppUtils {
    /**
     * デバッグモードかどうかを取得します.
     * 
     * @return true の場合はデバッグモード（apkをインストールして実行したものではないということ，実行モードは関係ない）
     */
    public static boolean isDebuggable() {
        PackageManager pm = null;
        try {
            pm = CustomApplication.getAppContext().getPackageManager();
        } catch (NullPointerException e) {
            // contextが取得できないということはアプリが起動していないのにこのメソッドが呼ばれたということなので，
            // テスト実行中と見なす．Debuggableとする
            return true;
        }
        ApplicationInfo info = null;
        try {
            info = pm.getApplicationInfo(CustomApplication.getAppContext().getPackageName(), 0);
        } catch (NameNotFoundException e) {
            return true;
        }
        if ((info.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE) {
            return true;
        }
        return false;
    }
}
