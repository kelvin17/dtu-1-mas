package searchclient.cbs.model;

import java.io.Serializable;

/**
 * Model for CBS
 * Represents for the vertex conflict
 */
public class VertexConflict extends AbstractConflict implements Serializable {

    public VertexConflict(SingleAgentPlan plan1, SingleAgentPlan plan2, MovableObj obj1, MovableObj obj2, int timeNow, Location locationOfAgent1, Location locationOfAgent2, Location targetLocation) {
        super(plan1, plan2, obj1, obj2, timeNow, locationOfAgent1, locationOfAgent2, targetLocation);
    }

    @Override
    public String getConflictType() {
        return "VertexConflict";
    }

    @Override
    public Constraint[] getPreventingConstraints() {
        return new Constraint[]{
                //Solution1 add a Constraint to Agent1
                new Constraint(plan1.getAgent(), getTimeNow(), getLocationOfObj1(), getTargetLocation()),
                //Solution2 add a Constraint to Agent2
                new Constraint(plan2.getAgent(), getTimeNow(), getLocationOfObj2(), getTargetLocation())
        };
    }

    @Override
    public String toString() {
        return "VertexConflict{" +
                "movableObj1.uniqueId=" + this.movableObj1.getUniqueId() +
                ", movableObj2.uniqueId=" + this.movableObj2.getUniqueId() +
                ", timeNow=" + this.timeNow +
                ", targetLocation=" + this.targetLocation +
                ", locationOfObj1=" + this.locationOfObj1 +
                ", locationOfObj2=" + this.locationOfObj2 +
                '}';
    }
}
