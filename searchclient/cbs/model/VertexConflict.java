package searchclient.cbs.model;

/**
 * Model for CBS
 * Represents for the vertex conflict
 */
public class VertexConflict extends AbstractConflict {

    public VertexConflict(SingleAgentPlan plan1, SingleAgentPlan plan2, MovableObj obj1, MovableObj obj2, int time, Location locationOfAgent1, Location locationOfAgent2, Location targetLocation) {
        super(plan1, plan2, obj1, obj2, time, locationOfAgent1, locationOfAgent2, targetLocation);
    }

    @Override
    public String getConflictType() {
        return "VertexConflict";
    }

    @Override
    public Constraint[] getPreventingConstraints() {
        return new Constraint[]{
                //Solution1 add a Constraint to Agent1
                new Constraint(plan1.getAgent(), getTime(), getLocationOfObj1(), getTargetLocation()),
                //Solution2 add a Constraint to Agent2
                new Constraint(plan2.getAgent(), getTime(), getLocationOfObj2(), getTargetLocation())
        };
    }
}
