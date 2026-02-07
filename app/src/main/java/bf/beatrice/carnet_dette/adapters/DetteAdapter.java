package bf.beatrice.carnet_dette.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bf.beatrice.carnet_dette.R;
import bf.beatrice.carnet_dette.models.Dette;

public class DetteAdapter extends RecyclerView.Adapter<DetteAdapter.DetteViewHolder> {

    private List<Dette> dettes = new ArrayList<>();
    private OnDetteClickListener listener;
    private String clientNom; // ✅ AJOUT

    public interface OnDetteClickListener {
        void onDetteClick(Dette dette);
    }

    // ✅ CONSTRUCTEUR MODIFIÉ avec clientNom
    public DetteAdapter(String clientNom, OnDetteClickListener listener) {
        this.clientNom = clientNom;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DetteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dette, parent, false);
        return new DetteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetteViewHolder holder, int position) {
        Dette dette = dettes.get(position);


        // ✅ ESSAYER D'OBTENIR LE NOM DU CLIENT DE DIFFÉRENTES FAÇONS
        String nomAAfficher = null;

        if (dette.getClient_nom() != null && !dette.getClient_nom().isEmpty()) {
            nomAAfficher = dette.getClient_nom();
        } else if (this.clientNom != null) {
            nomAAfficher = this.clientNom;
        } else {
            // ✅ SI AUCUN NOM N'EST DISPONIBLE, AFFICHER UN MESSAGE PAR DÉFAUT
            nomAAfficher = "Client";
        }

        holder.bind(dette, listener, nomAAfficher);
    }

    @Override
    public int getItemCount() {
        return dettes.size();
    }

    public void setDettes(List<Dette> dettes) {
        this.dettes = dettes;
        notifyDataSetChanged();
    }

    static class DetteViewHolder extends RecyclerView.ViewHolder {
        TextView txtClientNom, txtDescription, txtMontant, txtStatut;

        public DetteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtClientNom = itemView.findViewById(R.id.txtClientNom);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtMontant = itemView.findViewById(R.id.txtMontant);
            txtStatut = itemView.findViewById(R.id.txtStatut);
        }

        public void bind(Dette dette, OnDetteClickListener listener, String clientNom) {
            // ✅ AFFICHER LE NOM DU CLIENT - GÉRER LE CAS NULL
            if (txtClientNom != null) {
                String nomFinal = clientNom;

                if (nomFinal == null && dette.getClient_nom() != null) {
                    nomFinal = dette.getClient_nom();
                }
                if (nomFinal != null && !nomFinal.isEmpty() && !nomFinal.equals("null")) {
                    txtClientNom.setText("👤 " + nomFinal);
                    txtClientNom.setVisibility(View.VISIBLE);
                } else {
                    // ✅ AFFICHER "Client" SI LE NOM EST INCONNU
                    txtClientNom.setText("👤 Client");
                    txtClientNom.setVisibility(View.VISIBLE);
                }
            }

            txtDescription.setText(dette.getDescription());

            NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);
            txtMontant.setText(formatter.format(dette.getMontant()) + " FCFA");

            String statut = dette.getStatut();
            txtStatut.setText(statut);

            if ("PAYEE".equals(statut)) {
                txtStatut.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                txtStatut.setBackgroundColor(itemView.getContext().getColor(android.R.color.holo_green_light));
            } else {
                txtStatut.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
                txtStatut.setBackgroundColor(itemView.getContext().getColor(android.R.color.holo_orange_light));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDetteClick(dette);
                }
            });
        }
    }
}