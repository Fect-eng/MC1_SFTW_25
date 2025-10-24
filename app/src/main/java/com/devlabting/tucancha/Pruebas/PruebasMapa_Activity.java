package com.devlabting.tucancha.Pruebas;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mapbox.bindgen.Value;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.gestures.GesturesPlugin;
import com.mapbox.maps.plugin.gestures.GesturesUtils;

import java.util.ArrayList;
import java.util.List;

import com.devlabting.tucancha.R;

public class PruebasMapa_Activity extends AppCompatActivity {


    private MapView mapView;

    private static final String SRC_ID   = "cancha-source";
    private static final String LAYER_ID = "cancha-layer";

    private final List<Feature> features = new ArrayList<>();
    private Style styleRef; // guardamos el Style para actualizar la fuente


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pruebas_mapa);
        mapView = findViewById(R.id.mapView);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
            styleRef = style;

            // Cámara inicial (Lima)
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                    .center(Point.fromLngLat(-77.0428, -12.0464))
                    .zoom(12.0)
                    .build());

            // --- Source GeoJSON vacía (JSON puro) ---
            String sourceJson =
                    "{ \"type\":\"geojson\", " +
                            "  \"data\":{\"type\":\"FeatureCollection\",\"features\":[]} }";
            style.addStyleSource(SRC_ID, Value.valueOf(sourceJson));

            // --- Layer de círculos (JSON puro) ---
            String layerJson =
                    "{ \"id\":\"" + LAYER_ID + "\"," +
                            "  \"type\":\"circle\"," +
                            "  \"source\":\"" + SRC_ID + "\"," +
                            "  \"paint\":{" +
                            "     \"circle-radius\":8.0," +
                            "     \"circle-color\":\"#1E88E5\"," +
                            "     \"circle-stroke-color\":\"#FFFFFF\"," +
                            "     \"circle-stroke-width\":2.0" +
                            "  }" +
                            "}";
            // El segundo parámetro posiciona la capa; null = al final
            style.addStyleLayer(Value.valueOf(layerJson), null);

            // Marcador de ejemplo
            addCircle(Point.fromLngLat(-77.0310, -12.0460));

            // Long-press para añadir más puntos
            GesturesPlugin gestures = GesturesUtils.getGestures(mapView);
            gestures.addOnMapLongClickListener(point -> {
                addCircle(point);
                return true;
            });
        });
    }

    /** Agrega un punto y actualiza la propiedad 'data' del GeoJSON source. */
    private void addCircle(@NonNull Point point) {
        features.add(Feature.fromGeometry(point));
        if (styleRef != null) {
            String fcJson = FeatureCollection.fromFeatures(features).toJson();
            // ✅ En Java es Value.fromJson(...), no Companion
            com.mapbox.bindgen.Value dataValue = Value.fromJson(fcJson).getValue();
            styleRef.setStyleSourceProperty(SRC_ID, "data", dataValue);
        }
    }

    @Override public void onStart()   { super.onStart();  mapView.onStart(); }
    @Override public void onStop()    { mapView.onStop(); super.onStop(); }
    @Override public void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
    @Override public void onLowMemory(){ super.onLowMemory(); mapView.onLowMemory(); }
}
