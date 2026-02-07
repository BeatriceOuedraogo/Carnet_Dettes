package bf.beatrice.carnet_dette;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
import bf.beatrice.carnet_dette.repository.ClientRepository;
import bf.beatrice.carnet_dette.repository.DetteRepository;

public class AddDetteActivity extends AppCompatActivity {

    private TextView txtClientNom;
    private Spinner spinnerClients;
    private TextInputEditText editDescription, editMontant, editDate;
    private Button btnSave, btnCancel;
    private ProgressBar progressBar;

    private DetteRepository detteRepository;
    private ClientRepository clientRepository;
    private List<Client> listClients = new ArrayList<>();

    private String clientId, clientNom;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dette);

        // Initialisation des Repositories
        detteRepository = new DetteRepository();
        clientRepository = new ClientRepository();
        selectedDate = Calendar.getInstance();

        // Récupération des données de l'Intent (si on vient d'un client précis)
        clientId = getIntent().getStringExtra("CLIENT_ID");
        clientNom = getIntent().getStringExtra("CLIENT_NOM");

        setTitle("Ajouter une dette");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        loadClients();
    }

    private void initViews() {
        txtClientNom = findViewById(R.id.txtClientNom);
        spinnerClients = findViewById(R.id.spinnerClients);
        editDescription = findViewById(R.id.editDescription);
        editMontant = findViewById(R.id.editMontant);
        editDate = findViewById(R.id.editDate);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);

        // Date par défaut (Aujourd'hui)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
        editDate.setText(sdf.format(selectedDate.getTime()));

        editDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveDette());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadClients() {
        progressBar.setVisibility(View.VISIBLE);
        clientRepository.getAllClients(new ClientRepository.ClientCallback() {
            @Override
            public void onSuccess(List<Client> clients) {
                listClients = clients;
                List<String> displayList = new ArrayList<>();
                int selectedIndex = 0;

                for (int i = 0; i < clients.size(); i++) {
                    Client c = clients.get(i);
                    displayList.add(c.getNom() + " " + c.getPrenom());

                    // Si un ID a été passé en paramètre, on le cherche
                    if (clientId != null && c.getId().equals(clientId)) {
                        selectedIndex = i;
                    }
                }

                int finalSelectedIndex = selectedIndex;
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AddDetteActivity.this,
                            android.R.layout.simple_spinner_item, displayList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerClients.setAdapter(adapter);

                    if (!clients.isEmpty()) {
                        spinnerClients.setSelection(finalSelectedIndex);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddDetteActivity.this,
                            "Erreur chargement clients",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void saveDette() {
        if (listClients.isEmpty()) {
            Toast.makeText(this, "Aucun client disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        // Récupérer le client sélectionné dans le Spinner
        Client selectedClient = listClients.get(spinnerClients.getSelectedItemPosition());
        clientId = selectedClient.getId();
        clientNom = selectedClient.getNom() + " " + selectedClient.getPrenom();

        String description = editDescription.getText().toString().trim();
        String montantStr = editMontant.getText().toString().trim();
        String date = editDate.getText().toString().trim();

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

        setLoading(true);

        // Vérification si une dette EN_COURS existe déjà
        detteRepository.getDettesByClientId(clientId, new DetteRepository.DetteCallback() {
            @Override
            public void onSuccess(List<Dette> dettes) {
                Dette detteExistante = null;
                for (Dette d : dettes) {
                    if (!"PAYEE".equals(d.getStatut())) {
                        detteExistante = d;
                        break;
                    }
                }

                if (detteExistante != null) {
                    setLoading(false);
                    showAlreadyExistsDialog(detteExistante, montant, description);
                } else {
                    createNewDette(description, montant, date);
                }
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                Toast.makeText(AddDetteActivity.this,
                        "Erreur : " + error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showAlreadyExistsDialog(Dette existante, double nouveauMontant, String nouvelleDesc) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);

        new AlertDialog.Builder(this)
                .setTitle("⚠️ Dette en cours")
                .setMessage(clientNom + " a déjà une dette de " +
                        formatter.format(existante.getMontant()) + " FCFA" +
                        "\n\nDescription : " + existante.getDescription() +
                        "\n\nVoulez-vous l'augmenter ?")
                .setPositiveButton("Augmenter", (d, w) ->
                        augmenterDette(existante, nouveauMontant, nouvelleDesc))
                .setNegativeButton("Annuler", (d, w) -> setLoading(false))
                .setNeutralButton("Voir", (d, w) -> {
                    Intent intent = new Intent(this, DetteDetailsActivity.class);
                    intent.putExtra("DETTE_ID", existante.getId());
                    intent.putExtra("DETTE_DESCRIPTION", existante.getDescription());
                    intent.putExtra("DETTE_MONTANT", existante.getMontant());
                    intent.putExtra("DETTE_STATUT", existante.getStatut());
                    startActivity(intent);
                    setLoading(false);
                })
                .show();
    }

    private void createNewDette(String description, double montant, String date) {
        Dette dette = new Dette(clientId,clientNom ,description, montant);
        if (!date.isEmpty()) {
            dette.setDate_dette(date);
        }

        detteRepository.addDette(dette, new DetteRepository.SingleDetteCallback() {
            @Override
            public void onSuccess(Dette newDette) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(AddDetteActivity.this,
                            "✅ Dette créée avec succès !",
                            Toast.LENGTH_SHORT).show();

                    // ✅ REDIRECTION VERS LA LISTE DES DETTES DU CLIENT
                    Intent intent = new Intent(AddDetteActivity.this, ClientDetailsActivity.class);
                    intent.putExtra("CLIENT_ID", clientId);
                    intent.putExtra("CLIENT_NOM", clientNom.split(" ")[0]); // Nom
                    intent.putExtra("CLIENT_PRENOM", clientNom.contains(" ") ?
                            clientNom.substring(clientNom.indexOf(" ") + 1) : "");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(AddDetteActivity.this,
                            "❌ Erreur : " + error,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void augmenterDette(Dette existante, double montantPlus, String descPlus) {
        setLoading(true);
        existante.setMontant(existante.getMontant() + montantPlus);
        existante.setDescription(existante.getDescription() + " + " + descPlus);

        detteRepository.updateDette(existante, new DetteRepository.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(AddDetteActivity.this,
                            "✅ Dette mise à jour avec succès !",
                            Toast.LENGTH_SHORT).show();

                    // ✅ REDIRECTION VERS LA LISTE DES DETTES DU CLIENT
                    Intent intent = new Intent(AddDetteActivity.this, ClientDetailsActivity.class);
                    intent.putExtra("CLIENT_ID", clientId);
                    intent.putExtra("CLIENT_NOM", clientNom.split(" ")[0]);
                    intent.putExtra("CLIENT_PRENOM", clientNom.contains(" ") ?
                            clientNom.substring(clientNom.indexOf(" ") + 1) : "");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(AddDetteActivity.this,
                            "❌ Erreur : " + error,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
            editDate.setText(sdf.format(selectedDate.getTime()));
        },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!isLoading);
        btnCancel.setEnabled(!isLoading);
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
        if (clientRepository != null) {
            clientRepository.shutdown();
        }
    }
}