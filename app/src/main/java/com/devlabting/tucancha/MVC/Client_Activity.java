package com.devlabting.tucancha.MVC;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.devlabting.tucancha.R;
import com.devlabting.tucancha.util.PortraitLock;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

// ===== Facebook SDK =====
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

public class Client_Activity extends AppCompatActivity {

    private static final String TAG = "Client_Activity";

    // Firebase
    private FirebaseAuth mAuth;

    // Google
    private GoogleSignInClient googleClient;
    private ActivityResultLauncher<Intent> googleLauncher;

    // Facebook
    private CallbackManager fbCallbackManager;    // esto solo lo usaras para facebook

    // UI
    private Button btnGoogle;
    private Button btnFacebook;             // Boton para uso de Facebook

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        PortraitLock.apply(this); // Fuerza vertical (según tu util)
        setContentView(R.layout.activity_client);

        // Insets seguros (si existe el root con id 'main')
        View root = findViewById(R.id.main);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
                return insets;
            });
        }

        // =========================
        //   Facebook SDK INIT
        // =========================
        // IMPORTANTE: inicializar ANTES de usar LoginManager o fbCallbackManager
        if (!FacebookSdk.isInitialized()) {
            FacebookSdk.setAutoInitEnabled(true);
            try {
                // Define estos en strings.xml
                FacebookSdk.setApplicationId(getString(R.string.facebook_app_id));
                // ClientToken es opcional, pero recomendable si usas eventos
                // FacebookSdk.setClientToken(getString(R.string.facebook_client_token));
            } catch (Exception ignore) {
                // Si faltan los strings, no crashea; pero configura Manifest/strings luego.
            }
            FacebookSdk.fullyInitialize();
        }

        // =========================
        //   Firebase INIT
        // =========================
        try {
            FirebaseApp app = FirebaseApp.initializeApp(this);
            if (app == null) {
                Log.w(TAG, "google-services.json no encontrado. Usando FirebaseOptions manual.");
                // Fallback manual (coincide con tu proyecto)
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApplicationId("1:255253762194:android:68708eb5b61b454708164b") // mobilesdk_app_id
                        .setApiKey("AIzaSyBX_dR_wuulLVjH1T8lHNMR7moMgT9NwG0")               // api_key.current_key
                        .setProjectId("tucancha-7da42")                                     // project_id
                        .setDatabaseUrl("https://tucancha-7da42-default-rtdb.firebaseio.com") // firebase_url
                        .setStorageBucket("tucancha-7da42.appspot.com")                     // storage bucket
                        .build();
                FirebaseApp.initializeApp(this, options);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando Firebase", e);
            toast("No se pudo inicializar Firebase: " + e.getMessage());
        }

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
        //   Facebook Login
        // =========================
        fbCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
            @Override public void onSuccess(LoginResult loginResult) {
                AccessToken token = loginResult.getAccessToken();
                handleFacebookAccessToken(token);
            }
            @Override public void onCancel() {
                toast("Inicio con Facebook cancelado");
            }
            @Override public void onError(FacebookException error) {
                Log.w(TAG, "Facebook sign-in failed", error);
                toast("Falló Facebook: " + (error != null ? error.getMessage() : ""));
            }
        });

        // =========================
        //   UI: Botones
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
        } else {
            Log.w(TAG, "btnGoogle no encontrado en el layout");
        }

        btnFacebook = findViewById(R.id.btnFacebook);
        if (btnFacebook != null) {
            btnFacebook.setOnClickListener(v ->
                    LoginManager.getInstance()
                            .logInWithReadPermissions(
                                    Client_Activity.this,
                                    Arrays.asList("email", "public_profile")
                            )
            );
        } else {
            Log.w(TAG, "btnFacebook no encontrado en el layout");
        }

        // Ajusta etiqueta del botón según RTDB/usuario
        refreshButtonLabel();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser current = mAuth.getCurrentUser();
        if (current != null) {
            toast("Sesión activa: " + current.getEmail());
            goHome(current);
        }
    }

    // Facebook aún usa onActivityResult (para el callback manager)
    @Override
    @Deprecated
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (fbCallbackManager != null && data != null) {
            fbCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /** Cambia el texto del botón según si el usuario existe en RTDB o no. */
    private void refreshButtonLabel() {
        if (btnGoogle == null) return;

        FirebaseUser u = mAuth.getCurrentUser();
        if (u == null) {
            btnGoogle.setText("Registrar con Google");
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users").child(u.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snap) {
                if (snap.exists()) {
                    btnGoogle.setText("Ingresar");
                } else {
                    btnGoogle.setText("Registrar con Google");
                }
            }
            @Override public void onCancelled(DatabaseError error) {
                Log.w(TAG, "refreshButtonLabel:onCancelled", error.toException());
                btnGoogle.setText("Ingresar");
            }
        });
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
                if (u != null) {
                    upsertUserAndGo(u);
                } else {
                    goHome(null);
                }
            } else {
                toast("No se pudo autenticar con Firebase (Google)");
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        if (token == null) {
            toast("Token de Facebook nulo");
            return;
        }
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser u = mAuth.getCurrentUser();
                String email = (u != null && u.getEmail() != null) ? u.getEmail() : "(sin correo)";
                toast("Bienvenido: " + email);
                if (u != null) {
                    upsertUserAndGo(u);
                } else {
                    goHome(null);
                }
            } else {
                toast("No se pudo autenticar con Firebase (Facebook)");
            }
        });
    }

    /** Crea/actualiza users/{uid} y navega. */
    private void upsertUserAndGo(FirebaseUser u) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users").child(u.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snap) {
                if (!snap.exists()) {
                    ref.child("email").setValue(u.getEmail());
                    ref.child("displayName").setValue(u.getDisplayName());
                    ref.child("createdAt").setValue(ServerValue.TIMESTAMP);
                }
                ref.child("lastLogin").setValue(ServerValue.TIMESTAMP);

                refreshButtonLabel();
                goHome(u);
            }
            @Override public void onCancelled(DatabaseError error) {
                Log.w(TAG, "upsertUserAndGo:onCancelled", error.toException());
                goHome(u);
            }
        });
    }

    private void goHome(FirebaseUser u) {
        Intent i = new Intent(this, RegistroCancha_Activity.class); // Ajusta si tu destino es otro  TrackeoCliente_Activity
        if (u != null) {
            i.putExtra("uid", u.getUid());
            i.putExtra("email", u.getEmail());
            i.putExtra("displayName", u.getDisplayName());
            if (u.getPhotoUrl() != null) {
                i.putExtra("photoUrl", u.getPhotoUrl().toString());
            }
        }
        startActivity(i);
        finish();
    }

    /** Cierra sesión en Google, Facebook y Firebase. */
    private void signOut() {
        if (googleClient != null) {
            googleClient.signOut().addOnCompleteListener(t -> {
                LoginManager.getInstance().logOut();
                mAuth.signOut();
                toast("Sesión cerrada");
                refreshButtonLabel();
            });
        } else {
            LoginManager.getInstance().logOut();
            mAuth.signOut();
            toast("Sesión cerrada");
            refreshButtonLabel();
        }
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
        Log.d(TAG, m);
    }
}
