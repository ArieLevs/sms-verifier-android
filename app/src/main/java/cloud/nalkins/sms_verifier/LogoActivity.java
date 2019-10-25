package cloud.nalkins.sms_verifier;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LogoActivity extends AppCompatActivity {

    private static final String TAG = LogoActivity.class.getSimpleName(); // set TAG for logs
    private SharedPreferences sharedPreferences; // store info to shared preferences

    private ProgressDialog pDialog; // 'processing' dialog
    Handler uiHandler;

    // define namings to int variables
    final int SHOW_AUTHENTICATING_DIALOG = 1;
    final int HIDE_DIALOG = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            AppConfig.readProperties(getApplicationContext());
        } catch (IOException e) {
            Log.e(TAG, "error - failed to read properties file");
            e.printStackTrace();
        }

        // Session manager
        sharedPreferences = new SharedPreferences(getApplicationContext());

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Set UI Handler to send actions to UI
        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case SHOW_AUTHENTICATING_DIALOG:
                        pDialog.setMessage("Authenticating ...");
                        Functions.showDialog(pDialog);
                        break;
                    case HIDE_DIALOG:
                        Functions.hideDialog(pDialog);
                        break;
                }
            }
        };

        if (sharedPreferences.getToken().equals("NULL"))
            lunchLoginActivity();
        else
            getUsernameFromServer();
    }

    /**
     * Send server request with current access token
     * If all is valid, a success message will return containing logged in username
     * If there was an error, login activity will start
     * If received unauthorized response, try getting new access token
     */
    private void getUsernameFromServer() {
        Log.d(TAG, "Running 'getUsernameFromServer' function");
        String tag_check_health = "req_check_health";

        Message showDialog =
                uiHandler.obtainMessage(SHOW_AUTHENTICATING_DIALOG, pDialog);
        final Message hideDialog =
                uiHandler.obtainMessage(HIDE_DIALOG, pDialog);
        showDialog.sendToTarget();

        JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST,
                AppConfig.URL_HEALTH_CHECK, new JSONObject(), (JSONObject response) -> {

            Log.d(TAG, "Health check Response: " + response.toString());
            try {
                String status = response.getString("status");
                // Check if status is success
                if (status.equals("success")) {
                    // Users access token valid
                    sharedPreferences.setUsername(response.getString("message"));

                    // Launch main activity
                    Intent intent = new Intent(getApplicationContext(), LogoActivity.class);
                    getApplicationContext().startActivity(intent);
                    finish();
                } else {
                    Log.e(TAG, "Somethings wrong with server response, " +
                            "Check server side!");
                    lunchLoginActivity();
                }
            } catch (JSONException e) {
                // JSON error
                Log.e(TAG, "Json error: " + e.toString());
                Toast.makeText(getApplicationContext(),
                        "Something is wrong with the server," +
                                "Please try later", Toast.LENGTH_LONG).show();
                finish();
            }
            hideDialog.sendToTarget();
        }, (VolleyError error) -> {
            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                Log.e(TAG, error.toString());
                Toast.makeText(getApplicationContext(), "Server Time out error or no connection", Toast.LENGTH_LONG).show();
                finish();
                hideDialog.sendToTarget();
            } else {
                // Get response body and parse with appropriate encoding
                if (error.networkResponse.data != null) {
                    try {
                        String statusCode = String.valueOf(error.networkResponse.statusCode);
                        Log.e(TAG, "Server response code: " + statusCode);
                        String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        Log.e(TAG, "Login Error: " + body);
                        if (error.networkResponse.statusCode == 401) { // Unauthorized
                            requestNewToken();
                        } else {
                            lunchLoginActivity();
                            hideDialog.sendToTarget();
                        }

                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                        finish();
                        hideDialog.sendToTarget();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> header = new HashMap<>();
                String auth = "Bearer " + sharedPreferences.getToken();
                header.put("Authorization", auth);
                return header;
            }
        };

        // Adding request to request queue
        // Change the default time out request (35 sec), and set only one retry
        RetryPolicy policy = new DefaultRetryPolicy(35000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        strReq.setRetryPolicy(policy);
        NetworkRequests.getInstance().addToRequestQueue(strReq, tag_check_health, true);
    }

    private void requestNewToken() {
        Log.d(TAG, "Running 'requestNewToken' function");

        String tag_string_req = "req_refresh_token";

        final Message hideDialog =
                uiHandler.obtainMessage(HIDE_DIALOG, pDialog);

        // Start new StringRequest (HTTP)
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_AUTHENTICATION, (String response) -> {

            Log.d(TAG, "Refresh Token Response: " + response);
            try {
                JSONObject jObj = new JSONObject(response);
                String token = jObj.getString("access_token");
                // Check if the token contains any values
                if (!token.equals("")) {
                    // user successfully logged in
                    // Create login session
                    sharedPreferences.setToken(token);
                    sharedPreferences.setRefreshToken(jObj.getString("refresh_token"));

                    getUsernameFromServer();
                } else {
                    // Error in login. Get the error message
                    Log.d(TAG, "Error message: " + jObj.getString("error_msg"));
                    Toast.makeText(getApplicationContext(), "Refresh Token failed", Toast.LENGTH_LONG).show();

                    lunchLoginActivity();
                }
            } catch (JSONException e) {
                // JSON error
                Log.d(TAG, "Refresh Token error: " + e.toString());
                Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                lunchLoginActivity();
            }
            hideDialog.sendToTarget();

        }, (VolleyError error) -> {
            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                Log.e(TAG, "Server Time out error or no connection");
                Toast.makeText(getApplicationContext(),
                        "Timeout error! Server is not responding",
                        Toast.LENGTH_LONG).show();
            } else {
                String body;
                //get status code here
                String statusCode = String.valueOf(error.networkResponse.statusCode);
                Log.e(TAG, "Server response code: " + statusCode);
                //get response body and parse with appropriate encoding
                if (error.networkResponse.data != null) {
                    try {
                        body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        Log.e(TAG, "Refresh Token Error: " + body);
                        Toast.makeText(getApplicationContext(),
                                "Failed To Auto Authenticate", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, e.toString());
                    }
                }
                lunchLoginActivity();
            }
            hideDialog.sendToTarget();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("grant_type", "refresh_token");
                params.put("client_id", AppConfig.OAUTH_CLIENT_ID);
                params.put("client_secret", AppConfig.OAUTH_CLIENT_SECRET);
                params.put("refresh_token", sharedPreferences.getRefreshToken());
                return params;
            }
        };
        // Adding request to request queue
        NetworkRequests.getInstance().addToRequestQueue(strReq, tag_string_req, true);
    }

    private void lunchLoginActivity() {
        // Launch login activity
        Intent mainIntent = new Intent(LogoActivity.this, LoginActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
