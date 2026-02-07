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

import bf.beatrice.carnet_dette.R;
import bf.beatrice.carnet_dette.models.Paiement;

public class PaiementAdapter extends RecyclerView.Adapter<PaiementAdapter.PaiementViewHolder> {
    private bf.beatrice.carnet_dette.adapters.PaiementDetailAdapter paiementAdapter;

    private List<Paiement> paiements = new ArrayList<>();
    private OnPaiementActionListener listener;

    public interface OnPaiementActionListener {
        void onPaiementClick(Paiement paiement);
        void onModifierClick(Paiement paiement);
        void onSupprimerClick(Paiement paiement);
        void onVoirDetteClick(Paiement paiement); // Ajouté pour ton bouton "Voir"
    }

    public PaiementAdapter(OnPaiementActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PaiementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ON UTILISE TON NOUVEAU LAYOUT ICI
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paiement_detail, parent, false);
        return new PaiementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaiementViewHolder holder, int position) {
        Paiement paiement = paiements.get(position);
        holder.bind(paiement, listener);
    }

    @Override
    public int getItemCount() {
        return paiements.size();
    }

    public void setPaiements(List<Paiement> paiements) {
        this.paiements = paiements;
        notifyDataSetChanged();
    }

    static class PaiementViewHolder extends RecyclerView.ViewHolder {
        // On utilise les IDs exacts de ton fichier XML
        TextView txtClientNom, txtDetteDescription, txtMontant, txtDate, txtMode;
        Button btnVoirDette, btnModifier, btnSupprimer;

        public PaiementViewHolder(@NonNull View itemView) {
            super(itemView);
            txtClientNom = itemView.findViewById(R.id.txtClientNom);
            txtDetteDescription = itemView.findViewById(R.id.txtDetteDescription);
            txtMontant = itemView.findViewById(R.id.txtMontant);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtMode = itemView.findViewById(R.id.txtMode);
            btnVoirDette = itemView.findViewById(R.id.btnVoirDette);
            btnModifier = itemView.findViewById(R.id.btnModifier);
            btnSupprimer = itemView.findViewById(R.id.btnSupprimer);
        }

        public void bind(Paiement paiement, OnPaiementActionListener listener) {
            NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);

            // AFFICHAGE DIRECT DU NOM (Grâce à ta modif Supabase !)
            txtClientNom.setText("👤 " + (paiement.getClientNom() != null ? paiement.getClientNom() : "Client inconnu"));
            // 3. LE MODE DE PAIEMENT (Vérifie que ce n'est pas inversé avec le nom !)
            txtMode.setText("Mode : " + paiement.getMode_paiement());

            // Pour la description, on peut mettre l'ID de la dette par défaut
           // txtDetteDescription.setText("📄 Reçu pour : " + (paiement.getClientNom() != null ? paiement.getClientNom() : "ce client"));

            txtMontant.setText(formatter.format(paiement.getMontant()) + " FCFA");

            // Formatage de la date
            try {
                if (paiement.getDate_paiement() != null) {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
                    txtDate.setText(outputFormat.format(inputFormat.parse(paiement.getDate_paiement())));
                }
            } catch (Exception e) {
                txtDate.setText(paiement.getDate_paiement());
            }

            // Mode de paiement avec emoji
            txtMode.setText(formatMode(paiement.getMode_paiement()));

            // --- LISTENERS SUR LES BOUTONS ---

            btnModifier.setOnClickListener(v -> listener.onModifierClick(paiement));
            btnSupprimer.setOnClickListener(v -> listener.onSupprimerClick(paiement));

            if (btnVoirDette != null) {
                btnVoirDette.setOnClickListener(v -> listener.onVoirDetteClick(paiement));
            }

            itemView.setOnClickListener(v -> listener.onPaiementClick(paiement));
        }

        private String formatMode(String mode) {
            if (mode == null) return "💰 Paiement";
            switch (mode) {
                case "ESPECES": return "💵 Espèces";
                case "MOBILE_MONEY": return "📱 Mobile Money";
                case "VIREMENT": return "🏦 Virement";
                default: return mode;
            }
        }
    }
}