package com.devlabting.tucancha.MVC.ADMIN.reserva_canchas;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.devlabting.tucancha.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class reserva_canchaADM_Activity extends AppCompatActivity {

    private Spinner spMes, spCancha;
    private GridLayout gridHorarios;
    private LinearLayout contenedorDias;
    private Switch switchEstado;
    private Button btnGuardar;

    // Horarios desde las 08:00 hasta las 24:00
    private final String[] HORAS = {
            "08:00", "09:00", "10:00", "11:00", "12:00", "13:00",
            "14:00", "15:00", "16:00", "17:00", "18:00", "19:00",
            "20:00", "21:00", "22:00", "23:00", "24:00"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reserva_cancha_adm);

        // 🔹 Ajuste de márgenes con EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 Referencias del layout
        spMes = findViewById(R.id.spMes);
        spCancha = findViewById(R.id.spCancha);
        gridHorarios = findViewById(R.id.gridHorarios);
        contenedorDias = findViewById(R.id.contenedorDias);
        switchEstado = findViewById(R.id.switchEstado);
        btnGuardar = findViewById(R.id.btnGuardar);

        // 🔹 Inicialización de componentes
        configurarSpinnerMeses();
        configurarSpinnerCanchas();
        generarDias();
        generarHorarios();

        // 🔹 Evento Guardar
        btnGuardar.setOnClickListener(v ->
                Toast.makeText(this, "✅ Cambios guardados correctamente", Toast.LENGTH_SHORT).show()
        );

        // 🔹 Evento Switch (Abierto / Cerrado)
        switchEstado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "🟢 Cancha abierta", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "🔴 Cancha cerrada", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==========================
    // 🔸 SPINNER: Meses
    // ==========================
    private void configurarSpinnerMeses() {
        List<String> meses = Arrays.asList("Octubre", "Noviembre", "Diciembre");
        ArrayAdapter<String> adapterMeses = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, meses);
        spMes.setAdapter(adapterMeses);
    }

    // ==========================
    // 🔸 SPINNER: Canchas
    // ==========================
    private void configurarSpinnerCanchas() {
        List<String> canchas = new ArrayList<>();
        canchas.add("Cancha 1");
        canchas.add("Cancha 2");
        canchas.add("Cancha 3");

        ArrayAdapter<String> adapterCancha = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, canchas);
        spCancha.setAdapter(adapterCancha);
    }

    // ==========================
    // 🔸 GENERAR DÍAS
    // ==========================
    private void generarDias() {
        Calendar calendar = Calendar.getInstance();
        contenedorDias.removeAllViews();

        for (int i = 0; i < 12; i++) {
            Button dia = new Button(this);
            calendar.add(Calendar.DAY_OF_MONTH, (i == 0 ? 0 : 1));

            int diaNumero = calendar.get(Calendar.DAY_OF_MONTH);
            int mesNumero = calendar.get(Calendar.MONTH) + 1;

            dia.setText(String.format("%02d/%02d", diaNumero, mesNumero));
            dia.setAllCaps(false);
            dia.setBackgroundColor(0xFFE0E0E0);
            dia.setTextColor(0xFF000000);
            dia.setPadding(24, 16, 24, 16);

            dia.setOnClickListener(v ->
                    Toast.makeText(this, "📅 Día seleccionado: " + dia.getText(), Toast.LENGTH_SHORT).show()
            );

            contenedorDias.addView(dia);
        }
    }

    // ==========================
    // 🔸 GENERAR BLOQUES HORARIOS
    // ==========================
    private void generarHorarios() {
        gridHorarios.removeAllViews();

        for (String hora : HORAS) {
            TextView bloque = new TextView(this);
            bloque.setText(hora);
            bloque.setGravity(Gravity.CENTER);
            bloque.setPadding(8, 12, 8, 12);
            bloque.setTextColor(0xFFFFFFFF);
            bloque.setBackgroundColor(0xFF4CAF50); // Verde = Libre
            bloque.setTextSize(15);

            // 🔄 Cambiar color al hacer clic
            bloque.setOnClickListener(v -> cambiarEstado(bloque));

            gridHorarios.addView(bloque);
        }
    }

    // ==========================
    // 🔸 CAMBIAR ESTADO DE COLOR (por toque)
    // ==========================
    private void cambiarEstado(TextView bloque) {
        int color = ((ColorDrawable) bloque.getBackground()).getColor();

        if (color == 0xFF4CAF50) { // Verde → Naranja
            bloque.setBackgroundColor(0xFFFF9800);
            bloque.setText("En Proceso");
        } else if (color == 0xFFFF9800) { // Naranja → Rojo
            bloque.setBackgroundColor(0xFFF44336);
            bloque.setText("Reservado");
        } else if (color == 0xFFF44336) { // Rojo → Gris
            bloque.setBackgroundColor(0xFF9E9E9E);
            bloque.setText("Cerrado");
        } else { // Gris → Verde
            bloque.setBackgroundColor(0xFF4CAF50);
            bloque.setText("Libre");
        }
    }
}
