package searchclient.cbs.model;

import searchclient.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LowLevelState {

    private List<LowLevelColorGroup> colorGroups;
    private static List<Location> WALL_LOCATIONS;
    private static List<LowLevelGoalLocation> GOAL_LOCATIONS;

    private LowLevelState parent;

    public static LowLevelState parseLevel(BufferedReader serverMessages) throws IOException {
        // We can assume that the level file is conforming to specification, since the server verifies this.
        // Read domain
        serverMessages.readLine(); // #domain
        serverMessages.readLine(); // hospital

        // Read Level name
        serverMessages.readLine(); // #levelname
        serverMessages.readLine(); // <name>

        // Read colors
        serverMessages.readLine(); // #colors
        String line = serverMessages.readLine();

        Map<Integer, Agent> agents = new HashMap<>();
        Map<Character, Box> boxes = new HashMap<>();
        List<Location> wallLocations = new ArrayList<>();
        List<LowLevelGoalLocation> goalLocations = new ArrayList<>();
        List<LowLevelColorGroup> colorGroups = new ArrayList<>();

        while (!line.startsWith("#")) {
            String[] split = line.split(":");
            Color color = Color.fromString(split[0].strip());
            LowLevelColorGroup colorGroup = new LowLevelColorGroup(color);
            String[] entities = split[1].split(",");
            for (String entity : entities) {
                char c = entity.strip().charAt(0);
                if ('0' <= c && c <= '9') {
                    Agent agent = new Agent(c, color);
                    colorGroup.addAgent(agent);
                    agents.put((int) c, agent);
                } else if ('A' <= c && c <= 'Z') {
                    Box box = new Box(c, color);
                    colorGroup.addBox(box);
                    boxes.put(c, box);
                }
            }
            colorGroups.add(colorGroup);
            line = serverMessages.readLine();
        }

        // Read initial state
        // line is currently "#initial"
        int numRows = 0;
        int numCols = 0;
        ArrayList<String> levelLines = new ArrayList<>(64);
        line = serverMessages.readLine();
        while (!line.startsWith("#")) {
            levelLines.add(line);
            numCols = Math.max(numCols, line.length());
            ++numRows;
            line = serverMessages.readLine();
        }
        for (int row = 0; row < numRows; ++row) {
            line = levelLines.get(row);
            for (int col = 0; col < line.length(); ++col) {
                char c = line.charAt(col);

                Location initLoc = new Location(row, col);
                if ('0' <= c && c <= '9') {
                    Agent agent = agents.get((int) c);
                    agent.setInitialLocation(initLoc);
                } else if ('A' <= c && c <= 'Z') {
                    Box box = boxes.get(c);
                    box.setInitLocation(initLoc);
                } else if (c == '+') {
                    wallLocations.add(initLoc);
                }
            }
        }

        // Read goal state
        // line is currently "#goal"
        line = serverMessages.readLine();
        int row = 0;
        while (!line.startsWith("#")) {
            for (int col = 0; col < line.length(); ++col) {
                char c = line.charAt(col);
                Location goalLoc = new Location(row, col);
                goalLocations.add(new LowLevelGoalLocation(c, goalLoc));

                if ('0' <= c && c <= '9') {
                    Agent agent = agents.get((int) c);
                    agent.setGoalLocation(goalLoc);
                } else if ('A' <= c && c <= 'Z') {
                    Box box = boxes.get(c);
                    box.setGoalLocation(goalLoc);
                }
            }
            ++row;
            line = serverMessages.readLine();
        }

        return new LowLevelState(colorGroups, wallLocations, goalLocations);
    }

    public LowLevelState(List<LowLevelColorGroup> colorGroups, List<Location> wallLocations, List<LowLevelGoalLocation> goalLocations) {
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
