package com.lokacodes.kebunpintar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class kebunListAdapter extends ArrayAdapter<Kebun> {

    public kebunListAdapter(Context context, ArrayList<Kebun> kebunArrayList){

        super(context,R.layout.list_kebun,kebunArrayList);

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Kebun kebun = getItem(position);

        if (convertView == null){

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_kebun,parent,false);

        }

        TextView namaKebun = convertView.findViewById(R.id.nama_kebun);
        TextView lokasiKebun = convertView.findViewById(R.id.lokasi_kebun);

        namaKebun.setText(kebun.namaKebun);
        lokasiKebun.setText(kebun.lokasiKebun);

        return convertView;
    }
}
