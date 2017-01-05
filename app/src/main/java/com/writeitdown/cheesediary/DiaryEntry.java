package com.writeitdown.cheesediary;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public class DiaryEntry extends AppCompatActivity {

    /*
    List of badges one can earn:

    Gourmande (5 cheeses logged)
    Connosoir (10 cheeses logged)
    Adventurer (3 different countries)
    GlobeTrotter (10 different countries)
    PerfectPair (2 cheeses with 5 stars)
    LovingLife (5 cheeses with 5 stars)
    FoundTheBottom (First 1-star cheese)

    Since each of these can be earned through this activity
    We need to establish a SP file for each badge
     */

    public static final String BADGE_GOURMANDE = "GourmandeFile";
    SharedPreferences gourmandeSP;

    public static final String BADGE_ADVENTURER = "AdventurerFile";
    SharedPreferences adventureSP;

    public static final String BADGE_PERFECT_PAIR = "PerfectPairFile";
    SharedPreferences perfectSP;

    //Finally, need a file for the collection of badges
    public static final String MY_BADGES = "MyBadgesFile";
    SharedPreferences badgesSP;

    //Integer for camera intent
    static final int REQUEST_IMAGE_CAPTURE = 1;

    public static final String MY_DIARY = "MyDiaryFile";
    SharedPreferences diarySP;

    EditText etName, etType, etOrigin, etPrice, etBought, etFlavor, etTexture, etFood, etDrink, etPlaceDate;
    ImageButton ibCheese, ibLabel;
    Spinner classification;
    RatingBar ratingBar;

    CheeseTemplate cheese;

    String[] classOptions = {
            "(unspecified)",
            "Fresh Cheese",
            "Soft-ripened",
            "Washed Rind",
            "Blue-Veined",
            "Pressed, Uncooked",
            "Pressed, Cooked",
            "Stretchedd Curd",
            "Specialty Cheese"
    };

    //Initializing context object for db operations
    Context c = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_entry);

        diarySP = getSharedPreferences(MY_DIARY, MODE_PRIVATE);

        //Grabbing intent to determine name, if cheese exists
        Intent incomingIntent = getIntent();
        String nameKey = incomingIntent.getStringExtra("Name");

        //Set up spinner
        classification = (Spinner) findViewById(R.id.spin_classification);
        final ArrayList<String> classList = new ArrayList<>();
        Collections.addAll(classList, classOptions);
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, classList);
        classification.setAdapter(spinAdapter);
        classification.setSelection(0);

        //Set up rating bar
        ratingBar = (RatingBar) findViewById(R.id.rating);
        ratingBar.setEnabled(true);
        ratingBar.setMax(5);
        ratingBar.setStepSize(0.01f);

        //Check if this cheese already exists
        if (alreadyExists(nameKey)){
            //calls a function to set all views if this cheese exists already
            setTextViews(nameKey);
        }

        etName = (EditText) findViewById(R.id.et_name);

        //setting up image buttons
        ibCheese = (ImageButton) findViewById(R.id.ib_cheese);
        ibCheese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noName()){
                    Toast.makeText(getApplicationContext(), "Please specify a name first", Toast.LENGTH_LONG).show();
                } else {
                    if (!alreadyExists(etName.getText().toString())){
                        //If this cheese isn't already in the SP file, need to save it before moving it over
                        saveCheese(diarySP);
                    }
                    Intent startSnapManager = new Intent(getApplicationContext(), SnapshotManager.class);
                    startSnapManager.putExtra("Cheese", true); //to signify this is coming from the cheese button, not the label button
                    startSnapManager.putExtra("Name", etName.getText().toString());
                    startActivity(startSnapManager);
                }
            }
        });

        ibLabel = (ImageButton) findViewById(R.id.ib_label);
        ibLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noName()){
                    Toast.makeText(getApplicationContext(), "Please specify a name first", Toast.LENGTH_LONG).show();
                } else {
                    if (!alreadyExists(etName.getText().toString())){
                        //If this cheese isn't already in the SP file, need to save it before moving it over
                        saveCheese(diarySP);
                    }
                    Intent startSnapManager = new Intent(getApplicationContext(), SnapshotManager.class);
                    startSnapManager.putExtra("Cheese", false); //to signify this is coming from the label button, not the cheese button
                    startSnapManager.putExtra("Name", etName.getText().toString());
                    startActivity(startSnapManager);
                }
            }
        });

        //Set up buttons
        Button save = (Button) findViewById(R.id.b_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCheese(diarySP);
                startActivity(new Intent(getApplicationContext(), Dashboard.class));
            }
        });

        Button delete = (Button) findViewById(R.id.b_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!noName()){
                    etName = (EditText) findViewById(R.id.et_name);
                    String key = etName.getText().toString();
                    diarySP.edit().remove(key).apply();
                }
                startActivity(new Intent(getApplicationContext(), Dashboard.class));
            }
        });
    }

    private boolean noName(){
        etName = (EditText) findViewById(R.id.et_name);
        String nameCheck = etName.getText().toString();
        return (nameCheck.equals(""));
    }

    private boolean alreadyExists(String cheeseName){
        diarySP = getSharedPreferences(MY_DIARY, MODE_PRIVATE);
        return diarySP.contains(cheeseName);
    }

    private void saveCheese(SharedPreferences sp){
        if (noName()){
            Toast.makeText(getApplicationContext(), "Please specify a name first", Toast.LENGTH_LONG).show();
        } else {
            //Set up user input fields, im order
            etName = (EditText) findViewById(R.id.et_name);
            //Check and see if cheese already exists, if not need to initialize object
            if (alreadyExists(etName.getText().toString())){
                diarySP = getSharedPreferences(MY_DIARY, MODE_PRIVATE);
                Gson retrieveGson = new Gson();
                String retrieveJson = diarySP.getString(etName.getText().toString(), "");
                cheese = retrieveGson.fromJson(retrieveJson, CheeseTemplate.class);
            } else {
                //Initialize cheese object if it doesn't already exist
                cheese = new CheeseTemplate("", "", "", "", 0.0, "", "", "", "", "", "", 0.0, "", "");
            }

            cheese.name = etName.getText().toString();

            etType = (EditText) findViewById(R.id.et_type);
            if (!etType.getText().toString().equals("")) {
                cheese.type = etType.getText().toString();
            }

            //get spinner info and set classification attribute
            classification = (Spinner) findViewById(R.id.spin_classification);
            cheese.classification = classification.getSelectedItem().toString();

            etOrigin = (EditText) findViewById(R.id.et_origin);
            if (!etOrigin.getText().toString().equals("")){
                cheese.origin = etOrigin.getText().toString();
                updateAdventureBadge(cheese.origin);
            }

            etPrice = (EditText) findViewById(R.id.et_price);
            if (!etPrice.getText().toString().equals("")){
                String cheesePrice = etPrice.getText().toString();
                cheese.price = Double.parseDouble(cheesePrice);
            }
            /*
            etBought = (EditText) findViewById(R.id.et_purchased_loc);
            if (!etBought.getText().toString().equals("")){
                cheese.bought = etBought.getText().toString();
            }
            */
            etFlavor = (EditText) findViewById(R.id.et_flavors);
            if (!etFlavor.getText().toString().equals("")){
                cheese.flavor = etFlavor.getText().toString();
            }

            etTexture = (EditText) findViewById(R.id.et_texture);
            if (!etTexture.getText().toString().equals("")){
                cheese.texture = etTexture.getText().toString();
            }
            /*
            etDrink = (EditText) findViewById(R.id.et_drink);
            if (!etDrink.getText().toString().equals("")){
                cheese.drinkPair = etDrink.getText().toString();
            }

            etFood = (EditText) findViewById(R.id.et_food);
            if (!etFood.getText().toString().equals("")){
                cheese.foodPair = etFood.getText().toString();
            }

            etPlaceDate = (EditText) findViewById(R.id.et_location);
            if (!etPlaceDate.getText().toString().equals("")){
                cheese.locationTasted = etPlaceDate.getText().toString();
            }
            */
            //Store rating value
            cheese.rating = (double) ratingBar.getRating();
            //Based on rating, update appropriate badge
            badgesSP = getSharedPreferences(MY_BADGES, MODE_PRIVATE);
            if (cheese.rating <= 1.0){
                Log.i("Cheese: ", "rating is <= 1");
                //Check if user already has bottom badge
                Boolean hasFoundBottom = badgesSP.getBoolean("FoundBottom", false);
                if (!hasFoundBottom){
                    badgesSP.edit().putBoolean("FoundBottom", true).apply();
                    //notifyNewBadge(R.drawable.question_64, "Found the Bottom", "You've earned a new badge!");
                }
            } else if (cheese.rating == 5.0){
                updatePerfectBadge(cheese.name);
            }

            //Save object to Shared Prefs
            Gson gson = new Gson();
            String json = gson.toJson(cheese);
            sp.edit().putString(cheese.name, json).apply();

            //Likewise, save object to DB
            DatabaseOperations dbOp = new DatabaseOperations(c);
            dbOp.putNewCheese(dbOp, cheese.name, cheese.type, cheese.origin, cheese.rating, System.currentTimeMillis());


            //Last step: updating badge information
            updateGourmandeBadge(cheese.name);

            //And finish the thing.
            finish();
        }
    }

    private void setTextViews(String key) {
        diarySP = getSharedPreferences(MY_DIARY, MODE_PRIVATE);
        //Get current cheese object
        Gson retrieveGson = new Gson();
        String retrieveJson = diarySP.getString(key, "");
        final CheeseTemplate cheese = retrieveGson.fromJson(retrieveJson, CheeseTemplate.class);

        //Set up user input fields, in order
        etName = (EditText) findViewById(R.id.et_name);
        etName.setText(cheese.name);

        //Each of these lines below reads:
        //  "If this cheese value exists, set this textview with that information."
        etType = (EditText) findViewById(R.id.et_type);
        if (!cheese.type.equals("")) {etType.setText(cheese.type);}

        classification = (Spinner) findViewById(R.id.spin_classification);
        if (!cheese.classification.equals("")) {
            //Only need to worry about the scenario where a cheese classification is set
            //Otherwise, it's handled by the main thread
            //Line below is the most straightforward way to get the index of the stored classification
            classification.setSelection(Arrays.asList(classOptions).indexOf(cheese.classification));
        }

        etOrigin = (EditText) findViewById(R.id.et_origin);
        if (!cheese.origin.equals("")) {etOrigin.setText(cheese.origin);}

        etPrice = (EditText) findViewById(R.id.et_price);
        if (!cheese.price.equals(0.0)) {etPrice.setText(String.valueOf(cheese.price));}

        etFlavor = (EditText) findViewById(R.id.et_flavors);
        if (!cheese.flavor.equals("")) {etFlavor.setText(cheese.flavor);}

        etTexture = (EditText) findViewById(R.id.et_texture);
        if (!cheese.texture.equals("")) {etTexture.setText(cheese.texture);}

        /*
        etBought = (EditText) findViewById(R.id.et_purchased_loc);
        if (!cheese.bought.equals("")) {etBought.setText(cheese.bought);}

        etDrink = (EditText) findViewById(R.id.et_drink);
        if (!cheese.drinkPair.equals("")) {etDrink.setText(cheese.drinkPair);}

        etFood = (EditText) findViewById(R.id.et_food);
        if (!cheese.foodPair.equals("")) {etFood.setText(cheese.foodPair);}

        etPlaceDate = (EditText) findViewById(R.id.et_location);
        if (!cheese.locationTasted.equals("")) {etPlaceDate.setText(cheese.locationTasted);}
         */
        //Set stored images
        ibCheese = (ImageButton) findViewById(R.id.ib_cheese);
        if (!cheese.mCheesePhotoPath.equals("")) {
            //Calls function defined below that retrieves and sizes the image
            //setImage(ibCheese, cheese.mCheesePhotoPath);
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getApplicationContext().openFileInput(cheese.mCheesePhotoPath));
                ibCheese.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        ibLabel = (ImageButton) findViewById(R.id.ib_label);
        if (!cheese.mLabelPhotoPath.equals("")) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getApplicationContext().openFileInput(cheese.mLabelPhotoPath));
                ibLabel.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        //Set stored rating
        ratingBar = (RatingBar) findViewById(R.id.rating);
        ratingBar.setRating(Float.parseFloat(cheese.rating.toString()));
    }


    private void setImage(ImageButton button, String photoPath) {

        //Get dimensions of the view
        int targetW = button.getWidth();
        int targetH = button.getHeight();

        //Get dimensions of the bitmap
        BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
        bmpOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmpOptions);
        int photoW = bmpOptions.outWidth;
        int photoH = bmpOptions.outHeight;

        //Determine how much you want to scale the image
        //int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        //int scaleFactor = Math.min(photoW/targetW, photoH/targetW);

        //Decode the image file into a bitmap sized to fill the image view
        bmpOptions.inJustDecodeBounds = false;
        //bmpOptions.inSampleSize = scaleFactor;
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmpOptions);
        button.setImageBitmap(bitmap);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), Dashboard.class));
        finish();
    }

    private void updateGourmandeBadge(String name){
        //Start by putting a value in the file specific to this cheese
        gourmandeSP = getSharedPreferences(BADGE_GOURMANDE, MODE_PRIVATE);
        gourmandeSP.edit().putBoolean(name, true).apply();

        badgesSP = getSharedPreferences(MY_BADGES, MODE_PRIVATE);

        //Now to count the number of logged cheeses, to update badge status
        Integer counter = gourmandeSP.getAll().size();

        if (counter >= 5 && counter < 10 ){
            //Check if this badge is already set
            Boolean hasGourmande = badgesSP.getBoolean("Gourmande", false);
            if (!hasGourmande){
                badgesSP.edit().putBoolean("Gourmande", true).apply();
                //TODO update icon
                //notifyNewBadge(R.drawable.device_camera_icon, getString(R.string.gourmande), getString(R.string.new_badge));
            }
        } else if (counter >= 10 && counter <20){
            //Likewise, need to make sure we're not posting notifications if the badge is already set
            Boolean hasConnoisseur = badgesSP.getBoolean("Connoisseur", false);
            if (!hasConnoisseur){
                badgesSP.edit().putBoolean("Connoisseur", true).apply();
                //TODO update icon
                //notifyNewBadge(R.drawable.device_camera_icon, getString(R.string.connoisseur), getString(R.string.new_badge));
            }
        }//TODO add further cheese expert levels here
    }

    private void updateAdventureBadge(String country){
        //Start by filing a "true" for the country associated with this cheese
        adventureSP = getSharedPreferences(BADGE_ADVENTURER, MODE_PRIVATE);
        adventureSP.edit().putBoolean(country, true).apply();
        //Set up overall badges file
        badgesSP = getSharedPreferences(MY_BADGES, MODE_PRIVATE);
        //Setting up counter for unique country values (no valid checking on names)
        Integer counter = adventureSP.getAll().size();
        if (counter >= 3 && counter<10){
            //Check to see if user already has each tier of the badge
            Boolean hasAdventurer = badgesSP.getBoolean("Adventurer", false);
            if (!hasAdventurer){
                badgesSP.edit().putBoolean("Adventurer", true).apply();
                //TODO update icon
                //notifyNewBadge(R.drawable.device_camera_icon, getString(R.string.adventurer), getString(R.string.new_badge));
            }
        } else if (counter >= 10){
            Boolean hasGlobeTrotter = badgesSP.getBoolean("GlobeTrotter", false);
            if (!hasGlobeTrotter){
                badgesSP.edit().putBoolean("GlobeTrotter", true).apply();
                //TODO update icon
                //notifyNewBadge(R.drawable.device_camera_icon, getString(R.string.globe_trotter), getString(R.string.new_badge));
            }
        }//TODO additional traveler tiers can go here
    }

    private void updatePerfectBadge(String name){
        //This function should only be called if the rating for a particular cheese was 5
        perfectSP = getSharedPreferences(BADGE_PERFECT_PAIR, MODE_PRIVATE);
        perfectSP.edit().putBoolean(name, true).apply();
        //Set up global badge sp file
        badgesSP = getSharedPreferences(MY_BADGES, MODE_PRIVATE);
        //Set up integer counter for number of cheeses with perfect ratings
        Integer counter = badgesSP.getAll().size();
        if (counter >=2 && counter < 5){
            //Check to see if user already has each tier of the badge
            Boolean hasPerfectPair = badgesSP.getBoolean("PerfectPair", false);
            if (!hasPerfectPair){
                badgesSP.edit().putBoolean("PerfectPair",true).apply();
                //TODO update notification icon
                //notifyNewBadge(R.drawable.device_camera_icon, getString(R.string.perfect_pair), getString(R.string.new_badge));
            }
        } else if (counter >= 5 ){
            Boolean hasLovingLife = badgesSP.getBoolean("LovingLife", false);
            if(!hasLovingLife){
                badgesSP.edit().putBoolean("LovingLife", true).apply();
                //TODO update notification icon
                //notifyNewBadge(R.drawable.device_camera_icon, getString(R.string.loving_life), getString(R.string.new_badge));
            }
        }
    }

    private void notifyNewBadge(Integer icon, String title, String details){
        Log.i("notifyNewBadge", "new badge earned, attempting notification");
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(details);

        //Creating an explicit intent for the badges activity
        Intent resultIntent = new Intent(getApplicationContext(), Badges.class);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(Badges.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(9999, mBuilder.build());
    }
}
