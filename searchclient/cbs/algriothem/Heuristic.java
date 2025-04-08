package searchclient.cbs.algriothem;

import searchclient.cbs.model.Node;

public interface Heuristic {
    public int costFunction(Node node);
}
