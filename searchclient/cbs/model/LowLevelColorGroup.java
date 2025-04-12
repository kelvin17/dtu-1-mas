package searchclient.cbs.model;

import searchclient.Color;

import java.util.ArrayList;
import java.util.List;

public class LowLevelColorGroup {
    private final Color color;
    private final List<MovableObj> agents;
    private final List<MovableObj> boxes;

    public LowLevelColorGroup(Color color) {
        this.color = color;
        this.agents = new ArrayList<>();
        this.boxes = new ArrayList<>();
    }

    public void addAgent(MovableObj agent) {
        this.agents.add(agent);
    }

    public void addBox(MovableObj box) {
        this.boxes.add(box);
    }

    public Color getColor() {
        return color;
    }

    public List<MovableObj> getAgents() {
        return agents;
    }

    public List<MovableObj> getBoxes() {
        return boxes;
    }
}
