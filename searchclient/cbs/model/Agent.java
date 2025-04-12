package searchclient.cbs.model;

public class Agent {

    private final int agentId;
    private final MovableObj innerObj;
    private Location currentLocation;

    public Agent(MovableObj innerObj, Location currentLocation) {
        this.agentId = innerObj.getUniqueId();
        this.innerObj = innerObj;
        this.currentLocation = currentLocation;
    }

    public static Agent buildFromMovableObj(MovableObj innerObj) {
        return new Agent(innerObj, innerObj.getInitLocation());
    }

    public Agent copy() {
        return new Agent(innerObj, this.currentLocation.copy());
    }

    public int getAgentId() {
        return agentId;
    }

    public Location getGoalLocation() {
        return this.innerObj.getGoalLocation();
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }
}
