package jp.hackugyo.gatemail.ui.activity;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;

import jp.hackugyo.gatemail.Defines;
import jp.hackugyo.gatemail.R;
import jp.hackugyo.gatemail.service.RecognitionIntentService;
import jp.hackugyo.gatemail.ui.AbsFragmentActivity;
import jp.hackugyo.gatemail.ui.fragment.ErrorDialogFragment;
import jp.hackugyo.gatemail.util.LogUtils;

import java.util.ArrayList;

public class MailMapActivity extends AbsFragmentActivity {

    private static final int REQUEST = 9000;
    private final MailMapActivity self = this;
    private LocationClient mLocationClient;
    private ConnectionCallbacks mLocationClientConnectionCallbacks;
    private ConnectionCallbacks mActivityRecognitionClientConnectionCallbacks;
    private OnConnectionFailedListener mOnConnectionFailedListener;
    private OnAddGeofencesResultListener mOnAddGeofencesResultListener;
    private ActivityRecognitionClient mActivityRecognitionClient;

    /***********************************************
     * Life Cycle *
     ***********************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_map);
        checkGooglePlayServiceAvailable();

        mLocationClient = new LocationClient(self, getLocationClientConnectionCallbacks(), getOnConnectionFailedListener());
        mActivityRecognitionClient = new ActivityRecognitionClient(self, getActivityRecognitionClientConnectionCallbacks(), getOnConnectionFailedListener());
    }

    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.mail_map, menu);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int transitionType = LocationClient.getGeofenceTransition(intent);
        showSingleToast(transitionType == Geofence.GEOFENCE_TRANSITION_ENTER ? "in!" : "out", Toast.LENGTH_LONG);
        LogUtils.i(transitionType == Geofence.GEOFENCE_TRANSITION_ENTER ? "in!" : "out");
    }

    @Override
    protected void onStart() {
        super.onStart();
        setView();
        if (mLocationClient != null) mLocationClient.connect();
        mActivityRecognitionClient.connect();
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

    @Override
    protected void onStop() {
        super.onStop();
        if (mLocationClient != null) mLocationClient.disconnect();
        mActivityRecognitionClient.disconnect();
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
        // showCurrentLocation();
        Intent intent = new Intent(self, MapOfTheEarthActivity.class);
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

    /***********************************************
     * Location API *
     ***********************************************/

    private void showCurrentLocation() {
        if (mLocationClient.isConnected()) {
            Location location = mLocationClient.getLastLocation();
            showSingleToast(location.toString(), Toast.LENGTH_LONG);
        } else {
            showSingleToast("not connected", Toast.LENGTH_SHORT);
        }
    }

    private ConnectionCallbacks getLocationClientConnectionCallbacks() {
        if (mLocationClientConnectionCallbacks == null) {
            mLocationClientConnectionCallbacks = new ConnectionCallbacks() {

                @Override
                public void onConnected(Bundle connectionHint) {
                    addGeofence();
                }

                @Override
                public void onDisconnected() {
                    showSingleToast("onDIisconnected LocationClient", Toast.LENGTH_LONG);
                }
            };
        }
        return mLocationClientConnectionCallbacks;
    }

    private OnConnectionFailedListener getOnConnectionFailedListener() {
        if (mOnConnectionFailedListener == null) {
            mOnConnectionFailedListener = new OnConnectionFailedListener() {

                @Override
                public void onConnectionFailed(ConnectionResult result) {
                    showSingleToast("onConnectionFailed", Toast.LENGTH_LONG);
                }
            };
        }
        return mOnConnectionFailedListener;
    }

    /***********************************************
     * GeoFencing *
     ***********************************************/
    private void addGeofence() {
        // Geofence の作成
        // 緯度
        double latitude = 35.6472069;
        // 経度
        double longitude = 139.7013787;
        // 半径(メートル)
        float radius = 100;
        Geofence.Builder builder = new Geofence.Builder();
        builder.setRequestId("sample_fence");
        builder.setCircularRegion(latitude, longitude, radius);
        builder.setExpirationDuration(Geofence.NEVER_EXPIRE);
        builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);

        ArrayList<Geofence> geofences = new ArrayList<Geofence>();
        geofences.add(builder.build());

        // PendingIntentを作成
        Intent intent = new Intent(self, MailMapActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(self, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mLocationClient.addGeofences(geofences, pendingIntent, getOnAddGeofencesResultListener());
    }

    private OnAddGeofencesResultListener getOnAddGeofencesResultListener() {
        if (mOnAddGeofencesResultListener == null) {
            mOnAddGeofencesResultListener = new OnAddGeofencesResultListener() {

                @Override
                public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
                    showSingleToast("onAddGeofenceResult", Toast.LENGTH_LONG);

                }
            };
        }
        return mOnAddGeofencesResultListener;
    }

    /***********************************************
     * Activity Recognition *
     ***********************************************/

    private ConnectionCallbacks getActivityRecognitionClientConnectionCallbacks() {
        if (mActivityRecognitionClientConnectionCallbacks == null) {

            mActivityRecognitionClientConnectionCallbacks = new ConnectionCallbacks() {

                @Override
                public void onConnected(Bundle connectionHint) {
                    getActivityRecognition();
                }

                @Override
                public void onDisconnected() {
                    showSingleToast("onDIisconnected ActivityRecognitionClient", Toast.LENGTH_LONG);
                }
            };
        }
        return mActivityRecognitionClientConnectionCallbacks;
    }

    private void getActivityRecognition() {
        Intent intent = new Intent(self, RecognitionIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(self, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mActivityRecognitionClient.requestActivityUpdates(1000, pendingIntent);
    }
}
