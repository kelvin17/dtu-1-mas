package searchclient.cbs.algriothem;

import searchclient.Action;
import searchclient.State;
import searchclient.cbs.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CBSRunner {

    private final long DEFAULT_TIMEOUT = 3 * 60 * 1000;
    private boolean abortedForTimeout = false;
    private long startTime;

    //this class can be improved by different methods
    private final AStarRunner lowLevelRunner;
    private final MinTimeConflictDetection conflictDetection;

    private List<SingleAgentPlan> singleAgentPlanList = new ArrayList<>();

    public CBSRunner() {
        this.startTime = System.currentTimeMillis();
        this.lowLevelRunner = new AStarRunner(startTime, DEFAULT_TIMEOUT);
        this.conflictDetection = new MinTimeConflictDetection();
    }

    /**
     * High level of CBS
     *
     * @param initEnv
     * @return
     */
    public Action[][] findSolution(Environment initEnv) {
        Node rootNode = initRoot(initEnv);
        if (!rootNode.getSolution().isValid()) {
            return null;
        }

        OpenList openList = new OpenList();
        openList.add(rootNode);

        while (!openList.isEmpty() && !checkTimeout()) {
            Node node = openList.pop();
            AbstractConflict firstConflict = conflictDetection.detect(node);
            //find the final solution, when there isn't any conflict
            if (firstConflict == null) {
//                System.err.println("Conflict detected result - No conflict");
                return convertPaths2Actions(node.getSolution());
            }
//            System.err.println("Conflict detected result - " + firstConflict.toString());

            Constraint[] constraints = firstConflict.getPreventingConstraints();

            for (Constraint constraint : constraints) {
                Node child = buildChild(node, firstConflict, constraint);
                if (child.getSolution().isValid()) {
                    openList.add(child);
                }
            }
        }

        return null;
    }

    private Node buildChild(Node parentCTNode, AbstractConflict firstConflict, Constraint constraint) {
        Node childCTNode = new Node(parentCTNode, firstConflict, constraint);

        Solution childSolution = parentCTNode.getSolution().deepCopy();
        Agent agent = constraint.getAgent();
        SingleAgentPlan currentAgentPlan = childSolution.getPlanForAgent(agent.getAgentId());
        boolean findNewPath = lowLevelRunner.findPath(childCTNode, currentAgentPlan);

        childSolution.setValid(findNewPath);
        if (findNewPath) {
            childSolution.updateMaxSinglePath();
        }

        childCTNode.setSolution(childSolution);
        return childCTNode;
    }


    /**
     * Convert path of every agent into Action[][]
     *
     * @param solution
     * @return
     */
    private Action[][] convertPaths2Actions(Solution solution) {
        List<SingleAgentPlan> agentPathList = solution.getAgentPlansInOrder();
        //action 2-D array; 1st is the max steps of all agents; 2nd is the number of agents
        Action[][] actions = new Action[solution.getMaxSinglePath()][agentPathList.size()];
        for (int i = 0; i < solution.getMaxSinglePath(); i++) {
            for (int j = 0; j < agentPathList.size(); j++) {
                List<Move> moves = new ArrayList<>(agentPathList.get(j).getMoves().values());
                //as i the max steps of all agents, so some agent may not have this step
                //todo 如果一个agent到达后，又需要出来，在内部处理掉。从而让moves list的总大小表达agent最终的cost
                actions[i][j] = (moves.size() > i ? moves.get(i).getAction() : Action.NoOp);
            }
        }
        return actions;
    }

    private boolean checkTimeout() {
        if (System.currentTimeMillis() - startTime > DEFAULT_TIMEOUT) {
            this.abortedForTimeout = true;
            return true;
        }
        return false;
    }

    private Node initRoot(Environment environment) {
        //static group boxes to agent todo 还需要考虑一下agent没有goal的特殊情况
        for (LowLevelColorGroup colorGroup : environment.getColorGroups()) {
            int agentCounts = colorGroup.getAgents().size();
            int boxCounts = colorGroup.getBoxes().size();
            if (agentCounts == 1) {
                SingleAgentPlan singleAgentPlan = new SingleAgentPlan(colorGroup.getAgents().get(0), colorGroup.getBoxes(), environment);
                singleAgentPlanList.add(singleAgentPlan);
            } else {
                for (int i = 0; i < agentCounts; i++) {
                    SingleAgentPlan singleAgentPlan = new SingleAgentPlan(colorGroup.getAgents().get(i), environment);
                    for (int j = i; j < boxCounts; j = j + agentCounts) {
                        singleAgentPlan.addBox(colorGroup.getBoxes().get(j));
                    }
                    singleAgentPlanList.add(singleAgentPlan);
                }
            }
        }

        Node rootNode = new Node(null);

        Solution solution = new Solution();
        for (SingleAgentPlan singleAgentPlan : singleAgentPlanList) {
            boolean findPath = lowLevelRunner.findPath(rootNode, singleAgentPlan);
            if (!findPath) {
                //it will be invalid if anyone agent cannot find a path
                solution.setValid(false);
                break;
            }
            solution.addAgentPlan(singleAgentPlan.getAgentId(), singleAgentPlan);
        }
        rootNode.setSolution(solution);

        return rootNode;

    }

    private Location getGoalLocationForAgent(int agentId) {
        for (int row = 1; row < State.goals.length - 1; row++) {
            for (int col = 1; col < State.goals[row].length - 1; col++) {
                char goal = State.goals[row][col];

                if ('0' <= goal && goal <= '9') {
                    if (agentId == Integer.valueOf(goal)) {
                        new Location(row, col);
                    }
                }
            }
        }

        return null;
    }

    public boolean isAbortedForTimeout() {
        return abortedForTimeout;
    }
}
