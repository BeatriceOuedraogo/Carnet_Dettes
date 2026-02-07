package bf.beatrice.carnet_dette;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.IOException;

import bf.beatrice.carnet_dette.api.SupabaseClient;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editNom, editPrenom, editEmail, editPassword, editConfirmPassword;
    private Button btnRegister;
    private TextView txtLogin;
    private ProgressBar progressBar;

    private OkHttpClient client;
    private static final String SUPABASE_URL = bf.beatrice.carnet_dette.api.SupabaseClient.BASE_URL;
    private static final String SUPABASE_KEY = bf.beatrice.carnet_dette.api.SupabaseClient.API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setTitle("Créer un compte");

        client = new OkHttpClient();

        // Initialiser les vues
        editNom = findViewById(R.id.editNom);
        editPrenom = findViewById(R.id.editPrenom);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        txtLogin = findViewById(R.id.txtLogin);
        progressBar = findViewById(R.id.progressBar);

        // Bouton Inscription
        btnRegister.setOnClickListener(v -> registerUser());

        // Lien vers Login
        txtLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {
        // Récupérer les valeurs
        String nom = editNom.getText().toString().trim();
        String prenom = editPrenom.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        // Validation
        if (nom.isEmpty()) {
            editNom.setError("Le nom est requis");
            editNom.requestFocus();
            return;
        }

        if (prenom.isEmpty()) {
            editPrenom.setError("Le prénom est requis");
            editPrenom.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            editEmail.setError("L'email est requis");
            editEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Email invalide");
            editEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editPassword.setError("Le mot de passe est requis");
            editPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editPassword.setError("Le mot de passe doit contenir au moins 6 caractères");
            editPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            editConfirmPassword.setError("Les mots de passe ne correspondent pas");
            editConfirmPassword.requestFocus();
            return;
        }

        // Afficher le loader
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        // Créer le compte dans Supabase Auth
        createSupabaseAccount(email, password, nom, prenom);
    }

    private void createSupabaseAccount(String email, String password, String nom, String prenom) {
        try {
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("password", password);

            // Métadonnées utilisateur
            JSONObject metadata = new JSONObject();
            metadata.put("nom", nom);
            metadata.put("prenom", prenom);
            metadata.put("role", "USER");
            json.put("data", metadata);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(bf.beatrice.carnet_dette.api.SupabaseClient.BASE_URL + "/auth/v1/signup")
                    .addHeader("apikey", bf.beatrice.carnet_dette.api.SupabaseClient.API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);
                        Toast.makeText(RegisterActivity.this,
                                "Erreur de connexion : " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();

                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);

                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);

                            if (response.isSuccessful()) {

                                // Enregistrer l'utilisateur dans la table "users"
                                if (jsonResponse.has("user")) {
                                String userId = jsonResponse.getJSONObject("user").getString("id");
                                    // On lance la sauvegarde dans ta table personnalisée "utilisateurs"
                                saveUserToDatabase(userId, nom, prenom, email);
                                } else {
                                    // Cas où le compte est créé mais le JSON est différent
                                    goToLogin();
                                }

                            } else {
                                String errorMessage = "Erreur lors de l'inscription";
                                if (jsonResponse.has("msg")) {
                                    errorMessage = jsonResponse.getString("msg");
                                } else if (jsonResponse.has("error_description")) {
                                    errorMessage = jsonResponse.getString("error_description");
                                }else if (jsonResponse.has("message")) {
                                    errorMessage = jsonResponse.getString("message");
                                }


                                // Messages d'erreur en français
                                if (errorMessage.contains("already registered")) {
                                    errorMessage = "Cet email est déjà utilisé";
                                } else if (errorMessage.contains("security") || errorMessage.contains("seconds")) {
                                    errorMessage = "⏱️ Trop de tentatives. Veuillez patienter 1 minute avant de réessayer.";
                                } else if (errorMessage.contains("Email rate limit exceeded")) {
                                    errorMessage = "⏱️ Limite atteinte. Veuillez attendre quelques instants.";
                                }


                                Toast.makeText(RegisterActivity.this,
                                        errorMessage,
                                        Toast.LENGTH_LONG).show();
                            }

                        } catch (Exception e) {
                            Toast.makeText(RegisterActivity.this,
                                    "Erreur : " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
            Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveUserToDatabase(String userId, String nom, String prenom, String email) {
        try {
            JSONObject json = new JSONObject();
            json.put("id", userId);
            json.put("nom", nom);
            json.put("prenom", prenom);
            json.put("email", email);
            json.put("role", "USER");

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(bf.beatrice.carnet_dette.api.SupabaseClient.BASE_URL + "/rest/v1/utilisateurs")
                    .addHeader("apikey", bf.beatrice.carnet_dette.api.SupabaseClient.API_KEY)
                    .addHeader("Authorization", "Bearer " + bf.beatrice.carnet_dette.api.SupabaseClient.API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=minimal")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this,
                                "Compte créé mais erreur lors de la sauvegarde : " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        goToLogin();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this,
                                    "Compte créé avec succès ! Connectez-vous maintenant.",
                                    Toast.LENGTH_LONG).show();
                            goToLogin();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Compte créé. Vous pouvez vous connecter.",
                                    Toast.LENGTH_LONG).show();
                            goToLogin();
                        }
                    });
                }
            });

        } catch (Exception e) {
            Toast.makeText(this,
                    "Erreur : " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            goToLogin();
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}