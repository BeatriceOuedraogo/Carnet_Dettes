package bf.beatrice.carnet_dette.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import bf.beatrice.carnet_dette.R;
import bf.beatrice.carnet_dette.models.Client;

public class ClientDetailAdapter extends RecyclerView.Adapter<ClientDetailAdapter.ViewHolder> {

    private static final String TAG = "ClientDetailAdapter";
    private List<Client> clients = new ArrayList<>();
    private OnClientActionListener listener;

    public interface OnClientActionListener {
        void onVoirClick(Client client);
        void onModifierClick(Client client);
        void onSupprimerClick(Client client);
        void onClientClick(Client client);
    }

    public ClientDetailAdapter(OnClientActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder appelé");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_client_detail, parent, false);

        // Vérifiez que la vue contient des boutons
        ViewHolder holder = new ViewHolder(view);
        Log.d(TAG, "Boutons trouvés - Voir: " + (holder.btnVoir != null) +
                ", Modifier: " + (holder.btnModifier != null) +
                ", Supprimer: " + (holder.btnSupprimer != null));

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder pour position: " + position);
        Client client = clients.get(position);
        holder.bind(client, listener);
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNom, txtTelephone, txtAdresse;
        MaterialButton btnVoir, btnModifier, btnSupprimer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNom = itemView.findViewById(R.id.txtNom);
            txtTelephone = itemView.findViewById(R.id.txtTelephone);
            txtAdresse = itemView.findViewById(R.id.txtAdresse);

            // Cherchez les boutons
            btnVoir = itemView.findViewById(R.id.btnVoir);
            btnModifier = itemView.findViewById(R.id.btnModifier);
            btnSupprimer = itemView.findViewById(R.id.btnSupprimer);

            Log.d("ViewHolder", "btnVoir trouvé: " + (btnVoir != null));
            Log.d("ViewHolder", "btnModifier trouvé: " + (btnModifier != null));
            Log.d("ViewHolder", "btnSupprimer trouvé: " + (btnSupprimer != null));
        }

        public void bind(Client client, OnClientActionListener listener) {
            txtNom.setText(client.getNom() + " " + client.getPrenom());

            if (client.getTelephone() != null && !client.getTelephone().isEmpty()) {
                txtTelephone.setText(client.getTelephone());
                txtTelephone.setVisibility(View.VISIBLE);
            } else {
                txtTelephone.setVisibility(View.GONE);
            }

            if (client.getAdresse() != null && !client.getAdresse().isEmpty()) {
                txtAdresse.setText(client.getAdresse());
                txtAdresse.setVisibility(View.VISIBLE);
            } else {
                txtAdresse.setVisibility(View.GONE);
            }

            // Bouton Voir
            if (btnVoir != null) {
                btnVoir.setOnClickListener(v -> {
                    Log.d("ClientDetailAdapter", "Clic sur bouton Voir pour: " + client.getNom());
                    if (listener != null) {
                        listener.onVoirClick(client);
                    }
                });
            } else {
                Log.e("ClientDetailAdapter", "btnVoir est NULL!");
            }

            // Bouton Modifier
            if (btnModifier != null) {
                btnModifier.setOnClickListener(v -> {
                    Log.d("ClientDetailAdapter", "Clic sur bouton Modifier pour: " + client.getNom());
                    if (listener != null) {
                        listener.onModifierClick(client);
                    }
                });
            } else {
                Log.e("ClientDetailAdapter", "btnModifier est NULL!");
            }

            // Bouton Supprimer
            if (btnSupprimer != null) {
                btnSupprimer.setOnClickListener(v -> {
                    Log.d("ClientDetailAdapter", "Clic sur bouton Supprimer pour: " + client.getNom());
                    if (listener != null) {
                        listener.onSupprimerClick(client);
                    }
                });
            } else {
                Log.e("ClientDetailAdapter", "btnSupprimer est NULL!");
            }

            // Clic sur la carte
            itemView.setOnClickListener(v -> {
                Log.d("ClientDetailAdapter", "Clic sur la carte pour: " + client.getNom());
                if (listener != null) {
                    listener.onClientClick(client);
                }
            });
        }
    }
}