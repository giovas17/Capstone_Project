package com.softwaremobility.objects;

import android.os.Parcel;
import android.os.Parcelable;

public class Recipe implements Parcelable{
    private String id;
    private String description;
    private String imageURL;
    private String location;
    private String level;
    private int rating;
    private boolean alcohol;
    private boolean cofee;
    private String flagURL;
    private boolean imageFromBitmap;
    private boolean favorite;

    public Recipe() {
        this.id = "";
        this.description = "";
        this.imageURL = "";
        this.location = "";
        this.rating = 0;
        this.alcohol = false;
        this.cofee = false;
        this.flagURL = "";
        this.imageFromBitmap = false;
        this.level = "";
        this.favorite = false;
    }

    public Recipe(String id, String description, String imageURL, String location, int rating,
                  boolean alcohol, boolean cofee, String flagURL, String level, boolean favorite) {
        this.id = id;
        this.description = description;
        this.imageURL = imageURL;
        this.location = location;
        this.rating = rating;
        this.alcohol = alcohol;
        this.cofee = cofee;
        this.flagURL = flagURL;
        this.imageFromBitmap = false;
        this.level = level;
        this.favorite = favorite;
    }

    protected Recipe(Parcel in) {
        id = in.readString();
        description = in.readString();
        imageURL = in.readString();
        location = in.readString();
        rating = in.readInt();
        alcohol = in.readByte() != 0;
        cofee = in.readByte() != 0;
        flagURL = in.readString();
        imageFromBitmap = in.readByte() != 0;
        level = in.readString();
        favorite = in.readByte() != 0;
    }

    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public boolean isAlcohol() {
        return alcohol;
    }

    public void setAlcohol(boolean alcohol) {
        this.alcohol = alcohol;
    }

    public boolean isCofee() {
        return cofee;
    }

    public void setCofee(boolean cofee) {
        this.cofee = cofee;
    }

    public String getFlagURL() {
        return flagURL;
    }

    public void setFlagURL(String flagURL) {
        this.flagURL = flagURL;
    }

    public boolean isImageFromBitmap() {
        return imageFromBitmap;
    }

    public void setImageFromBitmap(boolean imageFromBitmap) {
        this.imageFromBitmap = imageFromBitmap;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(description);
        dest.writeString(imageURL);
        dest.writeString(location);
        dest.writeInt(rating);
        dest.writeByte((byte) (alcohol ? 1 : 0));
        dest.writeByte((byte) (cofee ? 1 : 0));
        dest.writeString(flagURL);
        dest.writeByte((byte) (imageFromBitmap ? 1 : 0));
        dest.writeString(level);
        dest.writeByte((byte) (favorite ? 1 : 0));
    }
}
