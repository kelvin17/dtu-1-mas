package searchclient.cbs.model;

import java.io.BufferedReader;
import java.util.List;

public class LowLevelState {

    private List<LowLevelColorGroup> colorGroups;
    private static List<Location> WALL_LOCATIONS;
    private static List<LowLevelGoalLocation> GOAL_LOCATIONS;

    private LowLevelState parent;

    public static LowLevelState parseLevel(BufferedReader levelInfo) {

    }

    public LowLevelState(List<LowLevelColorGroup> colorGroups, List<Location> wallLocations, List<Location> boxLocations, List<LowLevelGoalLocation> goalLocations) {
        WALL_LOCATIONS = wallLocations;
        GOAL_LOCATIONS = goalLocations;
        this.colorGroups = colorGroups;
    }

    public LowLevelState getParent() {
        return parent;
    }

    public void setParent(LowLevelState parent) {
        this.parent = parent;
    }

    public List<LowLevelColorGroup> getColorGroups() {
        return colorGroups;
    }
}
