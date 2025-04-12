package searchclient.cbs.algriothem;

import searchclient.cbs.model.LowLevelState;
import searchclient.cbs.model.Move;
import searchclient.cbs.model.Node;
import searchclient.cbs.model.SingleAgentPlan;

import java.util.List;

public class AStarRunner {

    private final long startTime;
    private final long timeoutLimit;
    private boolean abortedForTimeout = false;

    public AStarRunner(long startTime, long timeout) {
        this.startTime = startTime;
        this.timeoutLimit = timeout;
    }

    /**
     * Low-level
     * find path for single agent
     *
     * @param currentNode
     * @param singleAgentPlan
     * @return
     */
    public List<Move> findPath(Node currentNode, SingleAgentPlan singleAgentPlan) {

        List<Move> result = null;

        LowLevelState init = new LowLevelState(singleAgentPlan.getAgent(), singleAgentPlan.getBoxes(), singleAgentPlan.getEnv());
        AStarFrontier frontier = new AStarFrontier();
        frontier.add(init);

        while (!frontier.isEmpty() && !checkTimeout()) {
            LowLevelState currentState = frontier.pop();
            if (currentState.isGoal() && checkConstraints(currentState, currentNode)) {
                System.out.println("#Finish-Lower-level");
                result = currentState.extractMoves();
                break;
            }

            for (LowLevelState child : currentState.expand(currentNode)) {
                if (frontier.hasNotVisited(child)) {
                    frontier.add(child);
                }
            }
        }

        return result;
    }

    private boolean checkConstraints(LowLevelState currentState, Node currentNode) {
        //todo 限制写在expand可能就够了。这里可能不需要
        return true;
    }

    private boolean checkTimeout() {
        if (System.currentTimeMillis() - this.startTime > this.timeoutLimit) {
            this.abortedForTimeout = true;
            return true;
        }
        return false;
    }

    public boolean isAbortedForTimeout() {
        return abortedForTimeout;
    }
}
