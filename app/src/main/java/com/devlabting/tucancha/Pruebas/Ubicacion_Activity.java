package com.devlabting.tucancha.Pruebas;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.devlabting.tucancha.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;

import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;

public class Ubicacion_Activity extends AppCompatActivity {

    private MapView mapView;
    private LocationComponentPlugin locationComponent;
    private Point ultimaUbicacion;
    private FloatingActionButton btnRefrescar;

    private final OnIndicatorPositionChangedListener indicatorListener = point -> {
        ultimaUbicacion = point;
        mapView.getMapboxMap().setCamera(
                new CameraOptions.Builder()
                        .center(point)
                        .zoom(17.0)
                        .build()
        );
    };

    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fine = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarse = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if ((fine != null && fine) || (coarse != null && coarse)) {
                    activarMiUbicacion();
                } else {
                    Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion);

        mapView = findViewById(R.id.mapView);
        btnRefrescar = findViewById(R.id.btnRefrescar);

        btnRefrescar.setOnClickListener(v -> refrescarUbicacion());

        // 🔹 Cargar estilo y luego verificar permisos
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> verificarPermisos());
    }

    private void verificarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            activarMiUbicacion();
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void activarMiUbicacion() {
        try {
            // ✅ Obtener correctamente el plugin de ubicación
            locationComponent = mapView.getPlugin(Plugin.MAPBOX_LOCATION_COMPONENT_PLUGIN_ID);

            if (locationComponent == null) {
                Toast.makeText(this, "No se pudo inicializar el componente de ubicación.", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Activar el seguimiento de ubicación
            locationComponent.setEnabled(true);

            // ✅ Personalizar el punto (puck) con tu ícono
            LocationPuck2D customPuck = new LocationPuck2D(
                    null,
                    AppCompatResources.getDrawable(this, R.drawable.baseline_add_location_alt_24),
                    null
            );

            locationComponent.setLocationPuck(customPuck);

            // ✅ Escuchar posición en tiempo real
            locationComponent.addOnIndicatorPositionChangedListener(indicatorListener);

            Toast.makeText(this, "Ubicación activada ✅", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }




    private void refrescarUbicacion() {
        if (ultimaUbicacion != null) {
            mapView.getMapboxMap().setCamera(
                    new CameraOptions.Builder()
                            .center(ultimaUbicacion)
                            .zoom(17.0)
                            .build()
            );
            Toast.makeText(this, "📍 Ubicación centrada", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Esperando señal GPS...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationComponent != null) {
            locationComponent.removeOnIndicatorPositionChangedListener(indicatorListener);
        }
    }
}
