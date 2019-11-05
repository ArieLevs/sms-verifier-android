package cloud.nalkins.sms_verifier;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.json.JSONObject.NULL;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName(); // set TAG for logs
    private SharedPreferences sharedPreferences;

    Handler uiHandler;
    final int SHOW_PBAR = 1;
    final int HIDE_PBAR = 0;

    static JSONArray broadcastJsonList;

    ProgressBar progressBar;

    // Create the ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {
        case R.id.action_refresh:
            finish();
            startActivity(getIntent());
            return(true);
        case R.id.action_help:
            // Start the 'registerHelpFunction' and send the activity context
            Functions.helpFunction(getApplicationContext());
            return(true);
        case R.id.action_legal:
            // Start the 'legalFunction' and send the activity context
            Functions.legalFunction(getApplicationContext());
            return(true);
        case R.id.action_logout:
            // Start the 'exitFunction' and send the activity context
            logoutUser();
            return(true);
    }
        return(super.onOptionsItemSelected(item));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create new sharedPreferences manager object
        sharedPreferences = new SharedPreferences(getApplicationContext());

        // If no token present in shared preferences the logout the user
        if (sharedPreferences.getToken() == NULL) {
            finish();
        }
        // Print a welcome message
        Toast.makeText(getApplicationContext(), "Welcome " + sharedPreferences.getUsername(), Toast.LENGTH_LONG).show();

        // Progress Bar setup
        progressBar = new ProgressBar(MainActivity.this, null, android.R.attr.progressBarStyleLarge);
        progressBar.setBackgroundColor(Color.parseColor("#80FFFFFF"));
        progressBar.setClickable(false);
        progressBar.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(200, 200);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        RelativeLayout layout = findViewById(R.id.activity_main);

        // Set UI Handler to send actions to UI
        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case SHOW_PBAR:
                        layout.addView(progressBar, params);
                        break;
                    case HIDE_PBAR:
                        layout.removeView(progressBar);
                        break;
                }
            }
        };

        getBroadcastListFromServer();
    }

    /**
     *
     */
    public void getBroadcastListFromServer() {
        Log.d(TAG, "Running 'getBroadcastListFromServer' function");

        Message showDialog =
                uiHandler.obtainMessage(SHOW_PBAR, progressBar);
        final Message hideDialog =
                uiHandler.obtainMessage(HIDE_PBAR, progressBar);
        showDialog.sendToTarget();

        Log.d(TAG, "Sending request to: " + AppConfig.URL_BROADCAST_LIST);
        JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST,
                AppConfig.URL_BROADCAST_LIST, new JSONObject(), (JSONObject response) -> {
            Log.d(TAG, AppConfig.URL_BROADCAST_LIST+ " Response: " + response.toString());

            try {
                // If status response equals success
                if (response.getString("status").equals("success")) {
                    // Save response to in memory
                    broadcastJsonList = response.getJSONArray("message");

                    hideDialog.sendToTarget();

                    // Once successful, generate views
//                    initBroadcastLists();
                } else {
                    hideDialog.sendToTarget();
                    String responseMessage = response.getString("message");
                    Toast.makeText(getApplicationContext(), responseMessage, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                hideDialog.sendToTarget();
                // JSON error
                Log.d(TAG, "Json error: " + e.toString());
                Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, (VolleyError error) -> {
            try {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Log.e(TAG, "Server Time out error or no connection");
                    Toast.makeText(getApplicationContext(),
                            "Timeout error! Server is not responding for device list request",
                            Toast.LENGTH_LONG).show();
                } else {
                    String body;
                    try {
                        //get response body and parse with appropriate encoding
                        if (error.networkResponse.data != null) {
                            try {
                                body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                Log.e(TAG, body);
                                Toast.makeText(getApplicationContext(), body, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            hideDialog.sendToTarget();
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
        NetworkRequests.getInstance().addToRequestQueue(strReq, "req_broadcast_list", true);
    }

    /**
     *  Logout the user, remove token from shared preferences, finish
     * */
    private void logoutUser() {
        Log.d(TAG, "running 'logoutUser' function");

        new AlertDialog.Builder(this)
                .setTitle("Logout from " + getString(R.string.app_name))
                .setMessage("Do you really want to logout?")
                .setIcon(R.drawable.warning_64)
                .setPositiveButton(android.R.string.yes, (DialogInterface dialog, int whichButton) -> {
                    sharedPreferences.removeUsername(); // Remove username from shared preferences
                    sharedPreferences.removeToken();

                    Functions.revokeToken(getApplication());

                    // Launching the login activity
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(android.R.string.no, null).show();
    }
}
