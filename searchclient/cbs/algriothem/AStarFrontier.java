package searchclient.cbs.algriothem;

import searchclient.Frontier;
import searchclient.cbs.model.LowLevelState;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class AStarFrontier implements Frontier<LowLevelState> {

    private PriorityQueue<LowLevelState> queue;
    private Set<LowLevelState> visited = new HashSet<LowLevelState>();

    public AStarFrontier() {
        this.queue = new PriorityQueue<>(LowLevelState::compareTo);
    }

    @Override
    public void add(LowLevelState item) {
        this.queue.add(item);
        this.visited.add(item);
    }

    @Override
    public LowLevelState pop() {
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
    public boolean contains(LowLevelState item) {
        return this.visited.contains(item);
    }

    @Override
    public String getName() {
        return "A Star Frontier for CBS Low Level";
    }
}
