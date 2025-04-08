package searchclient.cbs.model;

/**
 * Model for CBS
 * Represents for the edge or swap conflict
 */
public class EdgeConflict extends AbstractConflict {

    public EdgeConflict(Agent agent1, Agent agent2, int time, Location locationOfAgent1, Location locationOfAgent2) {
        super(agent1, agent2, time, locationOfAgent1, locationOfAgent2, null);
    }

    @Override
    public String getConflictType() {
        return "EdgeConflict";
    }

    @Override
    public Constraint[] getPreventingConstraints() {
        return new Constraint[]{
                //Solution1, add Constraint to Agent1 from own location to location of Agent2
                new Constraint(getAgent1(), getTime(), getLocationOfAgent1(), getLocationOfAgent2()),
                //Solution2, add Constraint to Agent2 from own location to location of Agent1
                new Constraint(getAgent2(), getTime(), getLocationOfAgent2(), getLocationOfAgent1())
        };
    }
}
