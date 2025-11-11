package com.example.movrev;

import android.content.Context;
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

import java.util.ArrayList;

public class ChooseAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Searching.Movie> movies;

    public ChooseAdapter(Context context, ArrayList<Searching.Movie> movies) {
        this.context = context;
        this.movies = movies;
    }

    @Override
    public int getCount() {
        return movies.size();
    }

    @Override
    public Object getItem(int position) {
        return movies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.searchlistview, parent, false);
        }

        Searching.Movie movie = movies.get(position);

        ImageView poster = convertView.findViewById(R.id.poster1);
        TextView title = convertView.findViewById(R.id.title);
        TextView releaseDate = convertView.findViewById(R.id.releaseDate);
        TextView genre = convertView.findViewById(R.id.genre);
        TextView rating = convertView.findViewById(R.id.rating);

        // 设置数据
        title.setText(movie.getFname());
        releaseDate.setText("Release Date: " + movie.getReleaseDate());
        genre.setText("Genre: " + (movie.getTname().isEmpty() ? "N/A" : movie.getTname()));
        rating.setText("Rate: " + (movie.getRatingStr().equals("N/A") ? "N/A" : movie.getRatingStr()));

        // 处理 Base64 图片
        if (!movie.getImage().isEmpty()) {
            Log.d("ChooseAdapter", "Image Base64 length for fid " + movie.getFid() + ": " + movie.getImage().length());
            try {
                Log.d("ChooseAdapter", "First 20 char : " + movie.getImage().substring(0, 20));
                byte[] decodedBytes = Base64.decode(movie.getImage(), Base64.DEFAULT);
                Log.d("ChooseAdapter", "Decoded bytes length for fid " + movie.getFid() + ": " + decodedBytes.length);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap == null) {
                    Log.e("ChooseAdapter", "Bitmap is null for fid: " + movie.getFid());
                    poster.setImageResource(R.color.black);
                } else {
                    Log.d("ChooseAdapter", "Bitmap created for fid: " + movie.getFid());
                    poster.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                Log.e("ChooseAdapter", "Error decoding image for fid: " + movie.getFid(), e);
                poster.setImageResource(R.color.black);
            }
        } else {
            Log.d("ChooseAdapter", "Empty image data for fid: " + movie.getFid());
            poster.setImageResource(R.color.black);
        }

        return convertView;
    }
}