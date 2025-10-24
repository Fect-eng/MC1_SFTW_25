package com.devlabting.tucancha.MVC.ADMIN;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.devlabting.tucancha.R;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.gestures.GesturesUtils;
import com.mapbox.maps.plugin.gestures.OnMapClickListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class mapa_dialog_Activity extends AppCompatActivity {

    private MapView mapView;
    private Point puntoSeleccionado;
    private String direccionSeleccionada;

    private EditText etBuscar;
    private ImageButton btnBuscar, btnZoomIn, btnZoomOut;
    private TextView tvDireccion;

    private boolean primeraUbicacion = true;
    private String mapboxToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mapa_dialog);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mapView = findViewById(R.id.mapView);
        Button btnConfirmar = findViewById(R.id.btnConfirmarUbicacion);
        etBuscar = findViewById(R.id.etBuscarDireccion);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        tvDireccion = findViewById(R.id.tvDireccionSeleccionada);

        mapboxToken = getString(R.string.mapbox_access_token);

        // 🔹 Cargar el mapa
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
            habilitarMiUbicacion();
        });

        // 🔹 Clic en mapa → obtiene dirección + coordenadas
        GesturesUtils.getGestures(mapView).addOnMapClickListener(new OnMapClickListener() {
            @Override
            public boolean onMapClick(Point point) {
                puntoSeleccionado = point; // ✅ Guarda lat/lng
                obtenerDireccion(point);
                return true;
            }
        });

        // 🔹 Confirmar ubicación → enviar datos de regreso
        btnConfirmar.setOnClickListener(v -> {
            if (direccionSeleccionada != null && puntoSeleccionado != null) {
                Intent data = new Intent();
                data.putExtra("direccion", direccionSeleccionada);
                data.putExtra("lat", puntoSeleccionado.latitude());  // ✅ NUEVO
                data.putExtra("lng", puntoSeleccionado.longitude()); // ✅ NUEVO
                setResult(RESULT_OK, data);
                finish();
            } else {
                Toast.makeText(this, "Selecciona una ubicación en el mapa", Toast.LENGTH_SHORT).show();
            }
        });

        // 🔹 Buscar dirección manual
        btnBuscar.setOnClickListener(v -> {
            String query = etBuscar.getText().toString().trim();
            if (!query.isEmpty()) {
                buscarDireccion(query);
            } else {
                Toast.makeText(this, "Ingresa una dirección", Toast.LENGTH_SHORT).show();
            }
        });

        // 🔹 Zoom in/out
        btnZoomIn.setOnClickListener(v ->
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                        .zoom(mapView.getMapboxMap().getCameraState().getZoom() + 1)
                        .build())
        );

        btnZoomOut.setOnClickListener(v ->
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                        .zoom(mapView.getMapboxMap().getCameraState().getZoom() - 1)
                        .build())
        );
    }

    // 🔹 Reverse geocoding → obtiene nombre de dirección
    private void obtenerDireccion(Point point) {
        String url = "https://api.mapbox.com/geocoding/v5/mapbox.places/"
                + point.longitude() + "," + point.latitude()
                + ".json?access_token=" + mapboxToken;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray features = response.getJSONArray("features");
                        if (features.length() > 0) {
                            JSONObject obj = features.getJSONObject(0);
                            direccionSeleccionada = obj.getString("place_name");
                            tvDireccion.setText("📍 " + direccionSeleccionada);
                            Toast.makeText(this, "Ubicación: " + direccionSeleccionada, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error al obtener dirección", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    // 🔹 Forward geocoding → buscar por texto
    private void buscarDireccion(String query) {
        String url = "https://api.mapbox.com/geocoding/v5/mapbox.places/"
                + query + ".json?access_token=" + mapboxToken;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray features = response.getJSONArray("features");
                        if (features.length() > 0) {
                            JSONObject obj = features.getJSONObject(0);
                            JSONObject geometry = obj.getJSONObject("geometry");
                            JSONArray coords = geometry.getJSONArray("coordinates");

                            double lon = coords.getDouble(0);
                            double lat = coords.getDouble(1);

                            // ✅ Guardamos el punto buscado
                            puntoSeleccionado = Point.fromLngLat(lon, lat);

                            mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                                    .center(puntoSeleccionado)
                                    .zoom(15.0)
                                    .build());

                            direccionSeleccionada = obj.getString("place_name");
                            tvDireccion.setText("📍 " + direccionSeleccionada);
                            Toast.makeText(this, "Movido a: " + direccionSeleccionada, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "No se encontró la dirección", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error al buscar dirección", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    // 🔹 Mostrar ubicación actual del usuario
    private void habilitarMiUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        LocationComponentPlugin locationComponent = LocationComponentUtils.getLocationComponent(mapView);
        locationComponent.setEnabled(true);

        LocationPuck2D puckPersonalizado = new LocationPuck2D(
                null,
                AppCompatResources.getDrawable(this, R.drawable.baseline_ac_unit_24),
                null
        );

        locationComponent.setLocationPuck(puckPersonalizado);

        locationComponent.addOnIndicatorPositionChangedListener(point -> {
            if (primeraUbicacion) {
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                        .center(point)
                        .zoom(16.5)
                        .build());
                primeraUbicacion = false;
            }
        });
    }
}
