package com.example.movrev;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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
import java.util.jar.Attributes;

public class LoginActivity extends AppCompatActivity {

    private EditText userNameInput, passwordInput;
    private Button submitButton;
    private ImageButton backButton;
    private TextView registrationLink;

    String userPassword, userName, uid;

    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); // Replace with your XML layout file name

        // Initialize UI components
        backButton = findViewById(R.id.backButton);
        userNameInput = findViewById(R.id.userNameInput);
        passwordInput = findViewById(R.id.passwordInput);
        submitButton = findViewById(R.id.submitButton);
        registrationLink = findViewById(R.id.registrationLink);
        sharedPreferences = getSharedPreferences("UserPrefs",MODE_PRIVATE);
        // Back button click listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Submit button click listener
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = String.valueOf(userNameInput.getText());
                userPassword = String.valueOf(passwordInput.getText());

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "http://10.0.2.2/login.php";
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("name", userName);
                    jsonBody.put("pwd", userPassword);
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
                                        userName = jsonObject.getString("userName");
                                        uid = jsonObject.getString("uid");

                                        // 将数据保存到 SharedPreferences
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("logged", "true");
                                        editor.putString("userName", userName);
                                        editor.putString("uid", uid);
                                        editor.apply();

                                        // 转到下一个活动页面
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Some things Wrong", Toast.LENGTH_SHORT).show();
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
                        paramV.put("name", userName);
                        paramV.put("pwd", userPassword);
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

              //  if (username.isEmpty() || password.isEmpty()) {
                   // Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
               // } else {
                    // Add your login logic here
                 //   Toast.makeText(LoginActivity.this,
                     //       "Login attempt for: " + username, Toast.LENGTH_SHORT).show();

                    // Example: Check credentials and proceed to main activity
                    // if (isValidCredentials(username, password)) {
                    //     startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    // }
              //  }
            }
        });

        // Registration link click listener
        registrationLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start registration activity
                Intent intent = new Intent(LoginActivity.this,RegistrationActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Add your authentication logic here
    /*
    private boolean isValidCredentials(String username, String password) {
        // Implement your actual authentication check
        return true;
    }
    */
}