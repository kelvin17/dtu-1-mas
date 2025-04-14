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

    public boolean originEqual(Box box) {
        boolean equal = this.getUniqueId() == box.getUniqueId()
                && this.objType == box.objType
                && this.getColor().equals(box.getColor())
                && this.getInitLocation().equals(box.getInitLocation());

        if (!equal) {
            return false;
        }

        if (this.getGoalLocation() == null) {
            return box.getGoalLocation() == null;
        } else {
            return this.getGoalLocation().equals(box.getGoalLocation());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Box box = (Box) obj;
        boolean equals = (uniqueId == box.uniqueId
                && objType == box.objType
                && color.equals(box.color)
                && initLocation.equals(box.initLocation))
                && currentLocation.equals(box.currentLocation);
        if (!equals) {
            return false;
        }
        if (this.getGoalLocation() == null) {
            return box.getGoalLocation() == null;
        } else {
            return this.getGoalLocation().equals(box.getGoalLocation());
        }
    }

    @Override
    public int hashCode() {
        int result = Character.hashCode(uniqueId);
        result = 31 * result + objType.hashCode();
        result = 31 * result + color.hashCode();
        result = 31 * result + initLocation.hashCode();
        result = 31 * result + (goalLocation != null ? goalLocation.hashCode() : 0);
        result = 31 * result + currentLocation.hashCode();
        return result;
    }
}
