package searchclient.cbs.model;

import searchclient.Color;

public class Box extends MovableObj {

    public Box(char uniqueId, Color color) {
        super(ObjectType.BOX, uniqueId, color);
    }

    public Box copy() {
        Box newBox = new Box(this.getUniqueId(), this.getColor());
        newBox.setInitLocation(this.getInitLocation().copy());
        newBox.setGoalLocation(this.getGoalLocation() == null ? null : this.getGoalLocation().copy());
        newBox.setCurrentLocation(this.getCurrentLocation() == null ? null : this.getCurrentLocation().copy());

        return newBox;
    }

}
