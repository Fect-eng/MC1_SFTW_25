package com.devlabting.tucancha.MVC.CLIENTE.Geocoding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devlabting.tucancha.MVC.MODEL.CanchaModel;
import com.devlabting.tucancha.R;

import java.util.List;

public class CanchasAdapter extends RecyclerView.Adapter<CanchasAdapter.ViewHolder> {

    private final List<String> canchas;

    public CanchasAdapter(List<String> canchas) {
        this.canchas = canchas;
    }

    public CanchasAdapter(List<CanchaModel> listaCanchas, Object centrarCanchaEnMapa, List<String> canchas) {
        this.canchas = canchas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv.setText(canchas.get(position));
    }

    @Override
    public int getItemCount() {
        return canchas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        ViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(android.R.id.text1);
        }
    }
}
