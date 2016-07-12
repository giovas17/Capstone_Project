package com.softwaremobility.objects;

import android.os.Parcel;
import android.os.Parcelable;

public class Ingredient implements Parcelable {
    private double amount;
    private String measure;
    private String ingredient;
    private String fraction;
    private String fractionID;
    private String measureId;
    private String id;


    public Ingredient() {
        this.amount = 0d;
        this.measure = "";
        this.ingredient = "";
        this.fraction = "";
        this.fractionID = "";
        this.measureId = "";
        this.id = "";
    }

    public Ingredient(double amount, String measure, String ingredient, String fraction) {
        this.amount = amount;
        this.measure = measure;
        this.ingredient = ingredient;
        this.fraction = fraction;
    }

    public Ingredient(double amount, String measure, String ingredient, String fraction, String fractionID, String measureId, String id) {
        this.amount = amount;
        this.measure = measure;
        this.ingredient = ingredient;
        this.fraction = fraction;
        this.fractionID = fractionID;
        this.measureId = measureId;
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public String getFraction() {
        return fraction;
    }

    public void setFraction(String fraction) {
        this.fraction = fraction;
    }

    public String getFractionID() {
        return fractionID;
    }


    public String getMeasureId() {
        return measureId;
    }

    public void setMeasureId(String measureId) {
        this.measureId = measureId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFractionID(String fractionID) {
        this.fractionID = fractionID;
    }


    protected Ingredient(Parcel in) {
        amount = in.readDouble();
        measure = in.readString();
        ingredient = in.readString();
        fraction = in.readString();
        fractionID = in.readString();
        measureId = in.readString();
        id = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(amount);
        dest.writeString(measure);
        dest.writeString(ingredient);
        dest.writeString(fraction);
        dest.writeString(fractionID);
        dest.writeString(measureId);
        dest.writeString(id);
    }

    @SuppressWarnings("unused")
    public static final Creator<Ingredient> CREATOR = new Creator<Ingredient>() {
        @Override
        public Ingredient createFromParcel(Parcel in) {
            return new Ingredient(in);
        }

        @Override
        public Ingredient[] newArray(int size) {
            return new Ingredient[size];
        }
    };

    @Override
    public String toString() {
        return "Ingredient{" +
                "amount=" + amount +
                ", measure='" + measure + '\'' +
                ", ingredient='" + ingredient + '\'' +
                ", fraction='" + fraction + '\'' +
                ", fractionID='" + fractionID + '\'' +
                ", measureId='" + measureId + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}