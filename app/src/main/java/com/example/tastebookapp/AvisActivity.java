package com.example.tastebookapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AvisActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerAvis;
    private AvisAdapter adapter;
    private List<Map<String, Object>> avisList = new ArrayList<>();
    private static final String USER_ID = "test_user_001";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avis);

        toolbar      = findViewById(R.id.toolbar);
        recyclerAvis = findViewById(R.id.recyclerAvis);

        // Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // RecyclerView setup
        // LinearLayoutManager : affiche les items en liste verticale
        recyclerAvis.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AvisAdapter(this, avisList);
        recyclerAvis.setAdapter(adapter);

        // Charger les avis depuis Firestore
        chargerAvis();
    }

    private void chargerAvis() {
        FirebaseFirestore.getInstance()
                .collection("avis")
                .whereEqualTo("userId", USER_ID)
                .get(Source.SERVER)
                .addOnSuccessListener(querySnapshot -> {
                    avisList.clear();
                    for (var doc : querySnapshot.getDocuments()) {
                        avisList.add(doc.getData());
                    }
                    // Notifie l'adapter que les données ont changé
                    adapter.notifyDataSetChanged();

                    if (avisList.isEmpty()) {
                        Toast.makeText(this, "Aucun avis pour l'instant",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur : " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) { return null; }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) { return null; }
}