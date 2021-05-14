package com.uah.luis.everywhere.Service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.uah.luis.everywhere.Utils.UtilActividadService;

import java.util.List;

public class DetectarActividadIntentService extends IntentService {

    private static final String TAG = DetectarActividadIntentService.class.getSimpleName();

    public DetectarActividadIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Lo primero es obtener el resultado de reconocimiento.
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        // Luego, obtén actividad más probable.
        DetectedActivity detectedActivity = result.getMostProbableActivity();

        int type = detectedActivity.getType();

        if (DetectedActivity.ON_FOOT == type) {
            type = walkingOrRunning(result.getProbableActivities());
        }

        // Ahora, crea un intent con una acción personalizada.
        Intent localIntent = new Intent(UtilActividadService.BROADCAST_ACTION);

        // El siguiente paso, es poner las actividades en el intent
        localIntent.putExtra(UtilActividadService.ACTIVITY_KEY, type);
        // Y finalmente envía los datos hacia la activity
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }



    private int walkingOrRunning(List<DetectedActivity> probableActivities) {
        int walkActivity = 0, runningActivity = 0;

        for (DetectedActivity probableActivity : probableActivities) {
            if (probableActivity.getType() == DetectedActivity.WALKING) {
                walkActivity = probableActivity.getConfidence();
            } else if (probableActivity.getType() == DetectedActivity.RUNNING) {
                runningActivity = probableActivity.getConfidence();
            }
        }

        return (walkActivity >= runningActivity) ? DetectedActivity.WALKING : DetectedActivity.RUNNING;
    }

}
