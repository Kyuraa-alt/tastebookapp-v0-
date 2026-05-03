package com.example.tastebookapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecetteViewHolder> {

    private List<DocumentSnapshot> recetteList;
    private Context context;

    // Interface pour gérer le clic sur une recette
    public interface OnRecetteClickListener {
        void onRecetteClick(DocumentSnapshot recette);
    }

    private OnRecetteClickListener listener;

    public RecipeAdapter(Context context, List<DocumentSnapshot> recetteList,
                          OnRecetteClickListener listener) {
        this.context     = context;
        this.recetteList = recetteList;
        this.listener    = listener;
    }

    @NonNull
    @Override
    public RecetteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_recipe, parent, false);
        return new RecetteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecetteViewHolder holder, int position) {
        DocumentSnapshot doc = recetteList.get(position);

        String titre     = doc.getString("titre");
        String categorie = doc.getString("categorie");
        String temps     = doc.getString("temps");

        // Titre
        holder.tvTitreRecette.setText(titre != null ? titre : "Sans titre");

        // Catégorie
        holder.tvCategorie.setText(categorie != null ? categorie : "");

        // Temps
        holder.tvTemps.setText(temps != null ? "⏱ " + temps : "");

        // Date de création
        if (doc.getTimestamp("dateCreation") != null) {
            Date date = doc.getTimestamp("dateCreation").toDate();
            String dateFormatee = new SimpleDateFormat(
                    "dd MMM yyyy", Locale.FRENCH).format(date);
            holder.tvDate.setText("Ajoutée le " + dateFormatee);
        } else {
            holder.tvDate.setText("");
        }

        // Icône catégorie — emoji selon la catégorie
        String emoji = getEmojiCategorie(categorie);
        holder.tvCategorieIcon.setText(emoji);

        // Couleur de fond de l'icône selon la catégorie
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(12f);
        shape.setColor(Color.parseColor(getCouleurCategorie(categorie)));
        holder.tvCategorieIcon.setBackground(shape);

        // Clic sur la recette
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRecetteClick(doc);
        });
    }

    // Retourne un emoji selon la catégorie
    private String getEmojiCategorie(String categorie) {
        if (categorie == null) return "🍽";
        switch (categorie) {
            case "Entrée":   return "🥗";
            case "Plat":     return "🍲";
            case "Dessert":  return "🍰";
            case "Boisson":  return "🥤";
            case "Soupe":    return "🍜";
            default:         return "🍽";
        }
    }

    // Retourne une couleur selon la catégorie
    private String getCouleurCategorie(String categorie) {
        if (categorie == null) return "#2E6B3E";
        switch (categorie) {
            case "Entrée":   return "#3A7BD5";
            case "Plat":     return "#2E6B3E";
            case "Dessert":  return "#E8543A";
            case "Boisson":  return "#8E44AD";
            case "Soupe":    return "#E67E22";
            default:         return "#2E6B3E";
        }
    }

    @Override
    public int getItemCount() { return recetteList.size(); }

    // Met à jour la liste (utilisé pour la recherche)
    public void updateListe(List<DocumentSnapshot> nouvelleListe) {
        this.recetteList = nouvelleListe;
        notifyDataSetChanged();
    }

    static class RecetteViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategorieIcon, tvTitreRecette, tvCategorie, tvTemps, tvDate;

        public RecetteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategorieIcon  = itemView.findViewById(R.id.tvCategorieIcon);
            tvTitreRecette   = itemView.findViewById(R.id.tvTitreRecette);
            tvCategorie      = itemView.findViewById(R.id.tvCategorie);
            tvTemps          = itemView.findViewById(R.id.tvTemps);
            tvDate           = itemView.findViewById(R.id.tvDate);
        }
    }
}