package bf.beatrice.carnet_dette.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bf.beatrice.carnet_dette.ListPaiementActivity;
import bf.beatrice.carnet_dette.R;

public class PaiementDetailAdapter extends RecyclerView.Adapter<PaiementDetailAdapter.ViewHolder> {

    private List<ListPaiementActivity.PaiementDetail> paiements = new ArrayList<>();
    private OnPaiementActionListener listener;

    public interface OnPaiementActionListener {
        void onPaiementClick(ListPaiementActivity.PaiementDetail detail);
        void onModifierClick(ListPaiementActivity.PaiementDetail detail);
        void onSupprimerClick(ListPaiementActivity.PaiementDetail detail);
        void onVoirDetteClick(ListPaiementActivity.PaiementDetail detail);
    }

    public PaiementDetailAdapter(OnPaiementActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paiement_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListPaiementActivity.PaiementDetail detail = paiements.get(position);
        holder.bind(detail, listener);
    }

    @Override
    public int getItemCount() {
        return paiements.size();
    }

    public void setPaiements(List<ListPaiementActivity.PaiementDetail> paiements) {
        this.paiements = paiements;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtClientNom, txtDetteDescription, txtMontant, txtDate, txtMode;
        Button btnModifier, btnSupprimer, btnVoirDette;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtClientNom = itemView.findViewById(R.id.txtClientNom);
            txtDetteDescription = itemView.findViewById(R.id.txtDetteDescription);
            txtMontant = itemView.findViewById(R.id.txtMontant);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtMode = itemView.findViewById(R.id.txtMode);
            btnModifier = itemView.findViewById(R.id.btnModifier);
            btnSupprimer = itemView.findViewById(R.id.btnSupprimer);
            btnVoirDette = itemView.findViewById(R.id.btnVoirDette);
        }

        public void bind(ListPaiementActivity.PaiementDetail detail, OnPaiementActionListener listener) {
            NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);

            // Afficher les infos
            txtClientNom.setText("👤 " + detail.clientNom);
            txtDetteDescription.setText("📄 " + detail.detteDescription);
            txtMontant.setText(formatter.format(detail.paiement.getMontant()) + " FCFA");

            // Formater la date
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
                String dateFormatted = outputFormat.format(inputFormat.parse(detail.paiement.getDate_paiement()));
                txtDate.setText(dateFormatted);
            } catch (Exception e) {
                txtDate.setText(detail.paiement.getDate_paiement());
            }

            // Mode de paiement avec emoji
            String mode = detail.paiement.getMode_paiement();
            if ("ESPECES".equals(mode)) {
                txtMode.setText("💵 Espèces");
            } else if ("MOBILE_MONEY".equals(mode)) {
                txtMode.setText("📱 Mobile Money");
            } else if ("VIREMENT".equals(mode)) {
                txtMode.setText("🏦 Virement");
            } else {
                txtMode.setText(mode);
            }

            // Clic sur la carte
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPaiementClick(detail);
                }
            });

            // Bouton Modifier
            btnModifier.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onModifierClick(detail);
                }
            });

            // Bouton Supprimer
            btnSupprimer.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSupprimerClick(detail);
                }
            });

            // Bouton Voir Dette
            btnVoirDette.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVoirDetteClick(detail);
                }
            });
        }
    }
}