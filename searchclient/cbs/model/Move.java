package searchclient.cbs.model;

import searchclient.Action;

/**
 * Base Model
 * Agent move at timeNow, who is at currentLocation
 */
public class Move {
    private final Agent agent;
    private final int timeNow;
    private final Location currentLocation;
    private final Action action;
    private Location moveTo;

    public Move(Agent agent, int timeNow, Location currentLocation, Action action) {
        this.agent = agent;
        this.timeNow = timeNow;
        this.currentLocation = currentLocation;
        this.action = action;
    }

    public Agent getAgent() {
        return agent;
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
        return new Move(agent, timeNow, currentLocation.copy(), action);
    }

    //todo
    public Location getMoveTo() {
        return moveTo;
    }
}
