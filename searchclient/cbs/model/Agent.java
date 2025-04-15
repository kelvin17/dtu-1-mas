package searchclient.cbs.model;

import searchclient.Color;

import java.io.Serializable;
import java.util.Objects;

public class Agent extends MovableObj implements AbstractDeepCopy<Agent>, Serializable {

    private Location goalLocation;

    private char agentChar;

    public Agent(char uniqueId, Color color) {
        super(ObjectType.AGENT, String.valueOf(uniqueId), color);
        this.agentChar = uniqueId;
    }

    public Agent() {
        super();
    }

    public char getAgentId() {
        return this.agentChar;
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
        Agent agent = (Agent) obj;
        boolean equals = (Objects.equals(uniqueId, agent.uniqueId)
                && objType == agent.objType
                && color.equals(agent.color)
                && initLocation.equals(agent.initLocation))
                && currentLocation.equals(agent.currentLocation);
        if (!equals) {
            return false;
        }
        if (this.getGoalLocation() == null) {
            return agent.getGoalLocation() == null;
        } else {
            return this.getGoalLocation().equals(agent.getGoalLocation());
        }
    }

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
}
