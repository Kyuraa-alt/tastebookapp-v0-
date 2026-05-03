package com.example.tastebookapp;

public class Recipe {
    private String titre;
    private String categorie;
    private String temps;
    private String ingredients;
    private String etapes;
    private String userId;
    private String imageUrl; // Nouveau champ pour l'URL de l'image
    private long timestamp;

    public Recipe() {
        // Obligatoire pour Firebase Firestore
    }

    public Recipe(String titre, String categorie, String temps, String ingredients, String etapes, String userId, String imageUrl) {
        this.titre = titre;
        this.categorie = categorie;
        this.temps = temps;
        this.ingredients = ingredients;
        this.etapes = etapes;
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters et Setters
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getTemps() { return temps; }
    public void setTemps(String temps) { this.temps = temps; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getEtapes() { return etapes; }
    public void setEtapes(String etapes) { this.etapes = etapes; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
