package searchclient.cbs.model;

import searchclient.Action;

import java.io.Serializable;
import java.util.*;

public class LowLevelState implements Comparable<LowLevelState>, AbstractDeepCopy<LowLevelState>, Serializable {
    private Move move;
    private final Agent agent;
    private Map<String, Box> boxes = new HashMap<>();
    private final Box[][] loc2Box;
    private LowLevelState parent;
    private int timeNow = 0;
    private final int gridNumRows;
    private final int gridNumCol;

    public Agent getAgent() {
        return agent;
    }

    public Map<String, Box> getBoxes() {
        return boxes;
    }

    //Extract the moves from the root to this state
    public Map<Integer, Move> extractMoves() {
        Map<Integer, Move> moves = new TreeMap<>();
        LowLevelState current = this;
        while (current != null) {
            if (current.move != null) {
                moves.put(current.move.getTimeNow(), current.move);
            }
            current = current.parent;
        }

        return moves;
    }

    public LowLevelState(Agent agent, Map<String, Box> boxes, int gridNumRows, int gridNumCol) {
        this.agent = agent;
        this.gridNumRows = gridNumRows;
        this.gridNumCol = gridNumCol;
        this.loc2Box = new Box[gridNumRows][gridNumCol];
        if (boxes != null) {
            this.boxes = boxes;
        }
    }

    public static LowLevelState initRootStateForPlan(SingleAgentPlan singleAgentPlan) {
        LowLevelState rootState = new LowLevelState(singleAgentPlan.getAgent(), singleAgentPlan.getBoxes(), singleAgentPlan.getEnv().getGridNumRows(),
                singleAgentPlan.getEnv().getGridNumCol());
        rootState.agent.setCurrentLocation(rootState.agent.getInitLocation());
        if (!rootState.boxes.isEmpty()) {
            for (Box box : rootState.boxes.values()) {
                box.setCurrentLocation(box.getInitLocation());
                rootState.loc2Box[box.getCurrentLocation().getRow()][box.getCurrentLocation().getCol()] = box;
            }
        }
        return rootState;
    }

    public LowLevelState() {
        this.agent = null;
        this.gridNumRows = 0;
        this.gridNumCol = 0;
        this.loc2Box = new Box[0][0];
    }

    public List<LowLevelState> expand(Node currentNode, Environment env) {
        List<LowLevelState> newStates = new ArrayList<>();
        List<Constraint> constraints = new ArrayList<>();
        Node node = currentNode;
        while (node != null) {
            if (node.getAddedConstraint() != null && node.getAddedConstraint().getAgent().getAgentId() == this.agent.getAgentId()) {
                //this is the state of parent. Now we need to check if the constraint is added for its child
                if (this.timeNow + 1 == node.getAddedConstraint().getTime()) {
                    constraints.add(node.getAddedConstraint());
                }
            }
            node = node.getParent();
        }

        for (Action action : Action.values()) {
            Move move = this.getNextMove(this.agent, action, constraints, env, currentNode);
            if (move != null) {
                LowLevelState child = this.generateChildState(move);
                newStates.add(child);
            }
        }

        return newStates;
    }

