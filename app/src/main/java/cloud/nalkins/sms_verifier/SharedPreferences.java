package cloud.nalkins.sms_verifier;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * Class maintains session data across the app using the SharedPreferences
 */
public class SharedPreferences {
    // LogCat tag
    private static String TAG = SharedPreferences.class.getSimpleName();

    // Shared Preferences
    private android.content.SharedPreferences pref;

    private Editor _editor;
    private Context _context;

    // Shared pref mode
    private int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "CloudBitToken";
    private static final String KEY_ACCESS_TOKEN = "AccessToken";
    private static final String KEY_REFRESH_TOKEN = "RefreshToken";
    private static final String KEY_USERNAME = "Username";
    private static final String KEY_MAIN_ACTIVITY = "MainActivity";

    public SharedPreferences(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        _editor = pref.edit();
    }

    /**
     * Store current token to shared preferences
     *
     * @param Token current users token
     */
    public void setToken(String Token) {
        _editor.putString(KEY_ACCESS_TOKEN, Token);
        // commit changes
        _editor.commit();
        Log.d(TAG, "Access Token: " + Token + ", stored in shared preferences.");
    }

    /**
     * Return current token from shared preferences
     * @return String users current token
     * In case KEY_ACCESS_TOKEN = null, the String "NULL" will return
     */
    public String getToken(){
        String key_access_token = pref.getString(KEY_ACCESS_TOKEN, "NULL");
        Log.d(TAG, "Access Token returned " + key_access_token + " from shared preferences.");
        return key_access_token;
    }

    /**
     * Remove current token from shared preferences
     */
    public void removeToken(){
        Log.d(TAG, "Running 'removeToken'");
        _editor.remove(KEY_ACCESS_TOKEN);
        _editor.commit();
    }

    /**
     * Store current refresh token to shared preferences
     *
     * @param refreshToken current users refresh token
     */
    public void setRefreshToken(String refreshToken) {
        _editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        // commit changes
        _editor.commit();
        Log.d(TAG, "Refresh Token: " + refreshToken + ", stored in shared preferences.");
    }

    /**
     * Return current refresh token from shared preferences
     * @return String users current refresh token
     * In case KEY_REFRESH_TOKEN = null, the String "NULL" will return
     */
    public String getRefreshToken(){
        String key_refresh_token = pref.getString(KEY_REFRESH_TOKEN, "NULL");
        Log.d(TAG, "Refresh Token returned " + key_refresh_token + " from shared preferences.");
        return key_refresh_token;
    }

    /**
     * Store current username to shared preferences
     *
     * @param username current username
     */
    public void setUsername(String username) {
        _editor.putString(KEY_USERNAME, username);
        // commit changes
        _editor.commit();
        Log.d(TAG, "Username: " + username + ", stored in shared preferences.");
    }

    /**
     * Return current username from shared preferences
     *
     * @return String users current username
     * In case KEY_USERNAME = null, the String "NULL" will return
     */
    String getUsername(){
        String key_username = pref.getString(KEY_USERNAME, "NULL");
        Log.d(TAG, "getUsername returned: " + key_username + ", from shared preferences.");
        return key_username;
    }

    /**
     * Remove current username from shared preferences
     */
    public void removeUsername(){
        Log.d(TAG, "Running 'removeUsername'");
        _editor.remove(KEY_USERNAME);
        _editor.commit();
    }
}
