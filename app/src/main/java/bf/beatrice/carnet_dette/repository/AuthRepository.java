package bf.beatrice.carnet_dette.repository;

import android.util.Log;
import bf.beatrice.carnet_dette.api.SupabaseClient;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response; // Assure-toi que c'est bien okhttp3.Response
import java.io.IOException;
import org.json.JSONObject;

public class AuthRepository {

    private final SupabaseClient supabaseClient;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public AuthRepository() {
        this.supabaseClient = new SupabaseClient();
    }

    public interface AuthCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public void signUp(String email, String password, AuthCallback callback) {
        try {
            // Préparation du corps de la requête en JSON
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("password", password);

            RequestBody body = RequestBody.create(json.toString(), JSON);

            // Construction manuelle de la requête vers l'endpoint Auth de Supabase
            // URL format: https://votre-projet.supabase.co/auth/v1/signup
            Request request = new Request.Builder()
                    .url(SupabaseClient.BASE_URL + "/auth/v1/signup")
                    .addHeader("apikey", SupabaseClient.API_KEY)
                    .addHeader("Authorization", "Bearer " + SupabaseClient.API_KEY)
                    .post(body)
                    .build();

            // Utilisation du client HTTP interne de ton SupabaseClient
            supabaseClient.getClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Erreur réseau : " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // Ici, errorBody() devrait fonctionner si l'import okhttp3.Response est bon
                    if (response.isSuccessful()) {
                        callback.onSuccess("Vérifiez votre boîte mail pour confirmer l'inscription.");
                    } else {
                        String errorResponse = response.body() != null ? response.body().string() : "Erreur inconnue";
                        Log.e("AUTH_ERROR", errorResponse);
                        callback.onError("Erreur : " + errorResponse);
                    }
                    response.close();
                }
            });

        } catch (Exception e) {
            callback.onError("Erreur de préparation : " + e.getMessage());
        }
    }
}