package com.devlabting.tucancha.MVC;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import com.devlabting.tucancha.MVC.ADMIN.DetallesCancha_Activity;
import com.devlabting.tucancha.MVC.ADMIN.HomeAdm_Activity;
import com.devlabting.tucancha.MVC.ADMIN.mapa_dialog_Activity;
import com.devlabting.tucancha.MVC.MODEL.Cccancha;
import com.devlabting.tucancha.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

public class RegistroCancha_Activity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // ====== CAMPOS UI ======
    private EditText etNombre, etUbicacion, etCelular, etNumDoc, etHoraDesde, etHoraHasta, etTarifaDia, etTarifaNoche;
    private AutoCompleteTextView spTipoDoc;
    private CheckBox cbAcepto;
    private Button btnRegistrarse;
    private TextView tvTerminos;

    // ====== VARIABLES ======
    private double canchaLat = 0.0;
    private double canchaLng = 0.0;

    // ====== MAPA ======
    private ActivityResultLauncher<Intent> mapaLauncher;

    // ====== DRAWER ======
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_registro_cancha);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inicializarComponentes();
        configurarDrawerLayout();
        configurarMapaLauncher();
        configurarEventos();
    }

    // =====================================================
    // 🔹 Inicializar componentes
    // =====================================================
    private void inicializarComponentes() {
        etNombre = findViewById(R.id.etNombre);
        etUbicacion = findViewById(R.id.etUbicacion);
        etCelular = findViewById(R.id.etCelular);
       // etHoraDesde = findViewById(R.id.etHoraDesde);
        // etHoraHasta = findViewById(R.id.etHoraHasta);
        etTarifaDia = findViewById(R.id.etTarifaDia);
        etTarifaNoche = findViewById(R.id.etTarifaNoche);
        spTipoDoc = findViewById(R.id.spTipoDoc);
        etNumDoc = findViewById(R.id.etNumDoc);
        cbAcepto = findViewById(R.id.cbAcepto);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        tvTerminos = findViewById(R.id.tvTerminos);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.tipos_documento,
                android.R.layout.simple_dropdown_item_1line
        );
        spTipoDoc.setAdapter(adapter);

        btnRegistrarse.setEnabled(false);
        btnRegistrarse.setAlpha(0.5f);
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
    // 🔹 Configurar mapa
    // =====================================================
    private void configurarMapaLauncher() {
        mapaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String direccion = result.getData().getStringExtra("direccion");
                        double lat = result.getData().getDoubleExtra("lat", 0.0);
                        double lng = result.getData().getDoubleExtra("lng", 0.0);

                        etUbicacion.setText(direccion);
                        canchaLat = lat;
                        canchaLng = lng;
                    }
                }
        );
    }

    // =====================================================
    // 🔹 Configurar eventos
    // =====================================================
    private void configurarEventos() {
        etUbicacion.setOnClickListener(v -> abrirMapa());
        findViewById(R.id.tilUbicacion).setOnClickListener(v -> abrirMapa());

        cbAcepto.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnRegistrarse.setEnabled(isChecked);
            btnRegistrarse.setAlpha(isChecked ? 1f : 0.5f);
        });

        btnRegistrarse.setOnClickListener(v -> registrarCancha());
    }

    // =====================================================
    // 🔹 Registrar Cancha en Firebase (estructura exacta)
    // =====================================================
    private void registrarCancha() {
        String nombre = etNombre.getText().toString().trim();
        String ubicacion = etUbicacion.getText().toString().trim();
        String celular = etCelular.getText().toString().trim();
        String tipoDoc = spTipoDoc.getText().toString().trim();
        String numDoc = etNumDoc.getText().toString().trim();

        String horaDesde = "";
        String horaHasta = "";

        String tarifaDia = etTarifaDia.getText().toString().trim();
        String tarifaNoche = etTarifaNoche.getText().toString().trim();

        if (nombre.isEmpty() || ubicacion.isEmpty() || celular.isEmpty() || numDoc.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbAcepto.isChecked()) {
            Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "No hay sesión iniciada", Toast.LENGTH_SHORT).show();
            return;
        }

        Cccancha cancha = new Cccancha(
                nombre,
                ubicacion,
                celular,
                tipoDoc,
                numDoc,
                horaDesde,
                horaHasta,
                tarifaDia,
                tarifaNoche,
                System.currentTimeMillis()
        );
        cancha.setLat(canchaLat);
        cancha.setLng(canchaLng);

        String adminUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("admins")
                .child(adminUid)
                .child("canchas");

        String canchaId = ref.push().getKey();
        ref.child(canchaId).setValue(cancha)
                .addOnSuccessListener(aVoid -> {
                    // ✅ Paso 1: mostrar loader circular
                    android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(RegistroCancha_Activity.this);
                    progressDialog.setMessage("Guardando datos de la cancha...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    // ✅ Paso 2: incrementar contador total de canchas
                    DatabaseReference contadorRef = FirebaseDatabase.getInstance()
                            .getReference("admins")
                            .child(adminUid)
                            .child("totalCanchas");

                    contadorRef.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData currentData) {
                            Integer currentValue = currentData.getValue(Integer.class);
                            if (currentValue == null) {
                                currentData.setValue(1);
                            } else {
                                currentData.setValue(currentValue + 1);
                            }
                            return Transaction.success(currentData);
                        }

                        @Override
                        public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {}
                    });

                    // ✅ Paso 3: Esperar 1 segundo y redirigir
                    new Handler().postDelayed(() -> {
                        progressDialog.dismiss(); // Ocultar loader
                        Toast.makeText(RegistroCancha_Activity.this, "✅ Cancha registrada correctamente", Toast.LENGTH_SHORT).show();

                        // 🔹 Enviar a DetallesCancha_Activity con IDs
                        Intent intent = new Intent(RegistroCancha_Activity.this, DetallesCancha_Activity.class);
                        intent.putExtra("adminUid", adminUid);   // ✅ UID del usuario autenticado
                        intent.putExtra("canchaId", canchaId);   // ✅ ID de la cancha recién creada
                        startActivity(intent);
                        finish();

                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }, 1000); // Espera 1 segundo antes de redirigir

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }



    private void abrirMapa() {
        Intent i = new Intent(this, mapa_dialog_Activity.class);
        mapaLauncher.launch(i);
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
}
