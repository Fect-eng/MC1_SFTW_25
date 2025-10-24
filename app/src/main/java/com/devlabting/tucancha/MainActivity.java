package com.devlabting.tucancha;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.devlabting.tucancha.MVC.ADMIN.DetallesCancha_Activity;
import com.devlabting.tucancha.MVC.ADMIN.Horario_Activity;
import com.devlabting.tucancha.MVC.ADMIN.Precios_Promo_Activity;

import com.devlabting.tucancha.MVC.ADMIN.mapa_dialog_Activity;
import com.devlabting.tucancha.MVC.CLIENTT.Buscar_Canchas_Activity;
import com.devlabting.tucancha.MVC.Client_Activity;
import com.devlabting.tucancha.MVC.Client_Query_Activity;
import com.devlabting.tucancha.MVC.RegistroCancha_Activity;
import com.devlabting.tucancha.MVC.TrackeoCliente_Activity;
import com.devlabting.tucancha.Pruebas.DrawerrLayoutt.DrawerLayout1_Activity;
import com.devlabting.tucancha.Pruebas.Ipprueba_Activity;
import com.devlabting.tucancha.Pruebas.Ubicacion_Activity;
import com.devlabting.tucancha.Pruebas.pruebasDrawer.Drawer1_Activity;
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

public class MainActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 2000L; // 2 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        com.devlabting.tucancha.util.PortraitLock.apply(this); // ⬅️ ANTES de setContentView
        setContentView(R.layout.activity_main);


        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(MainActivity.this, RegistroCancha_Activity.class));   // Selection_Activity   Buscar_Canchas_Activity
            // solo para pruebas de red Ipprueba_Activity
            // Drawer1_Activity
            finish(); // evita volver al splash con el botón back =========== Selection_Activity  === Selection_Activity
            // mapa_dialog_Activity
        }, SPLASH_DELAY_MS);
    }
}