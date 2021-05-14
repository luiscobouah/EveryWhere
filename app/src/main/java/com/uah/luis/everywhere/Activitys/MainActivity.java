package com.uah.luis.everywhere.Activitys;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.uah.luis.everywhere.Configuracion.Constantes;
import com.uah.luis.everywhere.Configuracion.SesionUsuario;
import com.uah.luis.everywhere.Service.DetectarActividadIntentService;
import com.uah.luis.everywhere.Service.DetectarLocalizacionService;
import com.uah.luis.everywhere.Utils.EnviarEmail;
import com.uah.luis.everywhere.Utils.UtilActividadService;
import com.uah.luis.everywhere.R;


import butterknife.Bind;
import butterknife.ButterKnife;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.uah.luis.everywhere.Utils.UtilActividadService.ACTIVITY_RECOGNITION_INTERVAL;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    // Se utiliza para comprobar si hay permisos de tiempo de ejecución.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE_FINE_LOCATION = 34;


    // BroadcastReceiver utilizado para recibir las actulizaciones del servicio de localizacion.
    private LocationDetectionBroadcastReceiver mBroadcastReceiverLocalizacion;

    // BroadcastReceiver utilizado para recibir las actulizaciones del servicio de actividad.
    private ActivityDetectionBroadcastReceiver mBroadcastReceiverActividad;

    // referencia al servicio de localizacion.
    private DetectarLocalizacionService mService = null;

    // Variable para almcenar el estado de enlaze del servicio de ubicacion.
    private boolean mBound = false;


    // Monitorizamos el estado dde la conexion con el servicio de ubicacion.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DetectarLocalizacionService.LocalBinder binder = (DetectarLocalizacionService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };


    @Bind(R.id.ivActividadRealizada)
    ImageView _ivActividadRealizad;
    @Bind(R.id.tvCompartirUbicacionVivo)
    TextView _tvCompartirUbicacionVivo;
    @Bind(R.id.tvDireccion)
    TextView _tvDireccion;
    @Bind(R.id.tvCiudad)
    TextView _tvCiudad;
    @Bind(R.id.btIniciar)
    Button _btIniciar;
    @Bind(R.id.btEmergencia)
    Button _btEmergencia;


    private GoogleApiClient mGoogleApiClient;
    private int mImageResource;

    private int actividadActual;
    private int actividadAnterior;


    //Datos de preferencias de usuario.
    private String emailContacto;
    private String numeroMovil;
    private boolean enviarSMS;
    private boolean enviarEmail;
    private boolean emitirsonido;
    private boolean compartirUbicacionVivo;
    private String tiempoAlerta;
    private String nombreUsuario;


    private Double latitud;
    private Double longitud;
    private String direccion;
    private String ciudad;


    MediaPlayer sonidoEnvioAlerta;
    MediaPlayer sonidoInicioContador;
    CountDownTimer contadorEnvioAlerta;
    CountDownTimer contadorEnvioAlertaBotonEmergencia;

    private ProgressDialog progressBarDialogContadorAlerta;
    private ProgressDialog progressBarDialogContadorAlertaBotonEmergencia;
    private ProgressBar barraProgresoCircular;

    private SesionUsuario session;
    private boolean estaLogeado;

    private int i;

    private static final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Inicializamos elementos de interfaz
        ButterKnife.bind(this);
        //Inicializamos demas elementos componentes
        inicializarComponentes();

        //Obtenemos la session.
        session = new SesionUsuario(getApplicationContext());
        estaLogeado = session.estaLogeado();
        //Comprobamos si esta logeado para saltar el login.
        if (!estaLogeado) {
            finish();
            Intent intent = new Intent(MainActivity.this, IniciarSesionActivity.class);
            startActivity(intent);
        }

        mBroadcastReceiverLocalizacion = new LocationDetectionBroadcastReceiver();
        mBroadcastReceiverActividad = new ActivityDetectionBroadcastReceiver();



        // Enlazar el servicio de localizacion con este activity para recibir la localizacion cuando este en segundo plano la aplicación.
        bindService(new Intent(this, DetectarLocalizacionService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        // Establecer punto de entrada para la API de ubicación
        buildGoogleApiClient();


        //Listener para el boton de iniciar/detener
        _btIniciar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String textoButton = _btIniciar.getText().toString();
                if (textoButton.equalsIgnoreCase("INICIAR") || (textoButton.equalsIgnoreCase("START"))) {

                    //Si no se ha se ha introducido un numero o email de contacto, se le indica al
                    // usuario y no se inicia el seguimiento
                    if (numeroMovil.equalsIgnoreCase("") && emailContacto.equalsIgnoreCase("")) {

                        Snackbar.make(
                                findViewById(R.id.activity_main),
                                R.string.texto_Snackbar_introducir_contacto,
                                Snackbar.LENGTH_INDEFINITE)
                                .setAction(R.string.texto_Snackbar_ok, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //Se abre el activity de preferencias
                                        startActivity(new Intent(getApplicationContext(), PreferenciasActivity.class));

                                    }
                                })
                                .show();
                    } else {
                        if (!comprobarPermisoLocalizacion()) {
                            solicitarPermisoLocalizacion();
                        } else {
                            iniciarSeguimiento();
                        }
                    }

                } else if (textoButton.equalsIgnoreCase("DETENER") || (textoButton.equalsIgnoreCase("STOP"))) {
                    detenerSeguimiento();
                }


            }
        });

        //Listener para el boton de emergencia
        _btEmergencia.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                if (numeroMovil.equalsIgnoreCase("") && emailContacto.equalsIgnoreCase("")) {

                    Snackbar.make(
                            findViewById(R.id.activity_main),
                            R.string.texto_Snackbar_introducir_contacto,
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.texto_Snackbar_ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //Se abre el activity de preferencias
                                    startActivity(new Intent(getApplicationContext(), PreferenciasActivity.class));

                                }
                            })
                            .show();
                } else {
                    if (!comprobarPermisoLocalizacion()) {
                        solicitarPermisoLocalizacion();
                    } else {
                        //Activamos la recepcion de la ubicacion.
                        mService.requestLocationUpdates();
                        //se activa el contador del boton de emergencia.
                        activarContadorBotonEmergencia();
                    }
                }



            }
        });
    }

    /**
     * Metodo para iniciar componentes del activity
     */
    private void inicializarComponentes() {

        progressBarDialogContadorAlerta = new ProgressDialog(this);
        progressBarDialogContadorAlertaBotonEmergencia = new ProgressDialog(this);
        barraProgresoCircular = (ProgressBar) findViewById(R.id.progressBarCircle);
        barraProgresoCircular.setVisibility(View.GONE);
        sonidoEnvioAlerta = MediaPlayer.create(this, R.raw.sonido_alerta_enviada);
        sonidoInicioContador = MediaPlayer.create(this, R.raw.sonido_inicio_alerta);

    }

    // Establecer punto de entrada para la API de Reconocimiento de actividad
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .enableAutoManage(this, this)
                .build();
    }

    /**
     * Metodo para iniciar la recepción de actividades.
     */
    private void startActivityUpdates() {
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                ACTIVITY_RECOGNITION_INTERVAL,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
    }

    /**
     * Metodo para detener la recepción de actividades.
     */
    private void stopActivityUpdates() {
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
    }

    /**
     * Metodo para actualizar imagen de actividad realizada.
     */
    private void actualizarImagenActividadRealizada() {
        _ivActividadRealizad.setImageResource(mImageResource);

    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectarActividadIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    /**
     * Metodo para comprobar  si se han concedido los permisos o no
     */
    private boolean comprobarPermisoLocalizacion() {


        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    /**
     * Metodo para solicitar permiso de localizacion
     */
    private void solicitarPermisoLocalizacion() {

        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        //Se indica mediante una Snackbar al usuario que permita la ubicacion
        if (shouldProvideRationale) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE_FINE_LOCATION);

        } else {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE_FINE_LOCATION);
        }
    }


    /**
     * Se obtiene la respuesta del usuario con respcto a la asignacion del permiso de ubicacion y envio de SMS
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE_FINE_LOCATION) {
            if (grantResults.length <= 0) {
                // si se aha interrumpido, se obtiene un array vacio y los permisos se cancelan.
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El permiso ha sido asginado.
                iniciarSeguimiento();
            } else {

            }
        }


    }

    /**
     * Método para enviar el SMS
     */
    private void enviarSMS() {

        PackageManager pm = this.getPackageManager();
        //Comprobamos que se pueden enviar SMS desde el dispositivo.
        if (!pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY) && !pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_CDMA)) {
            Toast.makeText(this, getString(R.string.texto_no_posible_enviar_sms), Toast.LENGTH_SHORT).show();
        } else {

            try {
                //Configuramos el SMS con el nombre, mensaje, y destinatario.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
                String strPhone = numeroMovil;
                String strMessage = getString(R.string.texto_se_ha_detenido) + " https://maps.google.com/?q=" + latitud + "," + longitud;
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(strPhone, null, strMessage, null, null);
                Toast.makeText(this, getString(R.string.texto_sms_enviado), Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                // This will catch any exception, because they are all descended from Exception
                Toast.makeText(this, getString(R.string.texto_error_enviar_sms), Toast.LENGTH_SHORT).show();

            }

        }

    }

    /**
     * Método para enviar el Email
     */
    private void enviarEmail() {
        //Creamos el objeto Email
        EnviarEmail sm = new EnviarEmail(this, emailContacto, getString(R.string.texto_no_detecto_movimiento), nombreUsuario + " " + getString(R.string.texto_se_ha_detenido) + " " + direccion + ", "
                + ciudad + ". " + getString(R.string.texto_latitud) + latitud + " y " + getString(R.string.texto_longitud) + longitud + "    " + "https://maps.google.com/?q=" + latitud + "," + longitud);
        //Ejecutamos para el envio del email.
        sm.execute();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Método para obtener las preferencias del SharedPreferences
     */
    private void obtenerPreferencias() {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        enviarSMS = sharedPref.getBoolean(Constantes.SP_SWITCH_SMS, false);
        enviarEmail = sharedPref.getBoolean(Constantes.SP_SWITCH_EMAIL, true);
        compartirUbicacionVivo = sharedPref.getBoolean(Constantes.SP_SWITCH_UBICACION_VIVO, true);
        emitirsonido = sharedPref.getBoolean(Constantes.SP_SWITCH_SONIDO, true);
        emailContacto = sharedPref.getString(Constantes.SP_EMAIL_CONTACTO, "");
        numeroMovil = sharedPref.getString(Constantes.SP_NUMERO_CONTACTO, "");
        tiempoAlerta = sharedPref.getString(Constantes.SP_LISTA_TIEMPO_ALERTA, "2");
        nombreUsuario = sharedPref.getString(Constantes.SP_NOMBRE_USUARIO, "");

    }

    /**
     * Contador de x segundos configurados por el usuario.
     **/
    private void activarContador() {

        if (emitirsonido){
            //Reproducimos el sonido de inicio de contador
            sonidoInicioContador.start();
        }

        barraProgresoCircular.setVisibility(INVISIBLE);
        long tiempo;
        //Calculamos el tiempo que ha configurado el usuario.
        tiempo = Long.parseLong((tiempoAlerta)) * 60000;
        i = (Integer.parseInt(tiempoAlerta) * 60000) / 1000;

        progressBarDialogContadorAlerta.setCancelable(true);
        progressBarDialogContadorAlerta.setIcon(R.drawable.ic_notifications_black_24dp);


        if (!isFinishing()) {
            progressBarDialogContadorAlerta.show();  //show dialog
        }

        progressBarDialogContadorAlerta.setOnDismissListener(new DialogInterface.OnDismissListener() {

            //Metodo para cuando la barra de progreso se cierra.
            @Override
            public void onDismiss(DialogInterface dialog) {
                barraProgresoCircular.setVisibility(VISIBLE);
                //Asignamos actividadAnterior=6, para que sea diferente a la siguiente y asi detecte el cambio de actividad
                actividadAnterior = 6;
                detenerContador();

                sonidoInicioContador.pause();
            }

        });

        progressBarDialogContadorAlerta.setOnCancelListener(new ProgressDialog.OnCancelListener() {

            //Metodo para cuando la barra de progreso se cancela
            @Override
            public void onCancel(DialogInterface dialog) {
                barraProgresoCircular.setVisibility(VISIBLE);
                //Asignamos actividadAnterior=6, para que sea diferente a la siguiente y asi detecte el cambio de actividad
                actividadAnterior = 6;
                detenerContador();

                sonidoInicioContador.pause();
            }

        });


        contadorEnvioAlerta = new CountDownTimer(tiempo, 1000) {
            // Al declarar un nuevo CountDownTimer nos obliga a
            // sobreescribir algunos de sus eventos.
            @Override
            public void onTick(long millisUntilFinished) {
                // Este método se lanza por cada lapso de tiempo transcurrido
                progressBarDialogContadorAlerta.setMessage(getString(R.string.texto_se_enviara_alerta) + " " + i + " " + getString((R.string.texto_segundos)));
                i--;
            }

            @Override
            public void onFinish() {
                // Este método se lanza cuando finaliza el contador.
                progressBarDialogContadorAlerta.dismiss();

                if (emitirsonido) {
                    sonidoEnvioAlerta.start();
                }
                if (enviarEmail) {
                    enviarEmail();
                }
                if (enviarSMS) {
                    enviarSMS();
                }
            }
        }.start();
        // Una vez configurado el contador, lo iniciamos.

    }

    /*
    Contador de 20 segundos para cuando se presiona el boton de emergencia
     */
    private void activarContadorBotonEmergencia() {
        i = 5;

        //configuramose el cuadro de dialogo de la alerta
        progressBarDialogContadorAlertaBotonEmergencia.setCancelable(false);
        progressBarDialogContadorAlertaBotonEmergencia.setIcon(R.drawable.ic_notifications_black_24dp);

        if (!isFinishing()) {
            progressBarDialogContadorAlertaBotonEmergencia.show();  //show dialog
        }
        progressBarDialogContadorAlertaBotonEmergencia.setOnDismissListener(new DialogInterface.OnDismissListener() {

            //Metodo para cuando la barra de progreso se cierra.
            @Override
            public void onDismiss(DialogInterface dialog) {

                //Cambiamos el texto y el color al boton
                _tvDireccion.setText(getString(R.string.texto_direccion));
                _tvCiudad.setText(getString(R.string.texto_ciudad));
                _ivActividadRealizad.setImageResource(R.drawable.ic_desconocido);


                String textoButton = _btIniciar.getText().toString();
                if (textoButton.equalsIgnoreCase("INICIAR") || (textoButton.equalsIgnoreCase("START"))) {
                    //Desactivamos la recepcion de la ubicacion.
                    mService.removeLocationUpdates();
                }
            }

        });

        progressBarDialogContadorAlertaBotonEmergencia.setOnCancelListener(new ProgressDialog.OnCancelListener() {

            //Metodo para cuando la barra de progreso se cancela.
            @Override
            public void onCancel(DialogInterface dialog) {
                // detenerContadorBotonEmergencia();
            }

        });

        contadorEnvioAlertaBotonEmergencia = new CountDownTimer(5000, 1000) {
            // Al declarar un nuevo CountDownTimer nos obliga a
            // sobreescribir algunos de sus eventos.
            @Override
            public void onTick(long millisUntilFinished) {
                // Este método se lanza por cada lapso de tiempo transcurrido
                progressBarDialogContadorAlertaBotonEmergencia.setMessage(getString(R.string.texto_enviando_alerta));

                //progressBarDialogContadorAlertaBotonEmergencia.setMessage(getString(R.string.texto_se_enviara_alerta)+" " + i +" "+ getString((R.string.texto_segundos)));
                i--;

            }

            @Override
            public void onFinish() {
                // Este método se lanza cuando finaliza el contador.
                progressBarDialogContadorAlertaBotonEmergencia.dismiss();

                if (emitirsonido) {
                    sonidoEnvioAlerta.start();
                }
                if (enviarEmail) {
                    enviarEmail();
                }
                if (enviarSMS) {
                    enviarSMS();
                }
            }
        }.start();
        // Una vez configurado el contador, lo iniciamos.

    }


    /**
     * Metodo para detener el contador
     **/
    private void detenerContador() {
        if (contadorEnvioAlerta != null) {

            contadorEnvioAlerta.cancel();
            progressBarDialogContadorAlerta.dismiss();
        }

    }



    /**
     * Metodo para iniciar el seguimiento de actividad y ubicación
     **/
    private void iniciarSeguimiento() {

        // Cambiarmos el texto y el color al boton
        _btIniciar.setText(getString(R.string.texto_detener));
        _btIniciar.setBackgroundColor(getResources().getColor(R.color.colorAmarilloButton));
        //Activamos la recepcion de la ubicacion.
        mService.requestLocationUpdates();
        //Activamos la recepcion de actividad
        startActivityUpdates();
        //Activamos la barra de progreso circular.
        barraProgresoCircular.setVisibility(VISIBLE);
        barraProgresoCircular.setIndeterminate(true);


        IntentFilter intentFilter = new IntentFilter(UtilActividadService.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(mBroadcastReceiverActividad, intentFilter);

    }

    /**
     * Metodo para detener el seguimiento de actividad y ubicación
     **/
    private void detenerSeguimiento() {

        //Desactivamos la recepcion de la ubicacion.
        mService.removeLocationUpdates();
        //Desactivamos la recepcion de actividad
        stopActivityUpdates();
        //Detenemos el contador
        detenerContador();


        //Cambiamos el texto y el color al boton
        _btIniciar.setText(getString(R.string.texto_iniciar));
        _btIniciar.setBackgroundColor(getResources().getColor(R.color.colorVerdeButton));

        //Cambiamos el texto y el color al boton
        _tvDireccion.setText(getString(R.string.texto_direccion));
        _tvCiudad.setText(getString(R.string.texto_ciudad));
        _ivActividadRealizad.setImageResource(R.drawable.ic_desconocido);

        barraProgresoCircular.setVisibility(INVISIBLE);


        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(mBroadcastReceiverActividad);

    }


    @Override
    protected void onResume() {
        super.onResume();

        //Comprobamos siempre si el usuario esta logeado o no en la aplicación.
        if (!estaLogeado) {

            Intent intent = new Intent(MainActivity.this, IniciarSesionActivity.class);
            startActivity(intent);
        }
        IntentFilter intentFilter = new IntentFilter(UtilActividadService.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mBroadcastReceiverActividad, intentFilter);

        obtenerPreferencias();


        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverLocalizacion,
                new IntentFilter(DetectarLocalizacionService.ACTION_BROADCAST));


    }


    @Override
    protected void onStop() {
        if (mBound) {
            // Desvinculamos el servicio de ubicacion..
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
    }


    @Override
    public void onResult(@NonNull Status status) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public void onBackPressed() {

        //Preguntamos al usuario si realmente desea salir de la aplicación
        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(MainActivity.this);
        dialogo1.setTitle(getString(R.string.texto_AlertDialog_titulo));
        dialogo1.setMessage(getString(R.string.texto_AlertDialog_mensaje));
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton(getString(R.string.texto_AlertDialog_salir), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {

                //Detenemos el seguimiento y finalizamos la aplicación.
                detenerSeguimiento();
                finish();


            }
        });
        dialogo1.setNegativeButton(getString(R.string.texto_AlertDialog_cancelar), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {

            }
        });
        dialogo1.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_preferencias:
                //Detenemos el seguimiento y lanzamos la activity de ajustes
                detenerSeguimiento();
                startActivity(new Intent(this, PreferenciasActivity.class));

                return true;
            case R.id.action_seguimiento_usuarios:
                //Detenemos el seguimiento y lanzamos la activty de seguimiento de usuarios.
                detenerSeguimiento();
                startActivity(new Intent(this, SeguimientoUsuariosActivity.class));

                return true;

            case R.id.action_cerrar_sesion:
                //Se cierra sesion.
                //Detenemos el seguimiento y lanzamos la activty de seguimiento de usuarios.
                session.cerrarSesion();
                detenerSeguimiento();
                finish();
                startActivity(new Intent(this, IniciarSesionActivity.class));

                return true;


        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Receptor de emisiones enviadas por {@link DetectarLocalizacionService}.
     */
    private class LocationDetectionBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Indicamos si no se esta compartiendo la ubicacion en vivo.
            if (!compartirUbicacionVivo) {
                _tvCompartirUbicacionVivo.setText(getString(R.string.texto_no_compartiendo_ubicacion));
            } else {
                _tvCompartirUbicacionVivo.setText("");
            }

            //Obtenemos los datos del Intent.
            direccion = intent.getStringExtra(Constantes.KEY_INTENT_DIRECCION);
            ciudad = intent.getStringExtra(Constantes.KEY_INTENT_CIUDAD);
            latitud = intent.getDoubleExtra(Constantes.KEY_INTENT_LATITUD, 0);
            longitud = intent.getDoubleExtra(Constantes.KEY_INTENT_LONGITUD, 0);

            //Actulizamos los datos de direccion de la interfaz.
            _tvDireccion.setText(direccion);
            _tvCiudad.setText(ciudad);
        }
    }

    /**
     * Receptor de emisiones enviadas por {@link DetectarActividadIntentService}.
     */
    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Obtenemos el int del tiempo de actividad realizada.
            int type = intent.getIntExtra(UtilActividadService.ACTIVITY_KEY, -1);
            actividadActual = type;

            //Comprobamos si la actividad detectada es igual a actividad sin movimiento.
            if (actividadActual == DetectedActivity.STILL) {

                //Si la actividad detectata ( es sin movimiento) es diferente a la actividad anterior
                if ((actividadActual != actividadAnterior)) {
                    actividadAnterior = actividadActual;
                    //Activamos el contador para envio de alerta.

                    activarContador();

                    //Si la actividad detectada ( es sin movimiento) es igual a la anterior.
                } else {
                    actividadAnterior = actividadActual;
                }

                //Si la actividad detectada es diferente a actividad sin moviento
            } else {
                actividadAnterior = actividadActual;
                //detenemos el contador
                detenerContador();

            }
            //actulizamos la imagen de la actividad realizada.
            mImageResource = UtilActividadService.obtenerIconoActividad(type);
            actualizarImagenActividadRealizada();
        }


    }


}