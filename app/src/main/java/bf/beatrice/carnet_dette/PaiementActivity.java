package bf.beatrice.carnet_dette;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import bf.beatrice.carnet_dette.models.Paiement;
import bf.beatrice.carnet_dette.repository.PaiementRepository;

public class PaiementActivity extends AppCompatActivity {

    private RecyclerView recyclerPaiements;
    private PaiementAdapter paiementAdapter;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddPaiement;
    private TextView txtTitrePaiement, txtNoPaiements;

    private PaiementRepository paiementRepository;

    private String detteId;
    private String detteDescription;
    private double montantRestant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paiement);

        setTitle("Historique des paiements");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        detteId = getIntent().getStringExtra("DETTE_ID");
        detteDescription = getIntent().getStringExtra("DETTE_DESCRIPTION");
        montantRestant = getIntent().getDoubleExtra("MONTANT_RESTANT", 0);

        if (detteId == null) {
            Toast.makeText(this, "Erreur : Dette introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        paiementRepository = new PaiementRepository();

        txtTitrePaiement = findViewById(R.id.txtTitrePaiement);
        recyclerPaiements = findViewById(R.id.recyclerPaiements);
        progressBar = findViewById(R.id.progressBar);
        fabAddPaiement = findViewById(R.id.fabAddPaiement);
        txtNoPaiements = findViewById(R.id.txtNoPaiements);

        if (detteDescription != null) {
            txtTitrePaiement.setText("Paiements pour : " + detteDescription);
        }

        recyclerPaiements.setLayoutManager(new LinearLayoutManager(this));

        // CONFIGURATION DE L'ADAPTER CORRIGÉE
        paiementAdapter = new PaiementAdapter(new PaiementAdapter.OnPaiementActionListener() {
            @Override
            public void onPaiementClick(Paiement paiement) {
                NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);
                Toast.makeText(PaiementActivity.this,
                        "Détails : " + formatter.format(paiement.getMontant()) + " FCFA",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onModifierClick(Paiement paiement) {
                // SOLUTION SANS SERIALIZABLE : On passe les primitives
                Intent intent = new Intent(PaiementActivity.this, EditPaiementActivity.class);
                intent.putExtra("PAIEMENT_ID", paiement.getId());
                intent.putExtra("PAIEMENT_MONTANT", paiement.getMontant());
                intent.putExtra("PAIEMENT_MODE", paiement.getMode_paiement());
                intent.putExtra("PAIEMENT_DATE", paiement.getDate_paiement());
                startActivity(intent);
            }


            @Override
            public void onSupprimerClick(Paiement paiement) {
                confirmerSuppression(paiement);
            }
            @Override
            public void onVoirDetteClick(Paiement paiement) {
                // On redirige vers les détails de la dette
                Intent intent = new Intent(PaiementActivity.this, DetteDetailsActivity.class);
                intent.putExtra("DETTE_ID", paiement.getDette_id());
                startActivity(intent);
            }
        });

        recyclerPaiements.setAdapter(paiementAdapter);

        fabAddPaiement.setOnClickListener(v -> {
            Intent intent = new Intent(PaiementActivity.this, AddPaiementActivity.class);
            intent.putExtra("DETTE_ID", detteId);
            intent.putExtra("DETTE_DESCRIPTION", detteDescription);
            intent.putExtra("MONTANT_RESTANT", montantRestant);
            startActivity(intent);
        });

        loadPaiements();
    }


    // ... (Le reste de tes méthodes loadPaiements, confirmerSuppression, etc. reste identique)

    private void loadPaiements() {
        progressBar.setVisibility(View.VISIBLE);
        if (txtNoPaiements != null) txtNoPaiements.setVisibility(View.GONE);

        paiementRepository.getPaiementsByDetteId(detteId, new PaiementRepository.PaiementCallback() {
            @Override
            public void onSuccess(List<Paiement> paiements) {
                progressBar.setVisibility(View.GONE);
                paiementAdapter.setPaiements(paiements);
                if (paiements.isEmpty() && txtNoPaiements != null) {
                    txtNoPaiements.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PaiementActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmerSuppression(Paiement paiement) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer ce paiement ?")
                .setMessage("Montant : " + paiement.getMontant() + " FCFA")
                .setPositiveButton("Supprimer", (dialog, which) -> supprimerPaiement(paiement))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void supprimerPaiement(Paiement paiement) {
        progressBar.setVisibility(View.VISIBLE);
        paiementRepository.deletePaiement(paiement.getId(), new PaiementRepository.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PaiementActivity.this, "Supprimé", Toast.LENGTH_SHORT).show();
                loadPaiements();
            }
            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PaiementActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPaiements();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}