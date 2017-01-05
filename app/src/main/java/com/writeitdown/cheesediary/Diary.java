package com.writeitdown.cheesediary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.internal.Streams;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class Diary extends Activity {

    public static final String MY_DIARY = "MyDiaryFile";
    SharedPreferences diarySP;

    ArrayList<CheeseTemplate> rowList;
    ListView listView;
    private DiaryRowAdapter adapter;
    Context c = this;

    //Sort options for spinner at top of screen
    String[] sortOptions = {
            "(select sort option)",
            "Name",
            "Rating",
            "Type",
            "Region"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        listView = (ListView) findViewById(R.id.cheeseList);
        rowList = new ArrayList<>();

        diarySP = getSharedPreferences(MY_DIARY, MODE_PRIVATE);
        Map<String, ?> allCheeses = diarySP.getAll();
        for (final Map.Entry<String, ?> entry : allCheeses.entrySet()){
            Gson retrieveGson = new Gson();
            CheeseTemplate cheeseTemplate = retrieveGson.fromJson(entry.getValue().toString(), CheeseTemplate.class);
            rowList.add(cheeseTemplate);
        }

        //Set up spinner for sort options
        Spinner sortSpinner = (Spinner) findViewById(R.id.sort_options);
        final ArrayList<String> optionsList = new ArrayList<>();
        Collections.addAll(optionsList, sortOptions);
        ArrayAdapter<String> arrayAdapter= new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, optionsList);
        sortSpinner.setAdapter(arrayAdapter);
        sortSpinner.setSelection(0);
        //TODO: test for each scenario
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String order = orderBy(sortOptions[position]);
                rowList = sortRows(rowList, order);
                updateView(listView, rowList);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //If no spinner is selected, eg when the activity first starts, use default list
                updateView(listView, rowList);
            }
        });
    }

    private void updateView(ListView listView, final ArrayList<CheeseTemplate> rowList) {
        //First, remove all views
        //listView.removeAllViews();
        adapter = new DiaryRowAdapter(this, rowList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheeseTemplate selectedCheese = rowList.get(position);
                Intent manageDiaryEntry = new Intent(getApplicationContext(), DiaryEntry.class);
                manageDiaryEntry.putExtra("Name", selectedCheese.name);
                startActivity(manageDiaryEntry);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private ArrayList<CheeseTemplate> sortRows(ArrayList<CheeseTemplate> allRows, String order) {
        //Sorts the row list by iterating through the DB using the user-specified order, and adding cheese objects to the arraylist in the order desired
        //Begin by establishing DB operations
        DatabaseOperations dop = new DatabaseOperations(c);
        //Now initialize the sorted list
        ArrayList<CheeseTemplate> sortedList = new ArrayList<>();
        //Create a cursor to check the db
        Cursor cur = dop.getCheeseFromTable(dop, order);
        cur.moveToFirst();
        do {
            String curCheeseName = cur.getString(cur.getColumnIndex(TableData.TableInfo.CHEESE_NAME));
            //Loop over input list; as soon as a match is found, add that item to the sorted list.
            for (CheeseTemplate row:allRows) {
                if (Objects.equals(row.name, curCheeseName)){sortedList.add(row);}
            }
        } while (cur.moveToNext());
        cur.close();
        dop.close();
        return sortedList;
    }

    private String orderBy(String sortOption){
        //Simple function to get a SQL Order By string based on spinner selection
        switch (sortOption){
            case "Name":
                return TableData.TableInfo.CHEESE_NAME + " ASC";
            case "Rating":
                return TableData.TableInfo.RATING + " DESC";
            case "Type":
                return TableData.TableInfo.TYPE + " ASC";
            case "Region":
                return TableData.TableInfo.REGION + " ASC";
        }

        Log.d("Order By", "Failed to recognize order by selection:" + sortOption);
        Toast.makeText(getApplicationContext(), "Please make a different selection.", Toast.LENGTH_SHORT).show();
        return TableData.TableInfo.CHEESE_NAME + " ASC"; //Sort by name, by default
    }
}