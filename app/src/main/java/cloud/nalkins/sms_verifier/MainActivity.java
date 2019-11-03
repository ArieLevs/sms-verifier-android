package cloud.nalkins.sms_verifier;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static org.json.JSONObject.NULL;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName(); // set TAG for logs
    private SharedPreferences sharedPreferences;

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
