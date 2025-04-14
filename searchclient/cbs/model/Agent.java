package searchclient.cbs.model;

import searchclient.Color;

import java.io.Serializable;

public class Agent extends MovableObj implements AbstractDeepCopy<Agent>, Serializable {

    public Agent(char uniqueId, Color color) {
        super(ObjectType.AGENT, uniqueId, color);
    }

    public Agent() {
        super();
    }

    public char getAgentId() {
        return this.getUniqueId();
    }

    @Override
    public String toString() {
        return "Agent {uniqueId=" + this.uniqueId +
                ", objType=" + this.objType +
                ", color=" + this.getColor() +
                ", initLocation=" + this.getInitLocation() +
                ", goalLocation=" + this.getGoalLocation() +
                ", currentLocation=" + this.getCurrentLocation() +
                "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Agent box = (Agent) obj;
        boolean equals = (uniqueId == box.uniqueId
                && objType == box.objType
                && color.equals(box.color)
                && initLocation.equals(box.initLocation))
                && currentLocation.equals(box.currentLocation);
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
        result = 31 * result + currentLocation.hashCode();
        return result;
    }
}
