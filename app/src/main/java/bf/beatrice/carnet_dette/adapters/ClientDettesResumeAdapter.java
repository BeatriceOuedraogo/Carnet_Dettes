package bf.beatrice.carnet_dette.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bf.beatrice.carnet_dette.R;
import bf.beatrice.carnet_dette.models.Client;
import bf.beatrice.carnet_dette.models.Dette;

public class ClientDettesResumeAdapter extends RecyclerView.Adapter<ClientDettesResumeAdapter.ViewHolder> {

    private List<Client> clients = new ArrayList<>();
    private OnClientClickListener listener;
    private java.util.Map<String, Double> clientTotalDettes = new java.util.HashMap<>();
    private java.util.Map<String, Integer> clientNbDettes = new java.util.HashMap<>();

    public interface OnClientClickListener {
        void onClientClick(Client client, double totalDettes, int nbDettes);
    }

    public ClientDettesResumeAdapter(OnClientClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<Client> clients, java.util.Map<String, List<Dette>> dettesParClient) {
        this.clients = clients;
        this.clientTotalDettes.clear();
        this.clientNbDettes.clear();

        // Calculer les totaux pour chaque client
        for (Client client : clients) {
            List<Dette> dettes = dettesParClient.get(client.getId());
            if (dettes != null && !dettes.isEmpty()) {
                double total = 0;
                int nbDettesEnCours = 0;
                for (Dette dette : dettes) {
                    if (!"PAYEE".equals(dette.getStatut())) {
                        total += dette.getMontant();
                        nbDettesEnCours++;
                    }
                }
                clientTotalDettes.put(client.getId(), total);
                clientNbDettes.put(client.getId(), nbDettesEnCours);
            }
        }

        // Filtrer les clients qui ont des dettes en cours
        List<Client> clientsAvecDettes = new ArrayList<>();
        for (Client client : clients) {
            if (clientNbDettes.containsKey(client.getId()) &&
                    clientNbDettes.get(client.getId()) > 0) {
                clientsAvecDettes.add(client);
            }
        }
        this.clients = clientsAvecDettes;

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_client_dettes_resume, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Client client = clients.get(position);
        double total = clientTotalDettes.getOrDefault(client.getId(), 0.0);
        int nbDettes = clientNbDettes.getOrDefault(client.getId(), 0);

        holder.bind(client, total, nbDettes, listener);
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtClientNom, txtNbDettes, txtTotalDette, txtTelephone;
        private MaterialButton btnVoirDettes;
        private MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            txtClientNom = itemView.findViewById(R.id.txtClientNom);
            txtNbDettes = itemView.findViewById(R.id.txtNbDettes);
            txtTotalDette = itemView.findViewById(R.id.txtTotalDette);
            txtTelephone = itemView.findViewById(R.id.txtTelephone);
            btnVoirDettes = itemView.findViewById(R.id.btnVoirDettes);
        }

        public void bind(Client client, double totalDettes, int nbDettes, OnClientClickListener listener) {
            NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);

            // Afficher les infos
            txtClientNom.setText("👤 " + client.getNom() + " " + client.getPrenom());
            txtTelephone.setText("📞 " + client.getTelephone());
            txtNbDettes.setText(nbDettes + " dette(s) en cours");
            txtTotalDette.setText("Total dû : " + formatter.format(totalDettes) + " FCFA");

            // Personnaliser la carte en fonction du montant
            if (totalDettes > 50000) {
                // Dette élevée
                cardView.setStrokeColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                cardView.setStrokeWidth(2);
                txtTotalDette.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
            } else if (totalDettes > 10000) {
                // Dette moyenne
                cardView.setStrokeColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
                cardView.setStrokeWidth(1);
                txtTotalDette.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
            } else {
                // Dette faible
                cardView.setStrokeColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                cardView.setStrokeWidth(1);
                txtTotalDette.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
            }

            // Bouton "Voir dettes"
            btnVoirDettes.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClientClick(client, totalDettes, nbDettes);
                }
            });

            // Clic sur toute la carte
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClientClick(client, totalDettes, nbDettes);
                }
            });
        }
    }
}