package com.devlabting.tucancha.MVC;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.TextView;

import com.devlabting.tucancha.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.mapbox.bindgen.Value;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.CameraState;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.gestures.GesturesPlugin;
import com.mapbox.maps.plugin.gestures.GesturesUtils;

import java.util.ArrayList;
import java.util.List;

    public class TrackeoCliente_Activity extends AppCompatActivity {

        // UI
        private MapView mapView;
        private TextView tvLat, tvLon;
        private FloatingActionButton fabZoomIn, fabZoomOut;
        private ExtendedFloatingActionButton fabToggleMarkers;

        // Mapbox IDs
        private static final String MARKER_SRC_ID = "marker-source";
        private static final String MARKER_LAYER_ID = "marker-layer";
        private static final String ME_SRC_ID = "me-source";
        private static final String ME_LAYER_ID = "me-layer";

        // Estado
        private final List<Feature> markerFeatures = new ArrayList<>();
        private Style styleRef;
        private boolean markerMode = false;
        private boolean cameraCenteredOnce = false;

        // Location
        private LocationManager locationManager;
        private final LocationListener locationListener = new LocationListener() {
            @Override public void onLocationChanged(@NonNull Location loc) {
                // Actualiza UI con ubicación GPS actual
                updateGpsUI(loc);

                // Dibuja/actualiza el punto "mi ubicación"
                updateMeDot(Point.fromLngLat(loc.getLongitude(), loc.getLatitude()));

                // Centrar cámara una única vez
                if (!cameraCenteredOnce && mapView != null) {
                    cameraCenteredOnce = true;
                    mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                            .center(Point.fromLngLat(loc.getLongitude(), loc.getLatitude()))
                            .zoom(15.0)
                            .build());
                }
            }
            @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override public void onProviderEnabled(@NonNull String provider) {}
            @Override public void onProviderDisabled(@NonNull String provider) {}
        };

        // Launcher permisos
        private final ActivityResultLauncher<String[]> locationPermsLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    boolean granted = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false))
                            || Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false));
                    if (granted) startLocationUpdates();
                });

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_trackeo_cliente);

            // Insets (si tu root tiene @id/main)
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return insets;
            });

            // Referencias UI
            mapView          = findViewById(R.id.mapView);
            tvLat            = findViewById(R.id.tvLat);
            tvLon            = findViewById(R.id.tvLon);
            fabZoomIn        = findViewById(R.id.fabZoomIn);
            fabZoomOut       = findViewById(R.id.fabZoomOut);
            fabToggleMarkers = findViewById(R.id.fabToggleMarkers);

            // Estilo y capas/sources
            mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
                styleRef = style;

                // --- Source para "mi ubicación" (un solo punto) ---
                String meSrcJson = "{ \"type\":\"geojson\", " +
                        "  \"data\":{\"type\":\"FeatureCollection\",\"features\":[]} }";
                style.addStyleSource(ME_SRC_ID, Value.valueOf(meSrcJson));

                // Capa circle para "mi ubicación" (cyan)
                String meLayerJson = "{ \"id\":\"" + ME_LAYER_ID + "\"," +
                        "  \"type\":\"circle\"," +
                        "  \"source\":\"" + ME_SRC_ID + "\"," +
                        "  \"paint\":{" +
                        "     \"circle-radius\":6.0," +
                        "     \"circle-color\":\"#00BCD4\"," +
                        "     \"circle-stroke-color\":\"#FFFFFF\"," +
                        "     \"circle-stroke-width\":2.0" +
                        "  }" +
                        "}";
                style.addStyleLayer(Value.valueOf(meLayerJson), null);

                // --- Source para marcadores (varios puntos) ---
                String markerSrcJson = "{ \"type\":\"geojson\", " +
                        "  \"data\":{\"type\":\"FeatureCollection\",\"features\":[]} }";
                style.addStyleSource(MARKER_SRC_ID, Value.valueOf(markerSrcJson));

                // Capa circle para marcadores (azul)
                String markerLayerJson = "{ \"id\":\"" + MARKER_LAYER_ID + "\"," +
                        "  \"type\":\"circle\"," +
                        "  \"source\":\"" + MARKER_SRC_ID + "\"," +
                        "  \"paint\":{" +
                        "     \"circle-radius\":8.0," +
                        "     \"circle-color\":\"#1E88E5\"," +
                        "     \"circle-stroke-color\":\"#FFFFFF\"," +
                        "     \"circle-stroke-width\":2.0" +
                        "  }" +
                        "}";
                style.addStyleLayer(Value.valueOf(markerLayerJson), null);

                // Gestos: click en mapa -> si modo ON, agregar marcador
                GesturesPlugin gestures = GesturesUtils.getGestures(mapView);
                gestures.addOnMapClickListener(point -> {
                    if (markerMode) {
                        addMarker(point);
                        // Mostrar lat/lon del último marcador
                        tvLon.setText(String.format("Marcador: %.6f, %.6f", point.latitude(), point.longitude()));
                        return true;
                    }
                    return false;
                });

                // Cámara inicial (fallback Lima)
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                        .center(Point.fromLngLat(-77.0428, -12.0464))
                        .zoom(12.0)
                        .build());
            });

            // Zoom +
            fabZoomIn.setOnClickListener(v -> {
                CameraState cs = mapView.getMapboxMap().getCameraState();
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                        .zoom(cs.getZoom() + 1.0)
                        .build());
            });

            // Zoom -
            fabZoomOut.setOnClickListener(v -> {
                CameraState cs = mapView.getMapboxMap().getCameraState();
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                        .zoom(cs.getZoom() - 1.0)
                        .build());
            });

            // Toggle modo marcadores
            fabToggleMarkers.setOnClickListener(v -> {
                markerMode = !markerMode;
                fabToggleMarkers.setText(markerMode ? "Marcadores ON" : "Marcadores OFF");
                fabToggleMarkers.setIconResource(
                        markerMode ? android.R.drawable.btn_star_big_on
                                : android.R.drawable.btn_star_big_off
                );
            });

            // Permisos + ubicación
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            requestLocationPermissionsAndStart();
        }

        // --- Dibuja/actualiza el punto "mi ubicación" ---
        private void updateMeDot(@NonNull Point p) {
            if (styleRef == null) return;
            FeatureCollection fc = FeatureCollection.fromFeatures(new Feature[]{ Feature.fromGeometry(p) });
            Value dataValue = Value.fromJson(fc.toJson()).getValue();
            styleRef.setStyleSourceProperty(ME_SRC_ID, "data", dataValue);
        }

        // --- Marcadores ---
        private void addMarker(@NonNull Point point) {
            markerFeatures.add(Feature.fromGeometry(point));
            if (styleRef != null) {
                String fcJson = FeatureCollection.fromFeatures(markerFeatures).toJson();
                Value dataValue = Value.fromJson(fcJson).getValue();
                styleRef.setStyleSourceProperty(MARKER_SRC_ID, "data", dataValue);
            }
        }

        // ---- Permisos / ubicación ----
        private void requestLocationPermissionsAndStart() {
            boolean fine   = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            if (fine || coarse) {
                startLocationUpdates();
            } else {
                locationPermsLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            }
        }

        private void startLocationUpdates() {
            try {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                String best = locationManager.getBestProvider(criteria, true);
                if (best == null) best = LocationManager.GPS_PROVIDER;

                @SuppressWarnings("MissingPermission")
                Location last = locationManager.getLastKnownLocation(best);
                if (last != null) {
                    updateGpsUI(last);
                    updateMeDot(Point.fromLngLat(last.getLongitude(), last.getLatitude()));
                    if (!cameraCenteredOnce) {
                        cameraCenteredOnce = true;
                        mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                                .center(Point.fromLngLat(last.getLongitude(), last.getLatitude()))
                                .zoom(15.0)
                                .build());
                    }
                }

                @SuppressWarnings("MissingPermission")
                Void ignored = null;
                locationManager.requestLocationUpdates(best, 2000L, 1f, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000L, 5f, locationListener);
            } catch (SecurityException ignored) {}
        }

        private void stopLocationUpdates() {
            if (locationManager != null) {
                locationManager.removeUpdates(locationListener);
            }
        }

        // --- UI helpers ---
        private void updateGpsUI(@NonNull Location loc) {
            tvLat.setText(String.format("GPS: %.6f, %.6f", loc.getLatitude(), loc.getLongitude()));
            // tvLon se actualiza al colocar un marcador (último marcador)
        }

        // Ciclo de vida
        @Override public void onStart()   { super.onStart();  mapView.onStart(); }
        @Override public void onStop()    { mapView.onStop(); super.onStop(); }
        @Override public void onDestroy() { stopLocationUpdates(); mapView.onDestroy(); super.onDestroy(); }
        @Override public void onLowMemory(){ super.onLowMemory(); mapView.onLowMemory(); }
    }

