package com.example.tastebookapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class NewRecipeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private LinearLayout layoutAddPhoto;
    private ImageView ivPhotoRecette;
    private FloatingActionButton fabCamera;
    private EditText etTitre, etTemps, etIngredients, etEtapes;
    private Spinner spinnerCategorie;
    private Button btnEnregistrer;
    
    // Custom Navigation Views
    private View navHome, navSearch, navAdd, navFav, navProfile;

    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private Uri photoUri;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_recipe);

        toolbar              = findViewById(R.id.toolbar);
        layoutAddPhoto       = findViewById(R.id.layoutAddPhoto);
        ivPhotoRecette       = findViewById(R.id.ivPhotoRecette);
        fabCamera            = findViewById(R.id.fabCamera);
        etTitre              = findViewById(R.id.etTitre);
        spinnerCategorie     = findViewById(R.id.spinnerCategorie);
        etTemps              = findViewById(R.id.etTemps);
        etIngredients        = findViewById(R.id.etIngredients);
        etEtapes             = findViewById(R.id.etEtapes);
        btnEnregistrer       = findViewById(R.id.btnEnregistrer);

        setupCustomNavigation();

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        String[] categories = {"Select", "Appetizer", "Main Course", "Dessert", "Drink", "Soup"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategorie.setAdapter(adapter);

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                afficherPhoto(uri);
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success && photoUri != null) {
                selectedImageUri = photoUri;
                afficherPhoto(photoUri);
            }
        });

        layoutAddPhoto.setOnClickListener(v -> ouvrirChoixPhoto());
        fabCamera.setOnClickListener(v -> ouvrirChoixPhoto());

        btnEnregistrer.setOnClickListener(v -> saveRecipe());
    }

    private void setupCustomNavigation() {
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navAdd = findViewById(R.id.nav_add);
        navFav = findViewById(R.id.nav_fav);
        navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show());
        navSearch.setOnClickListener(v -> Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show());
        navAdd.setOnClickListener(v -> {
            // Already on Add Recipe
        });
        navFav.setOnClickListener(v -> Toast.makeText(this, "Favorites", Toast.LENGTH_SHORT).show());
        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }

    private void saveRecipe() {
        String titre = etTitre.getText().toString().trim();
        String categorie = spinnerCategorie.getSelectedItem().toString();
        String temps = etTemps.getText().toString().trim();
        String ingredients = etIngredients.getText().toString().trim();
        String etapes = etEtapes.getText().toString().trim();

        if (titre.isEmpty()) {
            etTitre.setError(getString(R.string.error_title_required));
            etTitre.requestFocus();
            return;
        }
        if (categorie.equals("Select")) {
            Toast.makeText(this, getString(R.string.error_category_required), Toast.LENGTH_SHORT).show();
            return;
        }
        if (temps.isEmpty()) {
            etTemps.setError(getString(R.string.error_time_required));
            etTemps.requestFocus();
            return;
        }
        if (ingredients.isEmpty()) {
            etIngredients.setError(getString(R.string.error_ingredients_required));
            etIngredients.requestFocus();
            return;
        }
        if (etapes.isEmpty()) {
            etEtapes.setError(getString(R.string.error_steps_required));
            etEtapes.requestFocus();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.saving_in_progress));
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (selectedImageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("recipe_images/" + UUID.randomUUID().toString());

            storageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        uploadRecipeData(titre, categorie, temps, ingredients, etapes, uri.toString(), progressDialog);
                    }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Upload error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            uploadRecipeData(titre, categorie, temps, ingredients, etapes, "", progressDialog);
        }
    }

    private void uploadRecipeData(String titre, String categorie, String temps, String ingredients, String etapes, String imageUrl, ProgressDialog progressDialog) {
        String userId = "test_users_001";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        final String finalUserId = userId;
        Recipe recipe = new Recipe(titre, categorie, temps, ingredients, etapes, userId, imageUrl);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("recipes")
                .add(recipe)
                .addOnSuccessListener(documentReference -> {
                    db.collection("users").document(finalUserId)
                            .update("nbRecettes", FieldValue.increment(1))
                            .addOnCompleteListener(task -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, getString(R.string.recipe_added_success), Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void afficherPhoto(Uri uri) {
        layoutAddPhoto.setVisibility(View.GONE);
        ivPhotoRecette.setVisibility(View.VISIBLE);
        ivPhotoRecette.setImageURI(uri);
    }

    private void ouvrirChoixPhoto() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_add_photo_title)
                .setItems(new String[]{getString(R.string.option_take_photo), getString(R.string.option_choose_gallery)}, (dialog, which) -> {
                    if (which == 0) ouvrirCamera();
                    else galleryLauncher.launch("image/*");
                }).show();
    }

    private void ouvrirCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            java.io.File photoFile = new java.io.File(getExternalFilesDir(null), "recipe_photo.jpg");
            photoUri = androidx.core.content.FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
            cameraLauncher.launch(photoUri);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
