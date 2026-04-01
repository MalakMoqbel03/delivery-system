package com.ubs.delivery.model;

public class DeliveryPoint extends Location {

    private String priority;
    public DeliveryPoint(int id, String code, String name,
                         double distanceKm, String priority) {
        super(id, code, name, distanceKm);
        this.priority = priority;
    }
    public String getPriority()              { return priority; }
    public void   setPriority(String p)      { this.priority = p; }

    @Override
    public String getType() {
        return "DP";
    }
    @Override
    public String describe() {
        return "id: " + getId()
                + ", type: DeliveryPoint"
                + ", name: " + getName()
                + ", distance: " + getDistanceKm() + " km"
                + ", priority: " + priority;
    }
}