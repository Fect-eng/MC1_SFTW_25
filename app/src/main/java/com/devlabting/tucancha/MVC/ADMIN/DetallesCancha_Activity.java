package com.devlabting.tucancha.MVC.ADMIN;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView; // ← ❗ necesario para usar TextView
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.devlabting.tucancha.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class DetallesCancha_Activity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    // ====== DRAWER ======
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detallescancha_drawer);     // activity_detalles_cancha


        // ============================================================================================
        // ============================================================================================
        // ============================================================================================

        // === Modal de Formas de Pago ===
        TextView tvFormasPago = findViewById(R.id.tvFormasPago);
        tvFormasPago.setOnClickListener(v -> {
            FormasPagoDialog dialog = new FormasPagoDialog();
            dialog.show(getSupportFragmentManager(), "FormasPagoDialog");
        });

        Spinner spTipoCancha = findViewById(R.id.spTipoCancha);

        // Lista de tipos de cancha
        String[] tipos = {"Grass sintético", "Grass natural (pasto)", "Loza cemento"};

        // Crea el adaptador personalizado
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item_white, // vista para ítem seleccionado
                tipos
        );

        // Define cómo se verá el menú desplegable
        adapter.setDropDownViewResource(R.layout.spinner_item_white);

        // Aplica el adaptador al Spinner
        spTipoCancha.setAdapter(adapter);

        configurarDrawerLayout();

        // ======================
// 🔹 Envío de datos a Firebase
// ======================

// Recuperar el adminUid y canchaId del intent
        String adminUid = getIntent().getStringExtra("adminUid");
        String canchaId = getIntent().getStringExtra("canchaId");

// Referencias de los controles (radio groups y spinner)
        RadioGroup rgCafetin = findViewById(R.id.rgCafetin);
        RadioGroup rgBar = findViewById(R.id.rgBar);
        RadioGroup rgCochera = findViewById(R.id.rgCochera);
        RadioGroup rgSSHH = findViewById(R.id.rgSSHH);
        RadioGroup rgDuchas = findViewById(R.id.rgDuchas);
        RadioGroup rgAreaRec = findViewById(R.id.rgAreaRec);
       // Spinner spTipoCancha = findViewById(R.id.spTipoCancha);
        MaterialButton btnRegistrarCancha = findViewById(R.id.btnRegistrarCancha);

// Evento para enviar datos
        btnRegistrarCancha.setOnClickListener(v -> {
            if (adminUid == null || canchaId == null) {
                Toast.makeText(this, "⚠️ Error interno: faltan IDs", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔹 Validar selección de todos los radio groups
            if (rgCafetin.getCheckedRadioButtonId() == -1 ||
                    rgBar.getCheckedRadioButtonId() == -1 ||
                    rgCochera.getCheckedRadioButtonId() == -1 ||
                    rgSSHH.getCheckedRadioButtonId() == -1 ||
                    rgDuchas.getCheckedRadioButtonId() == -1 ||
                    rgAreaRec.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Selecciona todas las opciones antes de continuar.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔹 Obtener valores seleccionados
            String cafetin = ((RadioButton) findViewById(rgCafetin.getCheckedRadioButtonId())).getText().toString();
            String bar = ((RadioButton) findViewById(rgBar.getCheckedRadioButtonId())).getText().toString();
            String cochera = ((RadioButton) findViewById(rgCochera.getCheckedRadioButtonId())).getText().toString();
            String sshh = ((RadioButton) findViewById(rgSSHH.getCheckedRadioButtonId())).getText().toString();
            String duchas = ((RadioButton) findViewById(rgDuchas.getCheckedRadioButtonId())).getText().toString();
            String areaRec = ((RadioButton) findViewById(rgAreaRec.getCheckedRadioButtonId())).getText().toString();
            String tipoCancha = spTipoCancha.getSelectedItem().toString();

            // 🔧 Crear mapa con los datos
            Map<String, Object> detalles = new HashMap<>();
            detalles.put("cafetin", cafetin);
            detalles.put("bar", bar);
            detalles.put("cochera", cochera);
            detalles.put("sshh", sshh);
            detalles.put("duchas", duchas);
            detalles.put("areaRecreativa", areaRec);
            detalles.put("tipoCancha", tipoCancha);
            detalles.put("fechaRegistro", System.currentTimeMillis());

            // 🔗 Enviar a Firebase bajo el mismo registro
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("admins")
                    .child(adminUid)
                    .child("canchas")
                    .child(canchaId)
                    .child("detalles");

            ref.setValue(detalles)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "✅ Detalles guardados correctamente", Toast.LENGTH_SHORT).show();

                        // 🔹 Loader de espera opcional
                        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(DetallesCancha_Activity.this);
                        progressDialog.setMessage("Redirigiendo al inicio...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        // 🔹 Espera un momento y redirige a HomeAdm_Activity
                        new android.os.Handler().postDelayed(() -> {
                            progressDialog.dismiss();
                            Intent intent = new Intent(DetallesCancha_Activity.this, HomeAdm_Activity.class);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }, 1200); // Espera 1.2 segundos
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "❌ Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );

        });

    }

    // =====================================================
    // 🔹 Configurar DrawerLayout y Toolbar
    // =====================================================
    private void configurarDrawerLayout() {
        drawerLayout = findViewById(R.id.drawerLayoutCancha);
        navigationView = findViewById(R.id.navigationViewCancha);
        toolbar = findViewById(R.id.toolbarr);

        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#ff669900"));
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle("Registrar Cancha");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toggle.setDrawerIndicatorEnabled(true);

        toggle.setToolbarNavigationClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
    }

    // =====================================================
    // 🔹 Menú Drawer
    // =====================================================
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    // ============================================================================================
    // ============================================================================================
    // ============================================================================================

}


