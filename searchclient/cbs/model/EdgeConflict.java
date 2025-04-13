package searchclient.cbs.model;

/**
 * Model for CBS
 * Represents for the edge or swap conflict
 */
public class EdgeConflict extends AbstractConflict {

    public EdgeConflict(SingleAgentPlan plan1, SingleAgentPlan plan2, MovableObj obj1, MovableObj obj2, int timeNow, Location locationOfObj1, Location locationOfObj2) {
        super(plan1, plan2, obj1, obj2, timeNow, locationOfObj1, locationOfObj2, null);
    }

    @Override
    public String getConflictType() {
        return "EdgeConflict";
    }

    @Override
    public Constraint[] getPreventingConstraints() {
        return new Constraint[]{
                //Solution1, add Constraint to Agent1 from own location to location of Agent2
                new Constraint(plan1.getAgent(), getTimeNow(), getLocationOfObj1(), getLocationOfObj2()),
                //Solution2, add Constraint to Agent2 from own location to location of Agent1
                new Constraint(plan2.getAgent(), getTimeNow(), getLocationOfObj2(), getLocationOfObj1())
        };
    }
}
