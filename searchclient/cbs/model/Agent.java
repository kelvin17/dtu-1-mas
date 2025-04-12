package searchclient.cbs.model;

import searchclient.Color;

public class Agent extends MovableObj {

    public Agent(char uniqueId, Color color) {
        super(ObjectType.AGENT, uniqueId, color);
    }

    public int getAgentId() {
        return this.getUniqueId();
    }

    public Agent copy() {
        return (Agent) super.copy();
    }
}
