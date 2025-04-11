package searchclient.cbs.algriothem;

import searchclient.cbs.model.Move;
import searchclient.cbs.model.Node;
import searchclient.cbs.model.SingleAgentPlan;

import java.util.ArrayList;
import java.util.List;

public class AStarRunner {

    /**
     * Low-level
     * find path for single agent
     *
     * @param currentNode
     * @param singleAgentPlan
     * @return
     */
    public List<Move> findPath(Node currentNode, SingleAgentPlan singleAgentPlan) {
        List<Move> moves = new ArrayList<>();
        //如果无解，返回null
        return moves;
    }
}
