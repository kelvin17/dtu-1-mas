package searchclient.cbs.model;

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
    private int timenow = 0;

    //todo 返回steps
    public List<Move> extractMoves(){
        return new ArrayList<>();
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

    public List<LowLevelState> expand(Node currentNode){
        return new ArrayList<>();
    }

    public LowLevelState generateChildState() {
        List<Box> newBoxes = new ArrayList<>();
        for (Box box : this.boxes) {
            newBoxes.add(box.copy());
        }
        LowLevelState child = new LowLevelState(this.agent.copy(), newBoxes, this.env);
        child.parent = this;
        child.timenow = this.timenow + 1;
        return child;
    }

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

    //todo 这个heuristic的计算
    public int getHeuristic() {
        int cost = 0;
        return cost;
    }

    @Override
    public int compareTo(LowLevelState o) {
        return Objects.compare(this, o, Comparator.comparing(LowLevelState::getHeuristic));
    }
}
