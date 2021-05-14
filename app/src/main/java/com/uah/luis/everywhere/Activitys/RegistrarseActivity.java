package com.uah.luis.everywhere.Activitys;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import com.uah.luis.everywhere.Configuracion.Constantes;
import com.uah.luis.everywhere.Utils.VolleySingleton;
import com.uah.luis.everywhere.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RegistrarseActivity extends AppCompatActivity {


    @Bind(R.id.etNombre)
    EditText _etNombre;
    @Bind(R.id.etEmail)
    EditText _etEmail;
    @Bind(R.id.etContrasena)
    EditText _etContrasena;
    @Bind(R.id.etConfirmarContrasena)
    EditText _etConfirmarContrasena;
    @Bind(R.id.btn_registrarse)
    Button _btn_registrarse;
    @Bind(R.id.link_login)
    TextView _link_login;

    ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_usuario);
        ButterKnife.bind(this);

        //progresDialog que informara al usuario de que se esta realizando el registro
        progressDialog = new ProgressDialog(RegistrarseActivity.this);

        _btn_registrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Comprobamos si el dispositivo tiene conexion a internet.
                if (comprobarConexion(getApplicationContext())) {

                    //comprobamos si el formato de los campos es válido
                    if (validate()) {
                        registrarUsuario();
                    }
                } else {

                    Snackbar.make(findViewById(android.R.id.content), R.string.texto_no_conexion_internet, Snackbar.LENGTH_LONG)
                            .show();
                }


            }
        });

        _link_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Finalizamos la activity actual y  volvemos a LoginActivity
                finish();
            }
        });
    }

    /**
     * Metodo para realizar la peticion HTTP, para el registro de usuario
     */
    public void registrarUsuario() {

        //Activamos la barra de progreso
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.texto_progresDialog_registrando));
        progressDialog.show();

        //Obtenemos los campos introduccidos por el usuario
        final String correo = _etEmail.getText().toString();
        final String contrasena = _etContrasena.getText().toString();
        final String nombre = _etNombre.getText().toString();

        // Mapeo previo
        HashMap<String, String> params = new HashMap<>();
        params.put(Constantes.KEY_EMAIL_USUARIO, correo);
        params.put(Constantes.KEY_CONTRASENA_USUARIO, contrasena);
        params.put(Constantes.KEY_NOMBRE_USUARIO, nombre);

        // Crear nuevo objeto Json basado en el mapa
        JSONObject jobject = new JSONObject(params);


        //Configuramos la petición HTTP
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                Constantes.REGISTRAR_USUARIO, jobject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String estado = response.getString("estado");
                            switch (estado) {
                                case "1":

                                    //Si el registro es correcto, lanzamos la activity de login.
                                    progressDialog.dismiss();
                                    finish();
                                    Intent intent2 = new Intent(RegistrarseActivity.this, MainActivity.class);
                                    startActivity(intent2);

                                    break;
                                case "2":
                                    //Si el registro no es correcto, le informamos al usuario.
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), getString(R.string.texto_error_registrar_usuario), Toast.LENGTH_LONG).show();
                                    break;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
               // Toast.makeText(getApplicationContext(), getString(R.string.texto_error_registrar_usuario), Toast.LENGTH_LONG).show();
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

        //Realizamos la petición HTTP
        jsonObjReq.setShouldCache(false);
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjReq);

    }

    /**
     * Metodo para validar el formato de los campos introduccidos por el usuario.
     */
    public boolean validate() {
        boolean valid = true;

        String name = _etNombre.getText().toString();
        String email = _etEmail.getText().toString();
        String password = _etContrasena.getText().toString();
        String confirmarPassword = _etConfirmarContrasena.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _etNombre.setError(getString(R.string.texto_error_nombre));
            valid = false;
        } else {
            _etNombre.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _etEmail.setError(getString(R.string.texto_error_correo));
            valid = false;
        } else {
            _etEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _etContrasena.setError(getString(R.string.texto_error_contrasena));
            valid = false;
        } else {

            if (password.equals(confirmarPassword)) {
                _etContrasena.setError(null);

            } else {
                _etContrasena.setError(getString(R.string.texto_error_confirmar_contrasena));
                valid = false;

            }
        }

        return valid;
    }
    /**
     * Metodo para comprobar la conexión a internet
     */
    public boolean comprobarConexion(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }


}