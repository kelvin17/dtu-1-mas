package searchclient.cbs.model;

import searchclient.Action;
import searchclient.Color;
import searchclient.cbs.utils.AStarReachabilityChecker;
import searchclient.cbs.utils.MapConverterHelper;

import java.io.Serializable;
import java.util.*;

public class LowLevelState implements Comparable<LowLevelState>, Serializable {
    private Map<Character, Move> agentMove = new HashMap<>();
    private Map<Character, Agent> agents = new HashMap<>();
    private Map<String, Box> boxes = new HashMap<>();
    private final Box[][] loc2Box;
    private LowLevelState parent;
    public int timeNow = 0;
    private final int gridNumRows;
    private final int gridNumCol;
    private boolean allInOne;

    public LowLevelState getParent() {
        return parent;
    }

    public Map<Character, Move> getAgentMove() {
        return agentMove;
    }

    public int getGridNumRows() {
        return this.gridNumRows;
    }

    public int getGridNumCol() {
        return this.gridNumCol;
    }

    public Map<Character, Agent> getAgents() {
        return this.agents;
    }

    public Map<String, Box> getBoxes() {
        return boxes;
    }

    //Extract the moves from the root to this state
    public Map<Integer, Move> extractMovesForOneAgent(Character agentId) {
        Map<Integer, Move> moves = new TreeMap<>();
        LowLevelState current = this;
        while (current != null) {
            Move move = current.agentMove.get(agentId);
            if (move != null) {
                moves.put(move.getTimeNow(), move);
            }
            current = current.parent;
        }

        return moves;
    }

    public LowLevelState(Map<Character, Agent> agents, Map<String, Box> boxes, int gridNumRows, int gridNumCol) {
        this.agents = agents;
        this.gridNumRows = gridNumRows;
        this.gridNumCol = gridNumCol;
        this.loc2Box = new Box[gridNumRows][gridNumCol];
        if (boxes != null) {
            this.boxes = boxes;
        }
    }

    public LowLevelState(LowLevelState parent) {
        this.parent = parent;
        this.gridNumRows = parent.gridNumRows;
        this.gridNumCol = parent.gridNumCol;
        this.allInOne = parent.allInOne;
        this.agentMove = new HashMap<>();

        for (Map.Entry<Character, Agent> entry : parent.agents.entrySet()) {
            this.agents.put(entry.getKey(), entry.getValue().deepCopy());
        }

        for (Map.Entry<String, Box> entry : parent.boxes.entrySet()) {
            this.boxes.put(entry.getKey(), entry.getValue().deepCopy());
        }

        this.loc2Box = new Box[this.gridNumRows][this.gridNumCol];
        if (!this.boxes.isEmpty()) {
            for (Box box : this.boxes.values()) {
                this.loc2Box[box.getCurrentLocation().getRow()][box.getCurrentLocation().getCol()] = box;
            }
        }
    }

    public static LowLevelState initRootStateForPlan(MetaAgentPlan metaAgentPlan) {
        Environment env = AppContext.getEnv();
        LowLevelState rootState = new LowLevelState(metaAgentPlan.getAgents(), metaAgentPlan.getBoxes(), env.getGridNumRows(), env.getGridNumCol());

        for (Agent agent : metaAgentPlan.getAgents().values()) {
            agent.setCurrentLocation(agent.getInitLocation());
        }

        if (!rootState.boxes.isEmpty()) {
            for (Box box : rootState.boxes.values()) {
                box.setCurrentLocation(box.getInitLocation());
                rootState.loc2Box[box.getCurrentLocation().getRow()][box.getCurrentLocation().getCol()] = box;
            }
        }
        return rootState;
    }

    public LowLevelState() {
        this.gridNumRows = 0;
        this.gridNumCol = 0;
        this.loc2Box = new Box[0][0];
    }

    private List<Character> getAgentIds() {
        return this.agents.values().stream().map(Agent::getAgentId).toList();
    }

