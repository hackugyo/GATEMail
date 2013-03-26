package jp.hackugyo.gatemail;


/**
 * 内部で用いるコードなどの定義をまとめた定数クラスです．<br>
 * 実際の値はvalues/defines.xmlに記述しています．<br>
 * メッセージの文言等はvalues/strings.xmlに記述してください．<br>
 * 
 * @author kwatanabe
 * 
 */
public class Defines {
    /****************************************************
     * Shared Preferences *
     ***************************************************/
    /** 共有設定のタグ */
    public static final String SHARED_PREFERENCES = "shared_pref";

    /** 登録済みアプリのJSON化したものをリスト化して保存しておくKEY */
    public static final String SHARED_PREFERENCES_KEY_REGISTERED_APP_LIST = "key_registered_app_list";


    /****************************************************
     * その他
     ***************************************************/
    /** ログ出力時のデフォルトタグ */
    public static final String LOG_TAG = "TAG";

}
