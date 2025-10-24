package com.devlabting.tucancha.MVC.ADMIN;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.devlabting.tucancha.MVC.RegistroCancha_Activity;
import com.devlabting.tucancha.R;

public class HomeAdm_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_adm);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 Detectar click en el card “Reservación”
        View cardReserva = findViewById(R.id.cardReserva);
        cardReserva.setOnClickListener(v -> {
            Intent intent = new Intent(HomeAdm_Activity.this, RegistroCancha_Activity.class);
            startActivity(intent);
        });

        // 🟩 Configurar el Toolbar desde el layout XML
        Toolbar toolbar = findViewById(R.id.toolbarr);
        setSupportActionBar(toolbar);

// Quitar el título por defecto del ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

// Fondo del Toolbar
        toolbar.setBackgroundColor(Color.parseColor("#ff669900")); // color general de app

// 🔹 Crear un TextView centrado para el título
        TextView tituloCentrado = new TextView(this);
        tituloCentrado.setText("ADMINISTRADOR");      // Titulo de Toolbar Personalizado
        tituloCentrado.setTextColor(Color.WHITE);
        tituloCentrado.setTextSize(20);
        tituloCentrado.setTypeface(Typeface.DEFAULT_BOLD);
        tituloCentrado.setGravity(Gravity.CENTER);

// 🔹 Configurar layout para centrar el texto
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.MATCH_PARENT,
                Toolbar.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.CENTER;
        tituloCentrado.setLayoutParams(layoutParams);

// 🔹 Agregar el TextView al Toolbar
        toolbar.addView(tituloCentrado);


    }
}
