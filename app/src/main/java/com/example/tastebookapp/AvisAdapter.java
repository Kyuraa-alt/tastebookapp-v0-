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
import java.util.List;
import java.util.Map;

public class AvisAdapter extends RecyclerView.Adapter<AvisAdapter.AvisViewHolder> {

    private List<Map<String, Object>> avisList;
    private Context context;

    public AvisAdapter(Context context, List<Map<String, Object>> avisList) {
        this.context = context;
        this.avisList = avisList;
    }

    @NonNull
    @Override
    public AvisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_avis, parent, false);
        return new AvisViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvisViewHolder holder, int position) {
        Map<String, Object> avis = avisList.get(position);

        // Initiale de l'avatar
        String auteur = (String) avis.get("auteur");
        holder.tvAvatar.setText(String.valueOf(auteur.charAt(0)).toUpperCase());

        // Couleur dynamique de l'avatar selon avatarCouleur
        String couleur = (String) avis.get("avatarCouleur");
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(Color.parseColor(couleur != null ? couleur : "#2E6B3E"));
        holder.tvAvatar.setBackground(shape);

        holder.tvAuteur.setText(auteur);
        holder.tvDate.setText((String) avis.get("date"));
        holder.tvRecette.setText("🍽 " + avis.get("recette"));
        holder.tvCommentaire.setText((String) avis.get("commentaire"));

        // Génère les étoiles selon la note (ex: note 4 → "★★★★☆")
        long note = (long) (avis.get("note") != null ? avis.get("note") : 0);
        StringBuilder etoiles = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            etoiles.append(i <= note ? "★" : "☆");
        }
        holder.tvNote.setText(etoiles.toString());
    }

    @Override
    public int getItemCount() { return avisList.size(); }

    static class AvisViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvAuteur, tvDate, tvRecette, tvCommentaire, tvNote;

        public AvisViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar      = itemView.findViewById(R.id.tvAvatar);
            tvAuteur      = itemView.findViewById(R.id.tvAuteur);
            tvDate        = itemView.findViewById(R.id.tvDate);
            tvRecette     = itemView.findViewById(R.id.tvRecette);
            tvCommentaire = itemView.findViewById(R.id.tvCommentaire);
            tvNote        = itemView.findViewById(R.id.tvNote);
        }
    }
}