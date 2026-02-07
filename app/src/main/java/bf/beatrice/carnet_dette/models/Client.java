package bf.beatrice.carnet_dette.models;

import java.util.Date;

public class Client {
    private String id;
    private String nom;
    private String prenom;
    private String telephone;
    private String adresse;
    private String created_at;

    // Constructeur vide (pour JSON parsing)
    public Client() {
    }

    // Constructeur pour nouveau client (sans id ni date)
    public Client(String nom, String prenom, String telephone, String adresse) {
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.adresse = adresse;
    }

    // Constructeur complet (pour données venant de la BDD)
    public Client(String id, String nom, String prenom, String telephone, String adresse, String created_at) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.adresse = adresse;
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

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        return nom + " " + prenom + " (" + telephone + ")";
    }
}
