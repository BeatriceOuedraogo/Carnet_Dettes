package bf.beatrice.carnet_dette;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import bf.beatrice.carnet_dette.adapters.ClientAdapter;
import bf.beatrice.carnet_dette.models.Client;
import bf.beatrice.carnet_dette.repository.ClientRepository;

public class ClientListActivity extends AppCompatActivity {

    private RecyclerView recyclerClients;
    private ProgressBar progressBar;
    private TextView txtEmpty;
    private ClientRepository clientRepository;
    private List<Client> clients = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_list);

        // Configurer la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Activer le bouton retour (flèche système apparaîtra automatiquement)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialiser les vues
        initViews();

        // Charger les clients
        loadClients();
    }

    private void initViews() {
        recyclerClients = findViewById(R.id.recyclerClients);
        progressBar = findViewById(R.id.progressBar);
        txtEmpty = findViewById(R.id.txtEmpty);
        FloatingActionButton fabAddClient = findViewById(R.id.fabAddClient);

        clientRepository = new ClientRepository();
        recyclerClients.setLayoutManager(new LinearLayoutManager(this));

        // Configurer le bouton Ajouter
        fabAddClient.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddClientActivity.class);
            startActivity(intent);
        });
    }

    private void loadClients() {
        progressBar.setVisibility(View.VISIBLE);
        txtEmpty.setVisibility(View.GONE);

        clientRepository.getAllClients(new ClientRepository.ClientCallback() {
            @Override
            public void onSuccess(List<Client> clientList) {
                progressBar.setVisibility(View.GONE);
                clients = clientList;

                if (clients.isEmpty()) {
                    txtEmpty.setVisibility(View.VISIBLE);
                    recyclerClients.setAdapter(null);
                } else {
                    txtEmpty.setVisibility(View.GONE);

                    // Créer et configurer l'adapter
                    ClientAdapter adapter = new ClientAdapter(client -> {
                        // Ouvrir les détails du client
                        Intent intent = new Intent(ClientListActivity.this, ClientDetailsActivity.class);
                        intent.putExtra("CLIENT_ID", client.getId());
                        intent.putExtra("CLIENT_NOM", client.getNom());
                        intent.putExtra("CLIENT_PRENOM", client.getPrenom());
                        intent.putExtra("CLIENT_TELEPHONE", client.getTelephone());
                        startActivity(intent);
                    });

                    adapter.setClients(clients);
                    recyclerClients.setAdapter(adapter);
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                txtEmpty.setText("Erreur de chargement");
                txtEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(ClientListActivity.this, "Erreur : " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClients();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Gérer le clic sur le bouton retour de la Toolbar
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clientRepository != null) {
            clientRepository.shutdown();
        }
    }
}