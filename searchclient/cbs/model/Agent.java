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
}
