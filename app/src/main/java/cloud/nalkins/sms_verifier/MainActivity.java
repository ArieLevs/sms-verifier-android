package cloud.nalkins.sms_verifier;

import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName(); // set TAG for logs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
