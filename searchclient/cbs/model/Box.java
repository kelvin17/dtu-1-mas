package searchclient.cbs.model;

import searchclient.Color;

import java.io.Serializable;

public class Box extends MovableObj implements AbstractDeepCopy<Box>, Serializable {

    public Box(char uniqueId, Color color) {
        super(ObjectType.BOX, uniqueId, color);
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
