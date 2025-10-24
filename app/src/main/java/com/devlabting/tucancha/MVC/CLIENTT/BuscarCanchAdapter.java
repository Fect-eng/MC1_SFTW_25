package com.devlabting.tucancha.MVC.CLIENTT;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devlabting.tucancha.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador para mostrar la lista de canchas desde Firebase.
 * Se usa dentro de Buscar_Canchas_Activity.
 */
public class BuscarCanchAdapter extends RecyclerView.Adapter<BuscarCanchAdapter.ViewHolder> {

    // Listener para manejar clics en los ítems del RecyclerView
    public interface OnCanchaClickListener {
        void onCanchaClick(Buscar_Canchas_Activity.Cancha cancha);
    }

    private final List<Buscar_Canchas_Activity.Cancha> listaCanchas;
    private final List<Buscar_Canchas_Activity.Cancha> listaFiltrada;
    private final OnCanchaClickListener listener;

    // ✅ Constructor corregido
    public BuscarCanchAdapter(List<Buscar_Canchas_Activity.Cancha> lista, OnCanchaClickListener listener) {
        this.listaCanchas = lista;
        this.listaFiltrada = new ArrayList<>(lista); // se inicializa correctamente
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cancha, parent, false); // usa item_cancha.xml
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Buscar_Canchas_Activity.Cancha cancha = listaFiltrada.get(position);

        // ✅ Asignar datos al layout del ítem
        holder.tvNombre.setText(cancha.nombre != null ? cancha.nombre : "Sin nombre");
        holder.tvDireccion.setText(cancha.direccion != null ? cancha.direccion : "Sin dirección");

        // En tu modelo actual no tienes campo "celular", así que usamos precio como ejemplo
        holder.tvCelular.setText(cancha.precio != null ? "Precio: S/ " + cancha.precio : "Precio: N/A");

        holder.tvHorario.setText(cancha.horario != null ? "Horario: " + cancha.horario : "Horario: N/A");

        // Click del ítem → devuelve la cancha seleccionada
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCanchaClick(cancha);
        });
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }

    /**
     * Filtro dinámico que actualiza los resultados según el texto buscado.
     */
    public void filtrar(String texto) {
        listaFiltrada.clear();

        if (texto == null || texto.trim().isEmpty()) {
            listaFiltrada.addAll(listaCanchas);
        } else {
            String filtro = texto.toLowerCase().trim();
            for (Buscar_Canchas_Activity.Cancha c : listaCanchas) {
                if ((c.nombre != null && c.nombre.toLowerCase().contains(filtro)) ||
                        (c.direccion != null && c.direccion.toLowerCase().contains(filtro))) {
                    listaFiltrada.add(c);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Clase interna que representa cada ítem (vista) del RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDireccion, tvCelular, tvHorario;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreCanchaItem);
            tvDireccion = itemView.findViewById(R.id.tvDireccionCanchaItem);
            tvCelular = itemView.findViewById(R.id.tvCelularCanchaItem);
            tvHorario = itemView.findViewById(R.id.tvHorarioCanchaItem);
        }
    }
}
