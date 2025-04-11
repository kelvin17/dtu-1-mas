package searchclient.cbs.model;

import searchclient.Color;

import java.util.ArrayList;
import java.util.List;

public class LowLevelColorGroup {
    private final Color color;
    private final List<Agent> agents;
    private final List<Box> boxes;

    public LowLevelColorGroup(Color color) {
        this.color = color;
        this.agents = new ArrayList<>();
        this.boxes = new ArrayList<>();
    }

    public Color getColor() {
        return color;
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public List<Box> getBoxes() {
        return boxes;
    }
}
