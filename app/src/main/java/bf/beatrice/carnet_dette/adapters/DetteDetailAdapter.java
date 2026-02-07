package bf.beatrice.carnet_dette.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bf.beatrice.carnet_dette.R;
import bf.beatrice.carnet_dette.models.Dette;

public class DetteDetailAdapter extends RecyclerView.Adapter<DetteDetailAdapter.ViewHolder> {

    private static final String TAG = "DetteDetailAdapter";
    private List<Dette> dettes = new ArrayList<>();

    // ✅ Correction : 'final' ajouté car ces valeurs ne changent pas après le constructeur
    private final String clientNom;
    private final OnDetteActionListener listener;

    public interface OnDetteActionListener {
        void onVoirClick(Dette dette);
        void onModifierClick(Dette dette);
        void onSupprimerClick(Dette dette);
        void onDetteClick(Dette dette);
    }

    public DetteDetailAdapter(String clientNom, OnDetteActionListener listener) {
        this.clientNom = clientNom;
        this.listener = listener;
        Log.d(TAG, "Adapter créé avec clientNom = " + clientNom);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dette, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Dette dette = dettes.get(position);
        holder.bind(dette, clientNom, listener);
    }

    @Override
    public int getItemCount() {
        return dettes.size();
    }

    public void setDettes(List<Dette> dettes) {
        this.dettes = dettes;
        // ✅ Suggestion Android Studio : notifyDataSetChanged() est moins efficace que DiffUtil
        // mais pour l'instant, c'est fonctionnel.
        notifyDataSetChanged();
        Log.d(TAG, "setDettes appelé avec " + dettes.size() + " dettes");
    }

    // ✅ Correction : Classe rendue 'static' pour éviter les fuites de mémoire
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtClientNom, txtDescription, txtMontant, txtStatut;
        private final Button btnVoir, btnModifier, btnSupprimer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtClientNom = itemView.findViewById(R.id.txtClientNom);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtMontant = itemView.findViewById(R.id.txtMontant);
            txtStatut = itemView.findViewById(R.id.txtStatut);
            btnVoir = itemView.findViewById(R.id.btnVoir);
            btnModifier = itemView.findViewById(R.id.btnModifier);
            btnSupprimer = itemView.findViewById(R.id.btnSupprimer);
        }

        public void bind(Dette dette, String clientNomIntent, OnDetteActionListener listener) {
            // ✅ Correction : Extraction de la logique du nom dans une méthode séparée
            String nomAffiche = getValideName(dette, clientNomIntent);

            if (txtClientNom != null) {
                // ✅ Note : Idéalement utiliser itemView.getContext().getString(R.string.votre_format, nomAffiche)
                txtClientNom.setText(String.format("👤 %s", nomAffiche));
                txtClientNom.setVisibility(View.VISIBLE);
            }

            if (txtDescription != null) txtDescription.setText(dette.getDescription());

            if (txtMontant != null) {
                NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);
                txtMontant.setText(String.format("%s FCFA", formatter.format(dette.getMontant())));
            }

            if (txtStatut != null) {
                setupStatusView(dette.getStatut());
            }

            // Listeners
            btnVoir.setOnClickListener(v -> listener.onVoirClick(dette));
            btnModifier.setOnClickListener(v -> listener.onModifierClick(dette));
            btnSupprimer.setOnClickListener(v -> listener.onSupprimerClick(dette));
            itemView.setOnClickListener(v -> listener.onDetteClick(dette));
        }

        // ✅ Méthode extraite pour nettoyer le code (Refactoring)
        private String getValideName(Dette dette, String backupNom) {
            if (isValid(dette.getClient_nom())) return dette.getClient_nom();
            if (isValid(backupNom)) return backupNom;
            return "Client";
        }

        private boolean isValid(String str) {
            return str != null && !str.trim().isEmpty() && !str.equalsIgnoreCase("null");
        }

        private void setupStatusView(String statut) {
            txtStatut.setText(statut);
            int colorDark = "PAYEE".equals(statut) ? android.R.color.holo_green_dark : android.R.color.holo_orange_dark;
            int colorLight = "PAYEE".equals(statut) ? android.R.color.holo_green_light : android.R.color.holo_orange_light;

            txtStatut.setTextColor(itemView.getContext().getColor(colorDark));
            txtStatut.setBackgroundColor(itemView.getContext().getColor(colorLight));
        }
    }
}