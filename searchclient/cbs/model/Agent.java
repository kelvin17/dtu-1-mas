package searchclient.cbs.model;

public class Agent {

    private final int agentId;

    private final Location initialLocation;

    private Location currentLocation;

    private final Location targetLocation;

    public Agent(int agentId, Location initialLocation, Location targetLocation) {
        this.agentId = agentId;
        this.initialLocation = initialLocation;
        this.targetLocation = targetLocation;
    }

    public int getAgentId() {
        return agentId;
    }

    public Location getInitialLocation() {
        return initialLocation;
    }

    public Location getTargetLocation() {
        return targetLocation;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }
}
