package searchclient.cbs.model;

import searchclient.Color;

import java.io.Serializable;

public class MovableObj implements Serializable {

    protected ObjectType objType;
    protected char uniqueId;
    protected Color color;
    protected Location initLocation;
    protected Location goalLocation;
    protected Location currentLocation;

    public MovableObj(ObjectType objType, char uniqueId, Color color) {
        this.objType = objType;
        this.uniqueId = uniqueId;
        this.color = color;
    }

    public MovableObj() {
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
        copy.setInitLocation(this.initLocation.deepCopy());
        copy.setGoalLocation(this.goalLocation.deepCopy());
        copy.setCurrentLocation(this.currentLocation.deepCopy());
        return copy;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        MovableObj other = (MovableObj) obj;
        if (this.uniqueId != other.uniqueId) {
            return false;
        }
        if (this.objType != other.objType) {
            return false;
        }
        if (this.color != other.color) {
            return false;
        }
        if (this.initLocation != null) {
            if (!this.initLocation.equals(other.initLocation)) {
                return false;
            }
        } else {
            if (other.initLocation != null) {
                return false;
            }
        }
        if (this.goalLocation != null) {
            if (!this.goalLocation.equals(other.goalLocation)) {
                return false;
            }
        } else {
            if (other.goalLocation != null) {
                return false;
            }
        }
        if (this.currentLocation != null) {
            if (!this.currentLocation.equals(other.currentLocation)) {
                return false;
            }
        } else {
            if (other.currentLocation != null) {
                return false;
            }
        }
        // If all checks passed, return true
        return true;
    }

    public int hashCode() {
        int result = 17;
        result = 31 * result + Character.hashCode(uniqueId);
        result = 31 * result + objType.hashCode();
        result = 31 * result + color.hashCode();
        result = 31 * result + (initLocation != null ? initLocation.hashCode() : 0);
        result = 31 * result + (goalLocation != null ? goalLocation.hashCode() : 0);
        result = 31 * result + (currentLocation != null ? currentLocation.hashCode() : 0);
        return result;
    }
}
