package bf.beatrice.carnet_dette;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import bf.beatrice.carnet_dette.models.Client;
import bf.beatrice.carnet_dette.models.Dette;
import bf.beatrice.carnet_dette.repository.ClientRepository;
import bf.beatrice.carnet_dette.repository.DetteRepository;

public class AddDetteRapideActivity extends AppCompatActivity {

    private Spinner spinnerClient;
    private TextInputEditText editDescription, editMontant;
    private Button btnSave, btnCancel;
    private ProgressBar progressBar;

    private ClientRepository clientRepository;
    private DetteRepository detteRepository;

    private List<Client> clients = new ArrayList<>();
    private Client selectedClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dette_rapide);

        setTitle("Ajouter une dette");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        clientRepository = new ClientRepository();
        detteRepository = new DetteRepository();

        // Initialiser les vues
        spinnerClient = findViewById(R.id.spinnerClient);
        editDescription = findViewById(R.id.editDescription);
        editMontant = findViewById(R.id.editMontant);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);

        // Bouton Annuler
        btnCancel.setOnClickListener(v -> finish());

        // Bouton Enregistrer
        btnSave.setOnClickListener(v -> saveDette());

        // Charger la liste des clients
        loadClients();
    }

    private void loadClients() {
        progressBar.setVisibility(View.VISIBLE);

        clientRepository.getAllClients(new ClientRepository.ClientCallback() {
            @Override
            public void onSuccess(List<Client> clientList) {
                progressBar.setVisibility(View.GONE);
                clients = clientList;

                if (clients.isEmpty()) {
                    Toast.makeText(AddDetteRapideActivity.this,
                            "Aucun client. Créez d'abord un client.",
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                // Préparer la liste pour le Spinner
                List<String> clientNames = new ArrayList<>();
                clientNames.add("-- Choisir un client --");
                for (Client client : clients) {
                    clientNames.add(client.getNom() + " " + client.getPrenom());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        AddDetteRapideActivity.this,
                        android.R.layout.simple_spinner_item,
                        clientNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerClient.setAdapter(adapter);

                spinnerClient.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position > 0) {
                            selectedClient = clients.get(position - 1);
                        } else {
                            selectedClient = null;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedClient = null;
                    }
                });
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddDetteRapideActivity.this,
                        "Erreur : " + error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveDette() {
        // Validation
        if (selectedClient == null) {
            Toast.makeText(this, "Veuillez choisir un client", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = editDescription.getText().toString().trim();
        String montantStr = editMontant.getText().toString().trim();

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

        // Créer l'objet Dette
        Dette dette = new Dette(selectedClient.getId(), selectedClient.getNom(), description, montant);


        // Afficher le loader
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        btnCancel.setEnabled(false);

        // Enregistrer dans Supabase
        detteRepository.addDette(dette, new DetteRepository.SingleDetteCallback() {
            @Override
            public void onSuccess(Dette newDette) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddDetteRapideActivity.this,
                        "Dette ajoutée avec succès !",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                btnCancel.setEnabled(true);
                Toast.makeText(AddDetteRapideActivity.this,
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
        if (detteRepository != null) {
            detteRepository.shutdown();
        }
    }
}