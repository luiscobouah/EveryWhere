package com.uah.luis.everywhere.Adaptadores;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.uah.luis.everywhere.Modelo.UsuarioSeguimiento;
import com.uah.luis.everywhere.R;

import java.util.ArrayList;

/**
 * Adaptador para rellenar el spinner de seguimiento de usuarios.
 */

public class AdaptadorSpinnerSeguimiento extends BaseAdapter {

    protected Activity activity;
    protected ArrayList<UsuarioSeguimiento> items;

    public AdaptadorSpinnerSeguimiento(Activity activity, ArrayList<UsuarioSeguimiento> items) {
        this.activity = activity;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }


    @Override
    public Object getItem(int arg0) {
        return items.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (convertView == null) {
            LayoutInflater inf = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inf.inflate(R.layout.item_lista_seguimiento, null);
        }

        UsuarioSeguimiento dir = items.get(position);


        //Se obtiene inicializan los componentes
        TextView tvNombre = (TextView) v.findViewById(R.id.tvNombre);
        TextView tvHoraActualizacion = (TextView) v.findViewById(R.id.tvHoraActualizacion);
        TextView tvDireccion = (TextView) v.findViewById(R.id.tvDireccion);
        TextView tvCiudad = (TextView) v.findViewById(R.id.tvCiudad);


        tvNombre.setText(dir.getNOMBRE());
        tvDireccion.setText(dir.getDIRECCION());
        tvCiudad.setText(dir.getCIUDAD());
        tvHoraActualizacion.setText(dir.getHORA_ACTULIZACION_UBICACION());


        return v;
    }
}

