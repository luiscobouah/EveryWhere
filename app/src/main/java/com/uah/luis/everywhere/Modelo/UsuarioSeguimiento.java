package com.uah.luis.everywhere.Modelo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Reflejo de la tabla 'meta' en la base de datos
 */
public class UsuarioSeguimiento {

    private static final String TAG = UsuarioSeguimiento.class.getSimpleName();
    /*
        Atributos
         */

    private String USUARIO_ID_USUARIO;
    private String NOMBRE;
    private String DIRECCION;
    private String CIUDAD;
    private String LATITUD;
    private String LONGITUD;
    private String HORA_ACTULIZACION_UBICACION;


    public UsuarioSeguimiento() {

    }

    public UsuarioSeguimiento(String usuario_id_usuario, String nombre, String ciudad, String direccion,
                              String latitud, String longitud, String horaActulizacionUbicacion) {


        this.USUARIO_ID_USUARIO = usuario_id_usuario;
        this.NOMBRE = nombre;
        this.DIRECCION = direccion;
        this.CIUDAD = ciudad;
        this.LATITUD = latitud;
        this.LONGITUD = longitud;
        this.HORA_ACTULIZACION_UBICACION = horaActulizacionUbicacion;

    }


    public UsuarioSeguimiento(JSONObject objetoJSON) throws JSONException {

        USUARIO_ID_USUARIO = objetoJSON.getString("ID_USUARIO");
        LONGITUD = objetoJSON.getString("LONGITUD");
        LATITUD = objetoJSON.getString("LATITUD");
        DIRECCION = objetoJSON.getString("DIRECCION");
        CIUDAD = objetoJSON.getString("CIUDAD");
        HORA_ACTULIZACION_UBICACION = objetoJSON.getString("HORA_ACTULIZACION");


    }


    public String getUSUARIO_ID_USUARIO() {
        return USUARIO_ID_USUARIO;
    }

    public void setUSUARIO_ID_USUARIO(String USUARIO_ID_USUARIO) {
        this.USUARIO_ID_USUARIO = USUARIO_ID_USUARIO;
    }

    public String getNOMBRE() {
        return NOMBRE;
    }

    public void setNOMBRE(String NOMBRE) {
        this.NOMBRE = NOMBRE;
    }

    public String getDIRECCION() {
        return DIRECCION;
    }

    public void setDIRECCION(String DIRECCION) {
        this.DIRECCION = DIRECCION;
    }

    public String getCIUDAD() {
        return CIUDAD;
    }

    public void setCIUDAD(String CIUDAD) {
        this.CIUDAD = CIUDAD;
    }

    public String getLATITUD() {
        return LATITUD;
    }

    public void setLATITUD(String LATITUD) {
        this.LATITUD = LATITUD;
    }

    public String getLONGITUD() {
        return LONGITUD;
    }

    public void setLONGITUD(String LONGITUD) {
        this.LONGITUD = LONGITUD;
    }

    public String getHORA_ACTULIZACION_UBICACION() {
        return HORA_ACTULIZACION_UBICACION;
    }

    public void setHORA_ACTULIZACION_UBICACION(String HORA_ACTULIZACION_UBICACION) {
        this.HORA_ACTULIZACION_UBICACION = HORA_ACTULIZACION_UBICACION;
    }



}
