package searchclient.cbs.algriothem;

import searchclient.Action;
import searchclient.TimeoutException;
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

    private List<MetaAgentPlan> metaAgentPlanList = new ArrayList<>();

    public CBSRunner() {
        this.startTime = System.currentTimeMillis();
        this.lowLevelRunner = new AStarRunner(startTime, DEFAULT_TIMEOUT);
        this.conflictDetection = new MinTimeConflictDetection();
    }

    /**
     * High level of CBS
     *
     * @return
     */
    public Action[][] findSolution(int superB) {
        Environment initEnv = AppContext.getEnv();
        Node rootNode = initRoot(initEnv);
        if (!rootNode.getSolution().isValid()) {
            System.err.println("Root node is invalid");
            return null;
        }

        OpenList openList = new OpenList();
        openList.add(rootNode);

        int[][] cmMatrix = new int[initEnv.getAgentNums()][initEnv.getAgentNums()];

        while (!openList.isEmpty() && !checkTimeout()) {
            Node node = openList.pop();
            AbstractConflict firstConflict = conflictDetection.detect(node);
            if (firstConflict == null) {
                return convertPaths2Actions(node.getSolution());
            }
            System.err.println("Conflict detected result - " + firstConflict);
            //update cmMatrix
            updateCMMatrix(cmMatrix, firstConflict);

            Constraint[] constraints = firstConflict.getPreventingConstraints();

            int conflictsCount = cmMatrix[firstConflict.getAgent1().getAgentIdNum()][firstConflict.getAgent2().getAgentIdNum()];

            if (superB > -1 && conflictsCount >= superB) {
                doMergeAndUpdate(node, firstConflict);
                if (node.getSolution().isValid()) {
                    openList.add(node);
                }
            } else {
                for (Constraint constraint : constraints) {
                    Node child = buildChild(node, firstConflict, constraint);
                    if (child.getSolution().isValid()) {
                        openList.add(child);
                    }
                }
            }
        }
        System.err.println("CBS openlist empty with a solution");
        return null;
    }

    private void doMergeAndUpdate(Node node, AbstractConflict firstConflict) {
        MetaAgentPlan plan1 = firstConflict.getPlan1();
        MetaAgentPlan plan2 = firstConflict.getPlan2();
        MetaAgentPlan metaAgentPlan = plan1.merge(plan2);

        node.getSolution().getMetaPlans().remove(plan1.getMetaId());
        node.getSolution().getMetaPlans().remove(plan2.getMetaId());
        node.getSolution().addMetaAgentPlan(metaAgentPlan.getMetaId(), metaAgentPlan);

        //在内部处理掉对于内部的冲突的过滤 - 见：searchclient.cbs.model.LowLevelState.expand
        boolean allInOne = node.getSolution().getMetaPlans().size() == 1;
        boolean findNewPath = lowLevelRunner.findPath(node, metaAgentPlan, allInOne);
        node.getSolution().setValid(findNewPath);
        if (findNewPath) {
            node.getSolution().updateMaxSinglePath();
        }
    }

    private void updateCMMatrix(int[][] cmMatrix, AbstractConflict conflict) {
        //冲突的meta计划里的每个元素都要相互加一
        for (Agent agent1 : conflict.getPlan1().getAgents().values()) {
            for (Agent agent2 : conflict.getPlan2().getAgents().values()) {
                int agent1Index = agent1.getAgentIdNum();
                int agent2Index = agent2.getAgentIdNum();
                cmMatrix[agent1Index][agent2Index]++;
                cmMatrix[agent2Index][agent1Index]++;
            }
        }
    }

    private Node buildChild(Node parentCTNode, AbstractConflict firstConflict, Constraint constraint) {
        Node childCTNode = new Node(parentCTNode, firstConflict, constraint);

        Solution childSolution = parentCTNode.getSolution().deepCopy();
//        Agent agent = constraint.getAgent();
        MetaAgentPlan currentAgentPlan = childSolution.getPlanForAgent(constraint.getBelongToMetaId());
        boolean allInOne = childSolution.getMetaPlans().size() == 1;
        boolean findNewPath = lowLevelRunner.findPath(childCTNode, currentAgentPlan, allInOne);

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
        List<MetaAgentPlan> agentPathList = solution.getMetaPlansInOrder();
        //action 2-D array; 1st is the max steps of all agents; 2nd is the number of agents
        int rows = solution.getMaxMetaPath();
        int cols = 0;
        List<List<Move>> agent2Moves = new ArrayList<>();
        for (MetaAgentPlan agentPath : agentPathList) {
            cols += agentPath.getAgents().size();
            for (Map.Entry<Character, Agent> entry : agentPath.getAgents().entrySet()) {
                List<Move> moves = new ArrayList<>(agentPath.getMoves(entry.getKey()).values());
                agent2Moves.add(moves);
            }
        }

        Action[][] actions = new Action[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                List<Move> moves = agent2Moves.get(j);
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
            throw new TimeoutException("High-level CBS Timeout");
        }
        return false;
    }

    private Node initRoot(Environment environment) {
        Map<Character, Integer> boxType2Index = new HashMap<>();
        for (Character key : environment.getBoxType2GoalMap().keySet()) {
            boxType2Index.put(key, 0);
        }
        //static group boxes to agent
        for (LowLevelColorGroup colorGroup : environment.getColorGroups().values()) {
            int agentCounts = colorGroup.getAgents().size();
            int boxCounts = colorGroup.getBoxes().size();
            if (agentCounts == 1) {
                Map<Character, Agent> agents = new HashMap<>();
                Agent agent = colorGroup.getAgents().get(0);
                agents.put(agent.getAgentId(), agent);
                MetaAgentPlan metaAgentPlan = new MetaAgentPlan(agents);
                for (Box box : colorGroup.getBoxes()) {
                    assignGoal2Box(box, environment, boxType2Index);
                    metaAgentPlan.addBox(box);
                }
                metaAgentPlanList.add(metaAgentPlan);
            } else {
                for (int i = 0; i < agentCounts; i++) {
                    Map<Character, Agent> agents = new HashMap<>();
                    Agent agent = colorGroup.getAgents().get(i);
                    agents.put(agent.getAgentId(), agent);

                    MetaAgentPlan metaAgentPlan = new MetaAgentPlan(agents);
                    for (int j = i; j < boxCounts; j = j + agentCounts) {
                        Box box = colorGroup.getBoxes().get(j);
                        assignGoal2Box(box, environment, boxType2Index);
                        metaAgentPlan.addBox(box);
                    }
                    metaAgentPlanList.add(metaAgentPlan);
                }
            }
        }

//        for (MetaAgentPlan metaAgentPlan : metaAgentPlanList) {
//            System.err.println(metaAgentPlan.toString());
//        }

        Node rootNode = new Node(null);

        Solution solution = new Solution();
        boolean allInOne = metaAgentPlanList.size() == 1;
        for (MetaAgentPlan metaAgentPlan : metaAgentPlanList) {
            boolean findPath = lowLevelRunner.findPath(rootNode, metaAgentPlan, allInOne);
            if (!findPath) {
                //it will be invalid if anyone agent cannot find a path
                solution.setValid(false);
                break;
            }
            solution.addMetaAgentPlan(metaAgentPlan.getMetaId(), metaAgentPlan);
        }
        rootNode.setSolution(solution);

        return rootNode;
    }

    /**
     * 1. assign goal location for box
     * 2. update the index of box goal list
     *
     * @param box
     * @param environment
     * @param type2CurrentIndex
     */
    private void assignGoal2Box(Box box, Environment environment, Map<Character, Integer> type2CurrentIndex) {
        List<Location> goalsForBoxType = environment.getBoxType2GoalMap().get(box.getBoxTypeLetter());
        if (goalsForBoxType == null || goalsForBoxType.isEmpty()) {
            System.err.printf("There is no goal for this type of box, [%s]\n", box.getBoxTypeLetter());
            return;
        }
        Integer index = type2CurrentIndex.get(box.getBoxTypeLetter());

        if (index >= goalsForBoxType.size()) {
            System.err.printf("There is no other goal for this box, [%s], which has been run out[%d]\n", box.getBoxTypeLetter(), goalsForBoxType.size());
            return;
        }

        box.setGoalLocation(goalsForBoxType.get(index));
        type2CurrentIndex.put(box.getBoxTypeLetter(), (index + 1));
    }

    public boolean isAbortedForTimeout() {
        return abortedForTimeout;
    }
}
