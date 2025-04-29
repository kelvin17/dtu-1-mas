package searchclient.cbs.model;

public class AssignmentOption {
    public Agent agent;
    public Box box;
    public int distance;

    public AssignmentOption(Agent agent, Box box, int distance) {
        this.agent = agent;
        this.box = box;
        this.distance = distance;
    }
}
