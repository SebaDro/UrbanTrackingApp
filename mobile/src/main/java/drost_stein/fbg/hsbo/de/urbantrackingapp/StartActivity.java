package drost_stein.fbg.hsbo.de.urbantrackingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class StartActivity extends AppCompatActivity {

    private static final String PREFS_USER_ID = "userId";
    private static final String PREFS_NAME = "user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userId = sharedPref.getString(PREFS_USER_ID, null);
        if (userId == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        finish();
    }
}
