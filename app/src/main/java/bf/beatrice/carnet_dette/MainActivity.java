package bf.beatrice.carnet_dette;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import bf.beatrice.carnet_dette.adapters.ClientDetailAdapter;
import bf.beatrice.carnet_dette.models.Client;
import bf.beatrice.carnet_dette.repository.ClientRepository;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerClients;
    private ClientDetailAdapter clientAdapter;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddClient;
    private TextInputEditText editSearch;
    private TextView txtNoClients; // Texte "Aucun client"

    private ClientRepository clientRepository;
    private SharedPreferences sharedPreferences;
    private List<Client> allClientsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser les préférences partagées
        sharedPreferences = getSharedPreferences("CarnetDettePrefs", MODE_PRIVATE);

        // Initialiser le repository
        clientRepository = new ClientRepository();

        // Initialiser les vues
        initViews();

        // Configurer la Toolbar et le menu latéral
        setupToolbarAndDrawer();

        // Charger les clients
        loadClients();
    }

    private void initViews() {
        // Trouver toutes les vues
        recyclerClients = findViewById(R.id.recyclerClients);
        progressBar = findViewById(R.id.progressBar);
        fabAddClient = findViewById(R.id.fabAddClient);
        editSearch = findViewById(R.id.editSearch);

        // Trouver le TextView pour "Aucun client" (s'il existe dans le layout)
        txtNoClients = findViewById(R.id.txtNoClients);

        // Si le TextView n'existe pas, créer un message d'erreur
        if (txtNoClients == null) {
            Toast.makeText(this, "TextView txtNoClients non trouvé dans le layout", Toast.LENGTH_SHORT).show();
        }

        // Configurer le RecyclerView
        recyclerClients.setLayoutManager(new LinearLayoutManager(this));

        // Configurer l'adapter pour les clients
        clientAdapter = new ClientDetailAdapter(new ClientDetailAdapter.OnClientActionListener() {
            @Override
            public void onVoirClick(Client client) {
                openClientDetails(client);
            }

            @Override
            public void onModifierClick(Client client) {
                openEditClient(client);
            }

            @Override
            public void onSupprimerClick(Client client) {
                confirmDelete(client);
            }

            @Override
            public void onClientClick(Client client) {
                openClientDetails(client);
            }
        });

        recyclerClients.setAdapter(clientAdapter);

        // Configurer la barre de recherche
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterClients(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Configurer le bouton Ajouter
        fabAddClient.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddClientActivity.class);
            startActivity(intent);
        });
    }

    private void setupToolbarAndDrawer() {
        // Configurer la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("📋 Liste des clients");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // ← IMPORTANT
        }


        // Initialiser le DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        // Configurer le NavigationView
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Configurer le clic sur un élément du header pour ouvrir le menu
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            headerView.setOnClickListener(v -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }
    }

    private void loadClients() {
        // Afficher le loader
        progressBar.setVisibility(View.VISIBLE);

        // Cacher le message "Aucun client"
        if (txtNoClients != null) {
            txtNoClients.setVisibility(View.GONE);
        }

        // Charger les clients depuis Supabase
        clientRepository.getAllClients(new ClientRepository.ClientCallback() {
            @Override
            public void onSuccess(List<Client> clients) {
                progressBar.setVisibility(View.GONE);
                allClientsList = clients;
                clientAdapter.setClients(clients);

                if (clients.isEmpty()) {
                    // Afficher le message "Aucun client"
                    if (txtNoClients != null) {
                        txtNoClients.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(MainActivity.this,
                            "Aucun client pour le moment",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Cacher le message "Aucun client"
                    if (txtNoClients != null) {
                        txtNoClients.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this,
                        "Erreur: " + error,
                        Toast.LENGTH_LONG).show();

                // Afficher le message d'erreur
                if (txtNoClients != null) {
                    txtNoClients.setText("Erreur de chargement");
                    txtNoClients.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void filterClients(String query) {
        if (query.isEmpty()) {
            clientAdapter.setClients(allClientsList);
            // Afficher/cacher le message "Aucun client"
            if (txtNoClients != null) {
                txtNoClients.setVisibility(allClientsList.isEmpty() ? View.VISIBLE : View.GONE);
            }
            return;
        }

        List<Client> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Client client : allClientsList) {
            String nom = client.getNom() != null ? client.getNom().toLowerCase() : "";
            String prenom = client.getPrenom() != null ? client.getPrenom().toLowerCase() : "";
            String telephone = client.getTelephone() != null ? client.getTelephone() : "";

            if (nom.contains(lowerQuery) ||
                    prenom.contains(lowerQuery) ||
                    telephone.contains(query)) {
                filteredList.add(client);
            }
        }

        clientAdapter.setClients(filteredList);

        // Afficher/cacher le message "Aucun client" pour les résultats filtrés
        if (txtNoClients != null) {
            txtNoClients.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void openClientDetails(Client client) {
        Intent intent = new Intent(MainActivity.this, ClientDetailsActivity.class);
        intent.putExtra("CLIENT_ID", client.getId());
        intent.putExtra("CLIENT_NOM", client.getNom());
        intent.putExtra("CLIENT_PRENOM", client.getPrenom());
        intent.putExtra("CLIENT_TELEPHONE", client.getTelephone());
        startActivity(intent);
    }

    private void openEditClient(Client client) {
        Intent intent = new Intent(MainActivity.this, EditClientActivity.class);
        intent.putExtra("CLIENT_ID", client.getId());
        intent.putExtra("CLIENT_NOM", client.getNom());
        intent.putExtra("CLIENT_PRENOM", client.getPrenom());
        intent.putExtra("CLIENT_TELEPHONE", client.getTelephone());
        intent.putExtra("CLIENT_ADRESSE", client.getAdresse());
        startActivity(intent);
    }

    private void confirmDelete(Client client) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer le client")
                .setMessage("Voulez-vous vraiment supprimer " + client.getNom() + " " + client.getPrenom() + " ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    clientRepository.deleteClient(client.getId(), new ClientRepository.ActionCallback() {
                        @Override
                        public void onSuccess(String message) {
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "✅ Client supprimé", Toast.LENGTH_SHORT).show();
                                loadClients(); // Recharger la liste
                            });
                        }
                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "❌ Erreur: " + error, Toast.LENGTH_LONG).show());
                        }
                    });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            startActivity(new Intent(this, DashboardActivity.class));
        } else if (id == R.id.nav_clients) {
            // On est déjà sur la liste des clients
            Toast.makeText(this, "Vous êtes déjà sur la liste des clients", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_clients_endettes) {
            startActivity(new Intent(this, ClientsEndettesActivity.class));
        } else if (id == R.id.nav_all_dettes) {
            startActivity(new Intent(this, AllDettesActivity.class));
        } else if (id == R.id.nav_add_dette) {
            startActivity(new Intent(this, AddDetteActivity.class));
        } else if (id == R.id.nav_dettes_en_cours) {
            Intent intent = new Intent(this, AllDettesActivity.class);
            intent.putExtra("FILTER_STATUT", "EN_COURS");
            startActivity(intent);
        } else if (id == R.id.nav_dettes_payees) {
            Intent intent = new Intent(this, AllDettesActivity.class);
            intent.putExtra("FILTER_STATUT", "PAYEE");
            startActivity(intent);
        } else if (id == R.id.nav_all_paiements || id == R.id.nav_historique) {
            startActivity(new Intent(this, ListPaiementActivity.class));
        } else if (id == R.id.nav_add_paiement_menu) {
            startActivity(new Intent(this, AddPaiementActivity.class));
        } else if (id == R.id.nav_profile) {
            String userName = sharedPreferences.getString("userName", "Utilisateur");
            Toast.makeText(this, "Profil de : " + userName, Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            logout();
        }

        // Fermer le menu après avoir sélectionné un item
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void logout() {
        // Effacer les données de session
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();

        // Rediriger vers l'écran de connexion
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les clients quand on revient sur l'écran
        loadClients();
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        // Fermer le menu latéral s'il est ouvert
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Demander confirmation pour quitter l'application
            new AlertDialog.Builder(this)
                    .setTitle("Quitter l'application")
                    .setMessage("Voulez-vous vraiment quitter ?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        // Fermer toutes les activités et quitter
                        finishAffinity();
                    })
                    .setNegativeButton("Non", null)
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clientRepository != null) {
            clientRepository.shutdown();
        }
    }
}