package bf.beatrice.carnet_dette.repository;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bf.beatrice.carnet_dette.api.SupabaseClient;
import bf.beatrice.carnet_dette.config.SupabaseConfig;
import bf.beatrice.carnet_dette.models.Paiement;

public class PaiementRepository {
    private static final String TAG = "PaiementRepository";
    private final SupabaseClient supabaseClient;

    public PaiementRepository() {
        this.supabaseClient = new SupabaseClient();
    }

    // Interfaces pour les callbacks
    public interface PaiementCallback {
        void onSuccess(List<Paiement> paiements);
        void onError(String error);
    }

    public interface SinglePaiementCallback {
        void onSuccess(Paiement paiement);
        void onError(String error);
    }

    public interface ActionCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    /**
     * Récupérer tous les paiements d'une dette
     */
    public void getPaiementsByDetteId(String detteId, PaiementCallback callback) {
        String url = SupabaseConfig.PAIEMENTS_ENDPOINT +
                "?dette_id=eq." + detteId +
                "&order=created_at.desc";

        supabaseClient.get(url, new SupabaseClient.SupabaseCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    List<Paiement> paiements = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonPaiement = jsonArray.getJSONObject(i);
                        Paiement paiement = parsePaiement(jsonPaiement);
                        paiements.add(paiement);
                    }

                    callback.onSuccess(paiements);
                } catch (JSONException e) {
                    Log.e(TAG, "Erreur parsing paiements", e);
                    callback.onError("Erreur de traitement des données");
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Ajouter un nouveau paiement
     */
    public void addPaiement(Paiement paiement, SinglePaiementCallback callback) {
        try {
            JSONObject jsonPaiement = new JSONObject();
            jsonPaiement.put("dette_id", paiement.getDette_id());

            // ✅ AJOUT DU NOM DU CLIENT
            if (paiement.getClientNom() != null && !paiement.getClientNom().isEmpty()) {
                jsonPaiement.put("client_nom", paiement.getClientNom());
            }

            jsonPaiement.put("montant", paiement.getMontant());
            jsonPaiement.put("mode_paiement", paiement.getMode_paiement());

            if (paiement.getDate_paiement() != null && !paiement.getDate_paiement().isEmpty()) {
                jsonPaiement.put("date_paiement", paiement.getDate_paiement());
            }

            Log.d(TAG, "Envoi paiement : " + jsonPaiement.toString());

            supabaseClient.post(SupabaseConfig.PAIEMENTS_ENDPOINT, jsonPaiement,
                    new SupabaseClient.SupabaseCallback() {
                        @Override
                        public void onSuccess(String response) {
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                if (jsonArray.length() > 0) {
                                    Paiement newPaiement = parsePaiement(jsonArray.getJSONObject(0));
                                    callback.onSuccess(newPaiement);
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Erreur parsing nouveau paiement", e);
                                callback.onError("Erreur de traitement");
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Erreur ajout paiement : " + error);
                            callback.onError(error);
                        }
                    });
        } catch (JSONException e) {
            Log.e(TAG, "Erreur création JSON", e);
            callback.onError("Erreur de préparation des données");
        }
    }

    /**
     * Supprimer un paiement
     */
    public void deletePaiement(String paiementId, ActionCallback callback) {
        String url = SupabaseConfig.PAIEMENTS_ENDPOINT + "?id=eq." + paiementId;

        supabaseClient.delete(url, new SupabaseClient.SupabaseCallback() {
            @Override
            public void onSuccess(String response) {
                callback.onSuccess("Paiement supprimé avec succès");
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Récupérer TOUS les paiements (toutes dettes confondues)
     */
    public void getAllPaiements(PaiementCallback callback) {
        String url = SupabaseConfig.PAIEMENTS_ENDPOINT + "?order=created_at.desc";

        supabaseClient.get(url, new SupabaseClient.SupabaseCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    List<Paiement> paiements = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        paiements.add(parsePaiement(jsonArray.getJSONObject(i)));
                    }
                    callback.onSuccess(paiements);
                } catch (JSONException e) {
                    Log.e(TAG, "Erreur parsing tous les paiements", e);
                    callback.onError("Erreur de traitement des données");
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Mettre à jour un paiement
     */
    public void updatePaiement(Paiement paiement, ActionCallback callback) {
        String url = SupabaseConfig.PAIEMENTS_ENDPOINT + "?id=eq." + paiement.getId();

        try {
            JSONObject jsonUpdate = new JSONObject();

            // ✅ AJOUT DU NOM DU CLIENT DANS LA MISE À JOUR
            if (paiement.getClientNom() != null && !paiement.getClientNom().isEmpty()) {
                jsonUpdate.put("client_nom", paiement.getClientNom());
            }

            jsonUpdate.put("montant", paiement.getMontant());
            jsonUpdate.put("mode_paiement", paiement.getMode_paiement());

            if (paiement.getDate_paiement() != null) {
                jsonUpdate.put("date_paiement", paiement.getDate_paiement());
            }

            supabaseClient.patch(url, jsonUpdate, new SupabaseClient.SupabaseCallback() {
                @Override
                public void onSuccess(String response) {
                    callback.onSuccess("Paiement mis à jour avec succès");
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Erreur création JSON pour update", e);
            callback.onError("Erreur de préparation des données");
        }
    }

    /**
     * Parser un JSONObject en Paiement
     */
    private Paiement parsePaiement(JSONObject json) throws JSONException {
        Paiement paiement = new Paiement();
        paiement.setId(json.getString("id"));
        paiement.setDette_id(json.getString("dette_id"));
        paiement.setMontant(json.getDouble("montant"));

        // ✅ RÉCUPÉRATION DU NOM DU CLIENT
        if (json.has("client_nom") && !json.isNull("client_nom")) {
            paiement.setClientNom(json.getString("client_nom"));
        } else {
            paiement.setClientNom("Client inconnu");
        }

        if (json.has("mode_paiement") && !json.isNull("mode_paiement")) {
            paiement.setMode_paiement(json.getString("mode_paiement"));
        }

        if (json.has("date_paiement") && !json.isNull("date_paiement")) {
            paiement.setDate_paiement(json.getString("date_paiement"));
        }

        if (json.has("created_at") && !json.isNull("created_at")) {
            paiement.setCreated_at(json.getString("created_at"));
        }

        return paiement;
    }

    public void shutdown() {
        if (supabaseClient != null) {
            supabaseClient.shutdown();
        }
    }
}