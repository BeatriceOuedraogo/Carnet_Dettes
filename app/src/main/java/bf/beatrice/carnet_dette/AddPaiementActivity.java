package bf.beatrice.carnet_dette;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import bf.beatrice.carnet_dette.models.Client;
import bf.beatrice.carnet_dette.models.Dette;
import bf.beatrice.carnet_dette.models.Paiement;
import bf.beatrice.carnet_dette.repository.ClientRepository;
import bf.beatrice.carnet_dette.repository.DetteRepository;
import bf.beatrice.carnet_dette.repository.PaiementRepository;

public class AddPaiementActivity extends AppCompatActivity {

    private TextView txtDetteInfo, txtMontantRestant;
    private TextInputEditText editMontant, editDate;
    private RadioGroup radioGroupMode;
    private Button btnSave, btnCancel;
    private ProgressBar progressBar;
    private Spinner spinnerDettes;

    private PaiementRepository paiementRepository;
    private DetteRepository detteRepository;
    private ClientRepository clientRepository;

    private List<Dette> listDettes = new ArrayList<>();
    private List<Client> listClients = new ArrayList<>();

    private String selectedDetteId;
    private String detteDescription = "";
    private String clientNom = "Client inconnu";
    private double montantRestant;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_paiement);

        // Titre
        setTitle("Enregistrer un paiement");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialiser les vues
        initViews();

        // Initialiser les repositories
        paiementRepository = new PaiementRepository();
        detteRepository = new DetteRepository();
        clientRepository = new ClientRepository();
        calendar = Calendar.getInstance();

        // Configurer les listeners
        setupListeners();

        // Gérer les données
        String intentDetteId = getIntent().getStringExtra("DETTE_ID");

        if (intentDetteId != null) {
            // Mode Direct depuis une fiche dette
            spinnerDettes.setVisibility(View.GONE);
            View lbl = findViewById(R.id.lblSelectDette);
            if (lbl != null) lbl.setVisibility(View.GONE);

            selectedDetteId = intentDetteId;
            detteDescription = getIntent().getStringExtra("DETTE_DESCRIPTION");
            montantRestant = getIntent().getDoubleExtra("MONTANT_RESTANT", 0);

            // ✅ CHARGER LE NOM DU CLIENT DEPUIS LA DETTE
            loadClientNameFromDette(selectedDetteId);

            txtDetteInfo.setText("Pour : " + (detteDescription != null ? detteDescription : "Dette"));
            updateMontantDisplay();
        } else {
            // Mode Menu : Charger toutes les dettes
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                    this::loadDataForSpinner, 300
            );
        }
    }

    private void initViews() {
        spinnerDettes = findViewById(R.id.spinnerDettes);
        txtDetteInfo = findViewById(R.id.txtDetteInfo);
        txtMontantRestant = findViewById(R.id.txtMontantRestant);
        editMontant = findViewById(R.id.editMontant);
        editDate = findViewById(R.id.editDate);
        radioGroupMode = findViewById(R.id.radioGroupMode);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        editDate.setOnClickListener(v -> showDatePicker());
        editDate.setFocusable(false);
        editDate.setClickable(true);

        btnSave.setOnClickListener(v -> savePaiement());
        btnCancel.setOnClickListener(v -> {
            Toast.makeText(this, "Annulation", Toast.LENGTH_SHORT).show();
            finish();
        });

        spinnerDettes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!listDettes.isEmpty() && position >= 0 && position < listDettes.size()) {
                    Dette dette = listDettes.get(position);
                    selectedDetteId = dette.getId();
                    detteDescription = dette.getDescription();
                    montantRestant = dette.getMontant();

                    // ✅ RÉCUPÉRER LE NOM DU CLIENT DEPUIS LA LISTE
                    for (Client client : listClients) {
                        if (client.getId().equals(dette.getClient_id())) {
                            clientNom = client.getNom() + " " + client.getPrenom();
                            break;
                        }
                    }

                    updateMontantDisplay();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ✅ NOUVELLE MÉTHODE POUR CHARGER LE NOM DU CLIENT
    private void loadClientNameFromDette(String detteId) {
        detteRepository.getDetteById(detteId, new DetteRepository.SingleDetteCallback() {
            @Override
            public void onSuccess(Dette dette) {
                String clientId = dette.getClient_id();

                // Charger le client
                clientRepository.getClientById(clientId, new ClientRepository.SingleClientCallback() {
                    @Override
                    public void onSuccess(Client client) {
                        clientNom = client.getNom() + " " + client.getPrenom();
                    }

                    @Override
                    public void onError(String error) {
                        clientNom = "Client inconnu";
                    }
                });
            }

            @Override
            public void onError(String error) {
                clientNom = "Client inconnu";
            }
        });
    }

    private void loadDataForSpinner() {
        progressBar.setVisibility(View.VISIBLE);

        clientRepository.getAllClients(new ClientRepository.ClientCallback() {
            @Override
            public void onSuccess(List<Client> clients) {
                listClients = clients;
                loadDettes();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddPaiementActivity.this,
                        "Erreur clients: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDettes() {
        detteRepository.getAllDettes(new DetteRepository.DetteCallback() {
            @Override
            public void onSuccess(List<Dette> dettes) {
                progressBar.setVisibility(View.GONE);
                listDettes.clear();
                List<String> displayList = new ArrayList<>();

                for (Dette dette : dettes) {
                    // Filtrer seulement les dettes NON payées
                    if (!"PAYEE".equals(dette.getStatut())) {
                        listDettes.add(dette);

                        // Trouver le nom du client
                        String nomClient = "Client inconnu";
                        for (Client client : listClients) {
                            if (client.getId().equals(dette.getClient_id())) {
                                nomClient = client.getNom() + " " + client.getPrenom();
                                break;
                            }
                        }

                        NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);
                        displayList.add(nomClient + " - " + dette.getDescription() +
                                " (Reste: " + formatter.format(dette.getMontant()) + " FCFA)");
                    }
                }

                if (displayList.isEmpty()) {
                    Toast.makeText(AddPaiementActivity.this,
                            "Aucune dette active",
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        AddPaiementActivity.this,
                        android.R.layout.simple_spinner_item,
                        displayList
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDettes.setAdapter(adapter);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddPaiementActivity.this,
                        "Erreur dettes: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMontantDisplay() {
        NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);
        txtMontantRestant.setText("Reste à payer : " +
                formatter.format(montantRestant) + " FCFA");
    }

    private void showDatePicker() {
        DatePickerDialog dp = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    calendar.set(year, month, day);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    editDate.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dp.show();
    }

    private void savePaiement() {
        // Validation du montant
        String montantStr = editMontant.getText().toString().trim();
        if (montantStr.isEmpty()) {
            editMontant.setError("Le montant est requis");
            editMontant.requestFocus();
            return;
        }

        if (selectedDetteId == null) {
            Toast.makeText(this, "Veuillez sélectionner une dette", Toast.LENGTH_SHORT).show();
            return;
        }

        double montantSaisi;
        try {
            montantSaisi = Double.parseDouble(montantStr);
        } catch (NumberFormatException e) {
            editMontant.setError("Montant invalide");
            editMontant.requestFocus();
            return;
        }

        if (montantSaisi <= 0) {
            editMontant.setError("Le montant doit être supérieur à 0");
            editMontant.requestFocus();
            return;
        }

        // ✅ VÉRIFIER QUE LE MONTANT NE DÉPASSE PAS LE RESTE
        if (montantSaisi > montantRestant) {
            NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);
            editMontant.setError("Le montant ne peut pas dépasser le reste à payer (" +
                    formatter.format(montantRestant) + " FCFA)");
            editMontant.requestFocus();
            return;
        }

        // ✅ RÉCUPÉRER LE MODE DE PAIEMENT
        int selectedModeId = radioGroupMode.getCheckedRadioButtonId();
        if (selectedModeId == -1) {
            Toast.makeText(this, "Veuillez choisir un mode de paiement", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadio = findViewById(selectedModeId);
        String modePaiement = selectedRadio.getText().toString();

        // Convertir en format attendu
        if (modePaiement.contains("Espèces")) {
            modePaiement = "ESPECES";
        } else if (modePaiement.contains("Mobile Money")) {
            modePaiement = "MOBILE_MONEY";
        } else if (modePaiement.contains("Virement")) {
            modePaiement = "VIREMENT";
        }

        // Date par défaut si non renseignée
        String datePaiement = editDate.getText().toString().trim();
        if (datePaiement.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            datePaiement = sdf.format(Calendar.getInstance().getTime());
        }

        // Afficher le loader
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        btnCancel.setEnabled(false);

        // ✅ CRÉER LE PAIEMENT AVEC LE NOM DU CLIENT CHARGÉ
        Paiement paiement = new Paiement(selectedDetteId, clientNom, montantSaisi, modePaiement);
        paiement.setDate_paiement(datePaiement);

        // ✅ ENREGISTRER LE PAIEMENT
        paiementRepository.addPaiement(paiement, new PaiementRepository.SinglePaiementCallback() {
            @Override
            public void onSuccess(Paiement newPaiement) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddPaiementActivity.this,
                        "✅ Paiement enregistré avec succès !",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AddPaiementActivity.this, ListPaiementActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();

            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                btnCancel.setEnabled(true);
                Toast.makeText(AddPaiementActivity.this,
                        "❌ Erreur : " + error,
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
        if (detteRepository != null) {
            detteRepository.shutdown();
        }
        if (clientRepository != null) {
             clientRepository.shutdown();
        }
    }
}