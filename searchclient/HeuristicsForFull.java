package searchclient;

import java.util.*;

public class HeuristicsForFull {

    public static int hDistance(State s) {
        int heuristicValue = 0;
        /**
         * 1. 基于goal找要移动的目标agent/box 并分组
         *      把目标所有box按type分组
         *   分组：
         *        组1：agent0；box00；box01
         *        组2：agent1
         *        组3：agent2：box21
         *
         * 2. 计算代价
         *      组1：sum{box00->goal00,box01->goal00}
         *      组2：agent1->goal1
         *      组3：box21->goal21
         *      cost = max(组1，组2，组3)
         */

        Map<Color, AgentAndBoxesGroup> color2AgentAndBoxesGroup = group(s);
        List<Integer> heuristicValueInGroup = new ArrayList<>();
        //2. calculate the cost
        for (AgentAndBoxesGroup group : color2AgentAndBoxesGroup.values()) {
            int agentRow = -1;
            int agentCol = -1;
            if (group.isAgentHasGoal()) {
                agentRow = group.getAgentGoalLoc().getObjRow();
                agentCol = group.getAgentGoalLoc().getObjCol();
            } else {
                int agent = 0;
                //need to get the agent
                for (Color color : State.agentColors) {
                    if (color == group.getColor()) {
                        //find
                        agentRow = s.agentRows[agent];
                        agentCol = s.agentCols[agent];
                        break;
                    }
                    agent++;
                }
            }

            //1. find agent->box->goal in every group
            if (group.getBoxesAndLoc().isEmpty()) {
                //no boxes - there will just be an agent
                int manhattanDistance = Math.abs(agentRow - group.getAgentGoalLoc().getGoalRow()) + Math.abs(agentCol - group.getAgentGoalLoc().getGoalCol());
                heuristicValueInGroup.add(manhattanDistance);
            } else {

                int distanceFromAgent2Box2Goal = 0;
                for (Map.Entry<Character, ObjAndGoalLoc> entry : group.getBoxesAndLoc().entrySet()) {

                    int boxRow = entry.getValue().getObjRow();
                    int boxCol = entry.getValue().getObjCol();
                    int mhatDisBetweenGoal;
                    if (s.isDeadlocked(boxRow, boxCol)) {
                        mhatDisBetweenGoal = Integer.MAX_VALUE; // Dead state, not solvable
                    } else {
                        int goalRow = entry.getValue().getGoalRow();
                        int goalCol = entry.getValue().getGoalCol();
                        mhatDisBetweenGoal = Math.abs(boxRow - goalRow) + Math.abs(boxCol - goalCol);
//                        int mhatDisBetweenAgent = Math.abs(boxRow - agentRow) + Math.abs(boxCol - agentCol);
                    }
                    distanceFromAgent2Box2Goal += mhatDisBetweenGoal;
                }
                heuristicValueInGroup.add(distanceFromAgent2Box2Goal);
            }
        }
        //2.2 组间取最大值
        for (Integer value : heuristicValueInGroup) {
            value = Math.min(value, Integer.MAX_VALUE);
            heuristicValue = Math.max(heuristicValue, value);
        }
        return heuristicValue;
    }

    private static Map<Color, AgentAndBoxesGroup> group(State s) {
        //1. group by the color
        Map<Color, AgentAndBoxesGroup> color2AgentAndBoxesGroup = new HashMap<>();
        for (int row = 0; row < State.goals.length; row++) {
            for (int col = 0; col < State.goals[row].length; col++) {
                char goal = State.goals[row][col];
                //skip non-goal
                if (!((goal <= '9' && goal >= '0') || (goal <= 'Z' && goal >= 'A'))) {
                    continue;
                }

                if ((goal <= '9' && goal >= '0')) {
                    //如果是agent
                    int agent = (int) goal;
                    Color color = State.agentColors[agent];
                    AgentAndBoxesGroup group = color2AgentAndBoxesGroup.get(color);
                    if (null == group) {
                        group = new AgentAndBoxesGroup(color);
                        color2AgentAndBoxesGroup.put(color, group);
                    }

                    group.setAgent(agent);
                    group.setAgentHasGoal(true);
                    group.setAgentGoalLoc(new ObjAndGoalLoc(s.agentRows[agent], s.agentCols[agent], row, col));

                } else {
                    //如果是box
                    char box = (char) goal;
                    Color color = State.boxColors[box - 'A'];
                    AgentAndBoxesGroup group = color2AgentAndBoxesGroup.get(color);
                    if (null == group) {
                        group = new AgentAndBoxesGroup(color);
                        color2AgentAndBoxesGroup.put(color, group);
                    }

                    int boxRow = s.boxLocation.get(box).getRow();
                    int boxCol = s.boxLocation.get(box).getCol();
                    group.getBoxesAndLoc().put(box, new ObjAndGoalLoc(boxRow, boxCol, row, col));
                }
            }
        }

        return color2AgentAndBoxesGroup;
    }
}


class AgentAndBoxesGroup {
    private Color color;
    private int agent;

    private boolean agentHasGoal = false;

    private ObjAndGoalLoc agentGoalLoc;
    private Map<Character, ObjAndGoalLoc> boxesAndLoc = new HashMap<>();

    public AgentAndBoxesGroup(Color color) {
        this.color = color;
    }

    public int getAgent() {
        return agent;
    }

    public void setAgent(int agent) {
        this.agent = agent;
    }

    public Color getColor() {
        return color;
    }

    public void setAgentGoalLoc(ObjAndGoalLoc agentGoalLoc) {
        this.agentGoalLoc = agentGoalLoc;
    }

    public boolean isAgentHasGoal() {
        return agentHasGoal;
    }

    public void setAgentHasGoal(boolean agentHasGoal) {
        this.agentHasGoal = agentHasGoal;
    }

    public Map<Character, ObjAndGoalLoc> getBoxesAndLoc() {
        return boxesAndLoc;
    }

    public void setBoxesAndLoc(Map<Character, ObjAndGoalLoc> boxesAndLoc) {
        this.boxesAndLoc = boxesAndLoc;
    }

    public ObjAndGoalLoc getAgentGoalLoc() {
        return agentGoalLoc;
    }
}

class ObjAndGoalLoc {

    private int objRow;
    private int objCol;
    private int goalRow;
    private int goalCol;

    public ObjAndGoalLoc(int objRow, int objCol) {
        this.objRow = objRow;
        this.objCol = objCol;
    }

    public ObjAndGoalLoc(int objRow, int objCol, int goalRow, int goalCol) {
        this.objRow = objRow;
        this.objCol = objCol;

        this.goalRow = goalRow;
        this.goalCol = goalCol;
    }

    public int getGoalCol() {
        return goalCol;
    }

    public int getGoalRow() {
        return goalRow;
    }

    public int getObjCol() {
        return objCol;
    }

    public int getObjRow() {
        return objRow;
    }
}