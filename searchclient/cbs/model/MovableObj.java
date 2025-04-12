package searchclient.cbs.model;

import searchclient.Color;

public class MovableObj {

    private final ObjectType objType;
    private final char uniqueId;
    private final Color color;
    private Location initLocation;
    private Location goalLocation;
    private Location currentLocation;

    public MovableObj(ObjectType objType, char uniqueId, Color color) {
        this.objType = objType;
        this.uniqueId = uniqueId;
        this.color = color;
    }

    public static MovableObj buildAgent(char uniqueId, Color color) {
        return new MovableObj(ObjectType.AGENT, uniqueId, color);
    }

    public static MovableObj buildBox(char uniqueId, Color color) {
        return new MovableObj(ObjectType.BOX, uniqueId, color);
    }

    public boolean isAgent() {
        return objType == ObjectType.AGENT;
    }

    public boolean isBox() {
        return objType == ObjectType.BOX;
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

    public Color getColor() {
        return color;
    }

    public char getUniqueId() {
        return uniqueId;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public MovableObj copy() {
        MovableObj copy = new MovableObj(this.objType, this.uniqueId, this.color);
        copy.setInitLocation(this.initLocation.copy());
        copy.setGoalLocation(this.goalLocation.copy());
        copy.setCurrentLocation(this.currentLocation.copy());
        return copy;
    }
}
