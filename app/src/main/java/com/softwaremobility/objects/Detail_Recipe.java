package com.softwaremobility.objects;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Detail_Recipe implements Parcelable {
    private String finalPortion;
    private int servingsSize;
    private boolean isFavorite;
    private String glass;
    private String level;
    private String instructions;
    private List<Direction> directions;
    private List<Ingredient> ingredients;

    public Detail_Recipe() {
        this.finalPortion = "";
        this.servingsSize = 0;
        this.level = "";
        this.glass = "";
        this.instructions = "";
        this.isFavorite = false;
        this.directions = new ArrayList<>();
        this.ingredients = new ArrayList<>();
    }

    public Detail_Recipe(String finalPortion, int servingsSize, String level, String instructions,
                         List<Direction> directions, @Nullable List<Ingredient> ingredients, String glass) {
        this.finalPortion = finalPortion;
        this.servingsSize = servingsSize;
        this.level = level;
        this.instructions = instructions;
        this.directions = directions;
        this.isFavorite = false;
        if(ingredients != null){
            this.ingredients = ingredients;
        }
        this.glass = glass;
    }

    public String getFinalPortion() {
        return finalPortion;
    }

    public void setFinalPortion(String finalPortion) {
        this.finalPortion = finalPortion;
    }

    public int getServingsSize() {
        return servingsSize;
    }

    public void setServingsSize(int servingsSize) {
        this.servingsSize = servingsSize;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public List<Direction> getDirections() {
        return directions;
    }

    public void setDirections(List<Direction> directions) {
        this.directions = directions;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public String getGlass() {
        return glass;
    }

    public void setGlass(String glass) {
        this.glass = glass;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }



    protected Detail_Recipe(Parcel in) {
        finalPortion = in.readString();
        servingsSize = in.readInt();
        glass = in.readString();
        level = in.readString();
        instructions = in.readString();
        if (in.readByte() == 0x01) {
            directions = new ArrayList<Direction>();
            in.readList(directions, Direction.class.getClassLoader());
        } else {
            directions = null;
        }
        if (in.readByte() == 0x01) {
            ingredients = new ArrayList<Ingredient>();
            in.readList(ingredients, Ingredient.class.getClassLoader());
        } else {
            ingredients = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(finalPortion);
        dest.writeInt(servingsSize);
        dest.writeString(glass);
        dest.writeString(level);
        dest.writeString(instructions);
        if (directions == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(directions);
        }
        if (ingredients == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(ingredients);
        }
    }

    @SuppressWarnings("unused")
    public static final Creator<Detail_Recipe> CREATOR = new Creator<Detail_Recipe>() {
        @Override
        public Detail_Recipe createFromParcel(Parcel in) {
            return new Detail_Recipe(in);
        }

        @Override
        public Detail_Recipe[] newArray(int size) {
            return new Detail_Recipe[size];
        }
    };
}