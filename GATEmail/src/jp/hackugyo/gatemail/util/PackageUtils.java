package jp.hackugyo.gatemail.util;

import java.util.List;

import jp.hackugyo.gatemail.R;
import jp.hackugyo.gatemail.CustomApplication;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;


public class PackageUtils {

    public static boolean isSystemApp(ResolveInfo info) {
        return ((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM);
    }

    /**
     * パッケージ名からアイコンを取得します．
     * 
     * @param pm
     *            （nullでも可）
     * @param packageName
     * @return アイコン
     */
    public static Drawable getAppIcon(PackageManager pm, String packageName) {
        if (pm == null) pm = CustomApplication.getAppContext().getPackageManager();
        Drawable icon = null;
        try {
            icon = pm.getApplicationIcon(packageName);
        } catch (NameNotFoundException e) {
            LogUtils.w(new StringBuilder("name not found: ").append(packageName).toString());
        }
        if (icon == null) {
            // FakeIt later, 見つからなかった場合に出すデフォルトのアイコンを準備
            icon = CustomApplication.getDrawableById(R.drawable.ic_launcher);
        }
        return icon;
    }

    /**
     * パッケージ名から起動インテントを取得します．
     * 
     * @param pm
     *            （nullでも可）
     * @param packageName
     * @return アイコン
     */
    public static Intent getIntent(PackageManager pm, String packageName) {
        if (pm == null) pm = CustomApplication.getAppContext().getPackageManager();
        Intent intent = null;
        if (isInstalled(pm, packageName)) {
            intent = pm.getLaunchIntentForPackage(packageName);
        } else {
            intent = getMarketIntent(packageName);
        }

        // このアプリとは違うタスクで起動する
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * Google Playを起動し，パッケージ名で検索するインテントを作ります．
     * 
     * @param packageName
     * @return Google Playインテント
     */
    public static Intent getMarketIntent(String packageName) {
        Uri uri = Uri.parse("market://details?id=" + packageName);
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    /**
     * パッケージ名から、インストールされているかどうかを返します．
     * 
     * @param pm
     *            （nullでも可）
     * @param packageName
     * @return true: インストールされている
     */
    public static boolean isInstalled(PackageManager pm, String packageName) {
        if (pm == null) pm = CustomApplication.getAppContext().getPackageManager();
        try {
            // intent = pm.getLaunchIntentForPackage(packageName);
            // なぜかthrowされないことになっている？ PackageManager.NameNotFoundException e
            // @see http://code.google.com/p/android/issues/detail?id=32328
            // かわりに，下記のメソッドを使うとよい．
            // @see http://qiita.com/items/8db3cabba86453469399
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if (applicationInfo != null) return true;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 暗黙的インテントを扱えるアプリが存在するかどうかチェックします．
     * 
     * @param pm
     *            （null可）
     * @param intent
     * @return 扱える：true
     * @see <a
     *      href="http://9ensan.com/blog/smartphone/android/implicit-intent-startactivity-activitynotfoundexception/">参考ページ</a>
     */
    public static boolean canHandleThisImplicitIntent(PackageManager pm, Intent intent) {
        if (pm == null) pm = CustomApplication.getAppContext().getPackageManager();
        List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
        if (apps.size() > 0) return true;
        return false;
    }
}
