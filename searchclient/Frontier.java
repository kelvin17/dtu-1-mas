package searchclient;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

public interface Frontier {
    void add(State state);

    State pop();

    boolean isEmpty();

    int size();

    boolean contains(State state);

    String getName();
}

class FrontierBFS
        implements Frontier {
    private final ArrayDeque<State> queue = new ArrayDeque<>(65536);
    private final HashSet<State> set = new HashSet<>(65536);

    @Override
    public void add(State state) {
        this.queue.addLast(state);
        this.set.add(state);
    }

    @Override
    public State pop() {
        State state = this.queue.pollFirst();
        this.set.remove(state);
        return state;
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
    public boolean contains(State state) {
        return this.set.contains(state);
    }

    @Override
    public String getName() {
        return "breadth-first search";
    }
}

class FrontierDFS
        implements Frontier {
    private final ArrayDeque<State> queue = new ArrayDeque<>(65536);
    private final HashSet<State> set = new HashSet<>(65536);

    @Override
    public void add(State state) {
        this.queue.add(state);
        this.set.add(state);
    }

    @Override
    public State pop() {
        State state = this.queue.pollLast();
        this.set.remove(state);
        return state;
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
    public boolean contains(State state) {
        return this.set.contains(state);
    }

    @Override
    public String getName() {
        return "depth-first search";
    }
}

class FrontierBestFirst
        implements Frontier {

    private Heuristic heuristic;
    private PriorityQueue<State> queue;
    private HashSet<State> visited;

    public FrontierBestFirst(Heuristic h) {
        this.heuristic = h;
        this.queue = new PriorityQueue<>(Comparator.comparingDouble(h::f));
        this.visited = new HashSet<>();
    }

    @Override
    public void add(State state) {
        if (!visited.contains(state)) { // 仅添加未访问状态
            queue.add(state);
            visited.add(state);
        }
    }

    @Override
    public State pop() {
        State s = queue.poll();
        this.visited.remove(s);
        return s;
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean contains(State state) {
        return visited.contains(state);
    }

    @Override
    public String getName() {
        return String.format("best-first search using %s", this.heuristic.toString());
    }
}