    public List<LowLevelState> expand(Node currentNode) {
        Environment env = AppContext.getEnv();
        List<LowLevelState> newStates = new ArrayList<>();
        List<Constraint> constraints = new ArrayList<>();
        Node node = currentNode;
        while (node != null) {
            if (node.getAddedConstraint() != null && this.getAgentIds().contains(node.getAddedConstraint().getAgent().getAgentId())) {
                boolean isInner = false;
                List<String> agentIdList = List.of(node.getAddedConstraint().getFromMetaId().split("-"));
                for (Character agentId : this.getAgentIds()) {
                    if (agentIdList.contains(agentId.toString())) {
                        isInner = true;
                        break;
                    }
                }

                if (!isInner) {
                    //this is the state of parent. Now we need to check if the constraint is added for its child
                    if (this.timeNow + 1 == node.getAddedConstraint().getTime()) {
                        constraints.add(node.getAddedConstraint());
                    }
                }
            }
            node = node.getParent();
        }

        Map<Character, List<Move>> agentAvailableMoves = new HashMap<>();
        //1. get single agent in a metaGroup actions
        for (Agent agent : this.agents.values()) {
            for (Action action : Action.values()) {
                Move move = this.getNextMove(agent, action, constraints, env, currentNode);
                if (move != null) {
                    agentAvailableMoves.computeIfAbsent(agent.getAgentId(), k -> new ArrayList<>()).add(move);
                }
            }
        }

        //2. joint moves from different agent. every item in the list is a map of agentId to move
        List<Map<Character, Move>> availableJoints = MapConverterHelper.convertMapToListOfMaps(agentAvailableMoves);

        //3. use viablePairs to generate new states
        for (Map<Character, Move> pairs : availableJoints) {
            if (checkInnerConflict(pairs)) {
                LowLevelState child = this.generateChildState(pairs);
                newStates.add(child);
            }
        }

        return newStates;
    }

