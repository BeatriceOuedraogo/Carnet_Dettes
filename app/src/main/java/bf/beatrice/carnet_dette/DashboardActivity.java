package bf.beatrice.carnet_dette;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.navigation.NavigationView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import bf.beatrice.carnet_dette.adapters.ClientDashboardAdapter;
import bf.beatrice.carnet_dette.models.Client;
import bf.beatrice.carnet_dette.models.Dette;
import bf.beatrice.carnet_dette.models.Paiement;
import bf.beatrice.carnet_dette.repository.ClientRepository;
import bf.beatrice.carnet_dette.repository.DetteRepository;
import bf.beatrice.carnet_dette.repository.PaiementRepository;
import androidx.activity.OnBackPressedCallback;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private TextView txtNbClients, txtNbDettes, txtTotalPaye, txtTotalRestant;
    private RecyclerView recyclerTopClients;
    private ClientDashboardAdapter adapter;
    private ProgressBar progressBar;
    private PieChart pieChart;
    private BarChart barChart;

    private ClientRepository clientRepository;
    private DetteRepository detteRepository;
    private PaiementRepository paiementRepository;
    private SharedPreferences sharedPreferences;

    private List<Client> allClients = new ArrayList<>();
    private List<Dette> allDettes = new ArrayList<>();
    private Map<String, Double> clientSoldes = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_dashboard);

            // Toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            // Drawer Layout
            drawerLayout = findViewById(R.id.drawer_layout);
            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            // Hamburger icon
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            // Dans le onCreate de DashboardActivity.java
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    // Si le menu latéral (Drawer) est ouvert, on le ferme
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    } else {
                        // Sinon, on ferme l'activité (ou on quitte l'app)
                        finish();
                    }
                }
            });

            // Afficher le nom de l'utilisateur dans le header
            View headerView = navigationView.getHeaderView(0);
            TextView txtUserName = headerView.findViewById(R.id.txtUserName);

            sharedPreferences = getSharedPreferences("CarnetDettePrefs", MODE_PRIVATE);
            String userName = sharedPreferences.getString("userName", "Utilisateur");
            txtUserName.setText(userName);

            // Initialiser les repositories
            clientRepository = new ClientRepository();
            detteRepository = new DetteRepository();
            paiementRepository = new PaiementRepository();

            // Initialiser les vues
            initViews();

            // Charger les données
            loadDashboardData();

        } catch (Exception e) {
            Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    private void initViews() {
        try {
            txtNbClients = findViewById(R.id.txtNbClients);
            txtNbDettes = findViewById(R.id.txtNbDettes);
            txtTotalPaye = findViewById(R.id.txtTotalPaye);
            txtTotalRestant = findViewById(R.id.txtTotalRestant);
            recyclerTopClients = findViewById(R.id.recyclerTopClients);
            progressBar = findViewById(R.id.progressBar);

            // AJOUTER LES GRAPHIQUES
            pieChart = findViewById(R.id.pieChart);
            barChart = findViewById(R.id.barChart);

            // Navigation : Cliquer sur la carte clients
            findViewById(R.id.cardClients).setOnClickListener(v -> {
                startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            });

            // Configurer le RecyclerView
            recyclerTopClients.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ClientDashboardAdapter(client -> {
                Intent intent = new Intent(DashboardActivity.this, ClientDetailsActivity.class);
                intent.putExtra("CLIENT_ID", client.getId());
                intent.putExtra("CLIENT_NOM", client.getNom());
                intent.putExtra("CLIENT_PRENOM", client.getPrenom());
                intent.putExtra("CLIENT_TELEPHONE", client.getTelephone());
                startActivity(intent);
            });
            recyclerTopClients.setAdapter(adapter);

        } catch (Exception e) {
            Toast.makeText(this, "Erreur initialisation : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            throw e;
        }
    }

    private void loadDashboardData() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Charger tous les clients
        clientRepository.getAllClients(new ClientRepository.ClientCallback() {
            @Override
            public void onSuccess(List<Client> clients) {
                allClients = clients;

                runOnUiThread(() -> {
                    if (txtNbClients != null) {
                        txtNbClients.setText(String.valueOf(clients.size()));
                    }
                });

                // Charger les dettes
                loadAllDettes();
            }

            @Override
            public void onError(String error) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Toast.makeText(DashboardActivity.this, "Erreur : " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllDettes() {
        final int[] clientsProcessed = {0};
        final int totalClients = allClients.size();
        allDettes.clear();
        clientSoldes.clear();

        if (totalClients == 0) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            displayStatistics();
            return;
        }

        for (Client client : allClients) {
            detteRepository.getDettesByClientId(client.getId(), new DetteRepository.DetteCallback() {
                @Override
                public void onSuccess(List<Dette> dettes) {
                    allDettes.addAll(dettes);

                    double soldeClient = 0;
                    for (Dette dette : dettes) {
                        if (!"PAYEE".equals(dette.getStatut())) {
                            soldeClient += dette.getMontant();
                        }
                    }
                    clientSoldes.put(client.getId(), soldeClient);

                    clientsProcessed[0]++;

                    if (clientsProcessed[0] == totalClients) {
                        loadAllPaiements();
                    }
                }

                @Override
                public void onError(String error) {
                    clientsProcessed[0]++;
                    if (clientsProcessed[0] == totalClients) {
                        loadAllPaiements();
                    }
                }
            });
        }
    }

    private void loadAllPaiements() {

        final int[] dettesProcessed = {0};
        final int totalDettes = allDettes.size();
        final double[] totalPaye = {0};

        if (totalDettes == 0) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            displayStatistics();
            return;
        }

        for (Dette dette : allDettes) {
            paiementRepository.getPaiementsByDetteId(dette.getId(), new PaiementRepository.PaiementCallback() {
                @Override
                public void onSuccess(List<Paiement> paiements) {
                    for (Paiement paiement : paiements) {
                        totalPaye[0] += paiement.getMontant();
                    }

                    dettesProcessed[0]++;

                    if (dettesProcessed[0] == totalDettes) {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        displayStatistics(totalPaye[0]);
                    }
                }

                @Override
                public void onError(String error) {
                    dettesProcessed[0]++;
                    if (dettesProcessed[0] == totalDettes) {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        displayStatistics(totalPaye[0]);
                    }
                }
            });
        }
    }

    private void displayStatistics() {
        displayStatistics(0);
    }

    private void displayStatistics(double totalPaye) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);

        // Nombre de dettes
        if (txtNbDettes != null) {
            txtNbDettes.setText(String.valueOf(allDettes.size()));
        }

        // Calculer le total des dettes
        double totalDettes = 0;
        for (Dette dette : allDettes) {
            totalDettes += dette.getMontant();
        }

        // Total payé
        if (txtTotalPaye != null) {
            txtTotalPaye.setText(formatter.format(totalPaye) + " FCFA");
        }

        // Reste à récupérer
        double resteARecuperer = totalDettes - totalPaye;
        if (txtTotalRestant != null) {
            txtTotalRestant.setText(formatter.format(resteARecuperer) + " FCFA");
        }

        // Trier les clients par solde décroissant
        List<Client> topClients = new ArrayList<>(allClients);
        Collections.sort(topClients, new Comparator<Client>() {
            @Override
            public int compare(Client c1, Client c2) {
                Double solde1 = clientSoldes.get(c1.getId());
                Double solde2 = clientSoldes.get(c2.getId());
                if (solde1 == null) solde1 = 0.0;
                if (solde2 == null) solde2 = 0.0;
                return Double.compare(solde2, solde1);
            }
        });

        // Garder seulement les 5 premiers
        if (topClients.size() > 5) {
            topClients = topClients.subList(0, 5);
        }

        // Filtrer les clients avec solde > 0
        List<Client> clientsEndettes = new ArrayList<>();
        for (Client client : topClients) {
            Double solde = clientSoldes.get(client.getId());
            if (solde != null && solde > 0) {
                clientsEndettes.add(client);
            }
        }

        // Afficher dans le RecyclerView
        if (adapter != null) {
            adapter.setData(clientsEndettes, clientSoldes);
        }

        // CRÉER LES GRAPHIQUES ICI
        setupPieChart(totalDettes, totalPaye);
        setupBarChart(clientsEndettes);
    }

    /**
     * Graphique Camembert : Montant payé vs Montant restant
     */
    private void setupPieChart(double totalDettes, double totalPaye) {
        if (pieChart == null) return;

        List<PieEntry> entries = new ArrayList<>();
        double montantRestant = totalDettes - totalPaye;

        if (totalPaye > 0) {
            entries.add(new PieEntry((float) totalPaye, "Déjà payé"));
        }
        if (montantRestant > 0) {
            entries.add(new PieEntry((float) montantRestant, "Reste à payer"));
        }

        if (entries.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        List<Integer> colors = new ArrayList<>();
        if (totalPaye > 0) colors.add(Color.parseColor("#4CAF50"));
        if (montantRestant > 0) colors.add(Color.parseColor("#F44336"));
        dataSet.setColors(colors);

        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(2f);

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value < 1000) return "";
                NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);
                return formatter.format(value);
            }
        });

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelColor(Color.BLACK);

        if (totalDettes > 0) {
            double pourcentage = (totalPaye / totalDettes) * 100;
            pieChart.setCenterText(String.format(Locale.FRANCE, "%.0f%%\npayé", pourcentage));
            pieChart.setCenterTextSize(16f);
        }

        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setTextSize(12f);

        pieChart.setVisibility(View.VISIBLE);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    /**
     * Graphique Barres : Top 5 clients avec le plus de dettes restantes
     */
    private void setupBarChart(List<Client> clientsEndettes) {
        if (barChart == null) return;

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int count = Math.min(5, clientsEndettes.size());
        for (int i = 0; i < count; i++) {
            Client client = clientsEndettes.get(i);
            Double solde = clientSoldes.get(client.getId());

            if (solde != null && solde > 0) {
                entries.add(new BarEntry(i, solde.floatValue()));
                labels.add(client.getNom() + " " + client.getPrenom().charAt(0) + ".");
            }
        }

        if (entries.isEmpty()) {
            barChart.setVisibility(View.GONE);
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "");

        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#D32F2F"));
        colors.add(Color.parseColor("#F44336"));
        colors.add(Color.parseColor("#FF5722"));
        colors.add(Color.parseColor("#FF9800"));
        colors.add(Color.parseColor("#FFC107"));
        dataSet.setColors(colors);

        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.BLACK);

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 1000) {
                    return String.format(Locale.FRANCE, "%.0fk", value / 1000);
                }
                return String.format(Locale.FRANCE, "%.0f", value);
            }
        });

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f);
        barChart.setData(data);

        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);
        barChart.setFitBars(true);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelRotationAngle(-20);
        xAxis.setTextSize(10f);
        xAxis.setDrawGridLines(false);

        barChart.getAxisLeft().setTextSize(10f);
        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisLeft().setGridColor(Color.LTGRAY);

        barChart.getAxisRight().setEnabled(false);

        Legend legend = barChart.getLegend();
        legend.setEnabled(false);

        barChart.setVisibility(View.VISIBLE);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        // --- SECTION TABLEAU DE BORD
        if (id == R.id.nav_dashboard) {
            Toast.makeText(this, "Vous êtes déjà sur le tableau de bord", Toast.LENGTH_SHORT).show();
        }
        // --- SECTION CLIENTS ---
        else if (id == R.id.nav_clients) {
            startActivity(new Intent(this, MainActivity.class));
        }
        else if (id == R.id.nav_add_client) {
            // Ouvre l'écran pour ajouter un client
            startActivity(new Intent(this, AddClientActivity.class));
        }
        else if (id == R.id.nav_clients_endettes) {
            startActivity(new Intent(this, AllDettesActivity.class));
        }
        // --- SECTION DETTES ---
        else if (id == R.id.nav_all_dettes) {
            startActivity(new Intent(this, AllDettesActivity.class));
        }
        else if (id == R.id.nav_add_dette) {
            // Ouvre l'écran pour créer une nouvelle dette
            startActivity(new Intent(this, AddDetteActivity.class));
        }
        else if (id == R.id.nav_dettes_en_cours) {
            Intent intent = new Intent(this, AllDettesActivity.class);
            intent.putExtra("FILTER_STATUT", "EN_COURS");
            startActivity(intent);
        }
        else if (id == R.id.nav_dettes_payees) {
            Intent intent = new Intent(this, AllDettesActivity.class);
            intent.putExtra("FILTER_STATUT", "PAYEE");
            startActivity(intent);
        }
        // --- SECTION PAIEMENTS ---
        else if (id == R.id.nav_all_paiements || id == R.id.nav_historique) {
            startActivity(new Intent(this, ListPaiementActivity.class));
        }
        else if (id == R.id.nav_add_paiement_menu) {
            // Ouvre l'écran pour enregistrer un paiement
            // Note: Ici, l'utilisateur devra choisir la dette après
            startActivity(new Intent(this, AddPaiementActivity.class));
        }
        // --- SECTION PARAMÈTRES ---
        else if (id == R.id.nav_profile) {
            String userName = sharedPreferences.getString("userName", "Utilisateur");
            Toast.makeText(this, "Profil de : " + userName, Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.nav_logout) {
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Nettoyage si nécessaire
    }
}