    /**
     * @param agent
     * @param action
     * @return
     */
    private Move getNextMove(Agent agent, Action action, List<Constraint> constraints, Environment env, Node currentNode) {
        switch (action.type) {
            case NoOp:
                return new Move(agent, this.timeNow + 1, action, null);
            case Move:
                Location currentLocation = agent.getCurrentLocation();
                Location newLocation = new Location(currentLocation.getRow() + action.agentRowDelta,
                        currentLocation.getCol() + action.agentColDelta);
                //1. check是不是墙
                if (env.isWall(newLocation)) {
                    return null;
                }
                //2. check会不会有当前组内的box
                if (this.loc2Box[newLocation.getRow()][newLocation.getCol()] != null) {
                    return null;
                }
                //3. 不需要检查考虑一下这里要不要考虑其他的agent和box - 应该可以不考虑。等同于ignore处理 todo
                //4. 检查新加入的指定时点的约束
                for (Constraint constraint : constraints) {
                    if (constraint.getFromLocation() == null) {
                        //vertex conflict or follow conflict
                        if (constraint.getToLocation().equals(newLocation)) {
                            return null;
                        }
                    } else {
                        //edge conflict
                        if (constraint.getFromLocation().equals(currentLocation) && constraint.getToLocation().equals(newLocation)) {
                            return null;
                        }
                    }
                }
                return new Move(agent, this.timeNow + 1, action, null);
            case Push:
                Location targetBox = new Location(agent.getCurrentLocation().getRow() + action.agentRowDelta, agent.getCurrentLocation().getCol() + action.agentColDelta);
                Box box = this.boxAt(targetBox);
                if (box == null) {
                    return null;
                }

                Location newOccupiedLocation = new Location(targetBox.getRow() + action.boxRowDelta, targetBox.getCol() + action.boxColDelta);
                //1. check是不是墙
                if (env.isWall(newOccupiedLocation)) {
                    return null;
                }
                //2. check 会不会有当前组内的box
                if (this.loc2Box[newOccupiedLocation.getRow()][newOccupiedLocation.getCol()] != null) {
                    return null;
                }
                //3. check 是否在约束中
                for (Constraint constraint : constraints) {
                    if (constraint.getFromLocation() == null) {
                        //vertex conflict or follow conflict
                        if (constraint.getToLocation().equals(newOccupiedLocation)) {
                            return null;
                        }
                    } else {
                        //edge conflict -- todo 似乎不会走到这里- 需要考虑一下是用box（中间）。还是用agent（最边上）
                        if (constraint.getFromLocation().equals(targetBox) && constraint.getToLocation().equals(targetBox)) {
                            return null;
                        }
                    }
                }
                return new Move(agent, this.timeNow + 1, action, box);

            case Pull:
                Location targetBoxForPull = new Location(agent.getCurrentLocation().getRow() - action.boxRowDelta, agent.getCurrentLocation().getCol() - action.boxColDelta);
                Box boxForPull = this.boxAt(targetBoxForPull);
                if (boxForPull == null) {
                    return null;
                }
                Location newOccupiedLocationForPull = new Location(agent.getCurrentLocation().getRow() + action.agentRowDelta, agent.getCurrentLocation().getCol() + action.agentColDelta);
                //1. check是不是墙
                if (env.isWall(newOccupiedLocationForPull)) {
                    return null;
                }
                //2. check 会不会有当前组内的box
                if (this.loc2Box[newOccupiedLocationForPull.getRow()][newOccupiedLocationForPull.getCol()] != null) {
                    return null;
                }
                //3. check 是否在约束中
                for (Constraint constraint : constraints) {
                    if (constraint.getFromLocation() == null) {
                        //vertex conflict or follow conflict
                        if (constraint.getToLocation().equals(newOccupiedLocationForPull)) {
                            return null;
                        }
                    } else {
                        //edge conflict -- todo 似乎不会走到这里 - 需要考虑一下是用agent（中间）。还是用box（最边上）
                        if (constraint.getFromLocation().equals(agent.getCurrentLocation()) && constraint.getToLocation().equals(targetBoxForPull)) {
                            return null;
                        }
                    }
                }
                return new Move(agent, this.timeNow + 1, action, boxForPull);
            default:
                throw new IllegalStateException("Unexpected value: " + action.type);
        }
    }

