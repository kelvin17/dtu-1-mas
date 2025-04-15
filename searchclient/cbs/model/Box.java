package searchclient.cbs.model;

import searchclient.Color;

import java.io.Serializable;
import java.util.Objects;

public class Box extends MovableObj implements AbstractDeepCopy<Box>, Serializable {

    private final char boxTypeLetter;

    private Location goalLocation;

    public Box(char boxTypeLetter, Color color, Location initLocation) {
        super(ObjectType.BOX, boxTypeLetter + initLocation.toString(), color);
        this.initLocation = initLocation;
        this.boxTypeLetter = boxTypeLetter;
    }

    @Override
    public String toString() {
        return "Box{uniqueId=" + this.uniqueId + ", objType=" + this.objType + ", color=" + this.getColor() + ", initLocation=" + this.getInitLocation() + ", goalLocation=" + this.getGoalLocation() + ", currentLocation=" + this.getCurrentLocation() + '}';
    }

    public boolean originEqual(Box other) {
        if (other == null) return false;

        boolean equal = this.getUniqueId().equals(other.getUniqueId())
                && this.objType == other.objType
                && this.getColor().equals(other.getColor())
                && this.getInitLocation().equals(other.getInitLocation());

        if (!equal) {
            return false;
        }

        if (this.getGoalLocation() == null) {
            return other.getGoalLocation() == null;
        } else {
            return this.getGoalLocation().equals(other.getGoalLocation());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Box box = (Box) obj;
        boolean equals = (Objects.equals(uniqueId, box.uniqueId)
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

    //todo box允许相同的box-char - 改造ok
    @Override
    public int hashCode() {
        int result = uniqueId.hashCode();
        result = 31 * result + objType.hashCode();
        result = 31 * result + color.hashCode();
        result = 31 * result + initLocation.hashCode();
        result = 31 * result + (goalLocation != null ? goalLocation.hashCode() : 0);
        result = 31 * result + currentLocation.hashCode();
        return result;
    }

    public void setGoalLocation(Location goalLocation) {
        this.goalLocation = goalLocation;
    }

    public Location getGoalLocation() {
        return goalLocation;
    }

    public char getBoxTypeLetter() {
        return boxTypeLetter;
    }
}
