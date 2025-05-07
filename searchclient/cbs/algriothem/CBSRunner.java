package searchclient.cbs.algriothem;

import searchclient.Action;
import searchclient.TimeoutException;
import searchclient.cbs.model.*;
import searchclient.cbs.utils.AStarReachabilityChecker;

import java.util.*;

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
//            System.err.println("Conflict detected result - " + firstConflict);
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
        Map<Character, List<Move>> agent2MovesMap = new TreeMap<>();
        for (MetaAgentPlan agentPath : agentPathList) {
            cols += agentPath.getAgents().size();
            for (Map.Entry<Character, Agent> entry : agentPath.getAgents().entrySet()) {
                List<Move> moves = new ArrayList<>(agentPath.getMoves(entry.getKey()).values());
                agent2MovesMap.put(entry.getKey(), moves);
            }
        }

        List<List<Move>> agent2Moves = new ArrayList<>(agent2MovesMap.values());

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
        doDevide(environment, metaAgentPlanList);

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

    private void deDevideOld(Environment environment, List<MetaAgentPlan> metaAgentPlanList) {
        for (LowLevelColorGroup colorGroup : environment.getColorGroups().values()) {
            Map<Character, Integer> boxType2Index = new HashMap<>();
            for (Character key : environment.getBoxType2GoalMap().keySet()) {
                boxType2Index.put(key, 0);
            }

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

        for (MetaAgentPlan metaAgentPlan : metaAgentPlanList) {
            System.err.println(metaAgentPlan.toString());
        }
    }

    private void doDevide(Environment environment, List<MetaAgentPlan> metaAgentPlanList) {
        for (LowLevelColorGroup colorGroup : environment.getColorGroups().values()) {

            int agentCounts = colorGroup.getAgents().size();
            int boxCounts = colorGroup.getBoxes().size();

            //If this group don't have box, only have agents.
            if (colorGroup.getBoxes().isEmpty()) {
                Map<Character, Agent> agents = new HashMap<>();
                for (int i = 0; i < agentCounts; i++) {
                    Agent agent = colorGroup.getAgents().get(i);
                    agents.put(agent.getAgentId(), agent);
                }
                MetaAgentPlan metaAgentPlan = new MetaAgentPlan(agents);
                metaAgentPlanList.add(metaAgentPlan);
                continue;
            }

            //box-goals-steps
            Map<Box, Map<Location, Integer>> boxToReachableGoals = new HashMap<>();

//              1.检查每个box到goal的可达，并记录box-goal-距离表
            for (Box box : colorGroup.getBoxes()) {
                char type = box.getBoxTypeLetter();
                List<Location> goalList = environment.getBoxType2GoalMap().get(type);
                if(goalList == null){
                    continue;
                }
                Location boxLoc = box.getInitLocation();
                Map<Location, AStarReachabilityChecker.ReachableResult> reachMap = environment.getCostMap().get(boxLoc);
                for (Location goalLoc : goalList) {
                    AStarReachabilityChecker.ReachableResult result = reachMap.get(goalLoc);
                    if (result != null && result.isReachable()) {
                        boxToReachableGoals
                                .computeIfAbsent(box, k -> new HashMap<>())
                                .put(goalLoc, result.getSteps());
                    }
                }
            }

            //如果所有box都没有可达goal证明这一组box都没有goal，直接下一个colorgroup
            if(boxToReachableGoals.isEmpty()){
                System.err.printf("Color group [%s] has no boxes with reachable goals — skipped.\n", colorGroup.getColor());
                continue;
            }

//              2.检查每个goal都有至少一个box可达
            Set<Location> coveredGoals = new HashSet<>();
            for (Map<Location, Integer> goalMap : boxToReachableGoals.values()) {
                coveredGoals.addAll(goalMap.keySet());
            }

            List<Location> allGoals = new ArrayList<>();
            for (Box box : colorGroup.getBoxes()) {
                char boxType = box.getBoxTypeLetter();
                allGoals.addAll(environment.getBoxType2GoalMap().get(boxType));
            }

            List<Location> unreachableGoals = new ArrayList<>();
            for (Location goal : allGoals) {
                if (!coveredGoals.contains(goal)) {
                    unreachableGoals.add(goal);
                }
            }

            if (!unreachableGoals.isEmpty()) {
                System.err.println("Some goals are unreachable by any box:");
                for (Location goal : unreachableGoals) {
                    System.err.println("Unreachable goal at: " + goal);
                }
            } else {
                System.err.println("All goals are reachable by at least one box.");
            }

//              3.为box分配最短路径的goal
            Map<Box, Location> boxAssignment = new HashMap<>();
            Set<Location> assignedGoals = new HashSet<>();

            for (Map.Entry<Box, Map<Location, Integer>> entry : boxToReachableGoals.entrySet()) {
                Box box = entry.getKey();
                Map<Location, Integer> reachableGoals = entry.getValue();
                Location bestGoal = null;
                int minSteps = Integer.MAX_VALUE;

                for (Map.Entry<Location, Integer> goalEntry : reachableGoals.entrySet()) {
                    Location goal = goalEntry.getKey();
                    int steps = goalEntry.getValue();

                    if (steps < minSteps && !assignedGoals.contains(goal)) {
                        minSteps = steps;
                        bestGoal = goal;
                    }
                }

                if (bestGoal != null) {
                    boxAssignment.put(box, bestGoal);
                    assignedGoals.add(bestGoal);
                } else {
                    System.err.println("Warning: No available goal found for box " + box);
                }
            }

            for (Map.Entry<Box, Location> entry : boxAssignment.entrySet()) {
                Box box = entry.getKey();
                Location goal = entry.getValue();
                box.setGoalLocation(goal);
            }

//              4.记录未分配到goal的box
            List<Box> unassignedBoxes = new ArrayList<>();
            List<Box> allBoxes = colorGroup.getBoxes();
            for (Box box : allBoxes) {
                if (!boxAssignment.containsKey(box)) {
                    unassignedBoxes.add(box);
                }
            }

//          5.判断可达，并记录可达的agent-box-distance
            Map<Agent, Map<Box, Integer>> agentToReachableBoxes = new HashMap<>();

            List<Agent> agents = colorGroup.getAgents();
            List<Box> boxes = colorGroup.getBoxes();
            for (Agent agent : agents) {
                Location agentLoc = agent.getInitLocation();
                Map<Location, AStarReachabilityChecker.ReachableResult> agentReachMap = environment.getCostMap().get(agentLoc);

                for (Box box : boxes) {
                    Location boxLoc = box.getInitLocation();
                    AStarReachabilityChecker.ReachableResult result = agentReachMap.get(boxLoc);
                    if (result != null && result.isReachable()) {
                        agentToReachableBoxes
                                .computeIfAbsent(agent, k -> new HashMap<>())
                                .put(box, result.getSteps());
                    }
                }
            }

//          给box有无可达agent分类
            List<Box> reachableBoxes = new ArrayList<>();
            List<Box> unreachableBoxes = new ArrayList<>();

            for (Box box : boxes) {
                boolean hasReachableAgent = false;
                for (Map<Box, Integer> boxMap : agentToReachableBoxes.values()) {
                    if (boxMap.containsKey(box)) {
                        hasReachableAgent = true;
                        break;
                    }
                }
                if (hasReachableAgent) {
                    reachableBoxes.add(box);
                } else {
                    unreachableBoxes.add(box);
                }
            }

//         处理无agent的box
            for (Box box : unreachableBoxes) {
                List<Location> goalLocations = environment.getBoxType2GoalMap().get(box.getBoxTypeLetter());
                Location boxLoc = box.getInitLocation();
                if (goalLocations != null && goalLocations.contains(boxLoc)) {
                    Location loc = box.getInitLocation();
                    environment.setWallAt(loc);
                    System.err.printf("Box %s unreachable but already at goal — marked as wall.\n", box);
                } else if(box.getGoalLocation() == null){
                    Location loc = box.getInitLocation();
                    environment.setWallAt(loc);
                    System.err.printf("Box %s unreachable but no goal — marked as wall.\n", box);
                }else {
                    throw new IllegalStateException("Unreachable box " + box + " is not on goal — UNSOLVABLE.");
                }
            }

//          处理有agent的box, 按照负载分，存在boxAgentMap中      这里有问题，未分配到goal的box也被分配进来了  解决了
            Map<Box, Agent> boxAgentMap = new HashMap<>();
            Map<Agent, Integer> agentLoadMap = new HashMap<>();
            for (Box box : reachableBoxes) {
                if (!boxAssignment.containsKey(box)) continue; //跳过没有设置goal的box
                Agent bestAgent = null;
                int bestCost = Integer.MAX_VALUE;

                for (Map.Entry<Agent, Map<Box, Integer>> entry : agentToReachableBoxes.entrySet()) {
                    Agent agent = entry.getKey();
                    Map<Box, Integer> boxMap = entry.getValue();

                    if (!boxMap.containsKey(box)) continue;

                    int dist = boxMap.get(box);
                    int currentLoad = agentLoadMap.getOrDefault(agent, 0);
                    int totalCost = currentLoad + dist;

                    if (totalCost < bestCost) {
                        bestCost = totalCost;
                        bestAgent = agent;
                    }
                }

                if (bestAgent != null) {
                    boxAgentMap.put(box, bestAgent);
                    int updatedLoad = agentLoadMap.getOrDefault(bestAgent, 0) + agentToReachableBoxes.get(bestAgent).get(box);
                    agentLoadMap.put(bestAgent, updatedLoad);
                } else {
                    throw new IllegalStateException("Box " + box + " cannot be assigned — no agent can reach it.");
                }
            }

//            将每个分好组agent和box放到同一个MetaAgentplan里
            Map<Agent, List<Box>> agentToBoxesMap = new HashMap<>();
            for (Map.Entry<Box, Agent> entry : boxAgentMap.entrySet()) {
                Box box = entry.getKey();
                Agent agent = entry.getValue();
                agentToBoxesMap
                        .computeIfAbsent(agent, k -> new ArrayList<>())
                        .add(box);
            }
//            分配未分配的 box 给 agent（负载均衡 + 随机性）
            Random random = new Random();
            for (Box box : unassignedBoxes) {
                // 找当前负载最少的 agents（可以多个）
                int minLoad = Integer.MAX_VALUE;
                List<Agent> leastLoadedAgents = new ArrayList<>();

                for (Agent agent : agentToBoxesMap.keySet()) {
                    int load = agentToBoxesMap.get(agent).size();
                    if (load < minLoad) {
                        minLoad = load;
                        leastLoadedAgents.clear();
                        leastLoadedAgents.add(agent);
                    } else if (load == minLoad) {
                        leastLoadedAgents.add(agent);
                    }
                }

                // 随机选择一个负载最小的 agent
                Agent chosenAgent = leastLoadedAgents.get(random.nextInt(leastLoadedAgents.size()));
                agentToBoxesMap.get(chosenAgent).add(box);
            }


            for (Map.Entry<Agent, List<Box>> entry : agentToBoxesMap.entrySet()) {
                Agent agent = entry.getKey();
                List<Box> assignedBoxes = entry.getValue();

                Map<Character, Agent> agentMap = new HashMap<>();
                agentMap.put(agent.getAgentId(), agent);

                MetaAgentPlan metaAgentPlan = new MetaAgentPlan(agentMap);
                System.err.println("Agent ID: " + agent.getAgentId());
                for (Box box : assignedBoxes) {
                    metaAgentPlan.addBox(box);
                    System.err.println("  Box ID: " + box.getBoxTypeLetter() + " Box initLocation: " + box.getInitLocation());
                }
                metaAgentPlanList.add(metaAgentPlan);
            }
        }
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
//            System.err.printf("There is no goal for this type of box, [%s]\n", box.getBoxTypeLetter());
            return;
        }
        Integer index = type2CurrentIndex.get(box.getBoxTypeLetter());

        if (index >= goalsForBoxType.size()) {
//            System.err.printf("There is no other goal for this box, [%s], which has been run out[%d]\n", box.getBoxTypeLetter(), goalsForBoxType.size());
            return;
        }

        box.setGoalLocation(goalsForBoxType.get(index));
        type2CurrentIndex.put(box.getBoxTypeLetter(), (index + 1));
    }

    public boolean isAbortedForTimeout() {
        return abortedForTimeout;
    }
}
