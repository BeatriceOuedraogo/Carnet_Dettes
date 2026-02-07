package bf.beatrice.carnet_dette;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import bf.beatrice.carnet_dette.models.Dette;
import bf.beatrice.carnet_dette.repository.DetteRepository;

public class EditDetteActivity extends AppCompatActivity {

    private TextInputEditText editDescription, editMontant;
    private Button btnSave, btnCancel;
    private ProgressBar progressBar;

    private DetteRepository detteRepository;
    private String detteId;
    private String clientId;
    private String clientNom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_dette);

        // Récupérer les données
        detteId = getIntent().getStringExtra("DETTE_ID");
        String description = getIntent().getStringExtra("DETTE_DESCRIPTION");
        double montant = getIntent().getDoubleExtra("DETTE_MONTANT", 0);
        clientId = getIntent().getStringExtra("CLIENT_ID");
        clientNom = getIntent().getStringExtra("CLIENT_NOM");

        if (detteId == null) {
            Toast.makeText(this, "Erreur : Dette introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle("Modifier la dette");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        detteRepository = new DetteRepository();

        // Initialiser les vues
        editDescription = findViewById(R.id.editDescription);
        editMontant = findViewById(R.id.editMontant);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);

        // Pré-remplir les champs
        editDescription.setText(description);
        editMontant.setText(String.valueOf(montant));

        // Bouton Enregistrer
        btnSave.setOnClickListener(v -> updateDette());

        // Bouton Annuler
        btnCancel.setOnClickListener(v -> finish());
    }

    private void updateDette() {
        String description = editDescription.getText().toString().trim();
        String montantStr = editMontant.getText().toString().trim();

        // Validation
        if (description.isEmpty()) {
            editDescription.setError("La description est requise");
            editDescription.requestFocus();
            return;
        }

        if (montantStr.isEmpty()) {
            editMontant.setError("Le montant est requis");
            editMontant.requestFocus();
            return;
        }

        double montant;
        try {
            montant = Double.parseDouble(montantStr.replace(",", "."));
            if (montant <= 0) {
                editMontant.setError("Le montant doit être supérieur à 0");
                editMontant.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            editMontant.setError("Montant invalide");
            editMontant.requestFocus();
            return;
        }

        // Créer l'objet Dette avec les nouvelles valeurs
        Dette dette = new Dette();
        dette.setId(detteId);
        dette.setClient_id(clientId);
        dette.setClient_nom(clientNom);
        dette.setDescription(description);
        dette.setMontant(montant);
        // Garder le statut existant
        dette.setStatut(getIntent().getStringExtra("DETTE_STATUT"));

        // Afficher le loader
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        btnCancel.setEnabled(false);

        // Mettre à jour dans Supabase
        detteRepository.updateDette(dette, new DetteRepository.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditDetteActivity.this,
                            "✅ Dette modifiée avec succès",
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    btnCancel.setEnabled(true);
                    Toast.makeText(EditDetteActivity.this,
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
        if (detteRepository != null) {
            detteRepository.shutdown();
        }
    }
}