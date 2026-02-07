package bf.beatrice.carnet_dette.models;

import java.util.Date;

public class Paiement {

    private String id;
    private String dette_id;
    private String client_nom;
    private double montant;
    private String mode_paiement;  // "ESPECES", "MOBILE_MONEY", "VIREMENT"
    private String date_paiement;

    private String created_at;

    // Constructeur vide
    public Paiement() {
    }

    // Constructeur pour nouveau paiement
    public Paiement(String dette_id, String client_nom,double montant,String mode_paiement) {
        this.dette_id = dette_id;
        this.client_nom = client_nom;
        this.montant = montant;
        this.mode_paiement = mode_paiement;

    }

    // Constructeur complet (pour données de la BDD)
    public Paiement( String id,String dette_id,String client_nom, double montant, String mode_paiement,
                    String date_paiement, String created_at) {
        this.id=id;
        this.dette_id = dette_id;
        this.client_nom = client_nom;
        this.montant = montant;
        this.mode_paiement = mode_paiement;
        this.date_paiement = date_paiement;
        this.created_at = created_at;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDette_id() {
        return dette_id;
    }

    public void setDette_id(String dette_id) {
        this.dette_id = dette_id;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getMode_paiement() {
        return mode_paiement;
    }

    public void setMode_paiement(String mode_paiement) {
        this.mode_paiement = mode_paiement;
    }

    public String getDate_paiement() {
        return date_paiement;
    }

    public void setDate_paiement(String date_paiement) {
        this.date_paiement = date_paiement;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
    public String getClientNom() { return client_nom; }
    public void setClientNom(String client_nom) { this.client_nom = client_nom; }

    @Override
    public String toString() {
        return montant + " FCFA (" + mode_paiement + ")";
    }
}
