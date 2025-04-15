package searchclient.cbs.model;

import java.io.Serializable;
import java.util.*;

/**
 * Model for Single Agent Plan
 * Represents the plan of a single agent, including the env, agent, boxes and a effective move list for this plan.
 * 代表一个单独的代理的计划，包括环境、代理、箱子和使得该计划可解的一个有效移动列表。
 */
public class SingleAgentPlan implements AbstractDeepCopy<SingleAgentPlan>, Serializable {

    private Agent agent;
    private Map<String, Box> boxes = new HashMap<>();
    private Map<Integer, Move> moves = new TreeMap<>();
    private Environment env;

    private Location agentFinalLocation;
    private Map<String, Location> boxesFinalLocation = new HashMap<>();

    public SingleAgentPlan() {
    }

    /**
     * Update the plan to the final state
     * 1. get the moves from the final state
     * 2. set the agent's final location by the final state - used to check the conflict
     * 3. update the boxes' final location by the final state - used to check the conflict
     *
     * @param goalState
     */
    public void update2Final(LowLevelState goalState) {
        this.moves = goalState.extractMoves();

        this.agentFinalLocation = goalState.getAgent().getCurrentLocation();

        for (String uniqueId : this.boxes.keySet()) {
            Box goalBox = goalState.getBoxes().get(uniqueId);
            if (goalBox != null) {
                this.boxesFinalLocation.put(uniqueId, goalBox.getCurrentLocation().deepCopy());
            } else {
                throw new IllegalArgumentException("Box " + uniqueId + " not found in goal state");
            }

        }

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
        if (this.moves.isEmpty()) return 0;
        return this.moves.size();
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
        Box[][] tmpBoxLocations1 = new Box[env.getGridNumRows()][env.getGridNumCol()];
        Box[][] tmpBoxLocations2 = new Box[env.getGridNumRows()][env.getGridNumCol()];

        //初始化箱子的位置
        this.getBoxes().values().forEach(box -> tmpBoxLocations1[box.getInitLocation().getRow()][box.getInitLocation().getCol()] = box.deepCopy());
        otherPlan.getBoxes().values().forEach(box -> tmpBoxLocations2[box.getInitLocation().getRow()][box.getInitLocation().getCol()] = box.deepCopy());

        //move 的 time是从1开始的。表示它是走到了 time 时刻的位置
        for (int i = 1; i <= minEndTime; i++) {
            Move move1 = this.moves.get(i);
            Move move2 = otherPlan.moves.get(i);
            //1. simple check move
            AbstractConflict conflict = AbstractConflict.conflictBetween(this, otherPlan, move1, move2);
            if (conflict != null) return conflict;

            //2. check env conflict same as Vertex Conflic 这里箱子的位置是i-1时刻的。因为箱子无法自己移动。如果移动了，那么冲突在上面可以检测。这里主要检测的就是不动的箱子对于agent的影响
            //检查在plan2中箱子是否挡住move1了。所以用move1的目标位置去检查plan2的箱子
            Box checkForMove1 = tmpBoxLocations2[move1.getMoveTo().getRow()][move1.getMoveTo().getCol()];
            if (checkForMove1 != null) {
                return new VertexConflict(this, otherPlan, this.agent, otherPlan.agent, i, move1.getCurrentLocation(), move2.getCurrentLocation(), move1.getMoveTo(), true);
            }

            //检查在plan1中箱子是否挡住move2了。所以用move1的目标位置去检查plan2的箱子
            Box checkForMove2 = tmpBoxLocations1[move2.getMoveTo().getRow()][move2.getMoveTo().getCol()];
            if (checkForMove2 != null) {
                return new VertexConflict(otherPlan, this, otherPlan.agent, this.agent, i, move2.getCurrentLocation(), move1.getCurrentLocation(), move2.getMoveTo(), true);
            }

            //更新走完这一步，box的全局新位置
            if (move1.getBox() != null) {
                Box tmp = tmpBoxLocations1[move1.getBox().getCurrentLocation().getRow()][move1.getBox().getCurrentLocation().getCol()];
                if (!move1.getBox().originEqual(tmp)) {
                    throw new IllegalArgumentException("There must have the box :" + move1.getBox());
                }
                tmpBoxLocations1[tmp.getCurrentLocation().getRow()][tmp.getCurrentLocation().getCol()] = null;

                tmp.setCurrentLocation(move1.getBoxTargetLocation());
                tmpBoxLocations1[tmp.getCurrentLocation().getRow()][tmp.getCurrentLocation().getCol()] = tmp;
            }

            if (move2.getBox() != null) {
                Box tmp = tmpBoxLocations2[move2.getBox().getCurrentLocation().getRow()][move2.getBox().getCurrentLocation().getCol()];
                if (!move2.getBox().originEqual(tmp)) {
                    System.err.printf("There must have the box %s, but is %s\n", move2.getBox(), tmp);
                    throw new IllegalArgumentException("There must have the box");
                }

                tmpBoxLocations2[tmp.getCurrentLocation().getRow()][tmp.getCurrentLocation().getCol()] = null;
                tmp.setCurrentLocation(move2.getBoxTargetLocation());
                tmpBoxLocations2[tmp.getCurrentLocation().getRow()][tmp.getCurrentLocation().getCol()] = tmp;
            }
        }

        //2. check that whether one entity at the path of another when it is already at its goal
        if (plan1EndTime != plan2EndTime) {
            SingleAgentPlan earlyEndingPlan = (plan1EndTime > plan2EndTime ? otherPlan : this);
            SingleAgentPlan laterEndingPlan = (plan1EndTime > plan2EndTime ? this : otherPlan);

            List<Location> stayLocations = new ArrayList<>();
            stayLocations.add(earlyEndingPlan.getAgentFinalLocation());
            stayLocations.addAll(earlyEndingPlan.getBoxesFinalLocation().values());

            //如果plan2 先结束 - 则去检测plan1是否会经过2的goal - 只检测vertex Conflict即可
            //这里需要考虑所有的plan下所有的对象 agent+boxes - by stayLocations
            for (int time = minEndTime + 1; time <= maxEndTime; time++) {
                Move move2 = laterEndingPlan.getMoves().get(time);
                Location moveTo = move2.getMoveTo();
                if (stayLocations.contains(moveTo)) {
                    return new VertexConflict(earlyEndingPlan, laterEndingPlan, earlyEndingPlan.agent, laterEndingPlan.agent, time, moveTo, move2.getCurrentLocation(), moveTo, false);
                }
            }
        }

        return null;
    }

    public Map<String, Box> getBoxes() {
        return this.boxes;
    }

    public void addBox(Box box) {
        this.boxes.put(box.getUniqueId(), box);
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

    @Override
    public int hashCode() {
        return Objects.hash(agent, boxes, moves);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SingleAgentPlan that = (SingleAgentPlan) obj;
        return Objects.equals(agent, that.agent) &&
                Objects.equals(boxes, that.boxes) &&
                Objects.equals(moves, that.moves);
    }

    public Location getAgentFinalLocation() {
        return agentFinalLocation;
    }

    public Map<String, Location> getBoxesFinalLocation() {
        return boxesFinalLocation;
    }
}
