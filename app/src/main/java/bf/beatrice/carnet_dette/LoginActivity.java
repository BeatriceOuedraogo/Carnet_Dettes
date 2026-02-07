package bf.beatrice.carnet_dette;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bf.beatrice.carnet_dette.api.SupabaseClient;
import bf.beatrice.carnet_dette.config.SupabaseConfig;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editEmail, editPassword;
    private Button btnLogin, btnRegister;
    private ProgressBar progressBar;
    private SharedPreferences sharedPreferences;
    private SupabaseClient supabaseClient;
    private TextView txtRegisterLink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "LoginActivity créée");


        // creer le lien
        TextView txtRegisterLink = findViewById(R.id.txtRegister);
        txtRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On crée l'intention d'ouvrir RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        sharedPreferences = getSharedPreferences("CarnetDettePrefs", MODE_PRIVATE);
        supabaseClient = new SupabaseClient();


        // Initialiser les vues
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        // Gérer le clic sur connexion
        btnLogin.setOnClickListener(v -> login());
    }
    private void login() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Validation des champs
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // 1. Déclaration de l'objet (On l'appelle jsonBody)
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = SupabaseConfig.BASE_URL + "/auth/v1/token?grant_type=password";

        // 2. Correction : on passe jsonBody.toString() et non "json"
        supabaseClient.post(url, jsonBody, new SupabaseClient.SupabaseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    try {
                        // Pour le Login Auth, Supabase renvoie un Objet {}, pas un tableau []
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONObject user = jsonResponse.getJSONObject("user");

                        // Sauvegarde de la session
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("userId", user.getString("id"));
                        editor.putString("userEmail", user.getString("email"));
                        editor.apply();

                        Toast.makeText(LoginActivity.this, "Connexion réussie !", Toast.LENGTH_SHORT).show();
                        goToDashboardActivity();

                    } catch (JSONException e) {
                        Log.e(TAG, "Erreur parsing login: " + e.getMessage());
                        Toast.makeText(LoginActivity.this, "Erreur de session", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Email ou mot de passe incorrect", Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }

    private void goToDashboardActivity() {
        Intent intent = new Intent(this, DashboardActivity.class);
        Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (supabaseClient != null) {
            supabaseClient.shutdown();
        }
    }
}