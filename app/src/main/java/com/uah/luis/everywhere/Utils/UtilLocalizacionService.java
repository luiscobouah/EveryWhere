package com.uah.luis.everywhere.Utils;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Usuario on 01/06/2017.
 */

public class UtilLocalizacionService {


    public static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;


    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    public static final int NOTIFICATION_ID = 12345678;


    /**
     * Devuelve true si solicita actualizaciones de ubicación, de lo contrario devuelve false.
     *
     */
    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Almacena el estado de actualizaciones de ubicación en SharedPreferences.
     *
     */
    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

}
