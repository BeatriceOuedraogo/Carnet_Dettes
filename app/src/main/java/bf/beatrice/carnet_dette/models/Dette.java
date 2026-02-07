package bf.beatrice.carnet_dette.models;

import java.util.Date;

public class Dette {
    private String id;
    private String client_id;
    private String client_nom;
    private String description;
    private double montant;
    private String statut;
    private String date_dette;
    private String created_at;

    // Constructeur vide
    public Dette() {
    }

    // Constructeur pour nouvelle dette
    public Dette(String client_id, String client_nom,String description, double montant) {
        this.client_id = client_id;
        this.client_nom = client_nom;
        this.description = description;
        this.montant = montant;
        this.statut = "EN_COURS";
    }


    // Constructeur complet (pour données de la BDD)
    public Dette(String id, String client_id, String client_nom,String description, double montant,
                 String statut, String date_dette, String created_at) {
        this.id = id;
        this.client_id = client_id;
        this.client_nom = client_nom;
        this.description = description;
        this.montant = montant;
        this.statut = statut;
        this.date_dette = date_dette;
        this.created_at = created_at;
    }

    public String getClient_nom() {
        return client_nom;
    }

    public void setClient_nom(String client_nom) {
        this.client_nom = client_nom;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getDate_dette() {
        return date_dette;
    }

    public void setDate_dette(String date_dette) {
        this.date_dette = date_dette;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        return "Dette{" +
                "description='" + description + '\'' +
                ", montant=" + montant +
                ", statut='" + statut + '\'' +
                '}';
    }
}
