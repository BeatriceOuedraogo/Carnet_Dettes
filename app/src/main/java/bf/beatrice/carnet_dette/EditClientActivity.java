package bf.beatrice.carnet_dette;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import bf.beatrice.carnet_dette.models.Client;
import bf.beatrice.carnet_dette.repository.ClientRepository;

public class EditClientActivity extends AppCompatActivity {

    private static final String TAG = "EditClientActivity";
    private TextInputEditText editNom, editPrenom, editTelephone, editAdresse;
    private Button btnSave, btnCancel;
    private ProgressBar progressBar;

    private ClientRepository clientRepository;
    private String clientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_client);

        // Récupérer les infos du client
        clientId = getIntent().getStringExtra("CLIENT_ID");
        String nom = getIntent().getStringExtra("CLIENT_NOM");
        String prenom = getIntent().getStringExtra("CLIENT_PRENOM");
        String telephone = getIntent().getStringExtra("CLIENT_TELEPHONE");
        String adresse = getIntent().getStringExtra("CLIENT_ADRESSE");

        Log.d(TAG, "ID client reçu: " + clientId);
        Log.d(TAG, "Données reçues - Nom: " + nom + ", Prénom: " + prenom);

        if (clientId == null || clientId.isEmpty()) {
            Toast.makeText(this, "Erreur : Aucun client sélectionné", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "clientId est null ou vide");
            finish();
            return;
        }

        setTitle("Modifier le client");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        clientRepository = new ClientRepository();

        // Initialiser les vues
        editNom = findViewById(R.id.editNom);
        editPrenom = findViewById(R.id.editPrenom);
        editTelephone = findViewById(R.id.editTelephone);
        editAdresse = findViewById(R.id.editAdresse);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);

        // Vérifier que les vues existent
        if (editNom == null || editPrenom == null || editTelephone == null) {
            Toast.makeText(this, "Erreur : Layout incorrect", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Une des vues est null");
            finish();
            return;
        }

        // Pré-remplir les champs
        editNom.setText(nom != null ? nom : "");
        editPrenom.setText(prenom != null ? prenom : "");
        editTelephone.setText(telephone != null ? telephone : "");

        if (adresse != null) {
            editAdresse.setText(adresse);
        } else {
            editAdresse.setText("");
        }

        // Bouton Enregistrer
        btnSave.setOnClickListener(v -> updateClient());

        // Bouton Annuler
        btnCancel.setOnClickListener(v -> finish());
    }

    private void updateClient() {
        String nom = editNom.getText().toString().trim();
        String prenom = editPrenom.getText().toString().trim();
        String telephone = editTelephone.getText().toString().trim();
        String adresse = editAdresse.getText().toString().trim();

        Log.d(TAG, "Validation des données...");

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

        if (telephone.isEmpty()) {
            editTelephone.setError("Le téléphone est requis");
            editTelephone.requestFocus();
            return;
        }

        // Validation du téléphone
        if (telephone.length() < 8) {
            editTelephone.setError("Numéro de téléphone invalide");
            editTelephone.requestFocus();
            return;
        }

        Log.d(TAG, "Création de l'objet Client...");

        // Créer l'objet Client avec les nouvelles valeurs
        Client client = new Client();
        client.setId(clientId);
        client.setNom(nom);
        client.setPrenom(prenom);
        client.setTelephone(telephone);
        client.setAdresse(adresse.isEmpty() ? null : adresse);

        Log.d(TAG, "Client créé: " + client.getNom() + " " + client.getPrenom());

        // Afficher le loader
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        btnCancel.setEnabled(false);

        Log.d(TAG, "Appel à updateClient dans le repository...");

        // Mettre à jour dans Supabase
        clientRepository.updateClient(client, new ClientRepository.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "Succès: " + message);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditClientActivity.this,
                            "✅ Client modifié avec succès",
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Erreur updateClient: " + error);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    btnCancel.setEnabled(true);
                    Toast.makeText(EditClientActivity.this,
                            "❌ Erreur : " + error,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clientRepository != null) {
            clientRepository.shutdown();
        }
    }
}