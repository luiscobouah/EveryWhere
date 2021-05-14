package com.uah.luis.everywhere.Activitys;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.uah.luis.everywhere.Configuracion.Constantes;
import com.uah.luis.everywhere.Configuracion.SesionUsuario;
import com.uah.luis.everywhere.Modelo.Usuario;
import com.uah.luis.everywhere.Utils.VolleySingleton;
import com.uah.luis.everywhere.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Activity para gestionar el login de un usuario en la aplicación.
 */
public class IniciarSesionActivity extends AppCompatActivity {


    // Se utiliza para comprobar si hay permisos de tiempo de ejecución.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    @Bind(R.id.etEmail)
    EditText _etEmail;
    @Bind(R.id.etContrasena)
    EditText _etContrasena;
    @Bind(R.id.btn_iniciar_sesion)
    Button _btn_iniciar_sesion;
    @Bind(R.id.link_registrarse)
    TextView _link_registrarse;

    private ProgressDialog progressDialog;
    private SesionUsuario session;
    private boolean estaLogeado;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_sesion);
        ButterKnife.bind(this);


        progressDialog = new ProgressDialog(IniciarSesionActivity.this);

        session = new SesionUsuario(getApplicationContext());
        estaLogeado = session.estaLogeado();

        //Comprobamos si esta logeado para saltar el login.
        if (estaLogeado) {
            Intent intent = new Intent(IniciarSesionActivity.this, MainActivity.class);
            startActivity(intent);
        }


        //Comprobamos que se han cocedido los permisos
        if(!comprobarPermisosSMS()) {
            solicitarPermisosSMS();
        }

        //Fragmento de codigo encargado de la funcionalidad del button para logearse con correo y contraseña.
        _btn_iniciar_sesion.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isOnline(getApplicationContext())) {
                    if (validate()) {
                        comprobarLogin();
                    }
                } else {

                    Snackbar.make(findViewById(android.R.id.content), R.string.texto_no_conexion_internet, Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        });

        //Fragmento de codigo encargado de la funcionalidad del link de registrase
        _link_registrarse.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), RegistrarseActivity.class);
                startActivity(intent);
            }
        });


    }

    public void comprobarLogin() {

        // Añadir parámetro a la URL del web service
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.texto_progresDialog_autentificando));
        progressDialog.show();

        final String correo = _etEmail.getText().toString();
        final String contrasena = _etContrasena.getText().toString();

        // Mapeo previo
        HashMap<String, String> params = new HashMap<>();
        params.put(Constantes.KEY_EMAIL_USUARIO, correo);
        params.put(Constantes.KEY_CONTRASENA_USUARIO, contrasena);
        // Crear nuevo objeto Json basado en el mapa
        JSONObject jobject = new JSONObject(params);


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                Constantes.COMPROBAR_LOGIN, jobject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Obtener atributo "estado"
                            String estado = response.getString("estado");

                            switch (estado) {
                                case "1":
                                    progressDialog.dismiss();
                                    //Actualizamos el valor de logeado, en este caso en true, para indicar que si esta logeado
                                    session.actualizarLogeado(true);
                                    //obtenemos los datos de usuario
                                    obtenerDatosUsuario();
                                    break;
                                case "2":
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), getString(R.string.texto_error_usuario_contrasena), Toast.LENGTH_LONG).show();
                                    break;
                                case "3":
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), getString(R.string.texto_error_usuario_contrasena), Toast.LENGTH_LONG).show();
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

    public void obtenerDatosUsuario() {
        // Añadir parámetro a la URL del web service
        String email = _etEmail.getText().toString();
        HashMap<String, String> params = new HashMap<>();// Mapeo previo
        params.put(Constantes.KEY_EMAIL_USUARIO, email);
        // Crear nuevo objeto Json basado en el map
        JSONObject jobject = new JSONObject(params);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                Constantes.OBTENER_DATOS_USUARIO, jobject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Obtener atributo "estado"
                            String estado = response.getString("estado");

                            switch (estado) {
                                case "1":
                                    // Obtener objeto "usuario"
                                    JSONObject object = response.getJSONObject("usuario");
                                    Gson gson = new Gson();
                                    //Parsear objeto
                                    Usuario usuario = gson.fromJson(object.toString(), Usuario.class);

                                    //Actualizamos las preferencias del usuario, con los datos recibidos del servidor.
                                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    SharedPreferences.Editor editor = sharedPref.edit();

                                    editor.putString(Constantes.SP_NOMBRE_USUARIO, usuario.getNOMBRE());
                                    editor.putString(Constantes.SP_ID_USUARIO, usuario.getID_USUARIO());
                                    editor.putString(Constantes.SP_EMAIL_USUARIO, usuario.getEMAIL());

                                    editor.putString(Constantes.SP_NOMBRE_CONTACTO, usuario.getNOMBRE_CONTACTO());
                                    editor.putString(Constantes.SP_EMAIL_CONTACTO, usuario.getEMAIL_CONTACTO());
                                    editor.putString(Constantes.SP_NUMERO_CONTACTO, usuario.getNUMERO_MOVIL());
                                    editor.commit();
                                    finish();
                                    //Lanzamos la actividad principal
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);

                                    break;
                                case "2":
                                    Toast.makeText(getApplicationContext(), getString(R.string.texto_error_datos_usuario), Toast.LENGTH_LONG).show();
                                    break;


                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
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
     * Metodo para comprobar  si se han concedido los permisos o no
     * */
    private boolean comprobarPermisosSMS() {


        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS);
    }

    /**
     * Metodo para solicitar permiso de localizacion
     * */
    private void solicitarPermisosSMS() {

        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.SEND_SMS);

        if (shouldProvideRationale) {

                            // Se abre el cuadro de dialogo para permitir o no la ubicacion.
                            ActivityCompat.requestPermissions(IniciarSesionActivity.this,
                                    new String[]{Manifest.permission.SEND_SMS},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);


        } else {

            ActivityCompat.requestPermissions(IniciarSesionActivity.this,
                    new String[]{Manifest.permission.SEND_SMS},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     *Se obtiene la respuesta del usuario con respcto a la asignacion del permiso de ubicacion.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // si se aha interrumpido, se obtiene un array vacio y los permisos se cancelan.
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El permiso ha sido asginado.
            } else {

            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.exit(0);
        finish();
    }

    //Comprobamos que el movil tiene conoexión a internet, mediante wifi o cobertura movil.
    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

    //Método para validar que se introducen los datos con un formato correcto.
    private boolean validate() {
        boolean valid = true;

        String email = _etEmail.getText().toString();
        String password = _etContrasena.getText().toString();

        //Comprobamos que el email no es vacio, y que cumple el formato de email.
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _etEmail.setError(getString(R.string.texto_error_correo));
            valid = false;
        } else {
            _etEmail.setError(null);
        }

        //Comprobamos que la contraseña es mayor de 4 caracteres y menor que 10
        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _etContrasena.setError(getString(R.string.texto_error_contrasena));
            valid = false;
        } else {
            _etContrasena.setError(null);
        }

        return valid;
    }


}

