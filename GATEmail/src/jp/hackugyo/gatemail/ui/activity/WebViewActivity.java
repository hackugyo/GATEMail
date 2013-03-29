package jp.hackugyo.gatemail.ui.activity;

import android.os.Bundle;

public class WebViewActivity extends AbsWebViewActivity {

    /***********************************************
     * Life Cycle *
     ***********************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("読み込み中…");
    }

    /***********************************************
     * View *
     ***********************************************/
    @Override
    protected void setWebView() {
        super.setWebView();
    }

    @Override
    protected void setView() {
        // ActionBar設定
        setActionBar();
    }

    /***********************************************
     * OnClickListener *
     ***********************************************/
    @Override
    protected void setActionBar() {
    }
}
