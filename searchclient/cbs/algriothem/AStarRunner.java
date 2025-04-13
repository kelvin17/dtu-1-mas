package searchclient.cbs.algriothem;

import searchclient.cbs.model.LowLevelState;
import searchclient.cbs.model.Move;
import searchclient.cbs.model.Node;
import searchclient.cbs.model.SingleAgentPlan;

import java.util.Map;
import java.util.TreeMap;

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
    public boolean findPath(Node currentNode, SingleAgentPlan singleAgentPlan) {

        boolean findPath = false;
        LowLevelState initState = LowLevelState.initRootStateForPlan(singleAgentPlan);
        AStarFrontier frontier = new AStarFrontier();
        frontier.add(initState);

        Map<Integer, Move> result = new TreeMap<>();
        while (!frontier.isEmpty() && !checkTimeout()) {
            LowLevelState currentState = frontier.pop();
            if (currentState.isGoal()) {
                System.out.println("#Finish-Lower-level");
                result = currentState.extractMoves();
                findPath = true;
                break;
            }

            for (LowLevelState child : currentState.expand(currentNode, singleAgentPlan.getEnv())) {
                if (frontier.hasNotVisited(child)) {
                    frontier.add(child);
                }
            }
        }

        singleAgentPlan.setMoves(result);
        return findPath;
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
