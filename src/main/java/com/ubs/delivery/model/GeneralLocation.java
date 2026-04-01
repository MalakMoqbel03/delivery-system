package com.ubs.delivery.model;

public class GeneralLocation extends Location {

    public GeneralLocation(int id, String code, String name, double distanceKm) {
        super(id, code, name, distanceKm);
    }

    @Override
    public String getType() {
        return "LOC";
    }

    @Override
    public String describe() {
        return "id: " + getId()
                + ", type: GeneralLocation"
                + ", name: " + getName()
                + ", distance: " + getDistanceKm() + " km";
    }
}