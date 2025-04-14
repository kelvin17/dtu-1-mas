package searchclient.cbs.model;

import searchclient.Color;

import java.io.Serializable;

public class Box extends MovableObj implements AbstractDeepCopy<Box>, Serializable {

    public Box(char uniqueId, Color color) {
        super(ObjectType.BOX, uniqueId, color);
    }

    @Override
    public String toString() {
        return "Box{uniqueId=" + this.uniqueId + ", objType=" + this.objType + ", color=" + this.getColor() + ", initLocation=" + this.getInitLocation() + ", goalLocation=" + this.getGoalLocation() + ", currentLocation=" + this.getCurrentLocation() + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Box box = (Box) obj;
        boolean equals = (uniqueId == box.uniqueId && objType == box.objType && color.equals(box.color) && initLocation.equals(box.initLocation));
        if (!equals) {
            return false;
        }
        if (this.getGoalLocation() == null) {
            if (box.getGoalLocation() != null) {
                return false;
            } else {
                return true;
            }
        } else {
            if (this.getGoalLocation().equals(box.getGoalLocation())) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public int hashCode() {
        int result = Character.hashCode(uniqueId);
        result = 31 * result + objType.hashCode();
        result = 31 * result + color.hashCode();
        result = 31 * result + initLocation.hashCode();
        result = 31 * result + (goalLocation != null ? goalLocation.hashCode() : 0);
        return result;
    }
}
