package com.devlabting.tucancha.MVC.CLIENTT;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devlabting.tucancha.MainActivity;
import com.devlabting.tucancha.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin;
import com.mapbox.maps.plugin.animation.CameraAnimationsUtils;
import com.mapbox.maps.viewannotation.ViewAnnotationManager;

public class Buscar_Canchas_Activity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private MapView mapView;
    private MapboxMap mapboxMap;
    private DatabaseReference canchasRef;

    private EditText etBuscarCancha;
    private ImageButton btnBuscar;
    private LinearLayout layoutInfoCancha;
    private TextView tvNombreCancha, tvDireccionCancha, tvPrecioCancha, tvHorarioCancha;

    private PointAnnotationManager userAnnotationManager;
    private PointAnnotationManager canchaAnnotationManager;
    private ViewAnnotationManager viewAnnotationManager;


    private final List<Cancha> listaCanchas = new ArrayList<>();

    private LocationEngine locationEngine;
    private final long DEFAULT_INTERVAL = 1000L;
    private final long DEFAULT_MAX_WAIT_TIME = 2000L;

    private RecyclerView rvCanchas;
    private BuscarCanchAdapter adapter;

    // 📍 Manager de anotaciones visuales (tooltips)
   // private com.mapbox.maps.plugin.viewannotation.ViewAnnotationManager viewAnnotationManager;
   // private android.view.View infoMarkerView;
    FloatingActionButton btnActualizar;


    private ImageButton btnMiUbicacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buscar_canchas);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbarr);
        setSupportActionBar(toolbar);

        toolbar.setBackgroundColor(Color.parseColor("#45A2DB")); //color general de app
        toolbar.setTitleTextColor(Color.WHITE); // Cambiar el color del texto del título a blanco
        getSupportActionBar().setTitle("Buscar Canchas");

        // Habilitar la flecha de navegación (opcional)
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define aquí la actividad a la que quieres navegar
                Intent intent = new Intent(Buscar_Canchas_Activity.this, MainActivity.class);
                startActivity(intent);
                // Si quieres que se cierre la actividad actual
                finish();
            }
        });

        // Referencias UI
        mapView = findViewById(R.id.mapViewBuscar);
        etBuscarCancha = findViewById(R.id.etBuscarCancha);
        btnBuscar = findViewById(R.id.btnBuscar);
        layoutInfoCancha = findViewById(R.id.layoutInfoCancha);
        tvNombreCancha = findViewById(R.id.tvNombreCancha);
        tvDireccionCancha = findViewById(R.id.tvDireccionCancha);
        tvPrecioCancha = findViewById(R.id.tvPrecioCancha);
        tvHorarioCancha = findViewById(R.id.tvHorarioCancha);
        rvCanchas = findViewById(R.id.rvCanchas);
        // 🔹 Nueva referencia para el cuadro flotante (sobre el mapa)
       // LinearLayout layoutInfoFlotante = findViewById(R.id.layoutInfoFlotante);
       // TextView tvNombreFlotante = findViewById(R.id.tvNombreFlotante);

        // 🔹 Mostrar lista solo cuando el usuario toca el campo de texto
        etBuscarCancha.setOnTouchListener((v, event) -> {
            rvCanchas.setVisibility(View.GONE);
            return false; // Permite que el teclado se muestre también
        });

        // RecyclerView
        // ✅ RecyclerView con selección táctil funcional
        rvCanchas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BuscarCanchAdapter(listaCanchas, cancha -> {
            moverCamara(Point.fromLngLat(cancha.lng, cancha.lat));
            mostrarInfoCancha(cancha);

            // Ocultar la lista después de seleccionar
            rvCanchas.setVisibility(View.GONE);

            // Ocultar el teclado
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etBuscarCancha.getWindowToken(), 0);
        });
        rvCanchas.setAdapter(adapter);

        // ✅ Buscar cancha al presionar Enter en el teclado
        etBuscarCancha.setOnEditorActionListener((v, actionId, event) -> {
            String texto = etBuscarCancha.getText().toString().trim();
            if (!texto.isEmpty()) {
                buscarCancha(texto);

                // Ocultar lista y teclado al presionar Enter
                rvCanchas.setVisibility(View.GONE);
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etBuscarCancha.getWindowToken(), 0);
            }
            return true; // Indica que el evento se manejó
        });


        // Firebase referencia
        canchasRef = FirebaseDatabase.getInstance().getReference("admins");

        // Inicializar Mapbox
        // Inicializar Mapbox
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
            mapboxMap = mapView.getMapboxMap();

            // Crear el plugin de anotaciones
            var annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapView);

            // ✅ Crear los administradores de anotaciones
            userAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(
                    annotationPlugin,
                    new com.mapbox.maps.plugin.annotation.AnnotationConfig()
            );

            canchaAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(
                    annotationPlugin,
                    new com.mapbox.maps.plugin.annotation.AnnotationConfig()
            );

            //viewAnnotationManager = mapView.getViewAnnotationManager();


            // ✅ Agregar listener global de clics sobre los marcadores de cancha
            // ✅ Agregar listener global de clics sobre los marcadores de cancha
            canchaAnnotationManager.addClickListener(clicked -> {
                try {
                    if (clicked.getData() != null) {
                        com.google.gson.JsonObject obj = clicked.getData().getAsJsonObject();

                        String nombre = obj.has("nombre") ? obj.get("nombre").getAsString() : "Sin nombre";
                        String direccion = obj.has("direccion") ? obj.get("direccion").getAsString() : "Sin dirección";
                        String precio = obj.has("precio") ? obj.get("precio").getAsString() : "N/A";
                        String horario = obj.has("horario") ? obj.get("horario").getAsString() : "N/A";
                        double lat = obj.has("lat") ? obj.get("lat").getAsDouble() : 0;
                        double lng = obj.has("lng") ? obj.get("lng").getAsDouble() : 0;

                        // ✅ Mover la cámara al marcador seleccionado
                        moverCamara(Point.fromLngLat(lng, lat));

                        // ✅ Crear objeto cancha
                        Cancha c = new Cancha();
                        c.nombre = nombre;
                        c.direccion = direccion;
                        c.precio = precio;
                        c.horario = horario;
                        c.lat = lat;
                        c.lng = lng;

                        // ✅ Mostrar la info detallada (abajo)
                        mostrarInfoCancha(c);

                        // ✅ Mostrar cuadro flotante sobre el mapa (nombre de cancha)
                        LinearLayout layoutInfoFlotante = findViewById(R.id.layoutInfoFlotante);
                        TextView tvNombreFlotante = findViewById(R.id.tvNombreFlotante);

                        if (layoutInfoFlotante != null && tvNombreFlotante != null) {
                            tvNombreFlotante.setText(nombre);
                            layoutInfoFlotante.setVisibility(View.VISIBLE);

                            // Ocultar automáticamente después de 4 segundos
                            new android.os.Handler().postDelayed(() -> {
                                layoutInfoFlotante.setVisibility(View.GONE);
                            }, 4000);
                        }
                    }
                } catch (Exception e) {
                    Log.e("MARKER_CLICK", "Error al leer datos del marcador: " + e.getMessage());
                }
                return true;
            });

