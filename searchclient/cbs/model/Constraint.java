package searchclient.cbs.model;

import java.util.Objects;

/**
 * Model for CBS
 * This Constraint will be added to the Node
 */
public class Constraint {

    /**
     * The agent this constraint applied to at the specific time {@link #time}.
     */
    private final Agent agent;

    private final int time;

    /**
     * Vertex Conflict will use only the {@link #toLocation}
     * Edge Conflict will use both of them.
     */
    private final Location fromLocation;

    private final Location toLocation;

    public Constraint(Agent agent, int time, Location fromLocation, Location toLocation) {
        this.agent = agent;
        this.time = time;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
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

    public Agent getAgent() {
        return agent;
    }

    @Override
    public int hashCode() {
        return Objects.hash(agent, time, fromLocation, toLocation);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Constraint that = (Constraint) obj;
        return time == that.time
                && Objects.equals(agent, that.agent)
                && Objects.equals(fromLocation, that.fromLocation)
                && Objects.equals(toLocation, that.toLocation);
    }
}
