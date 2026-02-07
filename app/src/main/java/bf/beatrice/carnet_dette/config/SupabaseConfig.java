package bf.beatrice.carnet_dette.config;


public class SupabaseConfig {
    // Vos clés Supabase
    public static final String BASE_URL = "https://zeeusywosqmxdnnrnuff.supabase.co";
    public static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InplZXVzeXdvc3FteGRubnJudWZmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njg0MTg5MTksImV4cCI6MjA4Mzk5NDkxOX0.IfSSshrZI8RNOrPGKy9iPWtAudKOuYMN0qHfKEm-2V0";

    // Endpoints API
    public static final String CLIENTS_ENDPOINT = BASE_URL + "/rest/v1/clients";
    public static final String DETTES_ENDPOINT = BASE_URL + "/rest/v1/dettes";
    public static final String PAIEMENTS_ENDPOINT = BASE_URL + "/rest/v1/paiements";
    public static final String UTILISATEURS_ENDPOINT = BASE_URL + "/rest/v1/utilisateurs";

    // Rôles
    public static final String ROLE_PATRON = "PATRON";
    public static final String ROLE_VENDEUR = "VENDEUR";
}