    public LowLevelState generateChildState(Move move) {
        LowLevelState child = this.deepCopy();
        child.parent = this;
        child.timeNow = this.timeNow + 1;
        child.move = move;

        switch (move.getAction().type) {
            case NoOp:
                break;
            case Move:
                Location newAgentLoc = new Location(child.agent.getCurrentLocation().getRow() + move.getAction().agentRowDelta,
                        child.agent.getCurrentLocation().getCol() + move.getAction().agentColDelta);
                child.agent.setCurrentLocation(newAgentLoc);
                break;
            case Pull:
            case Push:
                Location newAgentLocForBox = new Location(child.agent.getCurrentLocation().getRow() + move.getAction().agentRowDelta,
                        child.agent.getCurrentLocation().getCol() + move.getAction().agentColDelta);
                child.agent.setCurrentLocation(newAgentLocForBox);

                Box box = move.getBox();
                Location newBoxLoc = new Location(box.getCurrentLocation().getRow() + move.getAction().boxRowDelta,
                        box.getCurrentLocation().getCol() + move.getAction().boxColDelta);
                child.updateBoxLocation(box.getCurrentLocation(), newBoxLoc);
                break;
            default:
                throw new IllegalStateException("generateChildState Unexpected value: " + move.getAction().type);
        }
        return child;
    }

    /**
     * update box location both in the box and loc2Box
     *
     * @param origin
     * @param newLocation
     */
    public void updateBoxLocation(Location origin, Location newLocation) {
        Box box = this.loc2Box[origin.getRow()][origin.getCol()];
        if (box != null) {
            box.setCurrentLocation(newLocation);
            this.loc2Box[newLocation.getRow()][newLocation.getCol()] = box;
            this.loc2Box[origin.getRow()][origin.getCol()] = null;
        } else {
            throw new IllegalArgumentException("No box found for location " + newLocation);
        }
    }

    /**
     * check if single agent group plan has a solution
     *
     * @return
     */
    public boolean isGoal() {
        if (this.agent.getGoalLocation() != null) {
            if (!this.agent.getCurrentLocation().equals(this.agent.getGoalLocation())) {
                return false;
            }
        }

        for (Box box : this.boxes.values()) {
            if (box.getGoalLocation() != null) {
                if (!box.getCurrentLocation().equals(box.getGoalLocation())) {
                    return false;
                }
            }
        }
        return true;
    }

    //manhattan distance heuristic
    public long getHeuristic() {
        int heuristicValue = 0;
        for (Box box : this.boxes.values()) {
            if (box.getGoalLocation() != null) {
                int mhtDis = Math.abs(box.getCurrentLocation().getRow() - box.getGoalLocation().getRow())
                        + Math.abs(box.getCurrentLocation().getCol() - box.getGoalLocation().getCol());
                heuristicValue += mhtDis;
            }
        }

        if (this.agent.getGoalLocation() != null) {
            int mhtDis = Math.abs(this.agent.getCurrentLocation().getRow() - this.agent.getGoalLocation().getRow())
                    + Math.abs(this.agent.getCurrentLocation().getCol() - this.agent.getGoalLocation().getCol());
            heuristicValue = Math.max(heuristicValue, mhtDis);
        }

        return heuristicValue;
    }

    // A* heuristic function
    public long getAStar() {
        return this.getHeuristic() + this.timeNow;
    }

    @Override
    public int compareTo(LowLevelState o) {
        return Objects.compare(this, o, Comparator.comparing(LowLevelState::getAStar));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        LowLevelState other = (LowLevelState) obj;

        boolean equals = Objects.equals(agent, other.agent);
        if (!equals) {
            return false;
        }

        equals = Objects.equals(this.move, other.move);
        if (!equals) {
            return false;
        }

        return Objects.deepEquals(this.boxes, other.boxes);
    }

    private Box boxAt(Location location) {
        return this.loc2Box[location.getRow()][location.getCol()];
    }

    public int hashCode() {
        return Objects.hash(agent, boxes, move);
    }

    @Override
    public String toString() {
        return "LowLevelState{" +
                "agent=" + agent +
                ", boxes=" + boxes +
                ", loc2Box=" + Arrays.deepToString(loc2Box) +
                ", gridNumRows=" + gridNumRows +
                ", gridNumCol=" + gridNumCol +
                ", parent=" + parent +
                ", timeNow=" + timeNow +
                ", move=" + move +
                '}';
    }
}
