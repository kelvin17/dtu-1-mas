package searchclient.cbs.model;

import searchclient.Color;

public class Box {
    private final char boxLetter;
    private final Color color;
    private Location initLocation;
    private Location goalLocation;
    private Location currentLocation;

    public Box(char boxLetter, Color color) {
        this.boxLetter = boxLetter;
        this.color = color;
    }

    public Box(char boxLetter, Color color, Location initLocation, Location goalLocation, Location currentLocation) {
        this.boxLetter = boxLetter;
        this.color = color;
        this.initLocation = initLocation;
        this.goalLocation = goalLocation;
        this.currentLocation = currentLocation;
    }

    public Box copy() {
        return new Box(this.boxLetter, this.color, this.initLocation.copy(), this.goalLocation.copy(), this.currentLocation.copy());
    }

    public Location getInitLocation() {
        return initLocation;
    }

    public void setInitLocation(Location initLocation) {
        this.initLocation = initLocation;
    }

    public Location getGoalLocation() {
        return goalLocation;
    }

    public void setGoalLocation(Location goalLocation) {
        this.goalLocation = goalLocation;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }
}
