package bf.beatrice.carnet_dette;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import bf.beatrice.carnet_dette.adapters.DetteAdapter;
import bf.beatrice.carnet_dette.models.Client;
import bf.beatrice.carnet_dette.models.Dette;
import bf.beatrice.carnet_dette.repository.ClientRepository;
import bf.beatrice.carnet_dette.repository.DetteRepository;

public class ClientDetailsActivity extends AppCompatActivity {

    private TextView txtClientNom, txtClientTelephone, txtSoldeTotal;
    private RecyclerView recyclerDettes;
    private DetteAdapter detteAdapter;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddDette;
    private Button btnRetour;

    private DetteRepository detteRepository;
    private ClientRepository clientRepository;
    private Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_details);

        // Bouton retour
        btnRetour = findViewById(R.id.btnRetour);
        btnRetour.setOnClickListener(v -> finish());

        // Récupérer le client passé en paramètre
        Intent intent = getIntent();
        String clientId = intent.getStringExtra("CLIENT_ID");
        String clientNom = intent.getStringExtra("CLIENT_NOM");
        String clientPrenom = intent.getStringExtra("CLIENT_PRENOM");
        String clientTelephone = intent.getStringExtra("CLIENT_TELEPHONE");

        if (clientId == null) {
            Toast.makeText(this, "Erreur : Client introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Créer l'objet client
        client = new Client();
        client.setId(clientId);
        client.setNom(clientNom);
        client.setPrenom(clientPrenom);
        client.setTelephone(clientTelephone);

        // Titre et bouton retour
        setTitle("Détails du client");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        detteRepository = new DetteRepository();
        clientRepository = new ClientRepository();

        // Initialiser les vues
        txtClientNom = findViewById(R.id.txtClientNom);
        txtClientTelephone = findViewById(R.id.txtClientTelephone);
        txtSoldeTotal = findViewById(R.id.txtSoldeTotal);
        recyclerDettes = findViewById(R.id.recyclerDettes);
        progressBar = findViewById(R.id.progressBar);
        fabAddDette = findViewById(R.id.fabAddDette);

        // Afficher les infos du client
        txtClientNom.setText(client.getNom() + " " + client.getPrenom());
        txtClientTelephone.setText("📞 " + client.getTelephone());

        // Configurer le RecyclerView
        recyclerDettes.setLayoutManager(new LinearLayoutManager(this));

// ✅ PASSER LE NOM DU CLIENT AU CONSTRUCTEUR
        String nomComplet = client.getNom() + " " + client.getPrenom();
        detteAdapter = new DetteAdapter(nomComplet, dette -> {
            // Quand on clique sur une dette, ouvrir ses détails
            Intent detailIntent = new Intent(ClientDetailsActivity.this, DetteDetailsActivity.class);
            detailIntent.putExtra("DETTE_ID", dette.getId());
            detailIntent.putExtra("DETTE_DESCRIPTION", dette.getDescription());
            detailIntent.putExtra("DETTE_MONTANT", dette.getMontant());
            detailIntent.putExtra("DETTE_STATUT", dette.getStatut());
            startActivity(detailIntent);
        });
        recyclerDettes.setAdapter(detteAdapter);

        // Bouton pour ajouter une dette
        fabAddDette.setOnClickListener(v -> {
            Intent addDetteIntent = new Intent(ClientDetailsActivity.this, AddDetteActivity.class);
            addDetteIntent.putExtra("CLIENT_ID", client.getId());
            addDetteIntent.putExtra("CLIENT_NOM", client.getNom() + " " + client.getPrenom());
            startActivity(addDetteIntent);
        });

        // Charger les dettes
        loadDettes();
    }

    private void loadDettes() {
        progressBar.setVisibility(View.VISIBLE);

        detteRepository.getDettesByClientId(client.getId(), new DetteRepository.DetteCallback() {
            @Override
            public void onSuccess(List<Dette> dettes) {
                progressBar.setVisibility(View.GONE);
                detteAdapter.setDettes(dettes);

                // Calculer le solde total
                double soldeTotal = 0;
                for (Dette dette : dettes) {
                    if (!"PAYEE".equals(dette.getStatut())) {
                        soldeTotal += dette.getMontant();
                    }
                }

                // Afficher le solde
                NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);
                txtSoldeTotal.setText("Solde total : " + formatter.format(soldeTotal) + " FCFA");

                if (dettes.isEmpty()) {
                    Toast.makeText(ClientDetailsActivity.this,
                            "Aucune dette pour ce client",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ClientDetailsActivity.this,
                        "Erreur : " + error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDettes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "🔄 Actualiser");
        menu.add(0, 2, 0, "✏️ Modifier client");
        menu.add(0, 3, 0, "🗑️ Supprimer client");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == 1) {
            loadDettes();
            return true;
        } else if (id == 2) {
            Intent intent = new Intent(this, EditClientActivity.class);
            intent.putExtra("CLIENT_ID", client.getId());
            intent.putExtra("CLIENT_NOM", client.getNom());
            intent.putExtra("CLIENT_PRENOM", client.getPrenom());
            intent.putExtra("CLIENT_TELEPHONE", client.getTelephone());
            intent.putExtra("CLIENT_ADRESSE", client.getAdresse());
            startActivity(intent);
            return true;
        } else if (id == 3) {
            confirmerSuppressionClient();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void confirmerSuppressionClient() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Supprimer ce client ?")
                .setMessage("Toutes ses dettes et paiements seront également supprimés.\n\n" +
                        "Client : " + client.getNom() + " " + client.getPrenom())
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    supprimerClient();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void supprimerClient() {
        progressBar.setVisibility(View.VISIBLE);

        clientRepository.deleteClient(client.getId(), new ClientRepository.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ClientDetailsActivity.this,
                        "✅ Client supprimé avec succès",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ClientDetailsActivity.this,
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
        if (detteRepository != null) {
            detteRepository.shutdown();
        }
        if (clientRepository != null) {
            clientRepository.shutdown();
        }
    }
}