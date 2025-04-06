package searchclient.cbs.model;

/**
 * Model for CBS
 * Represents for the vertex conflict
 */
public class VertexConflict extends AbstractConflict {

    public VertexConflict(int agent1, int agent2, int time, Location locationOfAgent1, Location locationOfAgent2, Location targetLocation) {
        super(agent1, agent2, time, locationOfAgent1, locationOfAgent2, targetLocation);
    }

    @Override
    public String getConflictType() {
        return "VertexConflict";
    }

    @Override
    public Constraint[] getPreventingConstraints() {
        return new Constraint[]{
                //Solution1 add a Constraint to Agent1
                new Constraint(getAgent1(), getTime(), getLocationOfAgent1(), getTargetLocation()),
                //Solution2 add a Constraint to Agent2
                new Constraint(getAgent2(), getTime(), getLocationOfAgent2(), getTargetLocation())
        };
    }
}