// ✅ Finalmente, activa la ubicación actual y carga las canchas
            habilitarUbicacionActual();
            cargarCanchasFirebase();

        });

        // Buscar cancha manual
        btnBuscar.setOnClickListener(v -> buscarCancha(etBuscarCancha.getText().toString().trim()));

        // Búsqueda dinámica mientras se escribe
        etBuscarCancha.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String texto = s.toString().trim();

                if (!texto.isEmpty()) {
                    // ✅ Solo cuando el usuario realmente escribe algo
                    adapter.filtrar(texto);
                    rvCanchas.setVisibility(View.VISIBLE);
                } else {
                    // 🚫 Si borra todo el texto, ocultamos la lista
                    rvCanchas.setVisibility(View.GONE);
                }
            }

            @Override public void afterTextChanged(android.text.Editable s) {}
        });


        btnMiUbicacion = findViewById(R.id.btnMiUbicacion);
        btnMiUbicacion.setOnClickListener(v -> {
            if (locationEngine != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show();
                    return;
                }

                locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
                    @Override
                    public void onSuccess(LocationEngineResult result) {
                        Location location = result.getLastLocation();
                        if (location != null) {
                            Point userPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                            moverCamara(userPoint);
                            Toast.makeText(Buscar_Canchas_Activity.this, "Ubicación actualizada", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Buscar_Canchas_Activity.this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(Buscar_Canchas_Activity.this, "Error al obtener ubicación", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        mapView.setOnTouchListener((v, event) -> {
            if (rvCanchas.getVisibility() == View.VISIBLE) {
                rvCanchas.setVisibility(View.GONE);
            }
            return false;
        });

        btnActualizar = findViewById(R.id.btnActualizar);
        btnActualizar.setOnClickListener(v -> {
            // 🧹 Limpiar campo de texto y ocultar resultados
            etBuscarCancha.setText("");
            rvCanchas.setVisibility(View.GONE);
            layoutInfoCancha.setVisibility(View.GONE);

            // 🗺️ Limpia todos los marcadores existentes
            if (canchaAnnotationManager != null) {
                canchaAnnotationManager.deleteAll();
            }

            // 🔄 Vuelve a cargar las canchas desde Firebase
            cargarCanchasFirebase();

            // 📍 Centra nuevamente en la ubicación actual si existe
            if (locationEngine != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
                        @Override
                        public void onSuccess(LocationEngineResult result) {
                            Location location = result.getLastLocation();
                            if (location != null) {
                                Point userPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                                moverCamara(userPoint);
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Buscar_Canchas_Activity.this, "No se pudo actualizar ubicación", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            Toast.makeText(this, "Vista actualizada", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    // Modelo de datos
    static class Cancha {
        public String id, nombre, direccion, precio, horario;
        public double lat, lng;
        public Cancha() {}
    }

    // Permisos de ubicación
    private void habilitarUbicacionActual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            iniciarUbicacion();
        }
    }

    private void iniciarUbicacion() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);
        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationEngine.requestLocationUpdates(request, new LocationCallback(this), getMainLooper());
        locationEngine.getLastLocation(new LocationCallback(this));
    }

    private static class LocationCallback implements LocationEngineCallback<LocationEngineResult> {
        private final WeakReference<Buscar_Canchas_Activity> activityRef;
        private boolean primeraUbicacionMostrada = false; // ✅ Nuevo flag

        LocationCallback(Buscar_Canchas_Activity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            Buscar_Canchas_Activity activity = activityRef.get();
            if (activity == null) return;

            Location location = result.getLastLocation();
            if (location != null) {
                Point userPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());

                // ✅ Mostrar solo la primera vez
                if (!primeraUbicacionMostrada) {
                    activity.moverCamara(userPoint);
                    activity.agregarMarcadorUbicacion(userPoint);
                    primeraUbicacionMostrada = true;

                    // ✅ Detener actualizaciones después de obtener la primera ubicación
                    if (activity.locationEngine != null) {
                        activity.locationEngine.removeLocationUpdates(this);
                    }
                }
            }
        }

        @Override
        public void onFailure(@NonNull Exception e) {
            Buscar_Canchas_Activity activity = activityRef.get();
            if (activity != null) {
                Toast.makeText(activity, "Error al obtener ubicación", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void agregarMarcadorUbicacion(Point punto) {
        if (userAnnotationManager == null) return;
        PointAnnotationOptions options = new PointAnnotationOptions()
                .withPoint(punto)
                .withIconImage("marker-15");
        userAnnotationManager.create(options);
    }

    // Cargar canchas desde Firebase
    private void cargarCanchasFirebase() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("admins");

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaCanchas.clear();
                int total = 0;

                for (DataSnapshot adminSnap : snapshot.getChildren()) {
                    DataSnapshot canchasSnap = adminSnap.child("canchas");
                    for (DataSnapshot canchaSnap : canchasSnap.getChildren()) {
                        if (canchaSnap.getKey().equals("totalCanchas")) continue;

                        Cancha cancha = new Cancha();
                        cancha.id = canchaSnap.getKey();
                        cancha.nombre = canchaSnap.child("nombre").getValue(String.class);
                        cancha.direccion = canchaSnap.child("ubicacion").getValue(String.class);
                        cancha.precio = canchaSnap.child("tarifaDia").getValue(String.class);
                        cancha.horario = canchaSnap.child("horaDesde").getValue(String.class) + " - " +
                                canchaSnap.child("horaHasta").getValue(String.class);

                        // ✅ Lectura robusta de latitud y longitud (acepta Double o String)
                        Object latObj = canchaSnap.child("lat").getValue();
                        Object lngObj = canchaSnap.child("lng").getValue();

                        try {
                            cancha.lat = (latObj != null) ? Double.parseDouble(latObj.toString()) : 0;
                            cancha.lng = (lngObj != null) ? Double.parseDouble(lngObj.toString()) : 0;
                        } catch (NumberFormatException e) {
                            cancha.lat = 0;
                            cancha.lng = 0;
                        }

                        // ✅ Log para depuración
                        Log.d("FIREBASE_CANCHA", "Cargada: " + cancha.nombre +
                                " | " + cancha.direccion +
                                " | LAT: " + cancha.lat +
                                " | LNG: " + cancha.lng);

                        listaCanchas.add(cancha);

                        // ✅ Agregar marcador solo si hay coordenadas válidas
                        if (cancha.lat != 0 && cancha.lng != 0) {
                            agregarMarcador(cancha);
                        }

                        total++;
                    }
                }

                adapter.notifyDataSetChanged();
                rvCanchas.setVisibility(View.GONE);

                Toast.makeText(Buscar_Canchas_Activity.this,
                        "Canchas totales encontradas: " + total,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Buscar_Canchas_Activity.this,
                        "Error Firebase: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    // Agregar marcador en el mapa
    private void agregarMarcador(Cancha cancha) {
        if (canchaAnnotationManager == null || cancha.lat == 0 || cancha.lng == 0) return;

        Point punto = Point.fromLngLat(cancha.lng, cancha.lat);

        // ✅ Crear JSON con la información de la cancha
        com.google.gson.JsonObject data = new com.google.gson.JsonObject();
        data.addProperty("nombre", cancha.nombre != null ? cancha.nombre : "Sin nombre");
        data.addProperty("direccion", cancha.direccion != null ? cancha.direccion : "Sin dirección");
        data.addProperty("precio", cancha.precio != null ? cancha.precio : "N/A");
        data.addProperty("horario", cancha.horario != null ? cancha.horario : "N/A");
        data.addProperty("lat", cancha.lat);
        data.addProperty("lng", cancha.lng);

        // ✅ Crear marcador con sus datos asociados
        PointAnnotationOptions options = new PointAnnotationOptions()
                .withPoint(punto)
                .withTextField(cancha.nombre != null ? cancha.nombre : "Cancha")
                .withIconImage("marker-15")
                .withData(data); // << aquí se asocian los datos al marcador

        canchaAnnotationManager.create(options);
    }



    private void mostrarInfoCancha(Cancha cancha) {
        layoutInfoCancha.setVisibility(View.VISIBLE);
        tvNombreCancha.setText(cancha.nombre != null ? cancha.nombre : "Sin nombre");
        tvDireccionCancha.setText(cancha.direccion != null ? cancha.direccion : "Sin dirección");
        tvPrecioCancha.setText(cancha.precio != null ? "Precio: S/ " + cancha.precio : "Precio: N/A");
        tvHorarioCancha.setText(cancha.horario != null ? "Horario: " + cancha.horario : "Horario: N/A");
    }

    private void moverCamara(Point punto) {
        if (mapboxMap != null) {
            // 🔹 Obtener el plugin de animación de cámara
            CameraAnimationsPlugin cameraPlugin = CameraAnimationsUtils.getCamera(mapView);

            // 🔹 Aplicar animación suave (easeTo)
            cameraPlugin.easeTo(
                    new CameraOptions.Builder()
                            .center(punto)
                            .zoom(16.0)
                            .build(),
                    new com.mapbox.maps.plugin.animation.MapAnimationOptions.Builder()
                            .duration(2000L) // 2 segundos
                            .build()
            );
        }
    }




    private void buscarCancha(String texto) {
        if (texto.isEmpty()) {
            Toast.makeText(this, "Ingrese un nombre o dirección", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Cancha cancha : listaCanchas) {
            if (cancha.nombre != null && cancha.nombre.toLowerCase().contains(texto.toLowerCase())) {
                // ✅ Validar que las coordenadas sean válidas (no 0,0)
                if (cancha.lat != 0 && cancha.lng != 0) {
                    // ✅ Asegurar que el orden sea correcto: (longitud, latitud)
                    Point punto = Point.fromLngLat(cancha.lng, cancha.lat);
                    moverCamara(punto);
                    mostrarInfoCancha(cancha);
                } else {
                    Toast.makeText(this, "Coordenadas inválidas para esta cancha", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }

        Toast.makeText(this, "No se encontró la cancha", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            iniciarUbicacion();
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
        }
    }
}
