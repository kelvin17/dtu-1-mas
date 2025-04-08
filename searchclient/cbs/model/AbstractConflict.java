package searchclient.cbs.model;

/**
 * Model for CBS
 */
public abstract class AbstractConflict {

    private Agent agent1;
    private Agent agent2;
    private int time;
    //Used for vertex conflict
    private Location targetLocation;
    //The origin loc of Agent1
    private Location locationOfAgent1;
    //The origin loc of Agent2
    private Location locationOfAgent2;

    public AbstractConflict() {

    }

    public AbstractConflict(Agent agent1, Agent agent2, int time, Location locationOfAgent1, Location locationOfAgent2, Location targetLocation) {
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

    public Agent getAgent1() {
        return agent1;
    }

    public Agent getAgent2() {
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
