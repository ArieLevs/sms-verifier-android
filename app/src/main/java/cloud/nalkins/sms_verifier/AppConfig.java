package cloud.nalkins.sms_verifier;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Main configuration file
 */
public class AppConfig {
    static String ENVIRONMENT= "dev"; // production, alpha, dev

    public final static String APP_NAME = "SMS-Verifier";

    // SSL Certificate configs
    static boolean TRUST_ALL_CERTIFICATES = false; // TRUST_ALL_CERTIFICATES set at readProperties function
    static String SERVER_SSL_CRT_FILE = ""; // SERVER_SSL_CRT_FILE set at readProperties function

    // Web Server URIs
    static String API_SERVER_HOST = ""; // API_SERVER_HOST set at readProperties function
    static String API_SERVER_PORT = ""; // API_SERVER_PORT set at readProperties function
    static String API_SERVER_PROTOCOL = ""; // API_SERVER_PROTOCOL set at readProperties function
    private static String API_SERVER_URI = ""; // API_SERVER_URI set at readProperties function

    static String URL_AUTHENTICATION = API_SERVER_URI + "/token/"; // Client Authentication
    static String URL_REVOKE_TOKEN = API_SERVER_URI + "/revoke_token/"; // Revoke clients Token
    public static String URL_CONTACT_LIST = API_SERVER_URI + "/contacts_list/";
    static String URL_HEALTH_CHECK = API_SERVER_URI + "/health_check/";

    // OAuth Client ID
    static String OAUTH_CLIENT_ID = ""; // OAUTH_CLIENT_ID set at readProperties function
    // Client secret
    static String OAUTH_CLIENT_SECRET = ""; // OAUTH_CLIENT_SECRET set at readProperties function

    static String SMS_VERIFIER_ANDROID_README_URL = "https://github.com/ArieLevs/sms-verifier-android/blob/master/README.md";
    static String SMS_VERIFIER_ANDROID_LICENSE_URL = "https://github.com/ArieLevs/sms-verifier-android/blob/master/LICENSE";

    private static Properties properties;

    private static String getProperty(String key) throws NullPointerException {
        return properties.getProperty(key);
    }

    public static void readProperties(Context context) throws IOException {

        String propertiesFileName;

        switch (ENVIRONMENT) {
            case "alpha": {
                propertiesFileName = "alpha.properties";
            }
            break;
            case "dev": {
                propertiesFileName = "dev.properties";
            }
            break;
            case "production": {
                propertiesFileName = "production.properties";
            }
            break;
            default: {
                propertiesFileName = "";
            }
            break;
        }
        properties = new Properties();

        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(propertiesFileName);
        properties.load(inputStream);

        TRUST_ALL_CERTIFICATES = Boolean.parseBoolean(getProperty("trust_all_certificates"));

        // API configurations read
        SERVER_SSL_CRT_FILE = getProperty("server_ssl_crt_file_name");

        API_SERVER_HOST = getProperty("api_server_host");
        API_SERVER_PORT = getProperty("api_server_port");
        API_SERVER_PROTOCOL = getProperty("api_server_protocol");
        API_SERVER_URI = API_SERVER_PROTOCOL + "://" + API_SERVER_HOST + ":" + API_SERVER_PORT;

        URL_AUTHENTICATION = API_SERVER_URI + "/token/";
        URL_REVOKE_TOKEN = API_SERVER_URI + "/revoke_token/";
        URL_CONTACT_LIST = API_SERVER_URI + "/contacts_list/";
        URL_HEALTH_CHECK = API_SERVER_URI + "/health_check/";

        OAUTH_CLIENT_ID = getProperty("oauth_client_id");
        OAUTH_CLIENT_SECRET = getProperty("oauth_client_secret");
    }
}
