package com.uah.luis.everywhere.Utils;

import com.google.android.gms.location.DetectedActivity;
import com.uah.luis.everywhere.R;

/**
 * constantes y metodos auxiliares para DetectetarActividadService
 */
public class UtilActividadService {


    public static final String BROADCAST_ACTION = "broadcast-action";
    public static final String ACTIVITY_KEY = "activites-key";

    public static final long ACTIVITY_RECOGNITION_INTERVAL = 3000;


    public static String obtenerStringActividad(int type) {
        switch (type) {
            case DetectedActivity.IN_VEHICLE:
                return "Vehículo";
            case DetectedActivity.ON_BICYCLE:
                return "Bicicleta";
            case DetectedActivity.ON_FOOT:
                return "Caminando o corriendo";
            case DetectedActivity.RUNNING:
                return "Corriendo";
            case DetectedActivity.STILL:
                return "Sin movimiento";
            case DetectedActivity.TILTING:
                return "Inclinación brusca";
            case DetectedActivity.UNKNOWN:
                return "Desconocido";
            case DetectedActivity.WALKING:
                return "Caminando";
            default:
                return "Tipo no idenficado";
        }
    }


    public static int obtenerIconoActividad(int type) {
        switch (type) {
            case DetectedActivity.STILL:
                return R.drawable.ic_sin_movimiento;
            case DetectedActivity.WALKING:
                return R.drawable.ic_caminando;
            case DetectedActivity.RUNNING:
                return R.drawable.ic_corriendo;
            default:
                return R.drawable.ic_desconocido;
        }
    }
}

