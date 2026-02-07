package bf.beatrice.carnet_dette;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.List;

import bf.beatrice.carnet_dette.adapters.DetteAdapter;
import bf.beatrice.carnet_dette.adapters.DetteDetailAdapter;
import bf.beatrice.carnet_dette.models.Dette;
import bf.beatrice.carnet_dette.repository.DetteRepository;

public class DetteListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView txtEmpty;
    private String clientId, clientNom;
    private DetteRepository detteRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dette_list);

        // Bouton retour
        ImageButton btnRetour = findViewById(R.id.btnRetour);
        if (btnRetour != null) {
            btnRetour.setOnClickListener(v -> finish());
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        clientId = getIntent().getStringExtra("CLIENT_ID");
        clientNom = getIntent().getStringExtra("CLIENT_NOM");

        clientNom = getIntent().getStringExtra("CLIENT_NOM");
        if (clientNom == null || clientNom.equalsIgnoreCase("null")) {
            clientNom = ""; // On force une chaîne vide plutôt qu'un texte "null"
        }


        initViews();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewDettes);
        progressBar = findViewById(R.id.progressBar);
        txtEmpty = findViewById(R.id.txtEmpty);
        FloatingActionButton fab = findViewById(R.id.fabAddDette);

        detteRepository = new DetteRepository();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadDettes();

        if (fab != null) {
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddDetteActivity.class);
                intent.putExtra("CLIENT_ID", clientId);
                intent.putExtra("CLIENT_NOM", clientNom);
                startActivity(intent);
            });
        }
    }

    private void loadDettes() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        detteRepository.getDettesByClientId(clientId, new DetteRepository.DetteCallback() {
            @Override
            public void onSuccess(List<Dette> dettes) {
                runOnUiThread(() -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    if (dettes == null || dettes.isEmpty()) {
                        if (txtEmpty != null) {
                            txtEmpty.setVisibility(View.VISIBLE);
                        }
                        recyclerView.setAdapter(null);
                    } else {
                        if (txtEmpty != null) {
                            txtEmpty.setVisibility(View.GONE);
                        }
                        setupAdapter(dettes);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Toast.makeText(DetteListActivity.this,
                            "Erreur : " + error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    private void setupAdapter(List<Dette> dettes) {
        // ✅ TEST 1 : Vérifier le nom
        android.util.Log.e("TEST", "========================================");
        android.util.Log.e("TEST", "NOM CLIENT = " + clientNom);
        android.util.Log.e("TEST", "NOMBRE DE DETTES = " + dettes.size());
        android.util.Log.e("TEST", "========================================");

        // ✅ TEST 2 : Afficher un Toast pour voir si cette méthode est appelée
        Toast.makeText(this, "Nom client : " + clientNom, Toast.LENGTH_LONG).show();

        // ✅ Adapter simple pour tester
        DetteDetailAdapter adapter = new DetteDetailAdapter(clientNom, new DetteDetailAdapter.OnDetteActionListener() {
            @Override
            public void onVoirClick(Dette dette) {
                android.util.Log.e("TEST", "BOUTON VOIR CLIQUÉ !");
                Toast.makeText(DetteListActivity.this, "Voir : " + dette.getDescription(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onModifierClick(Dette dette) {
                android.util.Log.e("TEST", "BOUTON MODIFIER CLIQUÉ !");
                Toast.makeText(DetteListActivity.this, "Modifier : " + dette.getDescription(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSupprimerClick(Dette dette) {
                android.util.Log.e("TEST", "BOUTON SUPPRIMER CLIQUÉ !");
                Toast.makeText(DetteListActivity.this, "Supprimer : " + dette.getDescription(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDetteClick(Dette dette) {
                android.util.Log.e("TEST", "CARTE CLIQUÉE !");
                Toast.makeText(DetteListActivity.this, "Dette : " + dette.getDescription(), Toast.LENGTH_SHORT).show();
            }
        });

        adapter.setDettes(dettes);
        recyclerView.setAdapter(adapter);
    }
    private void confirmDelete(Dette dette) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Suppression")
                .setMessage("Supprimer cette dette ?\n\n" +
                        "Description : " + dette.getDescription())
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    detteRepository.deleteDette(dette.getId(), new DetteRepository.ActionCallback() {
                        @Override
                        public void onSuccess(String message) {
                            runOnUiThread(() -> {
                                Toast.makeText(DetteListActivity.this,
                                        "✅ Dette supprimée",
                                        Toast.LENGTH_SHORT).show();
                                loadDettes();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                if (progressBar != null) {
                                    progressBar.setVisibility(View.GONE);
                                }
                                Toast.makeText(DetteListActivity.this,
                                        "❌ Erreur : " + error,
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDettes();
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
    }
}