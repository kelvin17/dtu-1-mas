package searchclient.cbs.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class LowLevelState implements Comparable<LowLevelState> {
    private Agent agent;
    private List<Box> boxes = new ArrayList<>();
    private Box[][] loc2Box;

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
