package bf.beatrice.carnet_dette.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import bf.beatrice.carnet_dette.R;
import bf.beatrice.carnet_dette.models.Client;

public class ClientDashboardAdapter extends RecyclerView.Adapter<ClientDashboardAdapter.ViewHolder> {

    private List<Client> clients;
    private Map<String, Double> clientSoldes;  // clientId -> solde
    private OnClientClickListener listener;

    public interface OnClientClickListener {
        void onClientClick(Client client);
    }

    public ClientDashboardAdapter(OnClientClickListener listener) {
        this.clients = new ArrayList<>();
        this.clientSoldes = new HashMap<>();
        this.listener = listener;
    }

    public void setData(List<Client> clients, Map<String, Double> soldes) {
        this.clients = clients;
        this.clientSoldes = soldes;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_client_dashboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Client client = clients.get(position);
        Double solde = clientSoldes.get(client.getId());
        holder.bind(client, solde != null ? solde : 0, position + 1, listener);
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtPosition;
        private TextView txtNom;
        private TextView txtTelephone;
        private TextView txtSolde;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPosition = itemView.findViewById(R.id.txtPosition);
            txtNom = itemView.findViewById(R.id.txtNom);
            txtTelephone = itemView.findViewById(R.id.txtTelephone);
            txtSolde = itemView.findViewById(R.id.txtSolde);
        }

        public void bind(Client client, double solde, int position, OnClientClickListener listener) {
            txtPosition.setText(String.valueOf(position));
            txtNom.setText(client.getNom() + " " + client.getPrenom());
            txtTelephone.setText(client.getTelephone());

            NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);
            txtSolde.setText(formatter.format(solde) + " FCFA");

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClientClick(client);
                }
            });
        }
    }
}