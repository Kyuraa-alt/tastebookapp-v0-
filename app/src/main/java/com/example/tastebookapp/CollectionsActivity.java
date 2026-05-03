package com.example.tastebookapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

public class CollectionsActivity extends AppCompatActivity {

    // ── VUES ──────────────────────────────────────────────────────────────
    private Toolbar toolbar;
    private RecyclerView recyclerRecettes;
    private EditText etRecherche;
    private TextView tvCompteurRecettes;
    private LinearLayout layoutVide;

    // ── ADAPTER & DONNÉES ─────────────────────────────────────────────────
    private RecipeAdapter adapter;
    private List<DocumentSnapshot> toutesLesRecettes = new ArrayList<>();
    private List<DocumentSnapshot> recettesFiltrees  = new ArrayList<>();

    // ── FIREBASE ───────────────────────────────────────────────────────────
    private FirebaseFirestore db;
    private static final String USER_ID = "test_user_001";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collections);

        db = FirebaseFirestore.getInstance();

        // ── LIAISON JAVA ↔ XML ─────────────────────────────────────────
        toolbar             = findViewById(R.id.toolbar);
        recyclerRecettes    = findViewById(R.id.recyclerRecettes);
        etRecherche         = findViewById(R.id.etRecherche);
        tvCompteurRecettes  = findViewById(R.id.tvCompteurRecettes);
        layoutVide          = findViewById(R.id.layoutVide);

        // ── TOOLBAR ────────────────────────────────────────────────────
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // ── RECYCLERVIEW ───────────────────────────────────────────────
        recyclerRecettes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this, recettesFiltrees, doc -> {
            // Clic sur une recette → affiche un Toast avec le titre
            // Plus tard tu pourras ouvrir une page de détail
            String titre = doc.getString("titre");
            Toast.makeText(this, "📖 " + titre, Toast.LENGTH_SHORT).show();
        });
        recyclerRecettes.setAdapter(adapter);

        // ── CHARGEMENT DES RECETTES ────────────────────────────────────
        chargerRecettes();

        // ── RECHERCHE EN TEMPS RÉEL ────────────────────────────────────
        etRecherche.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                filtrerRecettes(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // ── MÉTHODE : charger toutes les recettes depuis Firestore ────────────
    private void chargerRecettes() {
        db.collection("recettes")
                .whereEqualTo("userId", USER_ID)
                .orderBy("dateCreation", Query.Direction.DESCENDING)
                .get(Source.SERVER)
                .addOnSuccessListener(querySnapshot -> {
                    toutesLesRecettes.clear();
                    toutesLesRecettes.addAll(querySnapshot.getDocuments());

                    // Initialise la liste filtrée avec toutes les recettes
                    recettesFiltrees.clear();
                    recettesFiltrees.addAll(toutesLesRecettes);

                    adapter.notifyDataSetChanged();
                    mettreAJourUI();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Erreur : " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    // ── MÉTHODE : filtrer les recettes selon la recherche ─────────────────
    private void filtrerRecettes(String query) {
        List<DocumentSnapshot> resultats = new ArrayList<>();

        if (query.isEmpty()) {
            // Aucun filtre → affiche tout
            resultats.addAll(toutesLesRecettes);
        } else {
            // Filtre par titre ou catégorie (insensible à la casse)
            String queryLower = query.toLowerCase();
            for (DocumentSnapshot doc : toutesLesRecettes) {
                String titre     = doc.getString("titre");
                String categorie = doc.getString("categorie");

                boolean titreMatch     = titre != null &&
                        titre.toLowerCase().contains(queryLower);
                boolean categorieMatch = categorie != null &&
                        categorie.toLowerCase().contains(queryLower);

                if (titreMatch || categorieMatch) {
                    resultats.add(doc);
                }
            }
        }

        adapter.updateListe(resultats);
        mettreAJourCompteur(resultats.size());
        afficherOuCacherVide(resultats.isEmpty());
    }

    // ── MÉTHODE : met à jour le compteur et l'état vide ───────────────────
    private void mettreAJourUI() {
        int nb = recettesFiltrees.size();
        mettreAJourCompteur(nb);
        afficherOuCacherVide(nb == 0);
    }

    private void mettreAJourCompteur(int nb) {
        tvCompteurRecettes.setText(nb + (nb <= 1 ? " recette" : " recettes"));
    }

    private void afficherOuCacherVide(boolean vide) {
        layoutVide.setVisibility(vide ? View.VISIBLE : View.GONE);
        recyclerRecettes.setVisibility(vide ? View.GONE : View.VISIBLE);
    }

    // ── BOUTON RETOUR ←────────────────────────────────────────────────────
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) { return null; }
    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) { return null; }
}