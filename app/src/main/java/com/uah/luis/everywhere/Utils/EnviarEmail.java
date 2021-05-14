package com.uah.luis.everywhere.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.uah.luis.everywhere.Configuracion.Constantes;
import com.uah.luis.everywhere.R;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Clase que extiende de AsyncTask para enviar un email en segundo plano.
 */

public class EnviarEmail extends AsyncTask<Void, Void, Void> {

    private Context context;
    private Session session;
    //Informacion a enviar
    private String emailDestino;
    private String mensaje;
    private String asunto;


    public EnviarEmail(Context context, String email, String asunto, String mensaje) {
        this.context = context;
        this.emailDestino = email;
        this.asunto = asunto;
        this.mensaje = mensaje;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Toast.makeText(context, R.string.texto_email_enviado, Toast.LENGTH_LONG).show();
    }

    @Override
    protected Void doInBackground(Void... params) {

        Properties props = new Properties();

        //Configuracion de propiedades de gmail
        //Si no se utiliza gmail hay que cambiar los valores
        props.put("mail.smtp.host", Constantes.EMAIL_HOST);
        props.put("mail.smtp.socketFactory.port", Constantes.EMAIL_HOST);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", Constantes.EMAIL_AUTH);
        props.put("mail.smtp.port", Constantes.EMAIL_PORT);

        //Se crea una nueva session
        session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(Constantes.EMAIL_EMAIL, Constantes.EMIAL_CONTRASENA);
                    }
                });

        try {
            //Se crea un nuevo objeto Mine
            MimeMessage mm = new MimeMessage(session);
            //Asignamos el email del destinatario
            mm.setFrom(new InternetAddress(Constantes.EMAIL_EMAIL));
            //Asignamos el email de destino
            mm.addRecipient(Message.RecipientType.TO, new InternetAddress(emailDestino));
            //Asignamos el asunto
            mm.setSubject(asunto);
            //Asignamos el mensaje del email
            mm.setText(mensaje);

            //enviamos el email
            Transport.send(mm);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }
}