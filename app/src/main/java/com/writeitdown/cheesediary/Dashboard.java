package com.writeitdown.cheesediary;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.util.Map;
import java.util.jar.Manifest;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class Dashboard extends AppCompatActivity {

    //For testing purposes, setting up SharedPreferences access to clear logs and badges
    public static final String MY_DIARY = "MyDiaryFile";
    SharedPreferences diarySP;
    public static final String MY_BADGES = "MyBadgesFile";
    SharedPreferences badgeSP;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Check permissions
        verifyStoragePermissions(this);

        Button openLog = (Button) findViewById(R.id.b_diary);
        openLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Diary.class));
            }
        });

        //TODO delete this button and handler after testing
        Button clearLog = (Button) findViewById(R.id.b_clear);
        clearLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diarySP = getSharedPreferences(MY_DIARY, MODE_PRIVATE);
                Map<String, ?> allLogs = diarySP.getAll();
                for (final Map.Entry<String, ?> entry : allLogs.entrySet()){
                    diarySP.edit().remove(entry.getKey()).apply();
                }
                //This next part deletes all badges earned, for the sake of testing.
                badgeSP = getSharedPreferences(MY_BADGES, MODE_PRIVATE);
                Map<String, ?> allBadges = badgeSP.getAll();
                for (final Map.Entry<String, ?> entry : allBadges.entrySet()){
                    badgeSP.edit().remove(entry.getKey()).apply();
                }
                Toast.makeText(getApplicationContext(), "Cleared the log and badges. Resume testing.", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DiaryEntry.class);
                intent.putExtra("Name", "NULL");
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */



    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    Toast.makeText(getApplicationContext(), "Access granted!", Toast.LENGTH_SHORT).show();

                } else {

                    // permission denied, boo! TODO Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), "Whomp whomp.", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
