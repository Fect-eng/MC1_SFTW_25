package com.devlabting.tucancha.MVC.CLIENTT.ReservaCancha;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.devlabting.tucancha.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ReservaCancha_Cliente_Activity extends AppCompatActivity {

    private LinearLayout contenedorDias;
    private GridLayout gridHorarios;
    private Button btnCancha1, btnSeleccionarCancha, btnMesJulio, btnMesAgosto, btnPagar;
    private TextView tvInfoCancha;

    // 🔹 Firebase
    private DatabaseReference dbRef;

    // Variables de estado
    private String canchaSeleccionada = "";
    private String mesSeleccionado = "";
    private String diaSeleccionado = "";
    private String horaSeleccionada = "";

    // Listas de datos
    private final ArrayList<String> nombresCanchas = new ArrayList<>();
    private final HashMap<String, HashMap<String, Object>> mapaCanchas = new HashMap<>();

    // Horarios base (08:00 - 23:00)
    private final String[] HORAS = {
            "08:00", "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00",
            "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"
    };

    private EditText etBuscador;
    private final ArrayList<String> nombresCanchasFiltradas = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reserva_cancha_cliente);

        // Ajuste visual
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // Referencias UI
        contenedorDias = findViewById(R.id.contenedorDias);
        gridHorarios = findViewById(R.id.gridHorarios);
        btnCancha1 = findViewById(R.id.btnCancha1);
        btnSeleccionarCancha = findViewById(R.id.btnSeleccionarCancha);
        btnMesJulio = findViewById(R.id.btnMesJulio);
        btnMesAgosto = findViewById(R.id.btnMesAgosto);
        btnPagar = findViewById(R.id.btnPagar);
        tvInfoCancha = findViewById(R.id.tvInfoCancha);

        etBuscador = findViewById(R.id.etBuscador);

        // 🔹 Conexión Firebase (ahora apunta a "admins" general)
        dbRef = FirebaseDatabase.getInstance().getReference("admins");

        // Cargar todos los nodos (todos los admins y sus canchas)
        cargarTodosLosNodosFirebase();

        // Botón seleccionar cancha
        btnSeleccionarCancha.setOnClickListener(v -> {
            if (etBuscador.getText().toString().isEmpty()) {
                mostrarDialogoCanchas(nombresCanchas); // lista completa
            } else {
                mostrarDialogoCanchas(nombresCanchasFiltradas); // lista filtrada
            }
        });


        // Meses
        btnMesJulio.setOnClickListener(v -> {
            mesSeleccionado = "Julio";
            btnMesJulio.setBackgroundTintList(getColorStateList(R.color.azul_cancha));
            btnMesJulio.setTextColor(getColor(R.color.white));
            btnMesAgosto.setBackgroundTintList(getColorStateList(R.color.white));
            btnMesAgosto.setTextColor(getColor(R.color.black));
            generarDias(7);
        });

        btnMesAgosto.setOnClickListener(v -> {
            mesSeleccionado = "Agosto";
            btnMesAgosto.setBackgroundTintList(getColorStateList(R.color.azul_cancha));
            btnMesAgosto.setTextColor(getColor(R.color.white));
            btnMesJulio.setBackgroundTintList(getColorStateList(R.color.white));
            btnMesJulio.setTextColor(getColor(R.color.black));
            generarDias(8);
        });

        // Pago
        btnPagar.setOnClickListener(v -> {
            if (canchaSeleccionada.isEmpty() || mesSeleccionado.isEmpty()
                    || diaSeleccionado.isEmpty() || horaSeleccionada.isEmpty()) {
                Toast.makeText(this, "⚠️ Completa todos los campos antes de pagar.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "💰 Reserva confirmada:\n" +
                                "Cancha: " + canchaSeleccionada + "\n" +
                                "Fecha: " + diaSeleccionado + " de " + mesSeleccionado + "\n" +
                                "Hora: " + horaSeleccionada,
                        Toast.LENGTH_LONG).show();
            }
        });

        generarDias(7);
        generarHorarios();

        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String texto = s.toString().trim();

                if (!texto.isEmpty()) {
                    // 🔹 Buscar coincidencias con el texto escrito
                    ArrayList<String> coincidencias = new ArrayList<>();
                    for (String nombre : nombresCanchas) {
                        if (nombre.toLowerCase().contains(texto.toLowerCase())) {
                            coincidencias.add(nombre);
                        }
                    }

                    if (!coincidencias.isEmpty()) {
                        mostrarDialogoBusqueda(coincidencias);
                    }
                }
            }
        });


    }  // final del Oncreate no webearse !!!!!!!!

    private void mostrarDialogoBusqueda(ArrayList<String> listaFiltrada) {
        if (listaFiltrada.isEmpty()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Resultados");

        builder.setItems(listaFiltrada.toArray(new String[0]), (dialog, which) -> {
            String seleccion = listaFiltrada.get(which);
            etBuscador.setText(seleccion);
            canchaSeleccionada = seleccion.split(" \\(Admin")[0];
            btnCancha1.setText(seleccion);

            HashMap<String, Object> info = mapaCanchas.get(canchaSeleccionada);
            if (info != null) {
                String adminID = String.valueOf(info.get("adminID"));
                String detalles = "🏟️ " + canchaSeleccionada +
                        "\n👤 Admin ID: " + adminID +
                        "\n📍 " + info.get("ubicacion") +
                        "\n⏰ " + info.get("horaDesde") + " - " + info.get("horaHasta") +
                        "\n💵 Día: S/. " + info.get("tarifaDia") +
                        " | 🌙 Noche: S/. " + info.get("tarifaNoche") +
                        "\n📞 " + info.get("celular");
                tvInfoCancha.setText(detalles);
            }

            dialog.dismiss();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    private void filtrarCanchas(String texto) {
        nombresCanchasFiltradas.clear();
        String query = texto.toLowerCase();

        for (String nombre : nombresCanchas) {
            if (nombre.toLowerCase().contains(query)) {
                nombresCanchasFiltradas.add(nombre);
            }
        }

        if (nombresCanchasFiltradas.isEmpty() && !texto.isEmpty()) {
            Toast.makeText(this, "Sin coincidencias", Toast.LENGTH_SHORT).show();
        }
    }


    // 🔹 Cargar TODOS los nodos de Firebase (todos los administradores y sus canchas)
    private void cargarTodosLosNodosFirebase() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                nombresCanchas.clear();
                mapaCanchas.clear();

                long totalAdmins = snapshot.getChildrenCount();
                Toast.makeText(ReservaCancha_Cliente_Activity.this,
                        "📡 Administradores encontrados: " + totalAdmins,
                        Toast.LENGTH_SHORT).show();

                if (totalAdmins == 0) {
                    Toast.makeText(ReservaCancha_Cliente_Activity.this,
                            "⚠️ No se encontraron administradores.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot adminSnap : snapshot.getChildren()) {
                    String adminID = adminSnap.getKey();
                    if (adminSnap.hasChild("canchas")) {
                        DataSnapshot canchasSnap = adminSnap.child("canchas");
                        for (DataSnapshot canchaSnap : canchasSnap.getChildren()) {
                            String nombre = safeGet(canchaSnap, "nombre");
                            String tarifaDia = safeGet(canchaSnap, "tarifaDia");
                            String tarifaNoche = safeGet(canchaSnap, "tarifaNoche");
                            String ubicacion = safeGet(canchaSnap, "ubicacion");
                            String horaDesde = safeGet(canchaSnap, "horaDesde");
                            String horaHasta = safeGet(canchaSnap, "horaHasta");
                            String celular = safeGet(canchaSnap, "celular");

                            if (nombre.isEmpty()) continue;

                            HashMap<String, Object> datos = new HashMap<>();
                            datos.put("adminID", adminID);
                            datos.put("tarifaDia", tarifaDia);
                            datos.put("tarifaNoche", tarifaNoche);
                            datos.put("ubicacion", ubicacion);
                            datos.put("horaDesde", horaDesde);
                            datos.put("horaHasta", horaHasta);
                            datos.put("celular", celular);

                            mapaCanchas.put(nombre, datos);
                            nombresCanchas.add(nombre + " (Admin: " + adminID.substring(0, 6) + ")");
                        }
                    }
                }

                Toast.makeText(ReservaCancha_Cliente_Activity.this,
                        "✅ Total de canchas leídas: " + nombresCanchas.size(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ReservaCancha_Cliente_Activity.this,
                        "❌ Error al leer nodos: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Safe getter que evita nulls
    private String safeGet(DataSnapshot snap, String key) {
        if (snap.hasChild(key)) return String.valueOf(snap.child(key).getValue());
        for (DataSnapshot child : snap.getChildren()) {
            if (child.getKey().equalsIgnoreCase(key)) {
                return String.valueOf(child.getValue());
            }
        }
        return "";
    }

    // 🔹 Mostrar diálogo con TODAS las canchas
    private void mostrarDialogoCanchas(ArrayList<String> lista) {
        if (lista.isEmpty()) {
            Toast.makeText(this, "No hay canchas disponibles.", Toast.LENGTH_SHORT).show();
            return;
        }


        new AlertDialog.Builder(this)
                .setTitle("Selecciona una cancha")
                .setItems(nombresCanchas.toArray(new String[0]), (dialog, which) -> {
                    String seleccion = lista.get(which);

                    canchaSeleccionada = seleccion.split(" \\(Admin")[0];
                    btnCancha1.setText(seleccion);

                    HashMap<String, Object> info = mapaCanchas.get(canchaSeleccionada);
                    if (info != null) {
                        String adminID = String.valueOf(info.get("adminID"));

                        // 🔹 Mostrar información básica de la cancha
                        String detalles = "🏟️ " + canchaSeleccionada +
                                "\n👤 Admin ID: " + adminID +
                                "\n📍 " + info.get("ubicacion") +
                                "\n⏰ " + info.get("horaDesde") + " - " + info.get("horaHasta") +
                                "\n💵 Día: S/. " + info.get("tarifaDia") +
                                " | 🌙 Noche: S/. " + info.get("tarifaNoche") +
                                "\n📞 " + info.get("celular");

                        tvInfoCancha.setText(detalles);

                        // 🔹 Consultar información del propietario (perfil del admin)
                        DatabaseReference adminRef = FirebaseDatabase.getInstance()
                                .getReference("admins")
                                .child(adminID)
                                .child("perfil");

                        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String nombreAdmin = snapshot.child("nombre").getValue(String.class);
                                    String correoAdmin = snapshot.child("correo").getValue(String.class);
                                    String telefonoAdmin = snapshot.child("telefono").getValue(String.class);

                                    String detallesAdmin = "";
                                    if (nombreAdmin != null && !nombreAdmin.isEmpty())
                                        detallesAdmin += "\n👤 Dueño: " + nombreAdmin;
                                    if (correoAdmin != null && !correoAdmin.isEmpty())
                                        detallesAdmin += "\n📧 Correo: " + correoAdmin;
                                    if (telefonoAdmin != null && !telefonoAdmin.isEmpty())
                                        detallesAdmin += "\n📞 Contacto: " + telefonoAdmin;

                                    // 🔹 Añadir los datos del dueño debajo del detalle de la cancha
                                    tvInfoCancha.append(detallesAdmin);
                                } else {
                                    tvInfoCancha.append("\n👤 Sin datos del dueño registrados.");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                tvInfoCancha.append("\n⚠️ Error al cargar datos del dueño.");
                            }
                        });
                    }

                    btnCancha1.setBackgroundTintList(getColorStateList(R.color.azul_cancha));
                    btnCancha1.setTextColor(getColor(R.color.white));
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }


    // 🔹 Generar días
    private void generarDias(int mes) {
        contenedorDias.removeAllViews();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, mes - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        for (int i = 0; i < 10; i++) {
            Button diaBtn = new Button(this);
            int diaNumero = calendar.get(Calendar.DAY_OF_MONTH);
            String diaSemana = getDiaSemana(calendar.get(Calendar.DAY_OF_WEEK));

            diaBtn.setText(String.format("%02d\n%s", diaNumero, diaSemana));
            diaBtn.setAllCaps(false);
            diaBtn.setTextColor(0xFF000000);
            diaBtn.setBackgroundColor(0xFFE0E0E0);
            diaBtn.setPadding(24, 16, 24, 16);

            diaBtn.setOnClickListener(v -> {
                diaSeleccionado = diaBtn.getText().toString().replace("\n", " ");
                resetearDias();
                diaBtn.setBackgroundColor(0xFF1976D2);
                diaBtn.setTextColor(0xFFFFFFFF);
            });

            contenedorDias.addView(diaBtn);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void resetearDias() {
        for (int i = 0; i < contenedorDias.getChildCount(); i++) {
            Button b = (Button) contenedorDias.getChildAt(i);
            b.setBackgroundColor(0xFFE0E0E0);
            b.setTextColor(0xFF000000);
        }
    }

    private String getDiaSemana(int dia) {
        switch (dia) {
            case Calendar.MONDAY: return "LUN";
            case Calendar.TUESDAY: return "MAR";
            case Calendar.WEDNESDAY: return "MIE";
            case Calendar.THURSDAY: return "JUE";
            case Calendar.FRIDAY: return "VIE";
            case Calendar.SATURDAY: return "SAB";
            case Calendar.SUNDAY: return "DOM";
            default: return "";
        }
    }

    // 🔹 Generar horarios
    private void generarHorarios() {
        gridHorarios.removeAllViews();

        for (String hora : HORAS) {
            TextView bloque = new TextView(this);
            bloque.setText(hora);
            bloque.setGravity(Gravity.CENTER);
            bloque.setPadding(8, 12, 8, 12);
            bloque.setTextColor(0xFFFFFFFF);
            bloque.setBackgroundColor(0xFF4CAF50);
            bloque.setTextSize(15);

            bloque.setOnClickListener(v -> {
                resetearHorarios();
                horaSeleccionada = hora;
                bloque.setBackgroundColor(0xFF1976D2);
                bloque.setText("✅ " + hora);
            });

            gridHorarios.addView(bloque);
        }
    }

    private void resetearHorarios() {
        for (int i = 0; i < gridHorarios.getChildCount(); i++) {
            TextView t = (TextView) gridHorarios.getChildAt(i);
            t.setBackgroundColor(0xFF4CAF50);
            t.setTextColor(0xFFFFFFFF);
            t.setText(HORAS[i]);
        }
    }
}
