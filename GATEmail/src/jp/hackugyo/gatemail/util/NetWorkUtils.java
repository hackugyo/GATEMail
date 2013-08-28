package jp.hackugyo.gatemail.util;

import jp.hackugyo.gatemail.CustomApplication;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetWorkUtils {
    @SuppressWarnings("unused")
    private final NetWorkUtils self = this;

    /**
     * ネットワーク(3G, Wi-Fi)に接続しているかどうかを取得します.
     * 
     * @return ネットワークに接続しているかどうか。true の場合は接続している
     */
    public static boolean isConnected() {

        CustomApplication.getAppContext().enforceCallingOrSelfPermission(//
                android.Manifest.permission.ACCESS_NETWORK_STATE, "need permission: ACCESS_NETWORK_STATE");
        ConnectivityManager cm = (ConnectivityManager) CustomApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null) {
            return cm.getActiveNetworkInfo().isConnected();
        }
        return false;
    }
}
