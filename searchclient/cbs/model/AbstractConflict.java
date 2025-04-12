package searchclient.cbs.model;

import searchclient.ActionType;

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

    public static AbstractConflict conflictBetween(Move move1, Move move2) {
        if (move1.getAction().type == ActionType.NoOp || move2.getAction().type == ActionType.NoOp) {
            // NoOp action, no conflict
            return null;
        }
        if (move1.getMoveTo() == move2.getMoveTo()) {
            return new VertexConflict(move1.getAgent(), move2.getAgent(), move1.getTimeNow(), move1.getCurrentLocation(), move2.getCurrentLocation(), move1.getMoveTo());
        }
        if (move1.getCurrentLocation() == move2.getMoveTo() && move2.getCurrentLocation() == move1.getMoveTo()) {
            return new EdgeConflict(move1.getAgent(), move2.getAgent(), move1.getTimeNow(), move1.getCurrentLocation(), move2.getCurrentLocation());
        }
        return null;
    }

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
