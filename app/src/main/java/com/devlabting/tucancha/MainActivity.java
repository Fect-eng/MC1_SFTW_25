package com.devlabting.tucancha;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.devlabting.tucancha.MVC.ADMIN.reserva_canchas.reserva_canchaADM_Activity;
import com.devlabting.tucancha.MVC.CLIENTT.ReservaCancha.ReservaCancha_Cliente_Activity;

public class MainActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 2000L; // 2 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        com.devlabting.tucancha.util.PortraitLock.apply(this); // ⬅️ ANTES de setContentView
        setContentView(R.layout.activity_main);


        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(MainActivity.this, ReservaCancha_Cliente_Activity.class));   // Selection_Activity   Buscar_Canchas_Activity
            // solo para pruebas de red Ipprueba_Activity
            // HomeAdm_Activity
            //DetallesCancha_Activity
            // Drawer1_Activity
            finish(); // evita volver al splash con el botón back =========== Selection_Activity  === Selection_Activity
            // mapa_dialog_Activity
        }, SPLASH_DELAY_MS);
    }
}