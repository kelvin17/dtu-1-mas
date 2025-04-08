package searchclient.cbs.algriothem;

import searchclient.cbs.model.Node;

import java.util.PriorityQueue;

public class OpenList {

    private PriorityQueue<Node> queue;

    public OpenList() {
        this.queue = new PriorityQueue<>();
    }

    public void add(Node n) {
        this.queue.add(n);
    }

    public Node pop() {
        return this.queue.remove();
    }

    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

}
