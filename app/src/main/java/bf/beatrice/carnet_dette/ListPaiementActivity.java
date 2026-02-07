package bf.beatrice.carnet_dette;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import bf.beatrice.carnet_dette.adapters.PaiementDetailAdapter;
import bf.beatrice.carnet_dette.models.Client;
import bf.beatrice.carnet_dette.models.Dette;
import bf.beatrice.carnet_dette.models.Paiement;
import bf.beatrice.carnet_dette.repository.ClientRepository;
import bf.beatrice.carnet_dette.repository.DetteRepository;
import bf.beatrice.carnet_dette.repository.PaiementRepository;

public class ListPaiementActivity extends AppCompatActivity {

    private RecyclerView recyclerPaiements;
    private PaiementDetailAdapter paiementAdapter;
    private ProgressBar progressBar;
    private TextView txtNoPaiements, txtTotalPaiements, txtNbPaiements;
    private ImageButton btnRetour;

    private PaiementRepository paiementRepository;
    private DetteRepository detteRepository;
    private ClientRepository clientRepository;

    private List<PaiementDetail> paiementDetails = new ArrayList<>();
    private Map<String, Dette> dettesMap = new HashMap<>();
    private Map<String, Client> clientsMap = new HashMap<>();

    // Classe interne pour stocker les détails complets
    public static class PaiementDetail {
        public Paiement paiement;
        public String clientNom;
        public String detteDescription;

        public PaiementDetail(Paiement paiement, String clientNom, String detteDescription) {
            this.paiement = paiement;
            this.clientNom = clientNom;
            this.detteDescription = detteDescription;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_paiement);

        // Titre
        setTitle("Tous les paiements");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        paiementRepository = new PaiementRepository();
        detteRepository = new DetteRepository();
        clientRepository = new ClientRepository();

        // Initialiser les vues
        btnRetour = findViewById(R.id.btnRetour);
        recyclerPaiements = findViewById(R.id.recyclerPaiements);
        progressBar = findViewById(R.id.progressBar);
        txtNoPaiements = findViewById(R.id.txtNoPaiements);
        txtTotalPaiements = findViewById(R.id.TotalPaiements);
        txtNbPaiements = findViewById(R.id.NbPaiements);

        // Bouton retour
        if (btnRetour != null) {
            btnRetour.setOnClickListener(v -> finish());
        }

        // Configurer le RecyclerView
        recyclerPaiements.setLayoutManager(new LinearLayoutManager(this));
        paiementAdapter = new PaiementDetailAdapter(new PaiementDetailAdapter.OnPaiementActionListener() {
            @Override
            public void onPaiementClick(PaiementDetail detail) {
                afficherDetailsPaiement(detail);
            }

            @Override
            public void onModifierClick(PaiementDetail detail) {
                Intent intent = new Intent(ListPaiementActivity.this, EditPaiementActivity.class);
                intent.putExtra("PAIEMENT_ID", detail.paiement.getId());
                intent.putExtra("PAIEMENT_MONTANT", detail.paiement.getMontant());
                intent.putExtra("PAIEMENT_MODE", detail.paiement.getMode_paiement());
                intent.putExtra("PAIEMENT_DATE", detail.paiement.getDate_paiement());
                intent.putExtra("DETTE_ID", detail.paiement.getDette_id());
                startActivity(intent);
            }

            @Override
            public void onSupprimerClick(PaiementDetail detail) {
                confirmerSuppression(detail);
            }

            @Override
            public void onVoirDetteClick(PaiementDetail detail) {
                Dette dette = dettesMap.get(detail.paiement.getDette_id());
                if (dette != null) {
                    Intent intent = new Intent(ListPaiementActivity.this, DetteDetailsActivity.class);
                    intent.putExtra("DETTE_ID", dette.getId());
                    intent.putExtra("DETTE_DESCRIPTION", dette.getDescription());
                    intent.putExtra("DETTE_MONTANT", dette.getMontant());
                    intent.putExtra("DETTE_STATUT", dette.getStatut());
                    startActivity(intent);
                }
            }
        });
        recyclerPaiements.setAdapter(paiementAdapter);

        loadAllData();
    }

