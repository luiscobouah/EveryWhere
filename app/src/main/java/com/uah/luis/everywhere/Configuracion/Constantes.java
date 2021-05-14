package com.uah.luis.everywhere.Configuracion;


/**
 * Fichero con las constantes de conexión al servicio web y constates de valores de SharedPreferences
 */
public class Constantes {


    /**
     * Rutas de los ficheros del servicio web.
     */
    private static final String PUERTO_HOST = "";
    private static final String IP = "http://s616107927.mialojamiento.es/TFG";

    public static final String REGISTRAR_USUARIO = IP + PUERTO_HOST + "/registrar_usuario.php";
    public static final String COMPROBAR_LOGIN = IP + PUERTO_HOST + "/comprobar_login.php";
    public static final String OBTENER_USUARIOS_SEGUIMIENTO = IP + PUERTO_HOST + "/obtener_usuarios_seguimiento.php";
    public static final String OBTENER_DATOS_USUARIO = IP + PUERTO_HOST + "/obtener_datos_usuario.php";
    public static final String ACTUALIZAR_UBICACION = IP + PUERTO_HOST + "/actualizar_ubicacion.php";
    public static final String REGISTRAR_DATOS_CONTACTO = IP + PUERTO_HOST + "/actualizar_datos_contacto.php";


    /**
     * Keys de los parametros enviados en las peticiones al servicio web, se deben corresponder
     * con los parametros recibidos en los archivos PHP del servicio web
     */

    public static final String KEY_NOMBRE_USUARIO= "nombre";
    public static final String KEY_ID_USUARIO= "idUsuario";
    public static final String KEY_EMAIL_USUARIO = "email";
    public static final String KEY_CONTRASENA_USUARIO = "contrasena";

    public static final String KEY_LATITUD= "latitud";
    public static final String KEY_LONGITUD= "longitud";
    public static final String KEY_DIRECCION= "direccion";
    public static final String KEY_CIUDAD= "ciudad";

    public static final String KEY_EMAIL_CONTACTO= "email";
    public static final String KEY_NOMBRE_CONTACTO= "nombreContacto";
    public static final String KEY_NUMERO_CONTACTO= "numeroMovil";

    /**
     * Keys de los parametros enviados mediante el Intent al MainActivity desde DetectarLocalizacionService
     */

    public static final String KEY_INTENT_LATITUD= "latitud";
    public static final String KEY_INTENT_LONGITUD= "longitud";
    public static final String KEY_INTENT_DIRECCION= "direccion";
    public static final String KEY_INTENT_CIUDAD= "ciudad";


    /**
     * Configuracion para el envio de email.
     *
     */
    public static final String EMAIL_HOST = "smtp.gmail.com";
    public static final String EMAIL_PORT = "465";
    public static final String EMAIL_AUTH = "true";

    public static final String EMAIL_EMAIL = "everywhereapplication@gmail.com";
    public static final String EMIAL_CONTRASENA = "xzv%1234";


    /**
     * Keys para SharedPreferences, datos de usuario================================================
     */
    public static final String SP_ID_USUARIO= "idUsuario";
    public static final String SP_EMAIL_USUARIO= "emailUsuario";

    /**
     * Keys para SharedPreferences, se deben corresponder con las key de los XML de prefrerencias.
     */
    //Key Prefrencias Contacto

    public static final String SP_NOMBRE_CONTACTO= "etNombreContacto";
    public static final String SP_EMAIL_CONTACTO = "etEmailContacto";
    public static final String SP_NUMERO_CONTACTO = "etNumeroContacto";


    //Key Prefrencias Generales

    public static final String SP_NOMBRE_USUARIO= "etNombreUsuario";
    public static final String SP_LISTA_TIEMPO_ALERTA = "listaTiempoAlerta";
    public static final String SP_SWITCH_SMS = "switchSMS";
    public static final String SP_SWITCH_EMAIL = "switchEmail";
    public static final String SP_SWITCH_UBICACION_VIVO = "switchUbicacionVivo";
    public static final String SP_SWITCH_SONIDO = "switchSonido";

   //===============================================================================================
}
