package searchclient.cbs.model;

import searchclient.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment {

    private final int gridNumRows;
    private final int gridNumCol;
    private final Map<Color, LowLevelColorGroup> colorGroups;
    private final Map<Character, List<Location>> boxType2GoalMap;
    private final boolean[][] WALLS;
    private int agentNums;

    public static Environment parseLevel(BufferedReader serverMessages) throws IOException {
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

        Map<Character, Agent> agents = new HashMap<>();
        Map<Color, LowLevelColorGroup> colorGroupMap = new HashMap<>();
        Map<Character, List<Location>> boxType2GoalMap = new HashMap<>();
        Map<Character, Color> boxLetter2Color = new HashMap<>();

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
                    agents.put(c, agent);
                } else if ('A' <= c && c <= 'Z') {
                    boxLetter2Color.put(c, color);
                }
            }
            colorGroupMap.put(colorGroup.getColor(), colorGroup);
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
        boolean[][] walls = new boolean[numRows][numCols];
        for (int row = 0; row < numRows; ++row) {
            line = levelLines.get(row);
            for (int col = 0; col < line.length(); ++col) {
                char c = line.charAt(col);

                Location initLoc = new Location(row, col);
                if ('0' <= c && c <= '9') {
                    Agent agent = agents.get(c);
                    agent.setInitLocation(initLoc);
                } else if ('A' <= c && c <= 'Z') {
                    Color color = boxLetter2Color.get(c);
                    Box box = new Box(c, color, initLoc);
                    colorGroupMap.get(color).addBox(box);
                } else if (c == '+') {
                    walls[row][col] = true;
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

                if ('0' <= c && c <= '9') {
                    Agent agent = agents.get(c);
                    agent.setGoalLocation(goalLoc);
                } else if ('A' <= c && c <= 'Z') {
                    boxType2GoalMap.computeIfAbsent(c, k -> new ArrayList<>()).add(goalLoc);
                }
            }
            ++row;
            line = serverMessages.readLine();
        }

        return new Environment(colorGroupMap, walls, numRows, numCols, boxType2GoalMap, agents.size());
    }

    public Environment(Map<Color, LowLevelColorGroup> colorGroups, boolean[][] walls,
                       int gridNumRows, int gridNumCol, Map<Character, List<Location>> boxType2GoalMap, int agentNums) {
        WALLS = walls;
        this.colorGroups = colorGroups;
        this.gridNumRows = gridNumRows;
        this.gridNumCol = gridNumCol;
        this.boxType2GoalMap = boxType2GoalMap;
        this.agentNums = agentNums;
    }

    public Environment() {
        gridNumRows = 0;
        gridNumCol = 0;
        colorGroups = new HashMap<>();
        boxType2GoalMap = new HashMap<>();
        WALLS = new boolean[0][0];
    }


    /**
     * @param location
     * @return return true if the location is a wall
     */
    public boolean isWall(Location location) {
        return WALLS[location.getRow()][location.getCol()];
    }

    public Map<Color, LowLevelColorGroup> getColorGroups() {
        return colorGroups;
    }

    public int getGridNumRows() {
        return gridNumRows;
    }

    public int getGridNumCol() {
        return gridNumCol;
    }

    @Override
    public String toString() {
        return "Environment{" +
                "gridNumRows=" + gridNumRows +
                ", gridNumCol=" + gridNumCol +
                ", colorGroups=" + colorGroups +
                ", boxType2GoalMap=" + boxType2GoalMap +
                ", WALLS=" + (WALLS == null ? null : WALLS.length) +
                ", agentNums=" + agentNums +
                '}';
    }

    public Map<Character, List<Location>> getBoxType2GoalMap() {
        return boxType2GoalMap;
    }

    public int getAgentNums() {
        return agentNums;
    }
}
