package com.devlabting.tucancha;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.devlabting.tucancha.MVC.Admi_Activity;
import com.devlabting.tucancha.MVC.CLIENTE.Geocoding.Query_CanchasCli_Activity;
import com.devlabting.tucancha.MVC.Client_Activity;
import com.devlabting.tucancha.MVC.RegistroCancha_Activity;

public class Selection_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        com.devlabting.tucancha.util.PortraitLock.apply(this); // ⬅️ ANTES de setContentView
        setContentView(R.layout.activity_selection);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Fin de barra Toolbar

        // ===========================================================================================================================
        // ===========================================================================================================================
        // ===========================================================================================================================

        // Buscar el ImageView por su ID
        ImageView imgAdmin = findViewById(R.id.imgAdmin);           // Administrador Sesion
        ImageView imgCliente = findViewById(R.id.imgCliente);       // Cliente Sesion

        // Listener de clic Administrador
        imgAdmin.setOnClickListener(v -> {
            // Mostrar Toast
            Toast.makeText(Selection_Activity.this, "Entrando como Administrador…", Toast.LENGTH_SHORT).show();

            // Abrir otra Activity
            Intent intent = new Intent(Selection_Activity.this, Admi_Activity.class);
            startActivity(intent);
        });

        // Listener de clic Cliente Normal y corriente

        imgCliente.setOnClickListener(v -> {
            // Mostrar Toast
            Toast.makeText(Selection_Activity.this, "Entrando como Cliente…", Toast.LENGTH_SHORT).show();

            // Abrir otra Activity Cliente
            Intent intent = new Intent(Selection_Activity.this, Client_Activity.class);    // Client_Activity RegistroCancha_Activity
            startActivity(intent);
        });

        //todo revisado 19/09/2025

    }
}
