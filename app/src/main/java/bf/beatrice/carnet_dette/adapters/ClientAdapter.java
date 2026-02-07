package bf.beatrice.carnet_dette.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import bf.beatrice.carnet_dette.R;
import bf.beatrice.carnet_dette.models.Client;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder> {

    private List<Client> clients;
    private OnClientClickListener listener;

    public interface OnClientClickListener {
        void onClientClick(Client client);
    }

    public ClientAdapter(OnClientClickListener listener) {
        this.clients = new ArrayList<>();
        this.listener = listener;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_client_detail, parent, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        Client client = clients.get(position);
        holder.bind(client, listener);
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    static class ClientViewHolder extends RecyclerView.ViewHolder {
        private TextView txtNom;
        private TextView txtTelephone;
        private TextView txtAdresse;

        public ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNom = itemView.findViewById(R.id.txtNom);
            txtTelephone = itemView.findViewById(R.id.txtTelephone);
            txtAdresse = itemView.findViewById(R.id.txtAdresse);
        }

        public void bind(Client client, OnClientClickListener listener) {
            txtNom.setText(client.getNom() + " " + client.getPrenom());
            txtTelephone.setText(client.getTelephone());

            if (client.getAdresse() != null && !client.getAdresse().isEmpty()) {
                txtAdresse.setText(client.getAdresse());
                txtAdresse.setVisibility(View.VISIBLE);
            } else {
                txtAdresse.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClientClick(client);
                }
            });
        }
    }
}