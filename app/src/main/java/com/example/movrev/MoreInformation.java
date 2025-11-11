package com.example.movrev;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MoreInformation extends AppCompatActivity {

    private ImageButton backButton, addCommentButton, addforumButton;
    private TextView titleTextView, releaseDateTextView, genreTextView, descriptionTextView;
    private ImageView posterImageView;
    private RatingBar ratingBar, ratingBarSelectable;
    private EditText commentText;
    private ListView forumListView, commentListView;
    private ArrayList<Comment> comments;
    private ArrayList<ArrayList<Forum>> forums;
    private CommentAdapter commentAdapter;
    private ForumAdapter forumAdapter;
    private String fid;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reviewpage1);

        // 初始化 SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // 初始化 UI 组件
        backButton = findViewById(R.id.backButton);
        addCommentButton = findViewById(R.id.addCommentButton);
        addforumButton = findViewById(R.id.addForumButton);
        titleTextView = findViewById(R.id.loginTitle);
        posterImageView = findViewById(R.id.poster1);
        ratingBar = findViewById(R.id.ratingBar);
        ratingBarSelectable = findViewById(R.id.ratingBarselectable);
        releaseDateTextView = findViewById(R.id.releaseDateTextView);
        genreTextView = findViewById(R.id.genreTextView);
        descriptionTextView = findViewById(R.id.mainDescription);
        commentText = findViewById(R.id.CommentText);
        forumListView = findViewById(R.id.forumselectlistview);
        commentListView = findViewById(R.id.CommentView);

        // 初始化数据和适配器
        comments = new ArrayList<>();
        forums = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, comments);
        forumAdapter = new ForumAdapter(this, forums);
        commentListView.setAdapter(commentAdapter);
        forumListView.setAdapter(forumAdapter);

        // 获取 Intent 数据
        Intent intent = getIntent();
        fid = intent.getStringExtra("fid");
        String fname = intent.getStringExtra("fname");
        String releaseDate = intent.getStringExtra("releaseDate");
        String image = intent.getStringExtra("image");
        String tname = intent.getStringExtra("tname");
        String ratingStr = intent.getStringExtra("rating");

        if (fname != null && !fname.isEmpty()) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("last_movie", fname);
            editor.apply();
        }

        // 设置基本信息
        titleTextView.setText(fname != null ? fname : "N/A");
        releaseDateTextView.setText("Release Date: " + (releaseDate != null ? releaseDate : "N/A"));
        genreTextView.setText("Genre: " + (tname != null && !tname.isEmpty() ? tname : "N/A"));

        // 设置评分
        if (ratingStr != null && !ratingStr.equals("N/A")) {
            try {
                float rating = Float.parseFloat(ratingStr);
                ratingBar.setRating(rating);
            } catch (NumberFormatException e) {
                Log.e("MovieDetail", "Invalid rating format: " + ratingStr);
                ratingBar.setRating(0);
            }
        } else {
            ratingBar.setRating(0);
        }

        // 设置海报
        if (image != null && !image.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    posterImageView.setImageBitmap(bitmap);
                } else {
                    posterImageView.setImageResource(R.color.black);
                }
            } catch (Exception e) {
                Log.e("MovieDetail", "Error decoding image for fid: " + fid, e);
                posterImageView.setImageResource(R.color.black);
            }
        } else {
            posterImageView.setImageResource(R.color.black);
        }

        // 加载描述、评论和论坛
        if (fid != null) {
            loadMovieDetails(fid);
        } else {
            Toast.makeText(this, "Invalid movie ID", Toast.LENGTH_SHORT).show();
            finish();
        }
        String uid = sharedPreferences.getString("uid", null);
        if (uid != null) {
            sendTypeTime();
        }



        // 返回按钮
        backButton.setOnClickListener(v -> finish());

        addforumButton.setOnClickListener(v -> {
            if (uid == null) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivity(loginIntent);
            } else {
                Intent addForum = new Intent(this, AddForum.class);
                addForum.putExtra("fid", fid);
                addForum.putExtra("uid", uid);
                startActivity(addForum);
            }
        });

        // 提交评论
        addCommentButton.setOnClickListener(v -> {
            if (uid == null) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivity(loginIntent);
                return;
            }

            String comment = commentText.getText().toString().trim();
            float rating = ratingBarSelectable.getRating();
            if (comment.isEmpty()) {
                Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (rating <= 0) {
                Toast.makeText(this, "Please rate the movie", Toast.LENGTH_SHORT).show();
                return;
            }

            submitComment(uid, fid, comment, rating);
        });
    }

    private void loadMovieDetails(String fid) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2/MoreInfo.php?fid=" + fid;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (status.equals("success")) {
                            JSONObject movie = jsonObject.getJSONObject("movie");

                            // 设置描述
                            descriptionTextView.setText(movie.getString("description"));

                            // 设置评论
                            comments.clear();
                            JSONArray commentsArray = movie.getJSONArray("comments");
                            for (int i = 0; i < commentsArray.length(); i++) {
                                JSONObject comment = commentsArray.getJSONObject(i);
                                comments.add(new Comment(
                                        comment.getString("userName"),
                                        comment.getString("comment"),
                                        comment.getString("time"),
                                        (float) comment.getDouble("rating")
                                ));
                            }
                            commentAdapter.notifyDataSetChanged();

                            // 设置论坛
                            forums.clear();
                            JSONArray forumsArray = movie.getJSONArray("forums");
                            int index = 0;
                            ArrayList<Forum> temp_forum = new ArrayList<>();
                            for (int i = 0; i < forumsArray.length(); i++) {
                                JSONObject forum = forumsArray.getJSONObject(i);
                                if(index == 0){
                                    temp_forum = new ArrayList<>();
                                }
                                temp_forum.add(new Forum(
                                        forum.getString("foid"),
                                        forum.getString("foname")
                                ));
                                if(index == 2 || i + 1 == forumsArray.length()){
                                    forums.add(temp_forum);
                                }
                                if(index == 2){
                                    index = 0;
                                }else{
                                    index += 1;
                                }
                            }
                            forumAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("MovieDetail", "JSON parse error", e);
                        Toast.makeText(this, "Parsing Error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("MovieDetail", "Volley error", error);
                    Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show();
                });

        queue.add(stringRequest);
    }

    private void submitComment(String uid, String fid, String comment, float rating) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2/MoreInfo.php";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("uid", uid);
            jsonBody.put("fid", fid);
            jsonBody.put("comment", comment);
            jsonBody.put("rating", rating);
        } catch (JSONException e) {
            Log.e("MovieDetail", "JSON error", e);
            return;
        }
        final String requestBody = jsonBody.toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        if (status.equals("success")) {
                            commentText.setText("");
                            ratingBarSelectable.setRating(0);
                            loadMovieDetails(fid);
                            Toast.makeText(this, "Added comment successfully", Toast.LENGTH_SHORT).show();// 刷新评论
                        }
                    } catch (JSONException e) {
                        Log.e("MovieDetail", "JSON parse error", e);
                        Toast.makeText(this, "Parsing Error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("MovieDetail", "Volley error", error);
                    Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                try {
                    return requestBody.getBytes("utf-8");
                } catch (Exception e) {
                    Log.e("MovieDetail", "Body error", e);
                    return null;
                }
            }
        };

        queue.add(stringRequest);
    }

    private void sendTypeTime(){

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2/savetagtime.php";
        String uid = sharedPreferences.getString("uid", null);
        String tag = getIntent().getStringExtra("tname");

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("uid", uid);
            jsonBody.put("tag", tag);
        } catch (JSONException e) {
            Log.e("MovieDetail", "JSON error", e);
            return;
        }
        final String requestBody = jsonBody.toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");
//                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        if (status.equals("success")) {
//                            Toast.makeText(this,"create or add count times success", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("MovieDetail", "JSON parse error", e);
                        Toast.makeText(this, "Parsing Error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("MovieDetail", "Volley error", error);
                    Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                try {
                    return requestBody.getBytes("utf-8");
                } catch (Exception e) {
                    Log.e("MovieDetail", "Body error", e);
                    return null;
                }
            }
        };

        queue.add(stringRequest);
    }

    public static class Comment {
        private String userName, content, time;
        private float rating;

        public Comment(String userName, String content, String time, float rating) {
            this.userName = userName;
            this.content = content;
            this.time = time;
            this.rating = rating;
        }

        public String getUserName() { return userName; }
        public String getContent() { return content; }
        public String getTime() { return time; }
        public float getRating() { return rating; }
    }

    public static class Forum {
        private String foid, foname;

        public Forum(String foid, String foname) {
            this.foid = foid;
            this.foname = foname;
        }

        public String getFoid() { return foid; }
        public String getFoname() { return foname; }
    }
}