    private boolean checkInnerConflict(Map<Character, Move> pairs) {
        for (Move move : pairs.values()) {
            for (Move move1 : pairs.values()) {
                if (move.getAgent().getAgentId() != move1.getAgent().getAgentId()) {
                    //box conflict
                    if (move.getBox() != null && move.getBox().equals(move1.getBox())) {
                        return false;
                    }
                    //vertex conflict
                    if (move.getMoveTo().equals(move1.getMoveTo())) {
                        return false;
                    }
                    //edge conflict
                    if (move1.getMoveTo().equals(move.getCurrentLocation()) && move1.getCurrentLocation().equals(move.getMoveTo())) {
                        return false;
                    }
                    //follow conflict
                    if (move.getMoveTo().equals(move1.getCurrentLocation()) || move.getCurrentLocation().equals(move1.getMoveTo())) {
                        return false;
                    }

                    Location agent1Loc = move.getAgentTargetLocation();
                    Location box1Loc = move.getBoxTargetLocation();

                    Location agent2Loc = move1.getAgentTargetLocation();
                    Location box2Loc = move1.getBoxTargetLocation();

                    if (agent1Loc.equals(agent2Loc)
                            || agent1Loc.equals(box2Loc)
                            || agent2Loc.equals(box1Loc)) {
                        return false;
                    }

                    if (box1Loc != null && box1Loc.equals(box2Loc)) {
                        return false;
                    }

                    if (box2Loc != null && box2Loc.equals(box1Loc)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * @param agent
     * @param action
     * @return
     */
    private Move getNextMove(Agent agent, Action action, List<Constraint> constraints, Environment env, Node currentNode) {
        switch (action.type) {
            case NoOp:
                return new Move(agent, this.timeNow + 1, action, null);
            case Move:
                Location currentLocation = agent.getCurrentLocation();
                Location newLocation = new Location(currentLocation.getRow() + action.agentRowDelta, currentLocation.getCol() + action.agentColDelta);
                //1. check是不是墙
                if (env.isWall(newLocation)) {
                    return null;
                }
                //2. check会不会有当前组内的box
                if (this.loc2Box[newLocation.getRow()][newLocation.getCol()] != null) {
                    return null;
                }
                //3. 不需要检查考虑一下这里要不要考虑其他的agent和box - 应该可以不考虑。等同于ignore处理 todo
                //4. 检查新加入的指定时点的约束
                for (Constraint constraint : constraints) {
                    if (constraint.getFromLocation() == null) {
                        //vertex conflict or follow conflict
                        if (constraint.getToLocation().equals(newLocation)) {
                            return null;
                        }
                    } else {
                        //edge conflict
                        if (constraint.getFromLocation().equals(currentLocation) && constraint.getToLocation().equals(newLocation)) {
                            return null;
                        }
                    }
                }
                return new Move(agent, this.timeNow + 1, action, null);
            case Push:
                Location targetBox = new Location(agent.getCurrentLocation().getRow() + action.agentRowDelta, agent.getCurrentLocation().getCol() + action.agentColDelta);
                Box box = this.boxAt(targetBox);
                if (box == null || !box.getColor().equals(agent.getColor())) {
                    return null;
                }

                Location newOccupiedLocation = new Location(targetBox.getRow() + action.boxRowDelta, targetBox.getCol() + action.boxColDelta);
                //1. check是不是墙
                if (env.isWall(newOccupiedLocation)) {
                    return null;
                }
                //2. check 会不会有当前组内的box
                if (this.loc2Box[newOccupiedLocation.getRow()][newOccupiedLocation.getCol()] != null) {
                    return null;
                }
                //3. check 是否在约束中
                for (Constraint constraint : constraints) {
                    if (constraint.getFromLocation() == null) {
                        //vertex conflict or follow conflict
                        if (constraint.getToLocation().equals(newOccupiedLocation)) {
                            return null;
                        }
                    } else {
                        //edge conflict -- todo 似乎不会走到这里- 需要考虑一下是用box（中间）。还是用agent（最边上）
                        if (constraint.getFromLocation().equals(targetBox) && constraint.getToLocation().equals(targetBox)) {
                            return null;
                        }
                    }
                }
                return new Move(agent, this.timeNow + 1, action, box);

            case Pull:
                Location targetBoxForPull = new Location(agent.getCurrentLocation().getRow() - action.boxRowDelta, agent.getCurrentLocation().getCol() - action.boxColDelta);
                Box boxForPull = this.boxAt(targetBoxForPull);
                if (boxForPull == null || !boxForPull.getColor().equals(agent.getColor())) {
                    return null;
                }
                Location newOccupiedLocationForPull = new Location(agent.getCurrentLocation().getRow() + action.agentRowDelta, agent.getCurrentLocation().getCol() + action.agentColDelta);
                //1. check是不是墙
                if (env.isWall(newOccupiedLocationForPull)) {
                    return null;
                }
                //2. check 会不会有当前组内的box
                if (this.loc2Box[newOccupiedLocationForPull.getRow()][newOccupiedLocationForPull.getCol()] != null) {
                    return null;
                }
                //3. check 是否在约束中
                for (Constraint constraint : constraints) {
                    if (constraint.getFromLocation() == null) {
                        //vertex conflict or follow conflict
                        if (constraint.getToLocation().equals(newOccupiedLocationForPull)) {
                            return null;
                        }
                    } else {
                        //edge conflict -- todo 似乎不会走到这里 - 需要考虑一下是用agent（中间）。还是用box（最边上）
                        if (constraint.getFromLocation().equals(agent.getCurrentLocation()) && constraint.getToLocation().equals(targetBoxForPull)) {
                            return null;
                        }
                    }
                }
                return new Move(agent, this.timeNow + 1, action, boxForPull);
            default:
                throw new IllegalStateException("Unexpected value: " + action.type);
        }
    }

    public LowLevelState generateChildState(Map<Character, Move> AgentId2move) {

        LowLevelState child = new LowLevelState(this);
        child.timeNow = this.timeNow + 1;

        for (Map.Entry<Character, Move> entry : AgentId2move.entrySet()) {
            Character agentId = entry.getKey();
            Move move = entry.getValue();
            child.agentMove.put(agentId, move);
            Agent agent = child.getAgents().get(agentId);

            switch (move.getAction().type) {
                case NoOp:
                    break;
                case Move:
                    Location newAgentLoc = new Location(agent.getCurrentLocation().getRow() + move.getAction().agentRowDelta, agent.getCurrentLocation().getCol() + move.getAction().agentColDelta);
                    agent.setCurrentLocation(newAgentLoc);
                    break;
                case Pull:
                case Push:
                    Location newAgentLocForBox = new Location(agent.getCurrentLocation().getRow() + move.getAction().agentRowDelta, agent.getCurrentLocation().getCol() + move.getAction().agentColDelta);
                    agent.setCurrentLocation(newAgentLocForBox);

                    Box box = move.getBox();
                    Location newBoxLoc = new Location(box.getCurrentLocation().getRow() + move.getAction().boxRowDelta, box.getCurrentLocation().getCol() + move.getAction().boxColDelta);
                    child.updateBoxLocation(box.getCurrentLocation(), newBoxLoc);
                    break;
                default:
                    throw new IllegalStateException("generateChildState Unexpected value: " + move.getAction().type);
            }
        }
        return child;
    }

    /**
     * update box location both in the box and loc2Box
     *
     * @param origin
     * @param newLocation
     */
    public void updateBoxLocation(Location origin, Location newLocation) {
        Box box = this.loc2Box[origin.getRow()][origin.getCol()];
        if (box != null) {
            box.setCurrentLocation(newLocation);
            this.loc2Box[newLocation.getRow()][newLocation.getCol()] = box;
            this.loc2Box[origin.getRow()][origin.getCol()] = null;
        } else {
            throw new IllegalArgumentException("No box found for location " + newLocation);
        }
    }

    /**
     * check if single agent group plan has a solution
     *
     * @return
     */
    public boolean isGoal() {
        for (Agent agent : agents.values()) {
            if (agent.getGoalLocation() != null) {
                if (!agent.getCurrentLocation().equals(agent.getGoalLocation())) {
                    return false;
                }
            }
        }

        for (Box box : this.boxes.values()) {
            if (box.getGoalLocation() != null) {
                if (!box.getCurrentLocation().equals(box.getGoalLocation())) {
                    return false;
                }
            }
        }
        return true;
    }

    //manhattan distance heuristic
    public long getHeuristic() {
        Environment env = AppContext.getEnv();
        int heuristicValue = 0;
        Map<Color, List<Box>> reminderBox = new HashMap<>();
        for (Box box : this.boxes.values()) {
            if (box.getGoalLocation() != null) {
//                int mhtDis = Math.abs(box.getCurrentLocation().getRow() - box.getGoalLocation().getRow()) + Math.abs(box.getCurrentLocation().getCol() - box.getGoalLocation().getCol());

                AStarReachabilityChecker.ReachableResult result = env.getCostMap().get(box.getCurrentLocation()).get(box.getGoalLocation());
                if (!result.isReachable()) {
                    throw new IllegalStateException("box " + box.getCurrentLocation() + " is not reachable to " + box.getGoalLocation());
                }
                int mhtDis = result.getSteps();
                heuristicValue += mhtDis;

                if (mhtDis > 0) {
                    List<Box> boxList = reminderBox.computeIfAbsent(box.getColor(), k -> new ArrayList<>());
                    boxList.add(box);
                }
            }
        }

        if (!reminderBox.isEmpty()) {
            for (Agent agent : agents.values()) {
                List<Box> boxCanBePushed = reminderBox.get(agent.getColor());
                if (boxCanBePushed != null && !boxCanBePushed.isEmpty()) {
                    int mhtDis = -1;
                    for (Box box : boxCanBePushed) {
                        int tmpMhtDis = Math.abs(agent.getCurrentLocation().getRow() - box.getCurrentLocation().getRow())
                                + Math.abs(agent.getCurrentLocation().getCol() - box.getCurrentLocation().getCol());
                        mhtDis = ((mhtDis == -1) ? tmpMhtDis : Math.min(mhtDis, tmpMhtDis));
                    }
                    heuristicValue += mhtDis;
                }
            }
        }

        if (reminderBox.isEmpty()) {
            for (Agent agent : agents.values()) {
                if (agent.getGoalLocation() != null) {
                    AStarReachabilityChecker.ReachableResult result = env.getCostMap().get(agent.getCurrentLocation()).get(agent.getGoalLocation());
                    if (!result.isReachable()) {
                        throw new IllegalStateException("Agent " + agent.getCurrentLocation() + " is not reachable to " + agent.getGoalLocation());
                    }
                    int mhtDis = result.getSteps();
                    heuristicValue = Math.max(heuristicValue, mhtDis);
                }
            }
        }

        return heuristicValue;
    }

    // A* heuristic function
    public double getAStar() {
        return this.getHeuristic();
//        return 2*this.getHeuristic() + this.timeNow;
    }

    @Override
    public int compareTo(LowLevelState o) {
        return Objects.compare(this, o, Comparator.comparing(LowLevelState::getAStar));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        LowLevelState other = (LowLevelState) obj;

        if (!this.isAllInOne() && this.agents.size() == 1) {
            if (this.timeNow != other.timeNow) {
                return false;
            }
        }

        boolean equals = Objects.deepEquals(agents, other.agents);
        if (!equals) {
            return false;
        }

        return Objects.deepEquals(this.boxes, other.boxes);
    }

    private Box boxAt(Location location) {
        return this.loc2Box[location.getRow()][location.getCol()];
    }

    public int hashCode() {
        if (this.isAllInOne() || this.agents.size() > 1) {
            return Objects.hash(agents, boxes);
        } else {
            return Objects.hash(agents, boxes, timeNow);
        }
    }

    @Override
    public String toString() {
        return "LowLevelState{" + "agents=" + agents + ", boxes=" + boxes + ", loc2Box=" + Arrays.deepToString(loc2Box) + ", gridNumRows=" + gridNumRows + ", gridNumCol=" + gridNumCol + ", parent=" + parent + ", timeNow=" + timeNow + ", agentMove=" + agentMove + '}';
    }

    public boolean isAllInOne() {
        return allInOne;
    }

    public void setAllInOne(boolean allInOne) {
        this.allInOne = allInOne;
    }
}
