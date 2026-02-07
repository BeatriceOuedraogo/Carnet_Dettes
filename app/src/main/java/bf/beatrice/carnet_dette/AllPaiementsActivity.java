package bf.beatrice.carnet_dette;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import bf.beatrice.carnet_dette.adapters.PaiementAdapter;
import bf.beatrice.carnet_dette.models.Client;
import bf.beatrice.carnet_dette.models.Dette;
import bf.beatrice.carnet_dette.models.Paiement;
import bf.beatrice.carnet_dette.repository.ClientRepository;
import bf.beatrice.carnet_dette.repository.DetteRepository;
import bf.beatrice.carnet_dette.repository.PaiementRepository;

public class AllPaiementsActivity extends AppCompatActivity {

    private RecyclerView recyclerPaiements;
    private PaiementAdapter adapter;
    private ProgressBar progressBar;

    private ClientRepository clientRepository;
    private DetteRepository detteRepository;
    private PaiementRepository paiementRepository;

    private List<Paiement> allPaiements = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_paiements);

        // 1. Initialisation des vues
        recyclerPaiements = findViewById(R.id.recyclerPaiements);
        progressBar = findViewById(R.id.progressBar);

        if (recyclerPaiements == null) {
            Toast.makeText(this, "Erreur: RecyclerView introuvable", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 2. Configuration du titre et retour
        setTitle("Tous les paiements");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 3. Initialisation des Repositories
        clientRepository = new ClientRepository();
        detteRepository = new DetteRepository();
        paiementRepository = new PaiementRepository();

        // 4. Configuration de l'Adapter
        adapter = new PaiementAdapter(new PaiementAdapter.OnPaiementActionListener() {
            @Override
            public void onPaiementClick(Paiement paiement) {
                // Détails ou autre action
            }

            @Override
            public void onModifierClick(Paiement paiement) {
                // Logique de modification ici
                Toast.makeText(AllPaiementsActivity.this, "Modifier : " + paiement.getMontant(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSupprimerClick(Paiement paiement) {
                confirmerSuppression(paiement);
            }
            @Override
            public void onVoirDetteClick(Paiement paiement) {
                Intent intent = new Intent(AllPaiementsActivity.this, DetteDetailsActivity.class);
                intent.putExtra("DETTE_ID", paiement.getDette_id());
                startActivity(intent);
            }

        });

        // 5. Lier l'adapter au RecyclerView (C'ÉTAIT MANQUANT !)
        recyclerPaiements.setLayoutManager(new LinearLayoutManager(this));
        recyclerPaiements.setAdapter(adapter);

        // 6. Charger les données
        loadAllPaiements();
    } // FIN DE ONCREATE

    private void loadAllPaiements() {
        progressBar.setVisibility(View.VISIBLE);
        allPaiements.clear(); // On vide la liste avant de charger

        clientRepository.getAllClients(new ClientRepository.ClientCallback() {
            @Override
            public void onSuccess(List<Client> clients) {
                loadDettesForClients(clients);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AllPaiementsActivity.this, "Erreur : " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDettesForClients(List<Client> clients) {
        final int[] clientsProcessed = {0};
        final int totalClients = clients.size();
        final List<Dette> allDettes = new ArrayList<>();

        if (totalClients == 0) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        for (Client client : clients) {
            detteRepository.getDettesByClientId(client.getId(), new DetteRepository.DetteCallback() {
                @Override
                public void onSuccess(List<Dette> dettes) {
                    allDettes.addAll(dettes);
                    clientsProcessed[0]++;
                    if (clientsProcessed[0] == totalClients) {
                        loadPaiementsForDettes(allDettes);
                    }
                }

                @Override
                public void onError(String error) {
                    clientsProcessed[0]++;
                    if (clientsProcessed[0] == totalClients) {
                        loadPaiementsForDettes(allDettes);
                    }
                }
            });
        }
    }

    private void loadPaiementsForDettes(List<Dette> dettes) {
        final int[] dettesProcessed = {0};
        final int totalDettes = dettes.size();

        if (totalDettes == 0) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        for (Dette dette : dettes) {
            paiementRepository.getPaiementsByDetteId(dette.getId(), new PaiementRepository.PaiementCallback() {
                @Override
                public void onSuccess(List<Paiement> paiements) {
                    allPaiements.addAll(paiements);
                    dettesProcessed[0]++;
                    if (dettesProcessed[0] == totalDettes) {
                        displayPaiements();
                    }
                }

                @Override
                public void onError(String error) {
                    dettesProcessed[0]++;
                    if (dettesProcessed[0] == totalDettes) {
                        displayPaiements();
                    }
                }
            });
        }
    }

    private void displayPaiements() {
        progressBar.setVisibility(View.GONE);
        adapter.setPaiements(allPaiements);
        if (allPaiements.isEmpty()) {
            Toast.makeText(this, "Aucun paiement enregistré", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmerSuppression(Paiement paiement) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer")
                .setMessage("Voulez-vous supprimer ce paiement de " + paiement.getMontant() + " FCFA ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    // Ici, appelle ton repository pour supprimer
                    Toast.makeText(this, "Suppression en cours...", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Optionnel : ne pas shutdown ici si tu as des erreurs de RejectedExecutionException
    }
}