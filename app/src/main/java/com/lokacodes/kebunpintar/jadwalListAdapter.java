package com.lokacodes.kebunpintar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class jadwalListAdapter extends ArrayAdapter<Jadwal>{
    public jadwalListAdapter(Context context, ArrayList<Jadwal> jadwalArrayList){

        super(context,R.layout.list_jadwal,jadwalArrayList);

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Jadwal jadwal = getItem(position);

        if (convertView == null){

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_jadwal,parent,false);

        }

        TextView timeStart = convertView.findViewById(R.id.timeStart);
        TextView timeEnd = convertView.findViewById(R.id.timeEnd);

        timeStart.setText(jadwal.jam_on);
        timeEnd.setText(jadwal.jam_off);

        return convertView;
    }
}
