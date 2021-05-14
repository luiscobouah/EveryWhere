package com.uah.luis.everywhere.Activitys;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.uah.luis.everywhere.Adaptadores.AdaptadorSpinnerSeguimiento;
import com.uah.luis.everywhere.Configuracion.Constantes;
import com.uah.luis.everywhere.Modelo.UsuarioSeguimiento;
import com.uah.luis.everywhere.Utils.VolleySingleton;
import com.uah.luis.everywhere.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SeguimientoUsuariosActivity extends AppCompatActivity {

    private String emailUsuario;
    private MapView mMapView;
    private GoogleMap googleMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguimiento_usuarios);

        //Obtenemos las SharedPreferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //Obtenemos el email de usuario registrdo de las SharedPreferences
        emailUsuario = sharedPref.getString(Constantes.SP_EMAIL_USUARIO, "");


        //Inicializamos el MapView
        mMapView = (MapView) findViewById(R.id.mvUbicacionUsuario);

        try {
            mMapView.onCreate(savedInstanceState);

        } catch (Exception e) {
            onBackPressed();

        }
        mMapView.setClickable(false);
        try {
            mMapView.onResume();
        } catch (Exception e) {
            onBackPressed();

        }
        try {
            MapsInitializer.initialize(this.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            obtenerListaUsuariosSeguimiento(emailUsuario);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


    }

    public void obtenerListaUsuariosSeguimiento(String email) throws MalformedURLException {

        // Añadir parámetro a la URL del web service
        HashMap<String, String> params = new HashMap<>();
        // Mapeo previo
        params.put(Constantes.KEY_EMAIL_USUARIO, email);
        // Crear nuevo objeto Json basado en el mapa
        JSONObject jobject = new JSONObject(params);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                Constantes.OBTENER_USUARIOS_SEGUIMIENTO, jobject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Procesamos la respuesta del servidor.
                        procesarRespuesta(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(), getString(R.string.texto_error_obtener_usuarios), Toast.LENGTH_LONG).show();

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


    private void procesarRespuesta(JSONObject response) {

        try {
            // Obtener atributo "estado"
            String estado = response.getString("estado");


            switch (estado) {
                case "1":
                    //inicializamos la lista donde almacenaremos los objetos usuarios
                    final ArrayList<UsuarioSeguimiento> lista_usuarios = new ArrayList<UsuarioSeguimiento>();
                    //List<String> lista = new ArrayList<>();
                    final JSONArray json_array = response.getJSONArray("usuarios"); //cogemos cada uno de los elementos dentro de la etiqueta "usuarios"
                    for (int i = 0; i < json_array.length(); i++) {

                        //Insertamos los usuarios obtenidos en formato JSON, en un ArrayList<UsuarioSeguimiento>
                        UsuarioSeguimiento u1 = new UsuarioSeguimiento();
                        u1.setNOMBRE(String.valueOf(json_array.getJSONObject(i).get("NOMBRE")));
                        u1.setDIRECCION(String.valueOf(json_array.getJSONObject(i).get("DIRECCION")));
                        u1.setCIUDAD(String.valueOf(json_array.getJSONObject(i).get("CIUDAD")));
                        u1.setLATITUD(String.valueOf(json_array.getJSONObject(i).get("LATITUD")));
                        u1.setLONGITUD(String.valueOf(json_array.getJSONObject(i).get("LONGITUD")));
                        u1.setHORA_ACTULIZACION_UBICACION(String.valueOf(json_array.getJSONObject(i).get("HORA_ACTUALIZACION")));
                        lista_usuarios.add(u1);
                    }

                    Spinner spinner2 = (Spinner) findViewById(R.id.spinner);
                    spinner2.setAdapter(new AdaptadorSpinnerSeguimiento(this, lista_usuarios));


                    spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, final int pos, long id) {

                            //Insertamos un marcador en el mapa, con la ubicacion de el usuario seleccionado del spinner
                            mMapView.getMapAsync(new OnMapReadyCallback() {


                                @Override
                                public void onMapReady(GoogleMap mMap) {
                                    googleMap = mMap;

                                    //Obtenemos la ubicacion del usuario seleccionado
                                    Double latitud = Double.valueOf(lista_usuarios.get(pos).getLATITUD());
                                    Double longitud = Double.valueOf(lista_usuarios.get(pos).getLONGITUD());
                                    String direccion = lista_usuarios.get(pos).getDIRECCION();
                                    String ciudad = lista_usuarios.get(pos).getCIUDAD();

                                    googleMap.clear();
                                    final LatLng ubicacion = new LatLng(latitud, longitud);
                                    Marker mUbicacion = googleMap.addMarker(new MarkerOptions()
                                            .position(ubicacion).title(direccion + ". " + ciudad)
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                                            ));
                                    mUbicacion.showInfoWindow();


                                    CameraPosition cameraPosition = new CameraPosition.Builder().target(ubicacion).zoom(16).build();
                                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                }

                            });

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                    break;


                case "2":
                    Toast.makeText(getApplicationContext(), getString(R.string.texto_error_no_tiene_usuarios), Toast.LENGTH_LONG).show();
                    break;

                case "3":
                    Toast.makeText(getApplicationContext(), getString(R.string.texto_error_obtener_usuarios), Toast.LENGTH_LONG).show();
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_seguimiento_usuarios, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_actualizar:
                //Actualizamos el listado de usuario en seguimiento.
                try {
                    obtenerListaUsuariosSeguimiento(emailUsuario);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return true;


        }
        return super.onOptionsItemSelected(item);
    }
}
