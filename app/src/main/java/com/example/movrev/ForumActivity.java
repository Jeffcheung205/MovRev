package com.example.movrev;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ForumActivity extends AppCompatActivity {

    private TextView forumNameTextView;
    private ListView commentListView;
    private EditText commentInput;
    private ImageButton backButton, submitCommentButton;
    private ArrayList<ForumComment> forumcomments;
    private ForumCommentAdapter commentAdapter;
    private String foid, uid;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forum);

        forumNameTextView = findViewById(R.id.ForumName);
        commentListView = findViewById(R.id.CommentListView);
        commentInput = findViewById(R.id.TypeInComment);
        submitCommentButton = findViewById(R.id.SubmitCommentButton);
        backButton = findViewById(R.id.backButton);

        Intent intent = getIntent();
        foid = intent.getStringExtra("foid");
        String foname = intent.getStringExtra("foname");

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        uid = sharedPreferences.getString("uid", null);

        forumNameTextView.setText(foname != null ? foname : "Forum");

        forumcomments = new ArrayList<>();
        commentAdapter = new ForumCommentAdapter(this, forumcomments);
        commentListView.setAdapter(commentAdapter);

        backButton.setOnClickListener(v -> finish());

        submitCommentButton.setOnClickListener(v -> {
            if (uid == null) {
                Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }
            String comment = commentInput.getText().toString().trim();
            if (comment.isEmpty()) {
                Toast.makeText(this, "Comment content cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            submitComment(uid, foid, comment);
        });

        loadComments(foid);
    }

    private void loadComments(String foid) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2/InnerForum.php?foid=" + foid;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");
                        if (status.equals("success")) {
                            forumcomments.clear();
                            JSONArray innerforums = jsonObject.getJSONArray("innerforum");
                            for (int i = 0; i < innerforums.length(); i++) {
                                JSONObject innerforum = innerforums.getJSONObject(i);
                                forumcomments.add(new ForumComment(
                                        innerforum.getString("userName"),
                                        innerforum.getString("comment"),
                                        innerforum.getString("time")
                                ));
                            }
                            commentAdapter.notifyDataSetChanged();
                            if (innerforums.length() == 0) {
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("ForumActivity", "JSON parse error", e);
                        Toast.makeText(this, "解析错误", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("ForumActivity", "Volley error", error);
                    Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show();
                });

        queue.add(stringRequest);
    }

    private void submitComment(String uid, String foid, String comment) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2/InnerForum.php";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("uid", uid);
            jsonBody.put("foid", foid);
            jsonBody.put("comment", comment);
        } catch (JSONException e) {
            Log.e("ForumActivity", "JSON error", e);
            Toast.makeText(this, "JSON error", Toast.LENGTH_SHORT).show();
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
                            commentInput.setText("");
                            loadComments(foid);
                            Toast.makeText(this, "Added comment successfully", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("ForumActivity", "JSON parse error", e);
                        Toast.makeText(this, "解析错误", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("ForumActivity", "Volley error", error);
                    Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return requestBody.getBytes(StandardCharsets.UTF_8);
            }
        };

        queue.add(stringRequest);
    }

    public static class ForumComment {
        private String userName, comment, time;

        public ForumComment(String userName, String comment, String time) {
            this.userName = userName;
            this.comment = comment;
            this.time = time;
        }

        public String getUserName() { return userName; }
        public String getComment() { return comment; }
        public String getTime() { return time; }
    }

}