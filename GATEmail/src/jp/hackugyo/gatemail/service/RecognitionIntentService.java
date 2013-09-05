package jp.hackugyo.gatemail.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import jp.hackugyo.gatemail.R;
import jp.hackugyo.gatemail.ui.activity.MailMapActivity;

public class RecognitionIntentService extends IntentService {

    private final RecognitionIntentService self = this;

    public RecognitionIntentService(String name) {
        super(name);
    }

    public RecognitionIntentService() {
        super("RecognitionIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity mostProbableAction = result.getMostProbableActivity();
            int confidence = mostProbableAction.getConfidence();
            int actionType = mostProbableAction.getType();
            notify(getTypeName(actionType));
        }
    }

    private static String getTypeName(int actionType) {
        switch (actionType) {
            case DetectedActivity.IN_VEHICLE:
                return "車で移動中";
            case DetectedActivity.ON_BICYCLE:
                return "自転車で移動中";
            case DetectedActivity.ON_FOOT:
                return "徒歩で移動中";
            case DetectedActivity.STILL:
                return "待機中";
            case DetectedActivity.TILTING:
                return "デバイスが傾き中";
            case DetectedActivity.UNKNOWN:
            default:
                return "不明";
        }
    }

    private void notify(String message) {
        // Intent作成
        Intent intent = new Intent(self, MailMapActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(self, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        // NotificationBuilderで作成
        NotificationCompat.Builder builder = new NotificationCompat.Builder(self);
        builder.setContentIntent(pendingIntent);
        builder.setTicker(message);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentText(message);
        builder.setContentTitle("活動認識");
        builder.setLargeIcon(largeIcon);
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }
}
