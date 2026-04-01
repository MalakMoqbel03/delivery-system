package com.ubs.delivery.model;

public abstract class Location {

    private int    id;
    private String code;
    private String name;
    private double distanceKm;

    public Location(int id, String code, String name, double distanceKm) {
        this.id         = id;
        this.code       = code;
        this.name       = name;
        this.distanceKm = distanceKm;
    }

    public int    getId()         { return id; }
    public String getCode()       { return code; }
    public String getName()       { return name; }
    public double getDistanceKm() { return distanceKm; }

    public void setId(int id)                { this.id = id; }
    public void setCode(String code)         { this.code = code; }
    public void setName(String name)         { this.name = name; }
    public void setDistanceKm(double d)      { this.distanceKm = d; }

    public abstract String getType();
    public abstract String describe();

    @Override
    public String toString() {
        return name + " (" + code + ") - " + distanceKm + " km from HQ";
    }
}