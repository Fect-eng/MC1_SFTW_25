package com.devlabting.tucancha.Pruebas.DrawerrLayoutt;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.devlabting.tucancha.R;
import com.google.android.material.navigation.NavigationView;

public class DrawerLayout1_Activity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_drawer_layout1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 🔹 Cambiar título del Toolbar dinámicamente
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Actividad 1-2");
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        // 🎚 Toggle para abrir/cerrar el Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // ✅ Aumentar el tamaño del ícono hamburguesa
        toggle.getDrawerArrowDrawable().setBarLength(40f);     // largo de las líneas
        toggle.getDrawerArrowDrawable().setBarThickness(6f);   // grosor de las líneas
        toggle.getDrawerArrowDrawable().setGapSize(8f);        // espacio entre líneas
       // toggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.black));
        // ✅ Asegura que se vea el ícono hamburguesa
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.black));

        // 🎯 Acciones del menú
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Toast.makeText(this, "Has seleccionado Inicio", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Has seleccionado Perfil", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_settings) {
                Toast.makeText(this, "Has seleccionado Configuración", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
