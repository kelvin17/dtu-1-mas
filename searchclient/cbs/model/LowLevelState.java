package searchclient.cbs.model;

import searchclient.Action;

import java.util.*;

public class LowLevelState implements Comparable<LowLevelState> {
    private Agent agent;
    private List<Box> boxes = new ArrayList<>();
    private Box[][] loc2Box;
    private Environment env;
    private LowLevelState parent;
    private int timeNow = 0;
    private Move move;

    //Extract the moves from the root to this state
    public List<Move> extractMoves() {
        List<Move> moves = new ArrayList<>();
        LowLevelState current = this;
        while (current != null) {
            if (current.move != null) {
                moves.add(current.move);
            }
            current = current.parent;
        }

        Collections.reverse(moves);
        return moves;
    }

    public LowLevelState(Agent agent, List<Box> boxes, Environment env) {
        this.agent = agent;
        this.boxes = boxes;
        this.env = env;
        this.loc2Box = new Box[env.getGridNumRows()][env.getGridNumCol()];
    }

    public LowLevelState initForChild() {
        if (!this.boxes.isEmpty()) {
            for (Box box : this.boxes) {
                this.loc2Box[box.getCurrentLocation().getRow()][box.getCurrentLocation().getCol()] = box;
            }
        }
        return this;
    }

    public LowLevelState init() {
        this.agent.setCurrentLocation(agent.getInitLocation());
        if (!this.boxes.isEmpty()) {
            for (Box box : this.boxes) {
                box.setCurrentLocation(box.getInitLocation());
                this.loc2Box[box.getCurrentLocation().getRow()][box.getCurrentLocation().getCol()] = box;
            }
        }
        return this;
    }

    public List<LowLevelState> expand(Node currentNode) {
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
        LowLevelState child = new LowLevelState(this.agent.copy(), newBoxes, this.env).initForChild();
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
                Location newAgentLoc2 = new Location(child.agent.getCurrentLocation().getRow() + move.getAction().agentRowDelta,
                        child.agent.getCurrentLocation().getCol() + move.getAction().agentColDelta);
                child.agent.setCurrentLocation(newAgentLoc2);

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
        boolean equal = true;

        if (this.agent != null) {
            if (!this.agent.equals(other.agent)) {
                return false;
            }
        } else {
            if (other.agent != null) {
                return false;
            }
        }

        if (!this.boxes.isEmpty()) {
            if (this.boxes.size() != other.boxes.size()) {
                return false;
            }

            for (Box box : this.boxes) {
                Box otherBox = other.boxAt(box.getCurrentLocation());
                if (otherBox == null || !otherBox.equals(box)) {
                    return false;
                }
            }
        } else {
            if (!other.boxes.isEmpty()) {
                return false;
            }
        }

        return equal;
    }

    private Box boxAt(Location location) {
        return this.loc2Box[location.getRow()][location.getCol()];
    }

    public int hashCode() {
        return Objects.hash(agent, boxes);
    }
}
