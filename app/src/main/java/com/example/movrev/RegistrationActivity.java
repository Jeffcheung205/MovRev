package com.example.movrev;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private EditText userNameInput, passwordInput;
    private Button submitButton;
    private ImageButton backButton;

    String userPasswordRe , userNameRe , uid;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration); // Replace with your XML layout name

        // Initialize UI components
        backButton = findViewById(R.id.backButton);
        userNameInput = findViewById(R.id.userNameInputRegistration);
        passwordInput = findViewById(R.id.passwordInputRegistration);
        submitButton = findViewById(R.id.submitButtonRegistration);
        sharedPreferences = getSharedPreferences("MyAppName",MODE_PRIVATE);

        // Back button click listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            } // Close the current activity
        });

        // Registration submit button click listener
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userNameRe = userNameInput.getText().toString().trim();
                userPasswordRe = passwordInput.getText().toString().trim();

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "http://10.0.2.2/registration.php";
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("userName", userNameRe);
                    jsonBody.put("userPassword", userPasswordRe);
                }catch (JSONException e){
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
                                        userNameRe = jsonObject.getString("userName");
                                        uid = jsonObject.getString("uid");

                                        // 将数据保存到 SharedPreferences
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("logged", "true");
                                        editor.putString("userName", userNameRe);
                                        editor.putString("uid", uid);
                                        editor.apply();

                                        // 转到下一个活动页面
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        Toast.makeText(RegistrationActivity.this, "Create Account Success", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(RegistrationActivity.this, "Some things Wrong", Toast.LENGTH_SHORT).show();
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
                        paramV.put("userName", userNameRe);
                        paramV.put("userPassword", userPasswordRe);
                        // 确保在添加到参数前 UID 不为空
                        if (uid != null) {
                            paramV.put("uid", uid);
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
                /*if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegistrationActivity.this,
                            "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Add your registration logic here
                    Toast.makeText(RegistrationActivity.this,
                            "Registering: " + username, Toast.LENGTH_SHORT).show();*/

                    // Example: Save user credentials and proceed
                    // if (saveUserCredentials(username, password)) {
                    //     startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                    //     finish();
                    // }
            //    }
            }
        });
    }

}