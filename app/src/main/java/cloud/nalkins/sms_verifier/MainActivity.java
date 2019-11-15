package cloud.nalkins.sms_verifier;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
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
    final int SendSMSPermissionID = 1001;
    private boolean  sendSMSPermissionFlag = true;

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

//        final Fragment first = new BroadcastListLayout();

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


//        FragmentManager fm = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fm.beginTransaction();
//        fragmentTransaction.replace(R.id.dynamicBroadcastListLayout, first);
//        fragmentTransaction.commit();
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
                    // ### The response is not being validated, and assumed that the server responses
                    // with a very specific json structure ###
                    broadcastJsonList = response.getJSONArray("message");

                    hideDialog.sendToTarget();

                    // Once successful, generate views
                    initBroadcastLists();
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

    public void initBroadcastLists() {
        Log.d(TAG, "running 'initBroadcastLists' Function");

        // Set parent layout (this is the main layout)
        LinearLayout mainLayout = findViewById(R.id.dynamicBroadcastListLayout);

        for(int i = 0; i < broadcastJsonList.length(); i++) {
            try {
                JSONObject broadcast_list = broadcastJsonList.getJSONObject(i);
                String broadcastName = broadcast_list.getString("broadcast_name");
                String eventName = broadcast_list.getString("event_name");
                Log.d(TAG, "Working on list: " + broadcastName);

                String messageContent = broadcast_list.getString("message_content");
                JSONArray contacts = broadcast_list.getJSONArray("contacts");

                final BroadcastListLayout tempLayout = new BroadcastListLayout(getApplicationContext(), broadcastName, eventName);

                // Set the 'Automation toggle button'
                tempLayout.getBroadcastToggleButton().setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->

                    new AlertDialog.Builder(this)
                            .setTitle("Broadcast " + broadcastName)
                            .setMessage("Do you really want to send this broadcast?")
                            .setIcon(R.drawable.warning_64)
                            .setPositiveButton(android.R.string.yes, (DialogInterface dialog, int whichButton) ->
                                    sendMessages(messageContent, contacts) )
                            .setNegativeButton(android.R.string.no, null).show()
                );

                tempLayout.setBroadcastToggleButton(false);

                mainLayout.addView(tempLayout.getView());
            } catch (JSONException e) {
                // JSON error
                Log.d(TAG, "Json error: " + e.toString());
                Toast.makeText(getApplicationContext(), "initBroadcastLists Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void sendMessages(String messageContent, JSONArray contacts) {

        /* If no permission granted for ACCESS_FINE_LOCATION request it
           The permission is a MUST in order to use wifi network scan
           If the user did not granted the permission, auto wifi connection cannot be established
         */
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.SEND_SMS}, SendSMSPermissionID);
        }

        if (sendSMSPermissionFlag) {
            Log.d(TAG, "Sending message: " + messageContent);
            Log.d(TAG, "Sending SMS messages to: " + contacts.toString());

            for (int i = 0; i < contacts.length(); i++) {
                try {
                    String firstName = contacts.getJSONObject(i).getString("first_name");
                    String phoneNumber = contacts.getJSONObject(i).getString("phone_number");
                    String uuid = contacts.getJSONObject(i).getString("uuid");
                    Log.i(TAG, "Sending message to " + firstName + ", num: " + phoneNumber);

                    String text = String.format(messageContent, firstName, uuid);

                    Log.i(TAG, "content: " + text);

                    sendSMSMessage(phoneNumber, text);

                } catch (JSONException e) {
                    // JSON error
                    Log.d(TAG, "Json error: " + e.toString());
                }
            }
        } else {
            Log.e(TAG, "sendSMSPermissionFlag is: FALSE");
            Toast.makeText(getApplicationContext(), "SEND_SMS permission must be granted to send SMS messages", Toast.LENGTH_LONG).show();
        }
    }

    private void sendSMSMessage(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        // SMS sent (attempted) result
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "SMS sent");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Log.e(TAG, "Generic failure");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Log.e(TAG, "No service");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Log.e(TAG, "Null PDU");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Log.e(TAG, "Radio off");
                        break;
                }
            }
        }, new IntentFilter(SENT));

        // SMS delivery result
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "SMS delivered");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e(TAG, "SMS not delivered");
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPendingIntent, deliveredPendingIntent);
    }

    // Request SEND_SMS permissions if not granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Request permissions to send SMS messages
        if (requestCode == SendSMSPermissionID) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "SEND_SMS permission granted");
                sendSMSPermissionFlag = true;
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.SEND_SMS)) {
                    Log.d(TAG, "SEND_SMS was not granted");
                    Toast.makeText(getApplicationContext(), "SEND_SMS permission must be granted to send SMS messages", Toast.LENGTH_LONG).show();
                    sendSMSPermissionFlag = false;

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Critical permission required")
                            .setMessage("This permission in needed to send SMS messages")
                            .setNegativeButton("OK", null)
                            .create()
                            .show();
                } else {
                    Log.d(TAG, "SEND_SMS permission was not granted, using 'never ask again'");
                    sendSMSPermissionFlag = false;
                    Toast.makeText(getApplicationContext(), "SEND_SMS permission must be granted to send SMS messages", Toast.LENGTH_LONG).show();
                    //Never ask again and handle app without permission.
                }
            }
        }
    }
}
