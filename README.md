Carnet de Dettes Numérique - Application Android

Description du projet
Application mobile Android de gestion des dettes et paiements pour boutiques de quartier, permettant de remplacer les carnets papiers traditionnels par une solution numérique centralisée.

Fonctionnalités principales
 Authentification
Connexion sécurisée avec Supabase Auth

Persistance de session

Interface de connexion intuitive

 Gestion des clients
Ajout, modification et suppression de clients

Informations : Nom, prénom, téléphone, adresse (optionnelle)

Recherche en temps réel

Liste avec design Material Design

 Gestion des dettes
Enregistrement des dettes par client

Vérification des dettes existantes

Augmentation des dettes existantes

Statuts : EN_COURS / PAYEE

Historique complet

 Gestion des paiements
Enregistrement des remboursements

Trois modes de paiement : Espèces, Mobile Money, Virement

Validation des montants (ne peut pas dépasser le reste)

Calcul automatique du solde restant

 Tableau de bord
Statistiques globales (nombre de clients, dettes, montants)

Graphiques interactifs (camembert et barres)

Top 5 des clients les plus endettés

Montant total payé vs restant

 Navigation
Menu latéral (Navigation Drawer)

Interface intuitive et responsive

Retour arrière avec confirmation

 Architecture technique
Stack technique
Langage : Java

Architecture : MVC avec repositories

Base de données : Supabase (PostgreSQL)

Authentification : Supabase Auth

Graphiques : MPAndroidChart

UI : Material Design Components

Structure des packages
text
bf.beatrice.carnet_dette
activities           # Activités Android
adapters            # Adapteurs RecyclerView
models              # Modèles de données
repository          # Couche d'accès aux données
api                # Client HTTP Supabase
config              # Configuration
Modèles de données
Client : id, nom, prénom, téléphone, adresse

Dette : id, client_id, description, montant, statut, date

Paiement : id, dette_id, montant, mode_paiement, date

 Installation et configuration
Prérequis
Android Studio (version récente)

JDK 11 ou supérieur

Compte Supabase gratuit

Étapes d'installation
Cloner le projet

bash
git clone [URL_DU_PROJET]
cd carnet_dette
Configurer Supabase

Créer un projet sur supabase.com

Créer les tables suivantes :

clients (id, nom, prenom, telephone, adresse, created_at)

dettes (id, client_id, client_nom, description, montant, statut, date_dette, created_at)

paiements (id, dette_id, client_nom, montant, mode_paiement, date_paiement, created_at)

utilisateurs (id, email, role, created_at)

Configurer l'application

Ouvrir config/SupabaseConfig.java

Remplacer les constantes avec vos informations :

java
public static final String BASE_URL = "https://VOTRE_PROJET.supabase.co";
public static final String API_KEY = "VOTRE_CLE_API_SUPABASE";
Compiler et exécuter

Ouvrir le projet dans Android Studio

Synchroniser Gradle

Exécuter sur un émulateur ou appareil physique

 Utilisation
1. Connexion
Saisir email et mot de passe

La session est automatiquement sauvegardée

2. Ajouter un client
Cliquer sur le bouton "+" en bas à droite

Remplir les informations du client

Valider

3. Créer une dette
Dans la liste des clients, cliquer sur "Voir"

Cliquer sur le bouton "+" pour ajouter une dette

Saisir la description et le montant

4. Enregistrer un paiement
Depuis les détails d'une dette, cliquer sur le bouton "+"

Choisir le mode de paiement

Saisir le montant (validé automatiquement)

5. Consulter les statistiques
Ouvrir le menu latéral

Sélectionner "Tableau de bord"

Visualiser les graphiques et statistiques

🔧 Développement
Ajouter une nouvelle fonctionnalité
Créer l'activité dans le package activities

Créer le layout XML dans res/layout

Ajouter le modèle si nécessaire dans models

Créer l'adaptateur si nécessaire dans adapters

Mettre à jour le repository correspondant

Tests
Tester chaque écran manuellement

Vérifier les validations de formulaires

Tester les scénarios d'erreur réseau

Valider les calculs financiers

📝 Bonnes pratiques appliquées
Code
Séparation des responsabilités

Noms explicites en français

Logs de débogage complets

Gestion d'erreurs avec messages utilisateur

UI/UX
Design Material Design cohérent

Feedback utilisateur (Toasts, loaders)

Validation en temps réel

Navigation intuitive

Performance
RecyclerView pour les listes longues

Appels réseau asynchrones

Fermeture des ressources

Cache des données

 Dépannage
Problèmes courants
Connexion à Supabase échoue

Vérifier la clé API et l'URL dans SupabaseConfig.java

Vérifier la connexion Internet

Liste vide malgré des données

Vérifier les logs pour les erreurs réseau

Contrôler la structure des données dans Supabase

L'application plante

Vérifier les logs Android Studio

Vérifier les permissions Internet dans AndroidManifest.xml

Logs de débogage
Tous les logs sont préfixés avec le nom de l'activité

Les erreurs sont affichées avec et les succès avec 



 Licence
Projet académique - Université Joseph Ki Zerbo

 Auteurs
Binôme de développement : Ouedraogo Béactrice, Ouedraogo Abdoul Razaque

Encadrant : Marcus Kaborét



 
