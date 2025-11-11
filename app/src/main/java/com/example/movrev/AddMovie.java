package com.example.movrev;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddMovie extends AppCompatActivity {

    private EditText movieNameInput, movieDateInput, movieContentInput;
    private TextView movieTagInput;
    private Button submitButton, selectPhotoBtn;
    private ImageButton backButton;
    private static final int PICK_IMAGE = 1;
    private ImageView posterImageView;
    private Bitmap selectedBitmap;
    private String movieName, releaseDate, content, fid, tag, encodedImage;
    private boolean[] selectedtype;
    ArrayList<String> typelist = new ArrayList<>();
    private List<String> types, tids;





    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addfilm); // Replace with your XML layout name

//        if(sharedPreferences.getString("logged","false").equals("false")){
//            Intent intent = new Intent(AddMovie.this, MainActivity.class);
//            startActivity(intent);
//            Toast.makeText(AddMovie.this, "Some things Wrong", Toast.LENGTH_SHORT).show();
//            finish();
//        }
        types = new ArrayList<>();
        tids = new ArrayList<>();

        // Initialize UI components
        posterImageView = findViewById(R.id.poster1);
        selectPhotoBtn = findViewById(R.id.selectphoto);
        backButton = findViewById(R.id.backButton);
        movieNameInput = findViewById(R.id.MovieNameInput);
        movieDateInput = findViewById(R.id.MovieDate);
        movieContentInput = findViewById(R.id.MovieContent);
        movieTagInput = findViewById(R.id.MovieGenre);
        submitButton = findViewById(R.id.submitButtonNewMovie);

        loadType();


        // Back button click listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddMovie.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        selectPhotoBtn.setOnClickListener(v -> openGallery());

        // 提交按钮点击事件
//        submitButton.setOnClickListener(v -> {
//            if (validateInput() && selectedBitmap != null) {
//                encodeImage(); // 转换图片为Base64
//                uploadData();   // 上传数据
//            } else {
//                Toast.makeText(this, "请填写所有信息并选择图片", Toast.LENGTH_SHORT).show();
//            }
//        });


        movieTagInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String[] typeArray = types.toArray(new String[0]);

                // Initialize alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(AddMovie.this);

                // set title
                builder.setTitle("Select Type");

                // set dialog non cancelable
                builder.setCancelable(false);

                builder.setMultiChoiceItems(typeArray, selectedtype, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        // check condition
                        if (b) {
                            // when checkbox selected
                            // Add position  in lang list
                            typelist.add(tids.get(i));
                            // Sort array list
                            Collections.sort(typelist);
                        } else {
                            // when checkbox unselected
                            // Remove position from langList
                            typelist.remove(tids.get(i));
                        }
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Initialize string builder
                        StringBuilder stringBuilder = new StringBuilder();
                        // use for loop
                        for (int j = 0; j < typelist.size(); j++) {
                            // concat array value
                            stringBuilder.append(typeArray[tids.indexOf(typelist.get(j))]);
                            // check condition
                            if (j != typelist.size() - 1) {
                                // When j value  not equal
                                // to lang list size - 1
                                // add comma
                                stringBuilder.append(", ");
                            }
                        }
                        // set text on textView
                        movieTagInput.setText(stringBuilder.toString());
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // dismiss dialog
                        dialogInterface.dismiss();
                    }
                });
                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // use for loop
                        for (int j = 0; j < selectedtype.length; j++) {
                            // remove all selection
                            selectedtype[j] = false;
                            // clear language list
                            typelist.clear();
                            // clear text view value
                            movieTagInput.setText("");
                        }
                    }
                });
                // show dialog
                builder.show();
            }
        });
        // Submit button click listener
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                movieName = movieNameInput.getText().toString().trim();
                releaseDate = movieDateInput.getText().toString().trim();
                content = movieContentInput.getText().toString().trim();
//                tag = movieTagInput.getText().toString().trim();
                tag = TextUtils.join(", ", typelist);
                encodeImage();

                // Sending data to your server
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "http://10.0.2.2/AddMov.php"; // Replace with your server URL
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("movieName", movieName);
                    jsonBody.put("releaseDate", releaseDate);
                    jsonBody.put("content", content);
                    jsonBody.put("tag", tag);
                    jsonBody.put("image", encodedImage);
                } catch (JSONException e) {

                }
                final String requestBody = jsonBody.toString();

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("API", response);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String status = jsonObject.getString("status");
                                    String message = jsonObject.getString("message");
                                    if (status.equals("success")) {
                                        Toast.makeText(AddMovie.this, "Movie added successfully!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(AddMovie.this, "Some things Wrong", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("API", error.getMessage());
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> paramV = new HashMap<>();
                        paramV.put("movieName", movieName);
                        paramV.put("releaseDate", releaseDate);
                        paramV.put("content", content);
                        paramV.put("tag", tag);
                        paramV.put("image", encodedImage);
                        // 确保在添加到参数前 UID 不为空
                        if (fid != null) {
                            paramV.put("fid", fid);
                        }
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
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Poster"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                selectedBitmap = BitmapFactory.decodeStream(inputStream);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                Log.d("Test", "Changed bitmap : " + encodedImage.substring(0, 21));

                posterImageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // 图片转Base64
    private void encodeImage() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void loadType() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://10.0.2.2/type.php";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray typeArray = jsonObject.getJSONArray("type");
                            types.clear();
                            tids.clear();
                            for (int i = 0; i < typeArray.length(); i++) {
                                JSONObject aType = typeArray.getJSONObject(i);
                                String type = aType.getString("type");
                                String tid = aType.getString("tid");
                                types.add(type);
                                tids.add(tid);
                            }
                            selectedtype = new boolean[types.size()];
                            Log.d("Film Types", "Types: " + types);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VolleyError", "Error: " + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    // 数据验证
//    private boolean validateInput() {
//        return !movieName.isEmpty() &&
//                !releaseDate.isEmpty() &&
//                !content.isEmpty();
//    }

    // 上传数据到服务器
//    private void uploadData() {
//        StringRequest request = new StringRequest() {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<>();
//                params.put("movieName", movieName);
//                params.put("releaseDate", releaseDate);
//                params.put("content", content);
//                params.put("image", encodedImage);
//                return params;
//            }
//        };
//        // 添加到请求队列
//        Volley.newRequestQueue(this).add(request);
//    }
}
