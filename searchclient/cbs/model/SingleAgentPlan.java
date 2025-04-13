package searchclient.cbs.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Model for Single Agent Plan
 * Represents the plan of a single agent, including the env, agent, boxes and a effective move list for this plan.
 * 代表一个单独的代理的计划，包括环境、代理、箱子和使得该计划可解的一个有效移动列表。
 */
public class SingleAgentPlan implements AbstractDeepCopy<SingleAgentPlan>, Serializable {

    private Agent agent;
    private List<Box> boxes = new ArrayList<>();
    private Map<Integer, Move> moves = new TreeMap<>();
    private Environment env;

    public SingleAgentPlan(Agent agent, List<Box> boxes, Environment env) {
        this.agent = agent;
        this.boxes = boxes;
        this.env = env;
    }

    public SingleAgentPlan() {
    }

    public Environment getEnv() {
        return env;
    }

    public SingleAgentPlan(Agent agent, Environment env) {
        this.agent = agent;
        this.env = env;
    }

    public void setMoves(Map<Integer, Move> moves) {
        this.moves = moves;
    }

    public int getMoveSize() {
        return this.moves.size();
    }

    /**
     * The cost of the plan is the size of the move List.
     *
     * @return
     */
    public int getCost() {
        if (moves.isEmpty()) return 0;
        return moves.size();
    }

    public Map<Integer, Move> getMoves() {
        return this.moves;
    }

    public Character getAgentId() {
        return this.agent.getAgentId();
    }

    public Agent getAgent() {
        return this.agent;
    }

    public AbstractConflict firstConflict(SingleAgentPlan otherPlan) {
        //1. check every step, either vertex or edge conflict may happen
        int plan1EndTime = this.moves.size();
        int plan2EndTime = otherPlan.moves.size();
        int minEndTime = Math.min(plan1EndTime, plan2EndTime);
        int maxEndTime = Math.max(plan1EndTime, plan2EndTime);
        //move 的 time是从1开始的。表示它是走到了 time 时刻的位置
        for (int i = 1; i <= minEndTime; i++) {
            Move move1 = this.moves.get(i);
            Move move2 = otherPlan.moves.get(i);
            AbstractConflict conflict = AbstractConflict.conflictBetween(this, otherPlan, move1, move2);
            if (conflict != null) return conflict;
        }

        //2. check that whether one entity at the path of another when it is already at its goal
        if (plan1EndTime != plan2EndTime) {
            SingleAgentPlan earlyEndingPlan = (plan1EndTime > plan2EndTime ? otherPlan : this);
            SingleAgentPlan laterEndingPlan = (plan1EndTime > plan2EndTime ? this : otherPlan);

            List<Location> stayLocations = new ArrayList<>();
            if (earlyEndingPlan.agent.getGoalLocation() != null) {
                stayLocations.add(earlyEndingPlan.agent.getGoalLocation());
            }
            for (Box box : earlyEndingPlan.boxes) {
                if (box.getGoalLocation() != null) {
                    stayLocations.add(box.getGoalLocation());
                }
            }

            //如果plan2 先结束 - 则去检测plan1是否会经过2的goal - 只检测vertex Conflict即可
            //这里需要考虑所有的plan下所有的对象 agent+boxes - by stayLocations
            for (int time = minEndTime + 1; time <= maxEndTime; time++) {
                Move move2 = laterEndingPlan.getMoves().get(time);
                Location moveTo = move2.getMoveTo();
                if (stayLocations.contains(moveTo)) {
                    return new VertexConflict(earlyEndingPlan, laterEndingPlan, earlyEndingPlan.agent, laterEndingPlan.agent, time,
                            moveTo, move2.getCurrentLocation(), moveTo);
                }
            }
        }

        return null;
    }

    public List<Box> getBoxes() {
        return this.boxes;
    }

    public void addBox(Box box) {
        this.boxes.add(box);
    }

    @Override
    public String toString() {
        return "SingleAgentPlan{" +
                "agent=" + agent +
                ", boxes=" + boxes +
                ", moves=" + moves +
                ", env=" + env +
                '}';
    }
}
