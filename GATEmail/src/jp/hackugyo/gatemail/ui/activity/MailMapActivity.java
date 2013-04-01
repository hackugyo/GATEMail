package jp.hackugyo.gatemail.ui.activity;

import jp.hackugyo.gatemail.R;
import jp.hackugyo.gatemail.util.LogUtils;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class MailMapActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_map);
        LogUtils.i("on create");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mail_map, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
