package bf.beatrice.carnet_dette.models;



public class Utilisateur {
    private String id;
    private String nom;
    private String prenom;
    private String email;
    private String mot_de_passe;
    private String role;
    private String telephone;
    private boolean actif;
    private String created_at;

    // Constructeur vide
    public Utilisateur() {
    }

    // Constructeur complet
    public Utilisateur(String id, String nom,String prenom, String email, String role,
                       String telephone, boolean actif, String created_at) {
        this.id = id;
        this.nom = nom;
        this.prenom=prenom;
        this.email = email;
        this.role = role;
        this.telephone = telephone;
        this.actif = actif;
        this.created_at = created_at;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMot_de_passe() {
        return mot_de_passe;
    }

    public void setMot_de_passe(String mot_de_passe) {
        this.mot_de_passe = mot_de_passe;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public boolean isPatron() {
        return "PATRON".equals(role);
    }

    @Override
    public String toString() {
        return nom +" "+ prenom + " (" + role + ")";
    }
}
