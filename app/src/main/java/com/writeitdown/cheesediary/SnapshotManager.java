package com.writeitdown.cheesediary;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.Manifest;

public class SnapshotManager extends AppCompatActivity {

    //Integer for camera intent
    //static final int REQUEST_IMAGE_CAPTURE = 1;
    String mCurrentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;

    public static final String MY_DIARY = "MyDiaryFile";
    SharedPreferences diarySP;

    ImageButton button;

    //New approach beings here
    ImageView imageView;
    Toolbar toolbar;
    File file;
    Uri uri;
    Intent camIntent, galIntent, cropIntent;
    DisplayMetrics displayMetrics;
    int width, height;
    final int REQUEST_PERMISSION_CODE = 10;
    boolean imageReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snapshot_manager);

        //Below: all new in 2017
        imageReady = false;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Crop image");
        setSupportActionBar(toolbar);

        imageView = (ImageView) findViewById(R.id.iv_photo);
        int permissionCheck = ContextCompat.checkSelfPermission(SnapshotManager.this, android.Manifest.permission.CAMERA);
        if (permissionCheck == PackageManager.PERMISSION_DENIED){
            requestRuntimePermission();
        }




        Intent intent = getIntent();
        final String cheeseName = intent.getStringExtra("Name");
        final boolean isCheese = intent.getBooleanExtra("Cheese", false); //determines whether this image should be stored as a picture of cheese, or of a cheese label

        /* //TODO delete this if the above attempt works
        button = (ImageButton) findViewById(R.id.ib_photo);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        */

        diarySP = getSharedPreferences(MY_DIARY, MODE_PRIVATE);
        Button bSave = (Button) findViewById(R.id.b_save);
        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get current cheese object
                Gson retrieveGson = new Gson();
                String retrieveJson = diarySP.getString(cheeseName, "");
                final CheeseTemplate currentCheese = retrieveGson.fromJson(retrieveJson, CheeseTemplate.class);

                if (isCheese){ //Check against intent to make sure this is stored as the right photo
                    currentCheese.mCheesePhotoPath = mCurrentPhotoPath;
                } else {
                    currentCheese.mLabelPhotoPath = mCurrentPhotoPath;
                }

                Gson gson = new Gson();
                String json = gson.toJson(currentCheese);
                diarySP.edit().putString(currentCheese.name, json).apply();

                Intent backToDiary = new Intent(getApplicationContext(), DiaryEntry.class);
                backToDiary.putExtra("Name", currentCheese.name);
                startActivity(backToDiary);
            }
        });

        Button bCancel = (Button) findViewById(R.id.b_cancel);
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Just need to get back to DiaryEntry for this cheese
                Intent backToDiary = new Intent(getApplicationContext(), DiaryEntry.class);
                backToDiary.putExtra("Name", cheeseName);
                startActivity(backToDiary);
            }
        });
    }

    //New method to request runtime permission if necessary
    private void requestRuntimePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(SnapshotManager.this, android.Manifest.permission.CAMERA)){
            Toast.makeText(SnapshotManager.this, "Please allow camera use to store cheese and label snapshots", Toast.LENGTH_SHORT).show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(SnapshotManager.this, new String[]{android.Manifest.permission.CAMERA},REQUEST_PERMISSION_CODE);
            }
        }

    }

    //New override method for options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    //New method to launch activities based on menu item clicks


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_camera){
            cameraOpen();
        } else if (item.getItemId() == R.id.action_gallery){
            galleryOpen();
        }
        return true;
    }

    private void galleryOpen() {
        galIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(galIntent, "Select image from gallery"),2);
    }

    private void cameraOpen() {
        camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(Environment.getExternalStorageDirectory(),
                "cheese"+String.valueOf(System.currentTimeMillis())+".jpg");
        uri = Uri.fromFile(file);
        camIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        camIntent.putExtra("return-data",true);
        startActivityForResult(camIntent,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK){
            cropImage();
        } else if (requestCode == 2){
            if (data!= null){
                uri = data.getData();
                cropImage();
            }
        } else if (requestCode == 1){
            if (data != null){
                Bundle bundle = data.getExtras();
                Bitmap bitmap = bundle.getParcelable("data");
                imageView.setImageBitmap(bitmap);
                imageReady = true;
                mCurrentPhotoPath = formatFilePath(bitmap);
            }
        }
    }

    private String formatFilePath(Bitmap bitmap) {
        String filepath = "cheese" + String.valueOf(System.currentTimeMillis());
        try{
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bytes);
            FileOutputStream fo = openFileOutput(filepath, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (Exception e){
            e.printStackTrace();
            filepath = "";
        }
        return filepath;
    }

    private void cropImage() {

        try {
            cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(uri, "image/*");

            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("outputX",180);
            cropIntent.putExtra("outputY",180);
            cropIntent.putExtra("aspectX",3);
            cropIntent.putExtra("aspectX",4);
            cropIntent.putExtra("scaleUpIfNeeded", true);
            cropIntent.putExtra("return-data", true);

            startActivityForResult(cropIntent,1);
        } catch (ActivityNotFoundException ex) {

        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i("Snapshot", "IO exception: error creating file");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_PERMISSION_CODE:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied.", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "SayCheese_" + timeStamp + ".jpg";
        File photo = new File(Environment.getExternalStorageDirectory(), imageFileName);
/*
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );
*/
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = photo.getAbsolutePath();
        return photo;
    }

    //TODO: Delete below if current test works
    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse("file://" + mCurrentPhotoPath));
                button = (ImageButton) findViewById(R.id.ib_photo);
                button.setImageBitmap(imageBitmap);
                Log.i("OnResult: ", "image set!");
            } catch (IOException e) {
                Log.i("OnResult", "Exception!");
                e.printStackTrace();
            }
        }
    }
    */
}

/*
Posting method here in case it's needed later
private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                //TODO: make a toast or something
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.writeitdown.cheesediary",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

 */
