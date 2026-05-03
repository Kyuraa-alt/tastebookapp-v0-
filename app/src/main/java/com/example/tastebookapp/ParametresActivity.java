package com.example.tastebookapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParametresActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FrameLayout frameAvatar;
    private ImageView ivProfilePicture;
    private TextView tvAvatarInitiale, tvCompteur;
    private EditText etDescription, etPrenom, etNom;
    private Button btnChangerPhoto, btnSauvegarderParametres;
    private SwitchMaterial switchDarkMode;

    private ActivityResultLauncher<String> galleryLauncher;
    private Uri selectedImageUri;

    private FirebaseFirestore db;
    private String userId = "test_users_001";

    private boolean isInitialCreation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametres);

        isInitialCreation = (savedInstanceState == null);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        toolbar                    = findViewById(R.id.toolbar);
        frameAvatar                = findViewById(R.id.frameAvatar);
        ivProfilePicture           = findViewById(R.id.ivProfilePicture);
        tvAvatarInitiale           = findViewById(R.id.tvAvatarInitiale);
        tvCompteur                 = findViewById(R.id.tvCompteur);
        etDescription              = findViewById(R.id.etDescription);
        etPrenom                   = findViewById(R.id.etPrenom);
        etNom                      = findViewById(R.id.etNom);
        btnChangerPhoto            = findViewById(R.id.btnChangerPhoto);
        btnSauvegarderParametres   = findViewById(R.id.btnSauvegarderDescription);
        switchDarkMode             = findViewById(R.id.switchDarkMode);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        afficherPhoto(uri);
                    }
                }
        );

        chargerDonneesProfil();

        int modeActuel = AppCompatDelegate.getDefaultNightMode();
        switchDarkMode.setChecked(modeActuel == AppCompatDelegate.MODE_NIGHT_YES);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int newMode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            if (AppCompatDelegate.getDefaultNightMode() != newMode) {
                AppCompatDelegate.setDefaultNightMode(newMode);
            }
        });

        etDescription.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCompteur.setText(s.length() + "/200");
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        frameAvatar.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnChangerPhoto.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        btnSauvegarderParametres.setOnClickListener(v -> sauvegarderTout());
    }

    private void chargerDonneesProfil() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String prenom = document.getString("prenom");
                        if (prenom != null && !prenom.isEmpty()) {
                            tvAvatarInitiale.setText(String.valueOf(prenom.charAt(0)).toUpperCase());
                            etPrenom.setText(prenom);
                        }

                        String nom = document.getString("nom");
                        if (nom != null) {
                            etNom.setText(nom);
                        }

                        String description = document.getString("description");
                        if (description != null) {
                            etDescription.setText(description);
                        }

                        Boolean darkMode = document.getBoolean("darkMode");
                        if (darkMode != null && isInitialCreation) {
                            switchDarkMode.setChecked(darkMode);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.stat_recipes) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void sauvegarderTout() {
        String prenom = etPrenom.getText().toString().trim();
        String nom = etNom.getText().toString().trim();

        if (prenom.isEmpty()) {
            etPrenom.setError(getString(R.string.error_first_name_required));
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.saving_in_progress));
        progressDialog.setCancelable(false);
        progressDialog.show();

        String description = etDescription.getText().toString().trim();
        boolean isDarkMode = switchDarkMode.isChecked();

        if (selectedImageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("profile_pictures/" + userId + "_" + UUID.randomUUID().toString());

            storageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateUserInFirestore(prenom, nom, description, isDarkMode, uri.toString(), progressDialog);
                    }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Upload error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            updateUserInFirestore(prenom, nom, description, isDarkMode, null, progressDialog);
        }
    }

    private void updateUserInFirestore(String prenom, String nom, String description, boolean darkMode, String photoUrl, ProgressDialog progressDialog) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("prenom", prenom);
        updates.put("nom", nom);
        updates.put("description", description);
        updates.put("darkMode", darkMode);
        if (photoUrl != null) {
            updates.put("photoProfilUrl", photoUrl);
        }

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, getString(R.string.settings_saved_success), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void afficherPhoto(Uri uri) {
        tvAvatarInitiale.setVisibility(View.GONE);
        ivProfilePicture.setVisibility(View.VISIBLE);
        ivProfilePicture.setImageURI(uri);
    }

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
