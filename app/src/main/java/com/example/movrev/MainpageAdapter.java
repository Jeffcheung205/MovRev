package com.example.movrev;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainpageAdapter extends RecyclerView.Adapter<MainpageAdapter.ViewHolder> {
    private Context context;
    private ArrayList<MainActivity.Movie> movies;

    public MainpageAdapter(Context context, ArrayList<MainActivity.Movie> movies) {
        this.context = context;
        this.movies = movies;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recommendlistview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainActivity.Movie movie = movies.get(position);

        holder.title.setText(movie.getFname());

        if (!movie.getImage().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(movie.getImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    holder.poster.setImageBitmap(bitmap);
                } else {
                    holder.poster.setImageResource(R.color.black);
                }
            } catch (Exception e) {
                Log.e("MainpageAdapter", "Error decoding image for fid: " + movie.getFid(), e);
                holder.poster.setImageResource(R.color.black);
            }
        } else {
            holder.poster.setImageResource(R.color.black);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MoreInformation.class);
            intent.putExtra("fid", movie.getFid());
            intent.putExtra("fname", movie.getFname());
            intent.putExtra("releaseDate", movie.getReleaseDate());
            intent.putExtra("image", movie.getImage());
            intent.putExtra("tname", movie.getTname());
            intent.putExtra("rating", movie.getRating());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;

        ViewHolder(View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.poster);
            title = itemView.findViewById(R.id.title);
        }
    }
}