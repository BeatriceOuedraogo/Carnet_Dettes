package bf.beatrice.carnet_dette;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bf.beatrice.carnet_dette.adapters.ClientDettesResumeAdapter;
import bf.beatrice.carnet_dette.models.Client;
import bf.beatrice.carnet_dette.models.Dette;
import bf.beatrice.carnet_dette.repository.ClientRepository;
import bf.beatrice.carnet_dette.repository.DetteRepository;

public class AllDettesActivity extends AppCompatActivity {

    private RecyclerView recyclerClients;
    private ClientDettesResumeAdapter adapter;
    private ProgressBar progressBar;
    private TextView txtNoClients;
    private ImageButton btnRetour;

    private ClientRepository clientRepository;
    private DetteRepository detteRepository;

    private List<Client> clientsEndettes = new ArrayList<>();
    private Map<String, List<Dette>> dettesParClient = new HashMap<>();
    private String filterStatut = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_dettes);

        // Bouton retour
        btnRetour = findViewById(R.id.btnRetour);
        if (btnRetour != null) {
            btnRetour.setOnClickListener(v -> finish());
        }

        filterStatut = getIntent().getStringExtra("FILTER_STATUT");

        if (filterStatut == null) {
            setTitle("Tous les clients endettés");
        } else if (filterStatut.equals("EN_COURS")) {
            setTitle("Clients avec dettes en cours");
        } else if (filterStatut.equals("PAYEE")) {
            setTitle("Clients avec dettes payées");
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        clientRepository = new ClientRepository();
        detteRepository = new DetteRepository();

        recyclerClients = findViewById(R.id.recyclerDettes);
        progressBar = findViewById(R.id.progressBar);
        txtNoClients = findViewById(R.id.txtNoDettes);

        recyclerClients.setLayoutManager(new LinearLayoutManager(this));

        // Utiliser le nouvel adaptateur ClientDettesResumeAdapter
        adapter = new ClientDettesResumeAdapter(new ClientDettesResumeAdapter.OnClientClickListener() {
            @Override
            public void onClientClick(Client client, double totalDettes, int nbDettes) {
                // Ouvrir la liste des dettes de ce client
                openClientDettes(client);
            }
        });

        recyclerClients.setAdapter(adapter);

        loadAllClientsAvecDettes();
    }

    private void openClientDettes(Client client) {
        Intent intent = new Intent(AllDettesActivity.this, DetteListActivity.class);
        intent.putExtra("CLIENT_ID", client.getId());
        intent.putExtra("CLIENT_NOM", client.getNom());
        intent.putExtra("CLIENT_PRENOM", client.getPrenom());
        intent.putExtra("CLIENT_TELEPHONE", client.getTelephone());
        startActivity(intent);
    }

    private void loadAllClientsAvecDettes() {
        progressBar.setVisibility(View.VISIBLE);
        txtNoClients.setVisibility(View.GONE);

        // Charger tous les clients
        clientRepository.getAllClients(new ClientRepository.ClientCallback() {
            @Override
            public void onSuccess(List<Client> clients) {
                // Pour chaque client, charger ses dettes
                loadDettesPourClients(clients);
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AllDettesActivity.this,
                            "Erreur : " + error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadDettesPourClients(List<Client> clients) {
        final int[] clientsProcessed = {0};
        final int totalClients = clients.size();

        dettesParClient.clear();
        clientsEndettes.clear();

        if (totalClients == 0) {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                txtNoClients.setVisibility(View.VISIBLE);
            });
            return;
        }

        for (Client client : clients) {
            detteRepository.getDettesByClientId(client.getId(), new DetteRepository.DetteCallback() {
                @Override
                public void onSuccess(List<Dette> dettes) {
                    // Filtrer par statut si nécessaire
                    List<Dette> dettesFiltrees = new ArrayList<>();

                    if (filterStatut == null) {
                        dettesFiltrees = dettes;
                    } else {
                        for (Dette dette : dettes) {
                            if (filterStatut.equals(dette.getStatut())) {
                                dettesFiltrees.add(dette);
                            }
                        }
                    }

                    // Ajouter seulement si le client a des dettes
                    if (!dettesFiltrees.isEmpty()) {
                        clientsEndettes.add(client);
                        dettesParClient.put(client.getId(), dettesFiltrees);
                    }

                    clientsProcessed[0]++;

                    if (clientsProcessed[0] == totalClients) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            adapter.setData(clientsEndettes, dettesParClient);

                            if (clientsEndettes.isEmpty()) {
                                txtNoClients.setVisibility(View.VISIBLE);
                                Toast.makeText(AllDettesActivity.this,
                                        "Aucun client avec des dettes",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                txtNoClients.setVisibility(View.GONE);
                            }
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    clientsProcessed[0]++;
                    if (clientsProcessed[0] == totalClients) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            adapter.setData(clientsEndettes, dettesParClient);
                        });
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllClientsAvecDettes();
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