    private void loadAllData() {
        progressBar.setVisibility(View.VISIBLE);
        txtNoPaiements.setVisibility(View.GONE);
        paiementDetails.clear();
        dettesMap.clear();
        clientsMap.clear();

        // Étape 1 : Charger tous les clients
        clientRepository.getAllClients(new ClientRepository.ClientCallback() {
            @Override
            public void onSuccess(List<Client> clients) {
                // Stocker les clients dans une map
                for (Client client : clients) {
                    clientsMap.put(client.getId(), client);
                }
                // Étape 2 : Charger toutes les dettes
                loadAllDettes();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ListPaiementActivity.this,
                        "Erreur chargement clients : " + error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadAllDettes() {
        final List<String> clientIds = new ArrayList<>(clientsMap.keySet());
        final int[] clientsProcessed = {0};

        if (clientIds.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            txtNoPaiements.setVisibility(View.VISIBLE);
            return;
        }

        for (String clientId : clientIds) {
            detteRepository.getDettesByClientId(clientId, new DetteRepository.DetteCallback() {
                @Override
                public void onSuccess(List<Dette> dettes) {
                    // Stocker les dettes dans une map
                    for (Dette dette : dettes) {
                        dettesMap.put(dette.getId(), dette);
                    }
                    clientsProcessed[0]++;

                    if (clientsProcessed[0] == clientIds.size()) {
                        // Étape 3 : Charger tous les paiements
                        loadAllPaiements();
                    }
                }

                @Override
                public void onError(String error) {
                    clientsProcessed[0]++;
                    if (clientsProcessed[0] == clientIds.size()) {
                        loadAllPaiements();
                    }
                }
            });
        }
    }

    private void loadAllPaiements() {
        paiementRepository.getAllPaiements(new PaiementRepository.PaiementCallback() {
            @Override
            public void onSuccess(List<Paiement> paiements) {
                for (Paiement paiement : paiements) {
                    // Récupérer la dette et le client
                    Dette dette = dettesMap.get(paiement.getDette_id());
                    if (dette != null) {
                        Client client = clientsMap.get(dette.getClient_id());
                        String clientNom = client != null ?
                                (client.getNom() + " " + client.getPrenom()) :
                                "Client inconnu";

                        // ✅ Créer un PaiementDetail avec les bonnes infos
                        paiementDetails.add(new PaiementDetail(
                                paiement,
                                clientNom,
                                dette.getDescription()
                        ));
                    }
                }
                runOnUiThread(() -> displayPaiements());
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ListPaiementActivity.this,
                            "Erreur : " + error,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void displayPaiements() {
        progressBar.setVisibility(View.GONE);

        if (paiementDetails.isEmpty()) {
            txtNoPaiements.setVisibility(View.VISIBLE);
            txtNbPaiements.setText("0 paiement");
            txtTotalPaiements.setText("Total : 0 FCFA");
            return;
        }

        // Afficher les paiements
        paiementAdapter.setPaiements(paiementDetails);

        // Calculer les statistiques
        double totalMontant = 0;
        for (PaiementDetail detail : paiementDetails) {
            totalMontant += detail.paiement.getMontant();
        }

        NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);
        txtNbPaiements.setText(paiementDetails.size() + " paiement" +
                (paiementDetails.size() > 1 ? "s" : ""));
        txtTotalPaiements.setText("Total : " + formatter.format(totalMontant) + " FCFA");
    }

    private void afficherDetailsPaiement(PaiementDetail detail) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);

        new AlertDialog.Builder(this)
                .setTitle("📋 Détails du paiement")
                .setMessage(
                        "👤 Client : " + detail.clientNom + "\n\n" +
                                "📄 Dette : " + detail.detteDescription + "\n\n" +
                                "💰 Montant : " + formatter.format(detail.paiement.getMontant()) + " FCFA\n\n" +
                                "📱 Mode : " + formatModePaiement(detail.paiement.getMode_paiement()) + "\n\n" +
                                "📅 Date : " + detail.paiement.getDate_paiement()
                )
                .setPositiveButton("OK", null)
                .setNeutralButton("Voir la dette", (dialog, which) -> {
                    Dette dette = dettesMap.get(detail.paiement.getDette_id());
                    if (dette != null) {
                        Intent intent = new Intent(ListPaiementActivity.this, DetteDetailsActivity.class);
                        intent.putExtra("DETTE_ID", dette.getId());
                        intent.putExtra("DETTE_DESCRIPTION", dette.getDescription());
                        intent.putExtra("DETTE_MONTANT", dette.getMontant());
                        intent.putExtra("DETTE_STATUT", dette.getStatut());
                        startActivity(intent);
                    }
                })
                .show();
    }

    private String formatModePaiement(String mode) {
        if ("ESPECES".equals(mode)) {
            return "💵 Espèces";
        } else if ("MOBILE_MONEY".equals(mode)) {
            return "📱 Mobile Money";
        } else if ("VIREMENT".equals(mode)) {
            return "🏦 Virement";
        }
        return mode;
    }

    private void confirmerSuppression(PaiementDetail detail) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);

        new AlertDialog.Builder(this)
                .setTitle("⚠️ Supprimer ce paiement ?")
                .setMessage("Client : " + detail.clientNom + "\n" +
                        "Dette : " + detail.detteDescription + "\n" +
                        "Montant : " + formatter.format(detail.paiement.getMontant()) + " FCFA")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    supprimerPaiement(detail);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void supprimerPaiement(PaiementDetail detail) {
        progressBar.setVisibility(View.VISIBLE);

        paiementRepository.deletePaiement(detail.paiement.getId(), new PaiementRepository.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ListPaiementActivity.this,
                        "✅ Paiement supprimé avec succès",
                        Toast.LENGTH_SHORT).show();

                // Recharger la liste
                paiementDetails.clear();
                dettesMap.clear();
                clientsMap.clear();
                loadAllData();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ListPaiementActivity.this,
                        "❌ Erreur : " + error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger quand on revient sur l'écran
        paiementDetails.clear();
        dettesMap.clear();
        clientsMap.clear();
        loadAllData();
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