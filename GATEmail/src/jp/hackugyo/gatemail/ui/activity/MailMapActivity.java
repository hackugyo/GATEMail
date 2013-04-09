package jp.hackugyo.gatemail.ui.activity;

import jp.hackugyo.gatemail.Defines;
import jp.hackugyo.gatemail.Defines.Extra;
import jp.hackugyo.gatemail.R;
import jp.hackugyo.gatemail.ui.AbsFragmentActivity;
import jp.hackugyo.gatemail.util.LogUtils;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MailMapActivity extends AbsFragmentActivity {
    private final MailMapActivity  self = this;


    /***********************************************
     * Life Cycle *
     ***********************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_map);
        LogUtils.i("on create");

    }

    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.mail_map, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        setView();
    }


    /***********************************************
     * Set Views *
     ***********************************************/
    private void setView() {
        // nothing to do.
    }

    /***********************************************
     * OnClick Listener *
     ***********************************************/
    /**
     * binded with Layout.xml file.
     * @param view
     */
    public void onImageListClick(View view) {
        Intent intent = new Intent(this, ImageListActivity.class);
        intent.putExtra(Extra.IMAGES, getImages());
        startActivity(intent);
    }

    /***********************************************
     * Image URLs *
     ***********************************************/
    private String[] getImages() {
        return Defines.IMAGES;
    }
}
