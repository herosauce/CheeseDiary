package com.writeitdown.cheesediary;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

public class Badges extends AppCompatActivity {

    /*
    List of badges one can earn:

    Gourmande (5 cheeses logged)
    Connosoir (10 cheeses logged)
    Adventurer (3 different countries)
    GlobeTrotter (10 different countries)
    LovingLife (5 cheeses with 5 stars)
    FoundTheBottom (First 1-star cheese)
     */

    //This SP file contains booleans for each badge earned.
    //Every time a user meets the badge requirements set in whichever activity (mostly in DiaryEntry),
    //that activity will add a badge to this SP file with the bool=true
    public static final String MY_BADGES = "MyBadgesFile";
    SharedPreferences badgesSP;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badges);

        listView = (ListView) findViewById(R.id.badge_list);


    }
}
