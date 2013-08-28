package jp.hackugyo.gatemail.ui.activity;

import jp.hackugyo.gatemail.Defines;
import jp.hackugyo.gatemail.Defines.Extra;
import jp.hackugyo.gatemail.R;
import jp.hackugyo.gatemail.ui.AbsFragmentActivity;
import jp.hackugyo.gatemail.ui.fragment.ErrorDialogFragment;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MailMapActivity extends AbsFragmentActivity {

    private static final int REQUEST = 9000;
    private final MailMapActivity self = this;

    /***********************************************
     * Life Cycle *
     ***********************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_map);
        checkGooglePlayServiceAvailable();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent fromToDataIntent) {
        super.onActivityResult(requestCode, resultCode, fromToDataIntent);
        if (requestCode == REQUEST) {
            if (resultCode == RESULT_OK) {
                // TODO: API呼び出し処理
            }
        }
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
     * 
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

    /***********************************************
     * Check GooglePlayServices *
     ***********************************************/

    private void checkGooglePlayServiceAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(self);
        if (resultCode != ConnectionResult.SUCCESS) {
            // ErrorDialogはresultCodeに応じて違うものが返ってくる．
            // resultCodeがConnectionResult.SUCCESS(0)だとnullしか返ってこないので注意
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, self, REQUEST);
            if (dialog != null) {
                ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
                dialogFragment.setDialog(dialog);
                dialogFragment.show(getSupportFragmentManager(), "error_dialog_fragment");
            }
        }
    }
}
