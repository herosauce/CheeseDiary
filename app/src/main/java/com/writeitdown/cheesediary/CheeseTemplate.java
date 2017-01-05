package com.writeitdown.cheesediary;

import android.graphics.Bitmap;

public class CheeseTemplate {

    String name;
    String type;
    String classification;
    String origin;
    Double price;

    String bought;
    String flavor;
    String texture;
    String foodPair;
    String drinkPair;

    String locationTasted;
    Double rating;

    String mCheesePhotoPath;
    String mLabelPhotoPath;

    public CheeseTemplate(String name, String type, String classification, String origin, Double price, String bought, String flavor, String texture, String foodPair, String drinkPair, String locationTasted, Double rating, String mCheesePhotoPath, String mLabelPhotoPath) {
        this.name = name;
        this.type = type;
        this.classification = classification;
        this.origin = origin;
        this.price = price;
        this.bought = bought;
        this.flavor = flavor;
        this.texture = texture;
        this.foodPair = foodPair;
        this.drinkPair = drinkPair;
        this.locationTasted = locationTasted;
        this.rating = rating;
        this.mCheesePhotoPath = mCheesePhotoPath;
        this.mLabelPhotoPath = mLabelPhotoPath;
    }
}
