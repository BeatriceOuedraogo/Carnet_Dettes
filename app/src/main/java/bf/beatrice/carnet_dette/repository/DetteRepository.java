package bf.beatrice.carnet_dette.repository;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import bf.beatrice.carnet_dette.api.SupabaseClient;
import bf.beatrice.carnet_dette.config.SupabaseConfig;
import bf.beatrice.carnet_dette.models.Dette;

public class DetteRepository {
    private final SupabaseClient client = new SupabaseClient();

    public interface DetteCallback { void onSuccess(List<Dette> dettes); void onError(String error); }
    public interface SingleDetteCallback { void onSuccess(Dette dette); void onError(String error); }
    public interface ActionCallback { void onSuccess(String msg); void onError(String error); }

    /**
     * AJOUT : La méthode manquante pour mettre à jour après un paiement
     */
    public void updateMontantDette(String id, double nouveauMontant, String statut, ActionCallback callback) {
        String url = SupabaseConfig.DETTES_ENDPOINT + "?id=eq." + id;
        try {
            JSONObject json = new JSONObject();
            json.put("montant", nouveauMontant);
            json.put("statut", statut);

            client.patch(url, json, new SupabaseClient.SupabaseCallback() {
                @Override
                public void onSuccess(String response) {
                    callback.onSuccess("Montant de la dette mis à jour");
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });
        } catch (JSONException e) {
            callback.onError("Erreur JSON : " + e.getMessage());
        }
    }

    public void addDette(Dette dette, SingleDetteCallback cb) {
        try {
            JSONObject json = new JSONObject();
            json.put("client_id", dette.getClient_id());
            json.put("client_nom", dette.getClient_nom());
            json.put("description", dette.getDescription());
            json.put("montant", dette.getMontant());
            json.put("statut", "EN COURS");
            client.post(SupabaseConfig.DETTES_ENDPOINT, json, new SupabaseClient.SupabaseCallback() {
                @Override public void onSuccess(String res) {
                    try { cb.onSuccess(parseDette(new JSONArray(res).getJSONObject(0))); }
                    catch (JSONException e) { cb.onError("Erreur parsing"); }
                }
                @Override public void onError(String err) { cb.onError(err); }
            });
        } catch (JSONException e) { cb.onError("Erreur JSON"); }
    }

    // Gardée pour la modification manuelle de la dette
    public void updateDette(Dette dette, ActionCallback cb) {
        try {
            JSONObject json = new JSONObject();
            json.put("description", dette.getDescription());
            json.put("montant", dette.getMontant());
            json.put("statut", dette.getStatut());
            String url = SupabaseConfig.DETTES_ENDPOINT + "?id=eq." + dette.getId();

            client.patch(url, json, new SupabaseClient.SupabaseCallback() {
                @Override public void onSuccess(String response) { cb.onSuccess("Dette mise à jour"); }
                @Override public void onError(String err) { cb.onError(err); }
            });
        } catch (JSONException e) { cb.onError("Erreur JSON"); }
    }

    public void getDettesByClientId(String clientId, DetteCallback cb) {
        String url = SupabaseConfig.DETTES_ENDPOINT + "?client_id=eq." + clientId + "&order=created_at.desc";
        client.get(url, new SupabaseClient.SupabaseCallback() {
            @Override public void onSuccess(String res) {
                try {
                    JSONArray arr = new JSONArray(res);
                    List<Dette> list = new ArrayList<>();
                    for(int i=0; i<arr.length(); i++) list.add(parseDette(arr.getJSONObject(i)));
                    cb.onSuccess(list);
                } catch (JSONException e) { cb.onError("Erreur"); }
            }
            @Override public void onError(String err) { cb.onError(err); }
        });
    }
    public void deleteDette(String id, ActionCallback callback) {
        // L'URL cible l'ID spécifique de la dette
        String url = SupabaseConfig.DETTES_ENDPOINT + "?id=eq." + id;

        client.delete(url, new SupabaseClient.SupabaseCallback() {
            @Override
            public void onSuccess(String response) {
                callback.onSuccess("Dette supprimée avec succès");
            }

            @Override
            public void onError(String error) {
                callback.onError("Erreur lors de la suppression : " + error);
            }
        });
    }

    public void getDetteById(String id, SingleDetteCallback cb) {
        String url = SupabaseConfig.DETTES_ENDPOINT + "?id=eq." + id + "&select=*";
        client.get(url, new SupabaseClient.SupabaseCallback() {
            @Override public void onSuccess(String res) {
                try { cb.onSuccess(parseDette(new JSONArray(res).getJSONObject(0))); }
                catch (JSONException e) { cb.onError("Inconnu"); }
            }
            @Override public void onError(String err) { cb.onError(err); }
        });
    }

    public void getAllDettes(DetteCallback callback) {
        String url = SupabaseConfig.DETTES_ENDPOINT + "?select=*&order=created_at.desc";
        client.get(url, new SupabaseClient.SupabaseCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    List<Dette> dettes = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        dettes.add(parseDette(jsonArray.getJSONObject(i)));
                    }
                    callback.onSuccess(dettes);
                } catch (JSONException e) {
                    callback.onError("Erreur de lecture des données");
                }
            }
            @Override public void onError(String error) { callback.onError(error); }
        });
    }

    private Dette parseDette(JSONObject row) throws JSONException {
        Dette d = new Dette();
        d.setId(row.getString("id"));
        d.setClient_id(row.getString("client_id"));
        d.setClient_nom(row.optString("client_nom", ""));
        d.setDescription(row.getString("description"));
        d.setMontant(row.getDouble("montant"));
        d.setStatut(row.optString("statut", "EN COURS"));
        return d;
    }

    public void shutdown() { client.shutdown(); }
}