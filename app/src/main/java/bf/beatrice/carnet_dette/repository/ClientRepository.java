package bf.beatrice.carnet_dette.repository;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import bf.beatrice.carnet_dette.api.SupabaseClient;
import bf.beatrice.carnet_dette.config.SupabaseConfig;
import bf.beatrice.carnet_dette.models.Client;

public class ClientRepository {

    private static final String TAG = "ClientRepository";
    private final SupabaseClient supabaseClient;

    public ClientRepository() {
        this.supabaseClient = new SupabaseClient();
    }

    public void getClientById(String clientId, SingleClientCallback singleClientCallback) {


    }

    // --- Interfaces ---
    public interface ClientCallback { void onSuccess(List<Client> clients); void onError(String error); }
    public interface SingleClientCallback { void onSuccess(Client client); void onError(String error); }
    public interface ActionCallback { void onSuccess(String message); void onError(String error); }

    /**
     * RÉCUPÉRER TOUS LES CLIENTS
     */
    public void getAllClients(ClientCallback callback) {
        String url = SupabaseConfig.CLIENTS_ENDPOINT + "?order=created_at.desc";
        supabaseClient.get(url, new SupabaseClient.SupabaseCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    List<Client> clients = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        clients.add(parseClient(jsonArray.getJSONObject(i)));
                    }
                    callback.onSuccess(clients);
                } catch (JSONException e) { callback.onError("Erreur de traitement des données"); }
            }
            @Override
            public void onError(String error) { callback.onError(error); }
        });
    }

    /**
     * AJOUTER UN CLIENT AVEC VÉRIFICATION DE DOUBLON
     */
    public void addClient(Client client, SingleClientCallback callback) {
        String urlCheck = SupabaseConfig.CLIENTS_ENDPOINT + "?telephone=eq." + client.getTelephone();
        supabaseClient.get(urlCheck, new SupabaseClient.SupabaseCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray existing = new JSONArray(response);
                    if (existing.length() > 0) {
                        callback.onSuccess(parseClient(existing.getJSONObject(0)));
                    } else {
                        performActualInsert(client, callback);
                    }
                } catch (JSONException e) { callback.onError("Erreur vérification"); }
            }
            @Override
            public void onError(String error) { callback.onError(error); }
        });
    }


    /**
     * SUPPRIMER UN CLIENT (Fix de l'erreur "cannot find symbol")
     */
    public void deleteClient(String clientId, ActionCallback callback) {
        String url = SupabaseConfig.CLIENTS_ENDPOINT + "?id=eq." + clientId;
        supabaseClient.delete(url, new SupabaseClient.SupabaseCallback() {
            @Override
            public void onSuccess(String response) {
                callback.onSuccess("Client supprimé avec succès");
            }
            @Override
            public void onError(String error) {
                callback.onError("Erreur lors de la suppression : " + error);
            }
        });
    }

    /**
     * MODIFIER UN CLIENT (Finalisée)
     */
    public void updateClient(Client client, ActionCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("nom", client.getNom());
            json.put("prenom", client.getPrenom());
            json.put("telephone", client.getTelephone());
            json.put("adresse", client.getAdresse());

            String url = SupabaseConfig.CLIENTS_ENDPOINT + "?id=eq." + client.getId();

            // On utilise la méthode patch du SupabaseClient
            supabaseClient.patch(url, json, new SupabaseClient.SupabaseCallback() {
                @Override
                public void onSuccess(String response) {
                    callback.onSuccess("Client mis à jour avec succès");
                }
                @Override
                public void onError(String error) {
                    callback.onError("Erreur lors de la mise à jour : " + error);
                }
            });
        } catch (JSONException e) { callback.onError("Erreur JSON"); }
    }

    private void performActualInsert(Client client, SingleClientCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("nom", client.getNom());
            json.put("prenom", client.getPrenom());
            json.put("telephone", client.getTelephone());
            if (client.getAdresse() != null) json.put("adresse", client.getAdresse());

            supabaseClient.post(SupabaseConfig.CLIENTS_ENDPOINT, json, new SupabaseClient.SupabaseCallback() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        if (jsonArray.length() > 0) callback.onSuccess(parseClient(jsonArray.getJSONObject(0)));
                    } catch (JSONException e) { callback.onError("Erreur parsing insertion"); }
                }
                @Override
                public void onError(String error) { callback.onError(error); }
            });
        } catch (JSONException e) { callback.onError("Erreur JSON"); }
    }

    private Client parseClient(JSONObject json) throws JSONException {
        Client client = new Client();
        client.setId(json.getString("id"));
        client.setNom(json.getString("nom"));
        client.setPrenom(json.getString("prenom"));
        client.setTelephone(json.getString("telephone"));
        if (json.has("adresse") && !json.isNull("adresse")) client.setAdresse(json.getString("adresse"));
        return client;
    }

    public void shutdown() { if (supabaseClient != null) supabaseClient.shutdown(); }
}