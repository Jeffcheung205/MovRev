package com.example.movrev;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private TextView loginButton, TypeTextView;
    private ArrayList<Movie> recommendMovies, rankingMovies, likedTypeMovies;
    private RecyclerView recommendRecyclerView, rankingRecyclerView, likedTypeRecyclerView;
    private MainpageAdapter recommendAdapter, rankingAdapter, likedTypeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        loginButton = findViewById(R.id.loginButton);
        updateLoginButton();

        recommendRecyclerView = findViewById(R.id.recommendRecyclerView);
        rankingRecyclerView = findViewById(R.id.rankingRecyclerView);
        likedTypeRecyclerView = findViewById(R.id.likedTypeRecyclerView);

        TypeTextView = findViewById(R.id.postersLabel);

        recommendMovies = new ArrayList<>();
        rankingMovies = new ArrayList<>();
        likedTypeMovies = new ArrayList<>();

        recommendAdapter = new MainpageAdapter(this, recommendMovies);
        rankingAdapter = new MainpageAdapter(this, rankingMovies);
        likedTypeAdapter = new MainpageAdapter(this, likedTypeMovies);

        recommendRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rankingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        likedTypeRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        recommendRecyclerView.setAdapter(recommendAdapter);
        rankingRecyclerView.setAdapter(rankingAdapter);
        likedTypeRecyclerView.setAdapter(likedTypeAdapter);


        String uid = sharedPreferences.getString("uid", "0");
        if (uid != null) {
            TypeTextView.setText("Guess your favourite type of film:");
        }else {
            TypeTextView.setText("Most popular type of movie:");
        }

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Searching.class);
            startActivity(intent);
        });

        ImageButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            if (sharedPreferences.getString("logged", "false").equals("false")) {
                Toast.makeText(MainActivity.this, "Please log in first", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MainActivity.this, AddMovie.class);
                startActivity(intent);
            }
        });

        loadMovies();
        triggerRecommendation();
    }

    private void updateLoginButton() {
        String userName = sharedPreferences.getString("userName", null);
        if (userName == null || userName.isEmpty()) {
            loginButton.setText("Hello User！（Click here to login");
            loginButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            });
        } else {
            loginButton.setText("Hello User" + userName + "!Click here logout)");
            loginButton.setOnClickListener(v -> {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("logged", "false");
                editor.putString("userName", null);
                editor.putString("uid", null);
                editor.apply();
                updateLoginButton();
                Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadMovies() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2/Mainpage.php?uid=" + sharedPreferences.getString("uid", "0");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (status.equals("success")) {
                            rankingMovies.clear();
                            likedTypeMovies.clear();

                            recommendAdapter.notifyDataSetChanged();

                            JSONArray rankingArray = jsonObject.getJSONArray("getfilm");
                            for (int i = 0; i < rankingArray.length(); i++) {
                                JSONObject movie = rankingArray.getJSONObject(i);
                                String rating = movie.getString("rating");
                                rankingMovies.add(new Movie(
                                        movie.getString("fid"),
                                        movie.getString("fname"),
                                        movie.getString("releaseDate"),
                                        movie.getString("image"),
                                        movie.getString("tname"),
                                        rating.equals("N/A") ? "N/A" : String.valueOf(movie.getDouble("rating"))
                                ));
                            }
                            rankingAdapter.notifyDataSetChanged();

                            JSONArray recommendArray = jsonObject.getJSONArray("getfilmcount");
                            for (int i = 0; i < recommendArray.length(); i++) {
                                JSONObject movie = recommendArray.getJSONObject(i);
                                String rating = movie.getString("rating");
                                likedTypeMovies.add(new Movie(
                                        movie.getString("fid"),
                                        movie.getString("fname"),
                                        movie.getString("releaseDate"),
                                        movie.getString("image"),
                                        movie.getString("tname"),
                                        rating.equals("N/A") ? "N/A" : String.valueOf(movie.getDouble("rating"))
                                ));
                            }
                            likedTypeAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("MainActivity", "JSON parse error", e);
                        Toast.makeText(this, "解析错误", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("MainActivity", "Volley error", error);
                    Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show();
                });

        queue.add(stringRequest);
    }


    private void triggerRecommendation() {
        String fname = sharedPreferences.getString("last_movie", null);
        if (fname == null || fname.isEmpty()) {
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2/recommend.php?uid=" + sharedPreferences.getString("uid", "0") + "&fname=" + fname;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (status.equals("success")) {
                            recommendMovies.clear();
                            JSONArray films = jsonObject.getJSONArray("films");
                            for (int i = 0; i < films.length(); i++) {
                                JSONObject movie = films.getJSONObject(i);
                                String rating = movie.getString("rating");
                                recommendMovies.add(new Movie(
                                        movie.getString("fid"),
                                        movie.getString("fname"),
                                        movie.getString("releaseDate"),
                                        movie.getString("image"),
                                        movie.getString("tname"),
                                        rating.equals("N/A") ? "N/A" : String.valueOf(movie.getDouble("rating"))
                                ));
                            }
                            recommendAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("MainActivity", "JSON parse error", e);
                        Toast.makeText(this, "解析错误", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("MainActivity", "Volley error", error);
                    Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show();
                });

        queue.add(stringRequest);
    }

    public static class Movie {
        private String fid, fname, releaseDate, image, tname, rating;

        public Movie(String fid, String fname, String releaseDate, String image, String tname, String rating) {
            this.fid = fid;
            this.fname = fname;
            this.releaseDate = releaseDate;
            this.image = image;
            this.tname = tname;
            this.rating = rating;
        }

        public String getFid() { return fid; }
        public String getFname() { return fname; }
        public String getReleaseDate() { return releaseDate; }
        public String getImage() { return image; }
        public String getTname() { return tname; }
        public String getRating() { return rating; }
    }
}