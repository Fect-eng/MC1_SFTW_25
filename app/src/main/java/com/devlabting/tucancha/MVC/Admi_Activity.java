package com.devlabting.tucancha.MVC;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.devlabting.tucancha.MVC.ADMIN.HomeAdm_Activity;
import com.devlabting.tucancha.R;
import com.devlabting.tucancha.MainActivity;
import com.devlabting.tucancha.Selection_Activity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Admi_Activity extends AppCompatActivity {

    private static final String TAG = "Admi_Activity";

    // Firebase
    private FirebaseAuth mAuth;

    // Google
    private GoogleSignInClient googleClient;
    private ActivityResultLauncher<Intent> googleLauncher;

    // UI
    private Button btnGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admi);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /**
         * =======================================================================================
         * =======================================================================================
         **/

        // Configurar el Toolbar desde el layout XML
        Toolbar toolbar = findViewById(R.id.toolbarr);
        setSupportActionBar(toolbar);

        toolbar.setBackgroundColor(Color.parseColor("#ff669900")); //color general de app
        toolbar.setTitleTextColor(Color.WHITE); // Cambiar el color del texto del título a blanco
        getSupportActionBar().setTitle("Ingrese como Administrador");

        // Habilitar la flecha de navegación (opcional)
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define aquí la actividad a la que quieres navegar
                Intent intent = new Intent(Admi_Activity.this, Selection_Activity.class);
                startActivity(intent);
                // Si quieres que se cierre la actividad actual
                finish();
            }
        });


        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // =========================
        //   Google Sign-In
        // =========================
        String webId;
        try {
            webId = getString(R.string.default_web_client_id);
            if (webId == null || webId.trim().isEmpty()) {
                webId = "255253762194-uqggvvfvmbnubfsnibl5gupjm0b44dnk.apps.googleusercontent.com";
                Log.w(TAG, "default_web_client_id vacío. Usando fallback.");
            }
        } catch (Exception ignore) {
            webId = "255253762194-uqggvvfvmbnubfsnibl5gupjm0b44dnk.apps.googleusercontent.com";
            Log.w(TAG, "default_web_client_id no accesible. Usando fallback.");
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webId)
                .requestEmail()
                .build();

        googleClient = GoogleSignIn.getClient(this, gso);

        googleLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    if (data == null) {
                        toast("Inicio cancelado");
                        return;
                    }
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        } else {
                            toast("Cuenta nula");
                        }
                    } catch (ApiException e) {
                        Log.w(TAG, "Google sign-in failed", e);
                        toast("Falló Google Sign-In: " + e.getStatusCode());
                    }
                });

        // =========================
        //   Botón Google
        // =========================
        btnGoogle = findViewById(R.id.btnGoogle);
        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> {
                FirebaseUser current = mAuth.getCurrentUser();
                if (current != null) {
                    toast("Sesión activa: " + current.getEmail());
                    goHome(current);
                } else {
                    googleLauncher.launch(googleClient.getSignInIntent());
                }
            });
            // 👇 actualiza el texto cuando se crea la activity
            refreshButtonLabel();
        } else {
            Log.w(TAG, "btnGoogle no encontrado en el layout");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser current = mAuth.getCurrentUser();
        refreshButtonLabel(); // 👈 actualiza texto siempre al entrar
        if (current != null) {
            toast("Sesión activa: " + current.getEmail());
            goHome(current);
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null || idToken.isEmpty()) {
            toast("ID Token nulo");
            return;
        }
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser u = mAuth.getCurrentUser();
                String email = (u != null && u.getEmail() != null) ? u.getEmail() : "(sin correo)";
                toast("Bienvenido: " + email);
                goHome(u);
            } else {
                toast("No se pudo autenticar con Firebase (Google)");
            }
        });
    }

    private void goHome(FirebaseUser u) {
        Intent i = new Intent(this, HomeAdm_Activity.class); // 👈 ahora manda a MainActivity ========= RegistroCancha_Activity ======================================================================
        if (u != null) {
            i.putExtra("uid", u.getUid());
            i.putExtra("email", u.getEmail());
            i.putExtra("displayName", u.getDisplayName());
        }
        startActivity(i);
        finish();
    }

    /** 👇 Cambia el texto del botón según si el usuario ya está autenticado */
    private void refreshButtonLabel() {
        if (btnGoogle == null) return;
        FirebaseUser u = mAuth.getCurrentUser();
        if (u != null) {
            btnGoogle.setText("Ingresar");
        } else {
            btnGoogle.setText("Autenticar con Google");
        }
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
        Log.d(TAG, m);
    }
}
