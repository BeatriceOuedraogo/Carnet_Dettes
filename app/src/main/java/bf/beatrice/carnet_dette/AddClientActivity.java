package bf.beatrice.carnet_dette;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import bf.beatrice.carnet_dette.models.Client;
import bf.beatrice.carnet_dette.repository.ClientRepository;

public class AddClientActivity extends AppCompatActivity {

    private TextInputEditText editNom, editPrenom, editTelephone, editAdresse;
    private Button btnSave, btnCancel;
    private ProgressBar progressBar;

    private ClientRepository clientRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_client);



        // Titre de l'écran
        setTitle("Ajouter un client");

        // Afficher le bouton retour
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

        // Bouton Enregistrer
        btnSave.setOnClickListener(v -> saveClient());

        // Bouton Annuler
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveClient() {
        // Récupérer les valeurs
        String nom = editNom.getText().toString().trim();
        String prenom = editPrenom.getText().toString().trim();
        String telephone = editTelephone.getText().toString().trim();
        String adresse = editAdresse.getText().toString().trim();

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

        // Validation du numéro (optionnelle)
        if (telephone.length() < 8) {
            editTelephone.setError("Numéro de téléphone invalide");
            editTelephone.requestFocus();
            return;
        }

        // Créer l'objet Client
        Client client = new Client(nom, prenom, telephone, adresse);

        // Afficher le loader
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        btnCancel.setEnabled(false);

        // Enregistrer dans Supabase
        clientRepository.addClient(client, new ClientRepository.SingleClientCallback() {
            @Override
            public void onSuccess(Client newClient) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddClientActivity.this,
                        "Client ajouté avec succès !",
                        Toast.LENGTH_SHORT).show();

                // Retourner à l'écran précédent
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                btnCancel.setEnabled(true);

                Toast.makeText(AddClientActivity.this,
                        "Erreur : " + error,
                        Toast.LENGTH_LONG).show();
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