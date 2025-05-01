package searchclient.cbs.algriothem;

import searchclient.Frontier;
import searchclient.Memory;
import searchclient.State;
import searchclient.TimeoutException;
import searchclient.cbs.model.*;

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
     * @param metaAgentPlan
     * @return
     */
    public boolean findPath(Node currentNode, MetaAgentPlan metaAgentPlan, boolean allInOne) {

        boolean findPath = false;
        LowLevelState initState = LowLevelState.initRootStateForPlan(metaAgentPlan);
        initState.setAllInOne(allInOne);
        AStarFrontier frontier = new AStarFrontier();
        frontier.add(initState);

        while (!frontier.isEmpty() && !checkTimeout()) {
            LowLevelState currentState = frontier.pop();
            if (currentState.isGoal()) {
                metaAgentPlan.update2Final(currentState);
//                System.err.printf("#Finish-Lower-level, MetaId=%s , size=%d, VisitedSize=%,8d\n", metaAgentPlan.getMetaId(), metaAgentPlan.getCost(),
//                        frontier.getVisitedSize());
                findPath = true;
                break;
            }

//            if (frontier.getVisitedSize() % 10000 == 0) {
//                double elapsedTime = (System.currentTimeMillis() - this.startTime) / 1_000d;
//                System.err.printf("#Current-Lower-level, MetaId=%s , VisitedSize=%,4d, FrontierReminderSize=%,4d, time cost=%3.3f s, steps=%d\n",
//                        metaAgentPlan.getMetaId(), frontier.getVisitedSize(), frontier.size(), elapsedTime, currentState.timeNow);

//                if (currentState.timeNow > 58) {
//                    LowLevelState current = currentState;
//                    while (current != null) {
//                        System.err.printf("Current State, timeNow=%d\n", current.timeNow);
//                        for (Map.Entry<Character, Move> item : current.getAgentMove().entrySet()) {
//                            System.err.printf("Agent:[%s],Action: [%s],Box:[%s]\n", item.getValue().getAgent().getAgentId(), item.getValue().getAction().name, item.getValue().getBox() == null ? "" : item.getValue().getBox().getUniqueId());
//                        }
//                        current = current.getParent();
//                    }
//                }
//            }

            for (LowLevelState child : currentState.expand(currentNode)) {
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
            throw new TimeoutException("Low-level Multibody A* Timeout");
        }
        return false;
    }

    public boolean isAbortedForTimeout() {
        return abortedForTimeout;
    }
}
