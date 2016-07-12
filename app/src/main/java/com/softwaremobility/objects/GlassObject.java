package com.softwaremobility.objects;

/**
 * Created by darkgeat on 5/13/16.
 */
public class GlassObject {
    private String name;
    private String id;

    public GlassObject(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
