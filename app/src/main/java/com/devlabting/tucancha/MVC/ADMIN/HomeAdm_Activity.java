package com.devlabting.tucancha.MVC.ADMIN;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;


import com.devlabting.tucancha.MVC.RegistroCancha_Activity;
import com.devlabting.tucancha.R;
import com.google.android.material.navigation.NavigationView;

public class HomeAdm_Activity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_drawer_home);   // activity_home_adm

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

// 🔹 Crear un TextView alineado a la izquierda (junto al icono)
        TextView tituloIzquierda = new TextView(this);
        tituloIzquierda.setText("Administrador");
        tituloIzquierda.setTextColor(Color.WHITE);
        tituloIzquierda.setTextSize(20);
        tituloIzquierda.setTypeface(Typeface.DEFAULT_BOLD);
        tituloIzquierda.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

// 🔹 Configurar layout para colocarlo a la izquierda
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.MATCH_PARENT
        );
        layoutParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        tituloIzquierda.setLayoutParams(layoutParams);

// 🔹 Agregar el TextView al Toolbar
        toolbar.addView(tituloIzquierda);

        // ============================================
// 🔹 Configuración del DrawerLayout
// ============================================
        drawerLayout = findViewById(R.id.drawerLayoutAdm);
        navigationView = findViewById(R.id.navigationViewAdm);
        toolbar = findViewById(R.id.toolbarr);

// Toggle (icono de menú tipo hamburguesa)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

// Vincular toggle al Drawer
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

// Escuchar clics en el menú lateral
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            Toast.makeText(this, "Inicio", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_profile) {
            Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            Toast.makeText(this, "Configuración", Toast.LENGTH_SHORT).show();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
