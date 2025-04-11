package searchclient.cbs.model;

import searchclient.Color;

public class Agent {

    private final int agentId;

    private final Color color;

    private Location initialLocation;

    private Location currentLocation;

    private Location goalLocation;

    public Agent(int agentId, Color color) {
        this.agentId = agentId;
        this.color = color;
    }

    public Agent(int agentId, Color color, Location initialLocation, Location targetLocation) {
        this.agentId = agentId;
        this.color = color;
        this.initialLocation = initialLocation;
        this.goalLocation = targetLocation;
    }

    public Agent copy() {
        return new Agent(agentId, color, initialLocation.copy(), goalLocation.copy());
    }

    public int getAgentId() {
        return agentId;
    }

    public void setInitialLocation(Location initialLocation) {
        this.initialLocation = initialLocation;
    }

    public void setGoalLocation(Location goalLocation) {
        this.goalLocation = goalLocation;
    }

    public Location getInitialLocation() {
        return initialLocation;
    }

    public Location getGoalLocation() {
        return goalLocation;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }
}
