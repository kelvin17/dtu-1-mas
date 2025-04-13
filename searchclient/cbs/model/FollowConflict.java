package searchclient.cbs.model;

import java.io.Serializable;

public class FollowConflict extends AbstractConflict implements Serializable {

    /**
     * @param plan1
     * @param plan2
     * @param follower       跟随者
     * @param followee       被跟随者
     * @param timeNow
     * @param locationOfObj1 跟随者的位置
     * @param locationOfObj2 被跟随者当前的位置 - 也就是follower不能去的位置
     */
    public FollowConflict(SingleAgentPlan plan1, SingleAgentPlan plan2, MovableObj follower, MovableObj followee, int timeNow, Location locationOfObj1, Location locationOfObj2) {
        super(plan1, plan2, follower, followee, timeNow, locationOfObj1, locationOfObj2, locationOfObj2);
    }

    @Override
    public ConflictType getConflictType() {
        return ConflictType.FollowConflict;
    }

    @Override
    public Constraint[] getPreventingConstraints() {
        return new Constraint[]{
                //Solution1 add a Constraint to Agent1
                new Constraint(plan1.getAgent(), getTimeNow(), getLocationOfObj1(), getTargetLocation()),
        };
    }

    @Override
    public String toString() {
        return "FollowConflict{" +
                "movableObj1.uniqueId=" + this.movableObj1.getUniqueId() +
                ", movableObj2.uniqueId=" + this.movableObj2.getUniqueId() +
                ", timeNow=" + this.timeNow +
                ", targetLocation=" + this.targetLocation +
                ", locationOfObj1=" + this.locationOfObj1 +
                ", locationOfObj2=" + this.locationOfObj2 +
                '}';
    }
}
