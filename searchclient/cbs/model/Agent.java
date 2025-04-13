package searchclient.cbs.model;

import searchclient.Color;

public class Agent extends MovableObj implements AbstractDeepCopy<Agent> {

    public Agent(char uniqueId, Color color) {
        super(ObjectType.AGENT, uniqueId, color);
    }

    public Agent(char uniqueId, ObjectType objectType, Color color, Location initLocation, Location goalLocation, Location currentLocation) {
        super(ObjectType.AGENT, uniqueId, color);
        this.setInitLocation(initLocation);
        this.setGoalLocation(goalLocation);
        this.setCurrentLocation(currentLocation);
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
