package com.softwaremobility.objects;

/**
 * Created by cristianloki on 14/04/16.
 */
public class FractionOption {
    private String fraction,id;

    public FractionOption(String fraction, String id) {
        this.fraction = fraction;
        this.id = id;
    }

    public String getFraction() {
        return fraction;
    }

    public void setFraction(String fraction) {
        this.fraction = fraction;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
