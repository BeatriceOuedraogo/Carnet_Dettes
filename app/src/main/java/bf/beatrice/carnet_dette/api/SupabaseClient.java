package bf.beatrice.carnet_dette.api;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseClient {
    // Constantes d'accès
    public static final String BASE_URL = "https://zeeusywosqmxdnnrnuff.supabase.co";
    public static final String API_KEY ="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InplZXVzeXdvc3FteGRubnJudWZmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njg0MTg5MTksImV4cCI6MjA4Mzk5NDkxOX0.IfSSshrZI8RNOrPGKy9iPWtAudKOuYMN0qHfKEm-2V0";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public SupabaseClient() {
        this.client = new OkHttpClient();
        this.executorService = Executors.newFixedThreadPool(4);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // INTERFACE CALLBACK
    public interface SupabaseCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    // MÉTHODE MANQUANTE QUI CAUSAIT L'ERREUR
    public OkHttpClient getClient() {
        return this.client;
    }

    // --- MÉTHODES DE REQUÊTES ---

    public void get(String url, SupabaseCallback callback) {
        executorService.execute(() -> {
            try {
                Request request = new Request.Builder().url(url)
                        .addHeader("apikey", API_KEY)
                        .addHeader("Authorization", "Bearer " + API_KEY)
                        .get().build();
                Response response = client.newCall(request).execute();
                String res = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) mainHandler.post(() -> callback.onSuccess(res));
                else mainHandler.post(() -> callback.onError("Erreur " + response.code()));
            } catch (IOException e) { mainHandler.post(() -> callback.onError(e.getMessage())); }
        });
    }

    public void post(String url, String jsonData, SupabaseCallback callback) {
        executorService.execute(() -> {
            try {
                RequestBody body = RequestBody.create(jsonData, JSON);
                Request request = new Request.Builder().url(url)
                        .addHeader("apikey", API_KEY)
                        .addHeader("Authorization", "Bearer " + API_KEY)
                        .addHeader("Prefer", "return=representation")
                        .post(body).build();
                Response response = client.newCall(request).execute();
                String res = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) mainHandler.post(() -> callback.onSuccess(res));
                else mainHandler.post(() -> callback.onError("Erreur " + response.code()));
            } catch (IOException e) { mainHandler.post(() -> callback.onError(e.getMessage())); }
        });
    }

    public void post(String url, JSONObject data, SupabaseCallback cb) {
        post(url, data.toString(), cb);
    }

    public void patch(String url, JSONObject data, SupabaseCallback callback) {
        executorService.execute(() -> {
            try {
                RequestBody body = RequestBody.create(data.toString(), JSON);
                Request request = new Request.Builder().url(url)
                        .addHeader("apikey", API_KEY)
                        .addHeader("Authorization", "Bearer " + API_KEY)
                        .addHeader("Prefer", "return=representation")
                        .patch(body).build();
                Response response = client.newCall(request).execute();
                String res = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) mainHandler.post(() -> callback.onSuccess(res));
                else mainHandler.post(() -> callback.onError("Erreur " + response.code()));
            } catch (IOException e) { mainHandler.post(() -> callback.onError(e.getMessage())); }
        });
    }

    public void delete(String url, SupabaseCallback callback) {
        executorService.execute(() -> {
            try {
                Request request = new Request.Builder().url(url)
                        .addHeader("apikey", API_KEY)
                        .addHeader("Authorization", "Bearer " + API_KEY)
                        .delete().build();
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) mainHandler.post(() -> callback.onSuccess("OK"));
                else mainHandler.post(() -> callback.onError("Erreur " + response.code()));
            } catch (IOException e) { mainHandler.post(() -> callback.onError(e.getMessage())); }
        });
    }

    public void shutdown() { executorService.shutdown(); }
}