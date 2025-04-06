package searchclient.cbs.model;

/**
 * Model for CBS
 */
public abstract class AbstractConflict {

    private final int agent1;
    private final int agent2;
    private final int time;
    //Used for vertex conflict
    private final Location targetLocation;
    //The origin loc of Agent1
    private final Location locationOfAgent1;
    //The origin loc of Agent2
    private final Location locationOfAgent2;

    public AbstractConflict(int agent1, int agent2, int time, Location locationOfAgent1, Location locationOfAgent2, Location targetLocation) {
        this.agent1 = agent1;
        this.agent2 = agent2;
        this.time = time;
        this.locationOfAgent1 = locationOfAgent1;
        this.locationOfAgent2 = locationOfAgent2;
        this.targetLocation = targetLocation;
    }

    public abstract String getConflictType();

    /**
     * @return an array of constraints, each of which could prevent this conflict
     */
    public abstract Constraint[] getPreventingConstraints();

    public int getAgent1() {
        return agent1;
    }

    public int getAgent2() {
        return agent2;
    }

    public int getTime() {
        return time;
    }

    public Location getLocationOfAgent1() {
        return locationOfAgent1;
    }

    public Location getLocationOfAgent2() {
        return locationOfAgent2;
    }

    public Location getTargetLocation() {
        return targetLocation;
    }
}
