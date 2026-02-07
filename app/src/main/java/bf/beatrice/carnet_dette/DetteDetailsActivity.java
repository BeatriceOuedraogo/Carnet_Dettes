package bf.beatrice.carnet_dette;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import bf.beatrice.carnet_dette.adapters.PaiementAdapter;
import bf.beatrice.carnet_dette.models.Dette;
import bf.beatrice.carnet_dette.models.Paiement;
import bf.beatrice.carnet_dette.repository.DetteRepository;
import bf.beatrice.carnet_dette.repository.PaiementRepository;

public class DetteDetailsActivity extends AppCompatActivity {

    private TextView txtDescription, txtMontantTotal, txtMontantPaye, txtMontantRestant, txtStatut;
    private RecyclerView recyclerPaiements;
    private PaiementAdapter paiementAdapter;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddPaiement;

    private PaiementRepository paiementRepository;
    private DetteRepository detteRepository;

    private String detteId;
    private String description;
    private double montantTotal;
    private String statut;
    private double totalPaiements = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dette_details);

        // ✅ RÉCUPÉRER LES DONNÉES DE L'INTENT
        Intent intent = getIntent();
        detteId = intent.getStringExtra("DETTE_ID");
        // AJOUTE CETTE LIGNE ICI :
        String clientNomPourLePaiement = intent.getStringExtra("CLIENT_NOM");
        description = intent.getStringExtra("DETTE_DESCRIPTION");
        montantTotal = intent.getDoubleExtra("DETTE_MONTANT", 0);
        statut = intent.getStringExtra("DETTE_STATUT");


        if (detteId == null) {
            Toast.makeText(this, "Erreur : Dette introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bouton retour
        ImageButton btnRetour = findViewById(R.id.btnRetour);
        btnRetour.setOnClickListener(v -> finish());

        // Titre
        setTitle("Détails de la dette");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        paiementRepository = new PaiementRepository();
        detteRepository = new DetteRepository();

        // Initialiser les vues
        txtDescription = findViewById(R.id.txtDescription);
        txtMontantTotal = findViewById(R.id.txtMontantTotal);
        txtMontantPaye = findViewById(R.id.txtMontantPaye);
        txtMontantRestant = findViewById(R.id.txtMontantRestant);
        txtStatut = findViewById(R.id.txtStatut);
        recyclerPaiements = findViewById(R.id.recyclerPaiements);
        progressBar = findViewById(R.id.progressBar);
        fabAddPaiement = findViewById(R.id.fabAddPaiement);

        // Afficher les infos de la dette
        NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);
        txtDescription.setText(description);
        txtMontantTotal.setText("Montant total : " + formatter.format(montantTotal) + " FCFA");
        txtStatut.setText(statut);

        // ✅ CONFIGURER LE RECYCLERVIEW AVEC LES 3 ACTIONS
        recyclerPaiements.setLayoutManager(new LinearLayoutManager(this));
        paiementAdapter = new PaiementAdapter(new PaiementAdapter.OnPaiementActionListener() {
            @Override
            public void onPaiementClick(Paiement paiement) {
                NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);
                Toast.makeText(DetteDetailsActivity.this,
                        "Paiement : " + formatter.format(paiement.getMontant()) + " FCFA\n" +
                                "Mode : " + paiement.getMode_paiement() + "\n" +
                                "Date : " + paiement.getDate_paiement(),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onModifierClick(Paiement paiement) {
                // Ouvrir l'activité de modification
                Intent intent = new Intent(DetteDetailsActivity.this, EditPaiementActivity.class);
                intent.putExtra("PAIEMENT_ID", paiement.getId());
                intent.putExtra("PAIEMENT_MONTANT", paiement.getMontant());
                intent.putExtra("PAIEMENT_MODE", paiement.getMode_paiement());
                intent.putExtra("PAIEMENT_DATE", paiement.getDate_paiement());
                intent.putExtra("DETTE_ID", detteId);
                startActivity(intent);
            }
            // AJOUTE CETTE MÉTHODE POUR SUPPRIMER L'ERREUR ROUGE
            @Override
            public void onVoirDetteClick(Paiement paiement) {
                // Puisque nous sommes DÉJÀ dans DetteDetailsActivity,
                // on ne relance pas l'activité. On informe juste l'utilisateur.
                Toast.makeText(DetteDetailsActivity.this, "Vous visualisez déjà cette dette.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSupprimerClick(Paiement paiement) {
                confirmerSuppression(paiement);
            }

        });
        recyclerPaiements.setAdapter(paiementAdapter);

        //BOUTON + POUR AJOUTER UN PAIEMENT
        if (fabAddPaiement != null) {
            fabAddPaiement.setOnClickListener(v -> {
                // Vérifier qu'il reste quelque chose à payer
                double montantRestant = montantTotal - totalPaiements;

                if (montantRestant <= 0) {
                    Toast.makeText(this, "Cette dette est déjà entièrement payée", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent addPaiementIntent = new Intent(DetteDetailsActivity.this, AddPaiementActivity.class);
                addPaiementIntent.putExtra("DETTE_ID", detteId);
                addPaiementIntent.putExtra("DETTE_DESCRIPTION", description);
                addPaiementIntent.putExtra("MONTANT_RESTANT", montantRestant);
                // On récupère le nom du client qui était dans l'Intent de départ
                String nomClient = getIntent().getStringExtra("CLIENT_NOM");
                addPaiementIntent.putExtra("CLIENT_NOM", clientNomPourLePaiement);
                startActivity(addPaiementIntent);
            });
        } else {
            Toast.makeText(this, "ERREUR: Bouton + non trouvé dans le layout", Toast.LENGTH_LONG).show();
        }

        // Charger les paiements
        loadPaiements();
    }

    private void loadPaiements() {
        progressBar.setVisibility(View.VISIBLE);

        paiementRepository.getPaiementsByDetteId(detteId, new PaiementRepository.PaiementCallback() {
            @Override
            public void onSuccess(List<Paiement> paiements) {
                paiementAdapter.setPaiements(paiements);

                // Calculer le total des paiements
                totalPaiements = 0;
                for (Paiement paiement : paiements) {
                    totalPaiements += paiement.getMontant();
                }

                // Charger les détails de la dette
                loadDetteDetails();

                if (paiements.isEmpty()) {
                    Toast.makeText(DetteDetailsActivity.this,
                            "Aucun paiement pour cette dette",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DetteDetailsActivity.this,
                        "Erreur : " + error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadDetteDetails() {
        detteRepository.getDetteById(detteId, new DetteRepository.SingleDetteCallback() {
            @Override
            public void onSuccess(Dette dette) {
                progressBar.setVisibility(View.GONE);

                description = dette.getDescription();
                montantTotal = dette.getMontant();

                NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);
                txtDescription.setText(description);
                txtMontantTotal.setText("Montant total : " + formatter.format(montantTotal) + " FCFA");
                txtMontantPaye.setText("Déjà payé : " + formatter.format(totalPaiements) + " FCFA");

                // Calcul du montant restant
                double montantRestant = montantTotal - totalPaiements;
                txtMontantRestant.setText("Reste à payer : " + formatter.format(montantRestant) + " FCFA");

                // Mise à jour du statut
                if (montantRestant <= 0) {
                    txtStatut.setText("PAYÉE");
                    txtStatut.setTextColor(getColor(android.R.color.holo_green_dark));
                    txtStatut.setBackgroundColor(getColor(android.R.color.holo_green_light));
                    updateDetteStatut("PAYEE");
                } else {
                    txtStatut.setText("EN COURS");
                    txtStatut.setTextColor(getColor(android.R.color.holo_orange_dark));
                    txtStatut.setBackgroundColor(getColor(android.R.color.transparent));
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Log.e("DetteDetails", "Erreur: " + error);
            }
        });
    }

    private void updateDetteStatut(String nouveauStatut) {
        Dette dette = new Dette();
        dette.setId(detteId);
        dette.setDescription(description);
        dette.setMontant(montantTotal);
        dette.setStatut(nouveauStatut);

        detteRepository.updateDette(dette, new DetteRepository.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                // Statut mis à jour
            }

            @Override
            public void onError(String error) {
                // Erreur silencieuse
            }
        });
    }

    private void confirmerSuppression(Paiement paiement) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);
        new AlertDialog.Builder(this)
                .setTitle("Supprimer ce paiement ?")
                .setMessage("Montant : " + formatter.format(paiement.getMontant()) + " FCFA\n" +
                        "Mode : " + paiement.getMode_paiement() + "\n" +
                        "Date : " + paiement.getDate_paiement())
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    supprimerPaiement(paiement);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void supprimerPaiement(Paiement paiement) {
        progressBar.setVisibility(View.VISIBLE);

        paiementRepository.deletePaiement(paiement.getId(), new PaiementRepository.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DetteDetailsActivity.this,
                        "Paiement supprimé avec succès",
                        Toast.LENGTH_SHORT).show();
                loadPaiements(); // Recharger
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DetteDetailsActivity.this,
                        "Erreur : " + error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        totalPaiements = 0;
        loadPaiements();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "🔄 Actualiser");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            totalPaiements = 0;
            loadPaiements();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //if (paiementRepository != null) {
           // paiementRepository.shutdown();
       // }
        //if (detteRepository != null) {
            //detteRepository.shutdown();
        //}
    }
}