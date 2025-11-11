package com.example.movrev;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Movie;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Searching extends AppCompatActivity {

    private EditText searchBar;
    private ImageButton backButton, searchButton;
    private ListView chooseView;
    private ArrayList<Movie> movieList;
    private ChooseAdapter adapter;

    String searching, fname, fid, fphoto, tag, releaseDate;
    Double rating;

//    SharedPreferences sharedPreferences2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose);

        backButton = findViewById(R.id.backButton);
        searchBar = findViewById(R.id.searchBar);
        searchButton = findViewById(R.id.searchButton);
        chooseView = findViewById(R.id.ChooseView);
//        sharedPreferences2 = getSharedPreferences("MyAppName",MODE_PRIVATE);

        movieList = new ArrayList<>();
        adapter = new ChooseAdapter(this, movieList);
        chooseView.setAdapter(adapter);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Searching.this,MainActivity.class);
                startActivity(intent);
                finish();
            } // Close the current activity
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searching = searchBar.getText().toString().trim();
                if (searching.isEmpty()) {
                    Toast.makeText(Searching.this, "请输入搜索关键字", Toast.LENGTH_SHORT).show();
                    return;
                }

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "http://10.0.2.2/searching.php";
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("searching", searching);
                } catch (JSONException e) {
                    //do nothing
                }
                final String requestBody = jsonBody.toString();

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("API",response);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String status = jsonObject.getString("status");
                                    String message = jsonObject.getString("message");
                                    if(status.equals("success")){
                                        movieList.clear();
                                        JSONArray movies = jsonObject.getJSONArray("movies");
                                        for (int i = 0; i < movies.length(); i++) {
                                            JSONObject movie = movies.getJSONObject(i);
                                            String ratingStr = movie.getString("rating");
                                            double rating = ratingStr.equals("N/A") ? 0.0 : movie.getDouble("rating");
                                            movieList.add(new Movie(
                                                    movie.getString("fid"),
                                                    movie.getString("fname"),
                                                    movie.getString("releaseDate"),
                                                    movie.getString("image"),
                                                    movie.getString("tname"),
                                                    rating,
                                                    ratingStr
                                            ));
                                        }
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(Searching.this, "搜索成功", Toast.LENGTH_SHORT).show();

//                                        // 将数据保存到 SharedPreferences
//                                        SharedPreferences.Editor editor = sharedPreferences2.edit();
//                                        editor.putString("tag", tag);
//                                        editor.putString("fid", fid);
//                                        editor.putString("fname", fname);
//                                        //editor.putString("fphoto", fphoto);
//                                        editor.putString("releaseDate", releaseDate);
//                                        editor.putString("rating", String.valueOf(rating));
//                                        editor.apply();

                                        // 转到下一个活动页面
//                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                                        startActivity(intent);
//                                        Toast.makeText(Searching.this, "Searching Success", Toast.LENGTH_SHORT).show();
//                                        finish();


                                    } else {
                                        Toast.makeText(Searching.this, "Some things Wrong", Toast.LENGTH_SHORT).show();
                                        // textViewError.setText(message);
                                        // textViewError.setVisibility(View.VISIBLE);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //progressBar.setVisibility(View.GONE);
                        //textViewError.setText(error.getLocalizedMessage());
                        //textViewError.setVisibility(View.VISIBLE);
                        Log.d("API",error.getMessage());
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> paramV = new HashMap<>();
                        paramV.put("name", searching);
                        return paramV;
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return requestBody == null ? null : requestBody.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                            return null;
                        }
                    }

                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        String responseString = "";
                        if (response != null) {
//                            responseString = String.valueOf(response.statusCode);
                            // can get more details such as response.headers
                            responseString = new String(response.data, StandardCharsets.UTF_8);
                        }
                        return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                    }



                };
                queue.add(stringRequest);
            }
        });
        chooseView.setOnItemClickListener((parent, view, position, id) -> {
            Movie movie = movieList.get(position);
            Intent intent = new Intent(Searching.this, MoreInformation.class);
            // 传递电影数据
            intent.putExtra("fid", movie.getFid());
            intent.putExtra("fname", movie.getFname());
            intent.putExtra("releaseDate", movie.getReleaseDate());
            intent.putExtra("image", movie.getImage());
            intent.putExtra("tname", movie.getTname());
            intent.putExtra("rating", movie.getRatingStr());
            startActivity(intent);
        });
    }
    public static class Movie {
        private String fid, fname, releaseDate, image, tname, ratingStr;
        private double rating;

        public Movie(String fid, String fname, String releaseDate, String image, String tname, double rating, String ratingStr) {
            this.fid = fid;
            this.fname = fname;
            this.releaseDate = releaseDate;
            this.image = image; // Base64 字符串
            this.tname = tname;
            this.rating = rating;
            this.ratingStr = ratingStr;
        }

        public String getFid() { return fid; }
        public String getFname() { return fname; }
        public String getReleaseDate() { return releaseDate; }
        public String getImage() { return image; }
        public String getTname() { return tname; }
        public double getRating() { return rating; }
        public String getRatingStr() { return ratingStr; }
    }
}
