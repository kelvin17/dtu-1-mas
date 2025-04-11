package searchclient.cbs.algriothem;

import searchclient.cbs.model.Node;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class OpenList {

    private PriorityQueue<Node> queue;
    private Set<Node> visited = new HashSet<Node>();

    public OpenList() {
        this.queue = new PriorityQueue<>();
    }

    public void add(Node n) {
        this.queue.add(n);
        this.visited.add(n);
    }

    public Node pop() {
        return this.queue.remove();
    }

    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

}
