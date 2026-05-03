package com.example.tastebookapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvBonjour, tvNom, tvEmail, tvBio;
    private TextView tvAvatar, tvNbRecettes, tvNbFavoris, tvNoteMoy;
    private Button btnDeconnecter;
    
    // Custom Navigation Views
    private View navHome, navSearch, navAdd, navFav, navProfile;

    private ListenerRegistration profileListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvBonjour       = findViewById(R.id.tvBonjour);
        tvAvatar        = findViewById(R.id.tvAvatar);
        tvNom           = findViewById(R.id.tvNom);
        tvEmail         = findViewById(R.id.tvEmail);
        tvBio           = findViewById(R.id.tvBio);
        tvNbRecettes    = findViewById(R.id.tvNbRecettes);
        tvNbFavoris     = findViewById(R.id.tvNbFavoris);
        tvNoteMoy       = findViewById(R.id.tvNoteMoy);
        btnDeconnecter  = findViewById(R.id.btnDeconnecter);

        setupCustomNavigation();

        btnDeconnecter.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Déconnexion")
                    .setMessage("Voulez-vous vraiment vous déconnecter ?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(this, "Déconnecté", Toast.LENGTH_SHORT).show();
                        // Redirect to Login here if you have a LoginActivity
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });
    }

    private void setupCustomNavigation() {
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navAdd = findViewById(R.id.nav_add);
        navFav = findViewById(R.id.nav_fav);
        navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> {
            // Navigate to Home/Main Activity if implemented
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
        });

        navSearch.setOnClickListener(v -> {
            Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
        });

        navAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, NewRecipeActivity.class));
        });

        navFav.setOnClickListener(v -> {
            Toast.makeText(this, "Favorites", Toast.LENGTH_SHORT).show();
        });

        navProfile.setOnClickListener(v -> {
            // Already on Profile
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupProfileRealtimeUpdate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (profileListener != null) {
            profileListener.remove();
        }
    }

    private void setupProfileRealtimeUpdate() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = "test_users_001"; // Default test ID

        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        profileListener = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .addSnapshotListener((document, error) -> {
                if (error != null) {
                    Toast.makeText(this, "Erreur : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (document != null && document.exists()) {
                    String prenom      = document.getString("prenom");
                    String nom         = document.getString("nom");
                    String mail        = document.getString("email");
                    String description = document.getString("description");
                    Long recettes      = document.getLong("nbRecettes");
                    Long favoris       = document.getLong("nbFavoris");
                    Double note        = document.getDouble("noteMoyenne");

                    if (prenom != null) {
                        tvBonjour.setText("Bonjour, " + prenom);
                        tvAvatar.setText(String.valueOf(prenom.charAt(0)).toUpperCase());
                    }
                    tvNom.setText(nom != null ? (prenom != null ? prenom + " " + nom : nom) : "");
                    tvEmail.setText(mail != null ? mail : "");
                    
                    if (description != null && !description.isEmpty()) {
                        tvBio.setText(description);
                    } else {
                        tvBio.setText("Pas de description");
                    }

                    tvNbRecettes.setText(String.valueOf(recettes != null ? recettes : 0));
                    tvNbFavoris.setText(String.valueOf(favoris != null ? favoris : 0));

                    if (tvNoteMoy != null) {
                        tvNoteMoy.setText(String.format(Locale.getDefault(), "%.1f", note != null ? note : 0.0));
                    }
                }
            });
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) { return null; }
    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) { return null; }
}
