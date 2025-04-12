package searchclient.cbs.model;

import searchclient.ActionType;

/**
 * Model for CBS
 */
public abstract class AbstractConflict {

    protected SingleAgentPlan plan1;
    protected SingleAgentPlan plan2;
    private MovableObj movableObj1;
    private MovableObj movableObj2;
    private int time;
    //Used for vertex conflict
    private Location targetLocation;
    //The origin loc of Agent1
    private Location locationOfObj1;
    //The origin loc of Agent2
    private Location locationOfObj2;

    public AbstractConflict() {

    }

    public AbstractConflict(SingleAgentPlan plan1, SingleAgentPlan plan2, MovableObj movableObj1, MovableObj movableObj2,
                            int time, Location locationOfObj1, Location locationOfObj2, Location targetLocation) {
        this.movableObj1 = movableObj1;
        this.movableObj2 = movableObj2;
        this.time = time;
        this.locationOfObj1 = locationOfObj1;
        this.locationOfObj2 = locationOfObj2;
        this.targetLocation = targetLocation;
    }

    public abstract String getConflictType();

    //todo 还需要再考虑一下有box的情况，这个是否可行
    public static AbstractConflict conflictBetween(SingleAgentPlan plan1, SingleAgentPlan plan2, Move move1, Move move2) {
        if (move1.getAction().type == ActionType.NoOp || move2.getAction().type == ActionType.NoOp) {
            // NoOp action, no conflict
            return null;
        }
        if (move1.getMoveTo() == move2.getMoveTo()) {
            return new VertexConflict(plan1, plan2, plan1.getAgent(), plan2.getAgent(), move1.getTimeNow(), move1.getCurrentLocation(), move2.getCurrentLocation(), move1.getMoveTo());
        }
        if (move1.getCurrentLocation() == move2.getMoveTo() && move2.getCurrentLocation() == move1.getMoveTo()) {
            return new EdgeConflict(plan1, plan2, plan1.getAgent(), plan2.getAgent(), move1.getTimeNow(), move1.getCurrentLocation(), move2.getCurrentLocation());
        }
        return null;
    }

    /**
     * @return an array of constraints, each of which could prevent this conflict
     */
    public abstract Constraint[] getPreventingConstraints();

    public MovableObj getMovableObj1() {
        return movableObj1;
    }

    public MovableObj getMovableObj2() {
        return movableObj2;
    }

    public int getTime() {
        return time;
    }

    public Location getLocationOfObj1() {
        return locationOfObj1;
    }

    public Location getLocationOfObj2() {
        return locationOfObj2;
    }

    public Location getTargetLocation() {
        return targetLocation;
    }
}
