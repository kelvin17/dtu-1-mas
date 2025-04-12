package searchclient.cbs.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Base Model and For CBS
 * For low level of an agent
 */
public class SingleAgentPlan implements Comparable<SingleAgentPlan> {

    private final Agent agent;
    private List<Box> boxes = new ArrayList<>();
    private List<Move> moves = new ArrayList<>();
    private Box[][] loc2Box;
    private Environment env;

    public void updateBoxLocation(Location origin, Location newLocation) {
        Box box = this.loc2Box[origin.getRow()][origin.getCol()];
        if (box != null) {
            box.setCurrentLocation(newLocation);
            loc2Box[newLocation.getRow()][newLocation.getCol()] = box;
            loc2Box[origin.getRow()][origin.getCol()] = null;
        } else {
            throw new IllegalArgumentException("No box found for location " + newLocation);
        }
    }

    public SingleAgentPlan(Agent agent, List<Box> boxes, Environment env) {
        this.agent = agent;
        this.boxes = boxes;
        this.env = env;
        this.loc2Box = new Box[env.getGridNumRows()][env.getGridNumCol()];
        for (Box box : boxes) {
            Location location = box.getCurrentLocation();
            loc2Box[location.getRow()][location.getCol()] = box;
        }
    }

    public SingleAgentPlan(Agent agent, Environment env) {
        this.agent = agent;
        this.env = env;
        this.loc2Box = new Box[env.getGridNumRows()][env.getGridNumCol()];
    }

    public void addMove(Move move) {
        this.moves.add(move);
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

    public List<Move> getMoves() {
        return this.moves;
    }

    public int getAgentId() {
        return this.agent.getAgentId();
    }

    public SingleAgentPlan copy() {
        SingleAgentPlan copy = new SingleAgentPlan(this.agent.copy(), env);
        for (Move move : moves) {
            copy.addMove(move.copy());
        }
        return copy;
    }

    public AbstractConflict firstConflict(SingleAgentPlan otherPlan) {
        //1. check every step, either vertex or edge conflict may happen
        int plan1EndTime = this.moves.size();
        int plan2EndTime = otherPlan.moves.size();
        int minEndTime = Math.min(plan1EndTime, plan2EndTime);
        int maxEndTime = Math.max(plan1EndTime, plan2EndTime);
        for (int i = 0; i < minEndTime; i++) {
            Move move1 = moves.get(i);
            Move move2 = otherPlan.moves.get(i);
            AbstractConflict conflict = AbstractConflict.conflictBetween(move1, move2);
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
                stayLocations.add(box.getGoalLocation());
            }

            //如果plan2 先结束 - 则去检测plan1是否会经过2的goal
            for (int time = minEndTime; time < maxEndTime; time++) {
                Move move2 = laterEndingPlan.getMoves().get(time);
                Location moveTo = move2.getMoveTo();
                if (stayLocations.contains(moveTo)) {
                    return new VertexConflict(earlyEndingPlan.agent, laterEndingPlan.agent, time,
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
        Location location = box.getCurrentLocation();
        this.loc2Box[location.getRow()][location.getCol()] = box;
    }

    //todo 这个heuristic的计算
    public int getHeuristic() {
        int cost = 0;
        return cost;
    }

    @Override
    public int compareTo(SingleAgentPlan o) {
        return Objects.compare(this, o, Comparator.comparing(SingleAgentPlan::getHeuristic));
    }
}
