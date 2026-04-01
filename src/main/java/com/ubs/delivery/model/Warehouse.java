package com.ubs.delivery.model;
public class Warehouse extends Location {

    private int currentLoad;
    private int maxCapacity;

    public Warehouse(int id, String code, String name,double distanceKm, int currentLoad, int maxCapacity) {
        super(id, code, name, distanceKm);
        this.currentLoad = currentLoad;
        this.maxCapacity = maxCapacity;
    }

    public int  getCurrentLoad()          { return currentLoad; }
    public int  getMaxCapacity()          { return maxCapacity; }
    public void setCurrentLoad(int load)  { this.currentLoad = load; }
    public void setMaxCapacity(int cap)   { this.maxCapacity = cap; }

    public boolean hasSpace() {
        return currentLoad < maxCapacity;
    }

    @Override
    public String getType() {
        return "WH";
    }

    @Override
    public String describe() {
        return "id: " + getId()
                + ", type: Warehouse"
                + ", name: " + getName()
                + ", distance: " + getDistanceKm() + " km"
                + ", capacity: " + currentLoad + "/" + maxCapacity;
    }
}