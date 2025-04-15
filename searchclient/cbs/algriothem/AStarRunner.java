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

        while (!frontier.isEmpty() && !checkTimeout()) {
            LowLevelState currentState = frontier.pop();
            if (currentState.isGoal()) {
                singleAgentPlan.update2Final(currentState);
                System.err.printf("#Finish-Lower-level, agent=%s , size=%d\n", singleAgentPlan.getAgent().getAgentId(), singleAgentPlan.getCost());
                findPath = true;
                break;
            }

            for (LowLevelState child : currentState.expand(currentNode, singleAgentPlan.getEnv())) {
                if (frontier.hasNotVisited(child)) {
                    frontier.add(child);
                }
            }
        }
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
