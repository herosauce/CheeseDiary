package com.writeitdown.cheesediary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class DiaryRowAdapter extends BaseAdapter{

    ArrayList<CheeseTemplate> list;
    Context c;

    private ArrayList<CheeseTemplate> privateArray;

    public DiaryRowAdapter(Context c, ArrayList<CheeseTemplate> list){
        this.list = list;
        this.c = c;
        privateArray = new ArrayList<>();
        privateArray.addAll(list);
    }


    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.diary_row_item,null,true);

        ImageView image = (ImageView) row.findViewById(R.id.cheese_icon);
        TextView name = (TextView) row.findViewById(R.id.row_name);
        TextView type = (TextView) row.findViewById(R.id.tv_type);
        TextView classification = (TextView) row.findViewById(R.id.tv_class);
        TextView origin = (TextView) row.findViewById(R.id.tv_origin);

        RatingBar ratingBar = (RatingBar) row.findViewById(R.id.row_rating);
        ratingBar.setEnabled(false);
        ratingBar.setMax(5);
        ratingBar.setStepSize(0.01f);

        CheeseTemplate cheese = list.get(position);
        name.setText(cheese.name);

        //Set cheese image
        if (cheese.mCheesePhotoPath != null){

            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(c.openFileInput(cheese.mCheesePhotoPath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            image.setImageBitmap(bitmap);
            /* TODO delete this block before publishing
            //Get dimensions of the view
            int targetW = image.getWidth();
            int targetH = image.getHeight();

            //Get dimensions of the bitmap
            BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
            bmpOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(cheese.mCheesePhotoPath, bmpOptions);
            int photoW = bmpOptions.outWidth;
            int photoH = bmpOptions.outHeight;

            //Determine how much you want to scale the image
            //int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            //Decode the image file into a bitmap sized to fill the image view
            bmpOptions.inJustDecodeBounds = false;
            //bmpOptions.inSampleSize = scaleFactor;
            Bitmap bitmap = BitmapFactory.decodeFile(cheese.mCheesePhotoPath, bmpOptions);
            image.setImageBitmap(bitmap);
            */
        }
        //Set other text fields
        if (!cheese.type.contentEquals("")){
            type.setText(cheese.type);
        }
        if (!cheese.classification.contentEquals("")){classification.setText(cheese.classification);}
        if (!cheese.origin.contentEquals("")){origin.setText(cheese.origin);}

        //Set rating bar
        ratingBar.setRating(Float.parseFloat(cheese.rating.toString()));

        return row;
    }
}
