package searchclient.cbs.algriothem;

import searchclient.Action;
import searchclient.State;
import searchclient.cbs.model.*;

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

    private Map<Integer, SingleAgentPlan> agentId2Path = new HashMap<>();

    public CBSRunner() {
        this.lowLevelRunner = new AStarRunner();
        this.conflictDetection = new MinTimeConflictDetection();
    }

    /**
     * High level of CBS
     *
     * @param initialState
     * @return
     */
    public Action[][] findSolution(LowLevelState initialState) {
        this.startTime = System.currentTimeMillis();
        Node rootNode = initRoot(initialState);
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
                return convertPaths2Actions(node.getSolution());
            }

            Constraint[] constraints = firstConflict.getPreventingConstraints();

            Node leftChild = buildChild(node, firstConflict, constraints[0]);
            Node rightChild = buildChild(node, firstConflict, constraints[1]);

            node.setLeftChild(leftChild);
            node.setRightChild(rightChild);

            //if child has no new path, the solution is invalid
            if (leftChild.getSolution().isValid()) {
                openList.add(leftChild);
            }

            if (rightChild.getSolution().isValid()) {
                openList.add(rightChild);
            }
        }

        return null;
    }

    private Node buildChild(Node parent, AbstractConflict firstConflict, Constraint constraint) {
        Node child = new Node(parent, firstConflict, constraint);
        Solution childSolution = parent.getSolution().copy();
        Agent agent = constraint.getAgent();
        SingleAgentPlan singleAgentPlan = agentId2Path.get(agent.getAgentId());
        List<Move> newMoves = lowLevelRunner.findPath(child, singleAgentPlan);

        childSolution.setValid((newMoves != null));
        childSolution.addOrUpdateAgentPlan(agent.getAgentId(), singleAgentPlan);
        child.setSolution(childSolution);
        return child;
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
                List<Move> moves = agentPathList.get(j).getMoves();
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

    private Node initRoot(LowLevelState initialState) {
        //static group boxes to agent todo 还需要考虑一下agent没有goal的特殊情况
        for (LowLevelColorGroup colorGroup : initialState.getColorGroups()) {
            int agentCounts = colorGroup.getAgents().size();
            int boxCounts = colorGroup.getBoxes().size();
            if (agentCounts == 1) {
                Agent agent = colorGroup.getAgents().get(0);
                SingleAgentPlan singleAgentPlan = new SingleAgentPlan(agent, colorGroup.getBoxes());
                agentId2Path.put(agent.getAgentId(), singleAgentPlan);
            } else {
                for (int i = 0; i < agentCounts; i++) {
                    Agent agent = colorGroup.getAgents().get(i);
                    SingleAgentPlan singleAgentPlan = new SingleAgentPlan(agent);
                    for (int j = i; j < boxCounts; j = j + agentCounts) {
                        singleAgentPlan.addBox(colorGroup.getBoxes().get(j));
                    }
                    agentId2Path.put(agent.getAgentId(), singleAgentPlan);
                }
            }
        }

        Node rootNode = new Node(null);

        Solution solution = new Solution();
        for (SingleAgentPlan singleAgentPlan : agentId2Path.values()) {
            List<Move> newPath = lowLevelRunner.findPath(rootNode, singleAgentPlan);
            if (newPath == null || newPath.isEmpty()) {
                solution.setValid(false);
                break;
            }
            solution.addOrUpdateAgentPlan(singleAgentPlan.getAgentId(), singleAgentPlan);
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
