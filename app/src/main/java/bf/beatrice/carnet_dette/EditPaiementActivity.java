package bf.beatrice.carnet_dette;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import bf.beatrice.carnet_dette.models.Paiement;
import bf.beatrice.carnet_dette.repository.PaiementRepository;

public class EditPaiementActivity extends AppCompatActivity {

    private TextView txtDetteInfo;
    private TextInputEditText editMontant, editDate;
    private RadioGroup radioGroupMode;
    private Button btnSave, btnCancel;
    private ProgressBar progressBar;

    private PaiementRepository paiementRepository;
    private Paiement paiement;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_paiement); // Réutilise le même layout

        // Titre
        setTitle("Modifier le paiement");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Récupérer le paiement à modifier
        String paiementId = getIntent().getStringExtra("PAIEMENT_ID");
        double montant = getIntent().getDoubleExtra("PAIEMENT_MONTANT", 0);
        String mode = getIntent().getStringExtra("PAIEMENT_MODE");
        String date = getIntent().getStringExtra("PAIEMENT_DATE");
        String detteId = getIntent().getStringExtra("DETTE_ID");

        if (paiementId == null) {
            Toast.makeText(this, "Erreur : Paiement introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Créer l'objet paiement
        paiement = new Paiement();
        paiement.setId(paiementId);
        paiement.setDette_id(detteId);
        paiement.setMontant(montant);
        paiement.setMode_paiement(mode);
        paiement.setDate_paiement(date);

        paiementRepository = new PaiementRepository();
        calendar = Calendar.getInstance();

        // Initialiser les vues
        txtDetteInfo = findViewById(R.id.txtDetteInfo);
        editMontant = findViewById(R.id.editMontant);
        editDate = findViewById(R.id.editDate);
        radioGroupMode = findViewById(R.id.radioGroupMode);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);

        // Cacher le TextView montantRestant
        TextView txtMontantRestant = findViewById(R.id.txtMontantRestant);
        if (txtMontantRestant != null) {
            txtMontantRestant.setVisibility(View.GONE);
        }

        // Pré-remplir les champs
        txtDetteInfo.setText("Modification du paiement");
        editMontant.setText(String.valueOf(montant));
        editDate.setText(date);

        // Sélectionner le mode de paiement
        if ("ESPECES".equals(mode)) {
            ((RadioButton) findViewById(R.id.radioEspeces)).setChecked(true);
        } else if ("MOBILE_MONEY".equals(mode)) {
            ((RadioButton) findViewById(R.id.radioMobileMoney)).setChecked(true);
        } else if ("VIREMENT".equals(mode)) {
            ((RadioButton) findViewById(R.id.radioVirement)).setChecked(true);
        }

        // DatePicker
        editDate.setOnClickListener(v -> showDatePicker());
        editDate.setFocusable(false);
        editDate.setClickable(true);

        // Bouton Enregistrer
        btnSave.setText("💾 Mettre à jour");
        btnSave.setOnClickListener(v -> updatePaiement());

        // Bouton Annuler
        btnCancel.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    editDate.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updatePaiement() {
        // Récupérer les nouvelles valeurs
        String montantStr = editMontant.getText().toString().trim();
        String datePaiement = editDate.getText().toString().trim();

        // Validation
        if (montantStr.isEmpty()) {
            editMontant.setError("Le montant est requis");
            editMontant.requestFocus();
            return;
        }

        double montant;
        try {
            montant = Double.parseDouble(montantStr);
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

        // Mode de paiement
        int selectedModeId = radioGroupMode.getCheckedRadioButtonId();
        if (selectedModeId == -1) {
            Toast.makeText(this, "Veuillez choisir un mode de paiement", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadio = findViewById(selectedModeId);
        String modePaiement = selectedRadio.getText().toString();

        if (modePaiement.contains("Espèces")) {
            modePaiement = "ESPECES";
        } else if (modePaiement.contains("Mobile Money")) {
            modePaiement = "MOBILE_MONEY";
        } else if (modePaiement.contains("Virement")) {
            modePaiement = "VIREMENT";
        }

        // Mettre à jour l'objet
        paiement.setMontant(montant);
        paiement.setMode_paiement(modePaiement);
        paiement.setDate_paiement(datePaiement);

        // Afficher le loader
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        btnCancel.setEnabled(false);

        // Mettre à jour dans Supabase
        paiementRepository.updatePaiement(paiement, new PaiementRepository.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditPaiementActivity.this,
                        "Paiement modifié avec succès !",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                btnCancel.setEnabled(true);
                Toast.makeText(EditPaiementActivity.this,
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
        if (paiementRepository != null) {
            paiementRepository.shutdown();
        }
    }
}