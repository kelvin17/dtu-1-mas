package searchclient.cbs.model;

import searchclient.Color;

public class Agent extends MovableObj {

    public Agent(char uniqueId, Color color) {
        super(ObjectType.AGENT, uniqueId, color);
    }

    public Agent copy() {
        Agent newAgent = new Agent(this.getUniqueId(), this.getColor());
        newAgent.setInitLocation(this.getInitLocation().copy());
        newAgent.setGoalLocation(this.getGoalLocation() == null ? null : this.getGoalLocation().copy());
        newAgent.setCurrentLocation(this.getCurrentLocation() == null ? null : this.getCurrentLocation().copy());

        return newAgent;
    }

    public int getAgentId() {
        return this.getUniqueId();
    }

}
