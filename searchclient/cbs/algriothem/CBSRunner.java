package searchclient.cbs.algriothem;

import searchclient.Action;
import searchclient.State;
import searchclient.cbs.model.*;

import java.util.ArrayList;
import java.util.List;

public class CBSRunner {

    private final long DEFAULT_TIMEOUT = 3 * 60 * 1000;
    private boolean abortedForTimeout = false;
    private long startTime;

    private final AStarRunner lowLevelRunner;

    public CBSRunner(AStarRunner lowLevelRunner) {
        this.lowLevelRunner = lowLevelRunner;
    }

    /**
     * High level of CBS
     *
     * @param initialState
     * @return
     */
    public Action[][] findSolution(State initialState) {
        this.startTime = System.currentTimeMillis();
        Node rootNode = initRoot(initialState);
        if (!rootNode.getSolution().isValid()) {
            return null;
        }

        OpenList openList = new OpenList();
        openList.add(rootNode);

        while (!openList.isEmpty() && !checkTimeout()) {
            Node node = openList.pop();
            AbstractConflict firstConflict = vaildiate(node.getSolution());
            if (firstConflict == null) {
                //find the solution
                return buildPaths2Actions(node.getSolution());
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
        SingleAgentPath newPath = lowLevelRunner.findPath(child, agent);

        childSolution.setValid((newPath != null));
        childSolution.addOrUpdateAgentPlan(agent.getAgentId(), newPath);
        child.setSolution(childSolution);
        return child;
    }


    /**
     * Convert path of every agent into Action[][]
     *
     * @param solution
     * @return
     */
    private Action[][] buildPaths2Actions(Solution solution) {
        return new Action[][]{};
    }

    /**
     * To check there is any conflict in the node
     *
     * @param solution
     * @return
     */
    private AbstractConflict vaildiate(Solution solution) {
        return new AbstractConflict() {
            @Override
            public String getConflictType() {
                return "";
            }

            @Override
            public Constraint[] getPreventingConstraints() {
                return new Constraint[0];
            }
        };
    }

    private boolean checkTimeout() {
        if (System.currentTimeMillis() - startTime > DEFAULT_TIMEOUT) {
            this.abortedForTimeout = true;
            return true;
        }
        return false;
    }

    private Node initRoot(State initialState) {
        List<Agent> agents = new ArrayList<>();
        for (int agentId = 0; agentId < initialState.agentRows.length; agentId++) {
            int agentRow = initialState.agentRows[agentId];
            int agentCol = initialState.agentCols[agentId];
            Location initLocation = new Location(agentRow, agentCol);
            Location targetLocation = getGoalLocationForAgent(agentId);
            Agent agent = new Agent(agentId, initLocation, targetLocation);

            agent.setCurrentLocation(new Location(agentRow, agentCol));
            agents.add(agent);
        }

        Node rootNode = new Node(null);

        Solution solution = new Solution();
        for (Agent agent : agents) {
            SingleAgentPath singleAgentPath = lowLevelRunner.findPath(rootNode, agent);
            if (singleAgentPath == null) {
                solution.setValid(false);
                break;
            }
            solution.addOrUpdateAgentPlan(agent.getAgentId(), singleAgentPath);
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
