package searchclient.cbs.algriothem;

import searchclient.cbs.model.LowLevelState;
import searchclient.cbs.model.Move;
import searchclient.cbs.model.Node;
import searchclient.cbs.model.SingleAgentPlan;

import java.util.Map;

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
    public Map<Integer, Move> findPath(Node currentNode, SingleAgentPlan singleAgentPlan) {

        Map<Integer, Move> result = null;

        LowLevelState initState = new LowLevelState(singleAgentPlan.getAgent(), singleAgentPlan.getBoxes(), singleAgentPlan.getEnv().getGridNumRows(),
                singleAgentPlan.getEnv().getGridNumCol()).init();
        AStarFrontier frontier = new AStarFrontier();
        frontier.add(initState);

        while (!frontier.isEmpty() && !checkTimeout()) {
            LowLevelState currentState = frontier.pop();
            if (currentState.isGoal()) {
                System.out.println("#Finish-Lower-level");
                result = currentState.extractMoves();
                break;
            }

            for (LowLevelState child : currentState.expand(currentNode, singleAgentPlan.getEnv())) {
                if (frontier.hasNotVisited(child)) {
                    frontier.add(child);
                }
            }
        }

        singleAgentPlan.setMoves(result);
        return result;
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
