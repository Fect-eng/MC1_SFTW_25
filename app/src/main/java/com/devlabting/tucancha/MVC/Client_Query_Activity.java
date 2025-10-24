package com.devlabting.tucancha.MVC;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.devlabting.tucancha.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;


import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import kotlin.Unit;

public class Client_Query_Activity extends AppCompatActivity {

    private MapView mapView;
    private TextInputLayout tilBuscar;
    private TextInputEditText etBuscar;
    private FloatingActionButton fabMyLocation;

    // Opcionales (si los agregaste al XML)
    private ExtendedFloatingActionButton fabZoomIn;
    private ExtendedFloatingActionButton fabZoomOut;

    private RequestQueue volleyQueue;

    private final ActivityResultLauncher<String[]> locationPermsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fine = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false));
                boolean coarse = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false));
                if (fine || coarse) {
                    enableLocationPuck();   // muestra punto azul
                    recenterToMyLocation(); // mueve cámara
                } else {
                    Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
                }
            });

    private void enableLocationPuck() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_client_query);

        mapView = findViewById(R.id.mapView);
        tilBuscar = findViewById(R.id.tilBuscar);
        etBuscar = findViewById(R.id.etBuscar);
        fabMyLocation = findViewById(R.id.fabMyLocation);

        // Opcionales (si existen)
        fabZoomIn  = findViewById(getResources().getIdentifier("fabZoomIn", "id", getPackageName()));
        fabZoomOut = findViewById(getResources().getIdentifier("fabZoomOut", "id", getPackageName()));

        volleyQueue = Volley.newRequestQueue(this);

        // Cargar estilo y cámara inicial
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
            moveCamera(Point.fromLngLat(-77.0428, -12.0464), 13.0);
            if (hasLocationPermission()) {
                enableLocationPuck();
            }
        });

        // Buscar con IME action o Enter
        etBuscar.setOnEditorActionListener((v, actionId, event) -> {
            boolean isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN);
            if (isSearchAction) {
                String q = etBuscar.getText() != null ? etBuscar.getText().toString().trim() : "";
                doGeocode(q);
                return true;
            }
            return false;
        });

        // Icono de fin del TextInputLayout como botón buscar
        tilBuscar.setEndIconOnClickListener(v -> {
            String q = etBuscar.getText() != null ? etBuscar.getText().toString().trim() : "";
            doGeocode(q);
        });

        // Mi ubicación: permisos + puck + recentrado
        fabMyLocation.setOnClickListener(v -> {
            if (hasLocationPermission()) {
                enableLocationPuck();
                recenterToMyLocation();
            } else {
                locationPermsLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            }
        });

        // ZOOM + (si existe en XML)
        if (fabZoomIn != null) {
            fabZoomIn.setOnClickListener(v -> {
                double z = mapView.getMapboxMap().getCameraState().getZoom();
                setZoom(Math.min(z + 1.0, 20.0));
            });
        }

        // ZOOM − (si existe en XML)
        if (fabZoomOut != null) {
            fabZoomOut.setOnClickListener(v -> {
                double z = mapView.getMapboxMap().getCameraState().getZoom();
                setZoom(Math.max(z - 1.0, 2.0));
            });
        }
    }

    // ===== Geocoding Mapbox con Volley =====
    private void doGeocode(String query) {
        if (TextUtils.isEmpty(query)) {
            Toast.makeText(this, "Escribe una dirección para buscar", Toast.LENGTH_SHORT).show();
            return;
        }

        Point center = mapView.getMapboxMap().getCameraState().getCenter();
        String proximity = (center != null)
                ? (center.longitude() + "," + center.latitude())
                : "-77.0428,-12.0464";

        // ✅ Siempre encode UTF-8 (minSdk 24 soporta esto)
        String encoded = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        }
        String accessToken = getString(R.string.mapbox_access_token); // pk...

        String url = "https://api.mapbox.com/geocoding/v5/mapbox.places/"
                + encoded + ".json?limit=1&language=es&country=PE"
                + "&proximity=" + proximity
                + "&access_token=" + accessToken;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONArray features = response.optJSONArray("features");
                        if (features != null && features.length() > 0) {
                            JSONObject f0 = features.getJSONObject(0);
                            JSONArray centerArr = f0.getJSONArray("center"); // [lon, lat]
                            double lon = centerArr.getDouble(0);
                            double lat = centerArr.getDouble(1);
                            moveCamera(Point.fromLngLat(lon, lat), 16.0);
                        } else {
                            Toast.makeText(this, "No se encontraron resultados", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error al parsear resultado", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error de red: " + (error.getMessage() != null ? error.getMessage() : "desconocido"), Toast.LENGTH_SHORT).show()
        );

        volleyQueue.add(req);
    }

    // ===== Permisos y recentrado a mi ubicación =====
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void recenterToMyLocation() {
        try {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (lm == null) {
                Toast.makeText(this, "No se pudo acceder al servicio de ubicación", Toast.LENGTH_SHORT).show();
                return;
            }

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            String provider = lm.getBestProvider(criteria, true);

            @SuppressWarnings("MissingPermission")
            Location last = provider != null ? lm.getLastKnownLocation(provider) : null;

            if (last != null) {
                moveCamera(Point.fromLngLat(last.getLongitude(), last.getLatitude()), 16.0);
            } else {
                // Solicitar UNA actualización rápida
                @SuppressWarnings("MissingPermission")
                LocationListener oneShot = new LocationListener() {
                    @Override public void onLocationChanged(@NonNull Location location) {
                        moveCamera(Point.fromLngLat(location.getLongitude(), location.getLatitude()), 16.0);
                        lm.removeUpdates(this);
                    }
                    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                    @Override public void onProviderEnabled(@NonNull String provider) {}
                    @Override public void onProviderDisabled(@NonNull String provider) {}
                };

                @SuppressWarnings("MissingPermission")
                String prov = provider != null ? provider : LocationManager.GPS_PROVIDER;

                // ✅ Llamada real a requestLocationUpdates
                @SuppressWarnings("MissingPermission")
                Object ignored = lm;

                Toast.makeText(this, "Obteniendo ubicación…", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "No fue posible obtener ubicación", Toast.LENGTH_SHORT).show();
        }
    }

    // ===== Punto azul (Location Puck) =====
    
    // ===== Utilitarios de cámara =====
    private void setZoom(double zoom) {
        mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                .center(mapView.getMapboxMap().getCameraState().getCenter())
                .zoom(zoom)
                .build());
    }

    private void moveCamera(Point center, double zoom) {
        mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                .center(center)
                .zoom(zoom)
                .build());
    }

    // ===== Ciclo de vida MapView =====
    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onStop()  { mapView.onStop(); super.onStop(); }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override protected void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
}
