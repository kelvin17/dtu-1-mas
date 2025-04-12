package searchclient.cbs.algriothem;

import searchclient.Frontier;
import searchclient.cbs.model.SingleAgentPlan;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class AStarFrontier implements Frontier<SingleAgentPlan> {

    private PriorityQueue<SingleAgentPlan> queue;
    private Set<SingleAgentPlan> visited = new HashSet<SingleAgentPlan>();

    public AStarFrontier() {
        this.queue = new PriorityQueue<>(SingleAgentPlan::compareTo);
    }

    @Override
    public void add(SingleAgentPlan item) {
        this.queue.add(item);
        this.visited.add(item);
    }

    @Override
    public SingleAgentPlan pop() {
        return this.queue.poll();
    }

    @Override
    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    @Override
    public int size() {
        return this.queue.size();
    }

    @Override
    public boolean contains(SingleAgentPlan plan) {
        return this.visited.contains(plan);
    }

    @Override
    public String getName() {
        return "A Star Frontier for CBS Low Level";
    }
}
