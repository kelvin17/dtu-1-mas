package searchclient.cbs.model;

import searchclient.Action;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class LowLevelState implements Comparable<LowLevelState> {
    private Agent agent;
    private List<Box> boxes = new ArrayList<>();
    private Box[][] loc2Box;
    private Environment env;
    private LowLevelState parent;
    private int timeNow = 0;
    private Move move;

    //todo 返回steps - 从自己到根节点的路径
    public List<Move> extractMoves() {
        List<Move> moves = new ArrayList<>();
        LowLevelState current = this;
        while (current != null) {
            if (current.move != null) {
                moves.add(current.move);
            }
            current = current.parent;
        }
        return moves;
    }

    public LowLevelState(Agent agent, List<Box> boxes, Environment env) {
        this.agent = agent;
        this.boxes = boxes;
        this.env = env;
        this.loc2Box = new Box[env.getGridNumRows()][env.getGridNumCol()];
        for (Box box : boxes) {
            loc2Box[box.getCurrentLocation().getRow()][box.getCurrentLocation().getCol()] = box;
        }
    }

    public List<LowLevelState> expand(Node currentNode) {
//        this.agent
        List<LowLevelState> newState = new ArrayList<>();
        List<Constraint> constraints = new ArrayList<>();
        Node node = currentNode;
        while (node != null) {
            if (node.getAddedConstraint() != null && node.getAddedConstraint().getAgent() == this.agent) {
                if (this.timeNow == node.getAddedConstraint().getTime()) {
                    constraints.add(node.getAddedConstraint());
                }
            }
            node = node.getParent();
        }

        for (Action action : Action.values()) {
            Move move = this.getMove(this.agent, action, constraints);
            if (move != null) {
                LowLevelState child = this.generateChildState(move);
                newState.add(child);
            }
        }

        return newState;
    }

    /**
     * @param agent
     * @param action
     * @return
     */
    private Move getMove(Agent agent, Action action, List<Constraint> constraints) {
        switch (action.type) {
            case NoOp:
                return new Move(agent, this.timeNow + 1, action, null);
            case Move:
                Location currentLocation = agent.getCurrentLocation();
                Location newLocation = new Location(currentLocation.getRow() + action.agentRowDelta,
                        currentLocation.getCol() + action.agentColDelta);
                if (this.env.isWall(newLocation)) {
                    return null;
                }
                //todo 考虑一下这里要不要考虑其他的agent和box
                if (this.loc2Box[newLocation.getRow()][newLocation.getCol()] != null) {
                    return null;
                }
                for (Constraint constraint : constraints) {
                    if (constraint.getFromLocation() == null) {
                        //vertex conflict
                        if (constraint.getToLocation().equals(newLocation)) {
                            return null;
                        }
                    } else {
                        //edge conflict
                        if (constraint.getFromLocation().equals(newLocation) && constraint.getToLocation().equals(agent.getCurrentLocation())) {
                            return null;
                        }
                    }
                }
                return new Move(agent, this.timeNow + 1, action, null);
            case Push:
                return null;
            case Pull:
                return null;
            default:
                throw new IllegalStateException("Unexpected value: " + action.type);
        }
    }

    public LowLevelState generateChildState(Move move) {
        List<Box> newBoxes = new ArrayList<>();
        for (Box box : this.boxes) {
            newBoxes.add(box.copy());
        }
        LowLevelState child = new LowLevelState(this.agent.copy(), newBoxes, this.env);
        child.parent = this;
        child.timeNow = this.timeNow + 1;
        child.move = move;

        switch (move.getAction().type) {
            case NoOp:
            case Move:
                break;
            case Pull:
            case Push:
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
            loc2Box[newLocation.getRow()][newLocation.getCol()] = box;
            loc2Box[origin.getRow()][origin.getCol()] = null;
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

        for (Box box : this.boxes) {
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
        for (Box box : this.boxes) {
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
}
