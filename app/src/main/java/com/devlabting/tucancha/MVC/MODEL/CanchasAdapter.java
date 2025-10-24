package com.devlabting.tucancha.MVC.MODEL;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devlabting.tucancha.R;

import java.util.List;

public class CanchasAdapter extends RecyclerView.Adapter<CanchasAdapter.ViewHolder> {

    private final List<CanchaModel> listaCanchas;
    private final OnItemClick listener;

    public CanchasAdapter(List<CanchaModel> listaCanchas, OnItemClick listener) {
        this.listaCanchas = listaCanchas;
        this.listener = listener;
    }

    @FunctionalInterface
    public interface OnItemClick {
        void onClick(CanchaModel cancha);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cancha, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CanchaModel cancha = listaCanchas.get(position);
        holder.txtNombre.setText(cancha.getNombre());
        holder.itemView.setOnClickListener(v -> listener.onClick(cancha));
    }

    @Override
    public int getItemCount() {
        return listaCanchas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.etBuscar);
        }
    }
}
