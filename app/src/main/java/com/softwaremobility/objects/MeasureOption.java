package com.softwaremobility.objects;

public class MeasureOption {

    private String measure,id;

    public MeasureOption() {
        measure = "";
        id = "";
    }

    public MeasureOption(String measure, String id) {
        this.measure = measure;
        this.id = id;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
