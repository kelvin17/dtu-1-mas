package searchclient.cbs.model;

import searchclient.Color;

public class Box extends MovableObj implements AbstractDeepCopy<Box> {

    public Box(char uniqueId, Color color) {
        super(ObjectType.BOX, uniqueId, color);
    }

    public Box(char uniqueId, ObjectType objectType, Color color, Location initLocation, Location goalLocation, Location currentLocation) {
        super(ObjectType.BOX, uniqueId, color);
        this.setInitLocation(initLocation);
        this.setGoalLocation(goalLocation);
        this.setCurrentLocation(currentLocation);
    }

    @Override
    public String toString() {
        return "Box{uniqueId=" + this.uniqueId +
                ", objType=" + this.objType +
                ", color=" + this.getColor() +
                ", initLocation=" + this.getInitLocation() +
                ", goalLocation=" + this.getGoalLocation() +
                ", currentLocation=" + this.getCurrentLocation() +
                '}';
    }
}
