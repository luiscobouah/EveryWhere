package com.uah.luis.everywhere.Modelo;

/**
 *
 */
public class Usuario {

    private static final String TAG = Usuario.class.getSimpleName();
    /*
        Atributos
         */

    private String ID_USUARIO;
    private String NOMBRE;
    private String EMAIL;
    private String EMAIL_CONTACTO;
    private String NOMBRE_CONTACTO;
    private String NUMERO_MOVIL;


    public Usuario() {
    }

    public Usuario(String usuario_id_usuario, String nombre, String email) {

        this.ID_USUARIO = usuario_id_usuario;
        this.NOMBRE = nombre;
        this.EMAIL = email;


    }

    public String getID_USUARIO() {
        return ID_USUARIO;
    }

    public void setID_USUARIO(String ID_USUARIO) {
        this.ID_USUARIO = ID_USUARIO;
    }

    public String getNOMBRE() {
        return NOMBRE;
    }

    public void setNOMBRE(String NOMBRE) {
        this.NOMBRE = NOMBRE;
    }

    public String getEMAIL() {
        return EMAIL;
    }

    public void setEMAIL(String EMAIL) {
        this.EMAIL = EMAIL;
    }

    public String getEMAIL_CONTACTO() {
        return EMAIL_CONTACTO;
    }

    public void setEMAIL_CONTACTO(String EMAIL_CONTACTO) {
        this.EMAIL_CONTACTO = EMAIL_CONTACTO;
    }

    public String getNOMBRE_CONTACTO() {
        return NOMBRE_CONTACTO;
    }

    public void setNOMBRE_CONTACTO(String NOMBRE_CONTACTO) {
        this.NOMBRE_CONTACTO = NOMBRE_CONTACTO;
    }

    public String getNUMERO_MOVIL() {
        return NUMERO_MOVIL;
    }

    public void setNUMERO_MOVIL(String NUMERO_MOVIL) {
        this.NUMERO_MOVIL = NUMERO_MOVIL;
    }
}
