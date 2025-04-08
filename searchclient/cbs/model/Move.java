package searchclient.cbs.model;

import searchclient.Action;

/**
 * Base Model
 *  Agent move at timeNow, who is at currentLocation
 */
public class Move {
    private final int agentId;
    private final int timeNow;
    private final Location currentLocation;
    private final Action action;

    public Move(int agentId, int timeNow, Location currentLocation, Action action) {
        this.agentId = agentId;
        this.timeNow = timeNow;
        this.currentLocation = currentLocation;
        this.action = action;
    }

    public int getAgentId() {
        return agentId;
    }

    public int getTimeNow() {
        return timeNow;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public Action getAction() {
        return action;
    }

    public Move copy() {
        return new Move(agentId, timeNow, currentLocation.copy(), action);
    }
}
