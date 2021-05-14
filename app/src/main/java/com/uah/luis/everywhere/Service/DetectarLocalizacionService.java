package com.uah.luis.everywhere.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.uah.luis.everywhere.Configuracion.Constantes;
import com.uah.luis.everywhere.R;
import com.uah.luis.everywhere.Utils.UtilLocalizacionService;
import com.uah.luis.everywhere.Utils.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/*
 *Este ejemplo muestra cómo utilizar un servicio de larga duración para las actualizaciones de ubicación.
 *Cuando una actividad está vinculada a este servicio, se permiten actualizaciones de ubicación frecuentes.
 *Cuando se elimina la actividad del primer plano, el servicio se promociona a un servicio de primer plano
 *y las actualizaciones de ubicación continúan.
 */
public class DetectarLocalizacionService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {


    private static final String TAG = DetectarLocalizacionService.class.getSimpleName();

    public static final String ACTION_BROADCAST = "broadcast";

    public static final String EXTRA_LOCATION = "location";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = ".started_from_notification";

    private final IBinder mBinder = new LocalBinder();
    private boolean mChangingConfiguration = false;
    private NotificationManager mNotificationManager;
    /**
     * The entry point to Google Play Services.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;

    private Handler mServiceHandler;

    /**
     * The current location.
     */
    private Location mLocation;


    private String direccion;
    private String ciudad;
    private Double latitud;
    private Double longitud;
    private Boolean compartirUbicacionVivo;

    public DetectarLocalizacionService() {
    }


    @Override
    public void onCreate() {

        // Establecer punto de entrada para la API de localizacion
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        createLocationRequest();


        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Cuando el MainActivity  llega al primer plano, el servicio dejara de ser un servicio de primer plano.
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        //Cuando el MainActivity  vuelve al primer plano, el servicio dejara de ser un servicio de primer plano.
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {

        //Se llama cuando el cliente del  MainActivity se desvincula de este servicio.
        if (!mChangingConfiguration && UtilLocalizacionService.requestingLocationUpdates(this)) {

            startForeground(UtilLocalizacionService.NOTIFICATION_ID, getNotification());
        }
        return true;
    }

    /**
     * Se solicita una actualizacion de la ubicación
     */
    public void requestLocationUpdates() {

        UtilLocalizacionService.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), DetectarLocalizacionService.class));
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, DetectarLocalizacionService.this);
        } catch (SecurityException unlikely) {
            UtilLocalizacionService.setRequestingLocationUpdates(this, false);

        }
    }

    /**
     * Elimina las actualizaciones de ubicación.
     */
    public void removeLocationUpdates() {

        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
                    DetectarLocalizacionService.this);
            UtilLocalizacionService.setRequestingLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
            UtilLocalizacionService.setRequestingLocationUpdates(this, true);

        }
    }

    /**
     * Configura la notificacion.
     */
    private Notification getNotification() {

        return new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.texto_detectando_ubicacion_actividad))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis()).build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        try {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } catch (SecurityException unlikely) {

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Metodo que se llama automaticamente cuando la ubicacion cambia..
     */
    @Override
    public void onLocationChanged(Location location) {

        mLocation = location;

        //Obtenemos las prefererencia de compartir ubicacion en vivo
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        compartirUbicacionVivo = sharedPref.getBoolean(Constantes.SP_SWITCH_UBICACION_VIVO, true);


        //Obtenemos la direccion y ciudad, apartir de la latitud y longitud
        obtenerDireccion(location.getLatitude(), location.getLongitude());


        // Notificamos al ActitivtyMain de que se ha obtenido una nueva localización
        Intent intent = new Intent(ACTION_BROADCAST);
        //Añadimos al Intent los parametros a enviar

        intent.putExtra(Constantes.KEY_INTENT_DIRECCION, direccion);
        intent.putExtra(Constantes.KEY_INTENT_CIUDAD, ciudad);
        intent.putExtra(Constantes.KEY_INTENT_LATITUD, latitud);
        intent.putExtra(Constantes.KEY_INTENT_LONGITUD, longitud);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);


    }


    /**
     * Metodo para obtener una direccion, recibe como parametros latitud y longitud.
     **/
    private void obtenerDireccion(final double lat, final double longi) {


        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> list = null;
        try {
            list = geocoder.getFromLocation(lat, longi, 1);
        } catch (IOException e) {
        }

        if (!(list == null)) {

            if (!(list.isEmpty())) {

                latitud = lat;
                longitud = longi;
                Address address = list.get(0);
                direccion = address.getAddressLine(0);
                ciudad = address.getAddressLine(1);

                //Si el usuario ha habilitado compartir ubicacion en vivo
                if (compartirUbicacionVivo) {
                    //Actualizamos la ubicacion en el servidor
                    actulizarDireccionBD(lat, longi, direccion, ciudad);
                }

            }

        }


    }

    /**
     * Metodo para actualizar la ubicacion del usuario en el servidor (BBDD)
     **/
    private void actulizarDireccionBD(double latitud, double longitud, String direccion, String ciudad) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String idUsuario = sharedPref.getString(Constantes.SP_ID_USUARIO, "");

        // Mapeo previo
        HashMap<String, String> params = new HashMap<>();

        params.put(Constantes.KEY_ID_USUARIO, idUsuario);
        params.put(Constantes.KEY_LATITUD, String.valueOf(latitud));
        params.put(Constantes.KEY_LONGITUD, String.valueOf(longitud));
        params.put(Constantes.KEY_DIRECCION, direccion);
        params.put(Constantes.KEY_CIUDAD, ciudad);

        // Crear nuevo objeto Json basado en el mapa
        JSONObject jobject = new JSONObject(params);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                Constantes.ACTUALIZAR_UBICACION, jobject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Obtener atributo "estado"
                            String estado = response.getString("estado");
                            switch (estado) {
                                case "1":
                                    break;
                                case "2":
                                    break;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {


            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Accept", "application/json");
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8" + getParamsEncoding();
            }
        };
        jsonObjReq.setShouldCache(false);
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjReq);


    }

    /**
     * Metodo para configurar los parametros de la solicitud de ubicacion.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UtilLocalizacionService.UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(UtilLocalizacionService.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    public class LocalBinder extends Binder {
        public DetectarLocalizacionService getService() {
            return DetectarLocalizacionService.this;
        }
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */

    /*
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }*/
}