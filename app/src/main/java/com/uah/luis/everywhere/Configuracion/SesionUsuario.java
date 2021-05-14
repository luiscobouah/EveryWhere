package com.uah.luis.everywhere.Configuracion;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Lincoln on 05/05/16.
 */
public class SesionUsuario

{
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    private static final String ESTA_LOGEADO = "logeado";


    public SesionUsuario(Context context) {
        this._context = context;
        pref = PreferenceManager.getDefaultSharedPreferences(_context);
        editor = pref.edit();
    }

    // Devuelve el si esta logeado o no
    public boolean estaLogeado(){
        return pref.getBoolean(ESTA_LOGEADO, false);
    }

    //Actualizar el valor de logeado (true/false)
    public void actualizarLogeado(boolean logeado){
        // Storing name in pref
        editor.putBoolean(ESTA_LOGEADO, logeado);
        editor.commit();
    }

    public void cerrarSesion(){
        //Eliminamos todos los datos del Shared Preferences
        editor.clear();
        editor.commit();
    }



}
