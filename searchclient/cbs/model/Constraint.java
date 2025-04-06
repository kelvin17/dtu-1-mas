package searchclient.cbs.model;

/**
 * Model for CBS
 *  This Constraint will be added to the Node
 */
public class Constraint {

    /**
     * The agent this constraint applied to at the specific time {@link #time}.
     */
    private final int agentId;

    private final int time;

    /**
     * Vertex Conflict will use only the {@link #toLocation}
     * Edge Conflict will use both of them.
     */
    private final Location fromLocation;

    private final Location toLocation;

    public Constraint(int agentId, int time, Location fromLocation, Location toLocation) {
        this.agentId = agentId;
        this.time = time;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
    }

    public int getAgentId() {
        return agentId;
    }

    public int getTime() {
        return time;
    }

    public Location getFromLocation() {
        return fromLocation;
    }

    public Location getToLocation() {
        return toLocation;
    }
}
