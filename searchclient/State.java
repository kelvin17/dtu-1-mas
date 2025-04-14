package searchclient;

import searchclient.cbs.model.Location;

import java.util.*;

public class State {
    private static final Random RNG = new Random(1);

    /*
        The agent rows, columns, and colors are indexed by the agent number.
        For example, this.agentRows[0] is the row location of agent '0'.
    */
    public int[] agentRows;
    public int[] agentCols;
    public static Color[] agentColors;

    /*
        The walls, boxes, and goals arrays are indexed from the top-left of the level, row-major order (row, col).
               Col 0  Col 1  Col 2  Col 3
        Row 0: (0,0)  (0,1)  (0,2)  (0,3)  ...
        Row 1: (1,0)  (1,1)  (1,2)  (1,3)  ...
        Row 2: (2,0)  (2,1)  (2,2)  (2,3)  ...
        ...

        For example, this.walls[2] is an array of booleans for the third row.
        this.walls[row][col] is true if there's a wall at (row, col).

        this.boxes and this.char are two-dimensional arrays of chars.
        this.boxes[1][2]='A' means there is an A box at (1,2).
        If there is no box at (1,2), we have this.boxes[1][2]=0 (null character).
        Simiarly for goals.

    */
    public static boolean[][] walls;
    public char[][] boxes;
    public Map<Character, Location> boxLocation = new HashMap<>();
    public static char[][] goals;

    /*
        The box colors are indexed alphabetically. So this.boxColors[0] is the color of A boxes,
        this.boxColor[1] is the color of B boxes, etc.
    */
    public static Color[] boxColors;
    public final State parent;
    public final Action[] jointAction;
    private final int g;

    private int hash = 0;


    // Constructs an initial state.
    // Arguments are not copied, and therefore should not be modified after being passed in.
    public State(int[] agentRows, int[] agentCols, Color[] agentColors, boolean[][] walls, char[][] boxes, Color[] boxColors, char[][] goals, Map<Character, Location> boxLocation) {
        this.agentRows = agentRows;
        this.agentCols = agentCols;
        this.agentColors = agentColors;
        this.walls = walls;
        this.boxes = boxes;
        this.boxColors = boxColors;
        this.goals = goals;
        this.parent = null;
        this.jointAction = null;
        this.g = 0;
        this.boxLocation = boxLocation;
    }


    // Constructs the state resulting from applying jointAction in parent.
    // Precondition: Joint action must be applicable and non-conflicting in parent state.
    private State(State parent, Action[] jointAction) {
        // Copy parent
        this.agentRows = Arrays.copyOf(parent.agentRows, parent.agentRows.length);
        this.agentCols = Arrays.copyOf(parent.agentCols, parent.agentCols.length);
        this.boxLocation = deepCopy(parent.boxLocation);
        this.boxes = new char[parent.boxes.length][];
        for (int i = 0; i < parent.boxes.length; i++) {
            this.boxes[i] = Arrays.copyOf(parent.boxes[i], parent.boxes[i].length);
        }

        // Set own parameters
        this.parent = parent;
        this.jointAction = Arrays.copyOf(jointAction, jointAction.length);
        this.g = parent.g + 1;

        // Apply each action
        int numAgents = this.agentRows.length;
        int destinationBoxRow;
        int destinationBoxCol;

        for (int agent = 0; agent < numAgents; ++agent) {
            Action action = jointAction[agent];
            char box;

            switch (action.type) {
                case NoOp:
                    break;

                case Move:
                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;
                    break;

                case Push:
                    //1. update agent row and col num
                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;

                    //2. update boxes
                    //2.1 find old box
                    box = this.boxes[this.agentRows[agent]][this.agentCols[agent]];

                    destinationBoxRow = this.agentRows[agent] + action.boxRowDelta;
                    destinationBoxCol = this.agentCols[agent] + action.boxColDelta;

                    //1. set new boxes
                    this.boxes[destinationBoxRow][destinationBoxCol] = box;
                    //2. clear the old one
                    this.boxes[this.agentRows[agent]][this.agentCols[agent]] = '\0';
                    this.boxLocation.get(box).update(destinationBoxRow, destinationBoxCol);
                    break;

                case Pull:
                    //1. find box
                    int originAgentRow = this.agentRows[agent];
                    int originAgentCol = this.agentCols[agent];

                    destinationBoxRow = originAgentRow;
                    destinationBoxCol = originAgentCol;

                    //2. update agent row and col num
                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;

                    //3. find old box
                    int originBoxRow = originAgentRow;
                    int originBoxCol = originAgentCol;
                    if (action.boxRowDelta == 0) {
                        originBoxCol = originAgentCol - action.boxColDelta;
                    } else {
                        originBoxRow = originAgentRow - action.boxRowDelta;
                    }

                    //4. set box new position
                    box = this.boxes[originBoxRow][originBoxCol];
                    this.boxes[destinationBoxRow][destinationBoxCol] = box;
                    //5. clear old box position to empty
                    this.boxes[originBoxRow][originBoxCol] = '\0';
                    this.boxLocation.get(box).update(destinationBoxRow, destinationBoxCol);
                    break;
            }
        }
    }

    private Map<Character, Location> deepCopy(Map<Character, Location> boxLocation) {
        Map<Character, Location> newBoxLocation = new HashMap<>();
        for (Map.Entry<Character, Location> entry : boxLocation.entrySet()) {
            newBoxLocation.put(entry.getKey(), new Location(entry.getValue().getRow(), entry.getValue().getCol()));
        }
        return newBoxLocation;
    }

    public int g() {
        return this.g;
    }

    public boolean isGoalState() {
        for (int row = 1; row < this.goals.length - 1; row++) {
            for (int col = 1; col < this.goals[row].length - 1; col++) {
                char goal = this.goals[row][col];

                if ('A' <= goal && goal <= 'Z' && this.boxes[row][col] != goal) {
                    return false;
                } else if ('0' <= goal && goal <= '9' && !(this.agentRows[goal - '0'] == row && this.agentCols[goal - '0'] == col)) {
                    return false;
                }
            }
        }
        return true;
    }

    public ArrayList<State> getExpandedStates() {
        int numAgents = this.agentRows.length;

        // Determine list of applicable actions for each individual agent.
        Action[][] applicableActions = new Action[numAgents][];
        for (int agent = 0; agent < numAgents; ++agent) {
            ArrayList<Action> agentActions = new ArrayList<>(Action.values().length);
            for (Action action : Action.values()) {
                if (this.isApplicable(agent, action)) {
                    agentActions.add(action);
                }
            }
            applicableActions[agent] = agentActions.toArray(new Action[0]);
        }

        // Iterate over joint actions, check conflict and generate child states.
        Action[] jointAction = new Action[numAgents];
        int[] actionsPermutation = new int[numAgents];
        ArrayList<State> expandedStates = new ArrayList<>(16);
        while (true) {
            for (int agent = 0; agent < numAgents; ++agent) {
                jointAction[agent] = applicableActions[agent][actionsPermutation[agent]];
            }

            if (!this.isConflicting(jointAction)) {
                expandedStates.add(new State(this, jointAction));
            }

            // Advance permutation
            boolean done = false;
            for (int agent = 0; agent < numAgents; ++agent) {
                if (actionsPermutation[agent] < applicableActions[agent].length - 1) {
                    ++actionsPermutation[agent];
                    break;
                } else {
                    actionsPermutation[agent] = 0;
                    if (agent == numAgents - 1) {
                        done = true;
                    }
                }
            }

            // Last permutation?
            if (done) {
                break;
            }
        }

        Collections.shuffle(expandedStates, State.RNG);
        return expandedStates;
    }

    private boolean isApplicable(int agent, Action action) {
        int agentRow = this.agentRows[agent];
        int agentCol = this.agentCols[agent];
        Color agentColor = this.agentColors[agent];
        int boxRow;
        int boxCol;
        char box;
        Color boxColor;
        int destinationRow;
        int destinationCol;

        int destinationRowOfBox;
        int destinationColOfBox;
        switch (action.type) {
            case NoOp:
                return true;

            case Move:
                destinationRow = agentRow + action.agentRowDelta;
                destinationCol = agentCol + action.agentColDelta;
                return this.cellIsFree(destinationRow, destinationCol);

            case Push:
                destinationRow = agentRow + action.agentRowDelta;
                destinationCol = agentCol + action.agentColDelta;
                //1. condition1 - check  - 相同颜色的box
                boxRow = destinationRow;
                boxCol = destinationCol;
                box = this.boxes[boxRow][boxCol];
                if (box == '\0') {
                    //1. default -> no box
                    return false;
                }

                boxColor = boxColors[box - 'A'];
                if (boxColor != agentColor) {
                    return false;
                }

                //2. condition2
                destinationRowOfBox = boxRow + action.boxRowDelta;
                destinationColOfBox = boxCol + action.boxColDelta;
                return this.cellIsFree(destinationRowOfBox, destinationColOfBox);

            case Pull:
                destinationRow = agentRow + action.agentRowDelta;
                destinationCol = agentCol + action.agentColDelta;
                //1. cond1
                if (!this.cellIsFree(destinationRow, destinationCol)) {
                    return false;
                }

                //2. cond2 - find the box and check
                //2.1 find the box
                if (action.boxRowDelta == 0) {
                    //Row delta is 0.means box on the col of the agent
                    boxCol = agentCol - action.boxColDelta;
                    boxRow = agentRow;
                } else {
                    boxRow = agentRow - action.boxRowDelta;
                    boxCol = agentCol;
                }
                box = this.boxes[boxRow][boxCol];
                if (box == '\0') {
                    //1. default -> no box
                    return false;
                }

                //2.2 check color of the box
                boxColor = boxColors[box - 'A'];
                if (boxColor != agentColor) {
                    return false;
                }

                return true;
        }

        // Unreachable:
        return false;
    }

    private boolean isConflicting(Action[] jointAction) {
        int numAgents = this.agentRows.length;

        int[] destinationRows = new int[numAgents]; // row of new cell to become occupied by action
        int[] destinationCols = new int[numAgents]; // column of new cell to become occupied by action
        int[] boxRows = new int[numAgents]; // current row of box moved by action
        int[] boxCols = new int[numAgents]; // current column of box moved by action

        // Collect cells to be occupied and boxes to be moved
        for (int agent = 0; agent < numAgents; ++agent) {
            Action action = jointAction[agent];
            int agentRow = this.agentRows[agent];
            int agentCol = this.agentCols[agent];
            int boxRow;
            int boxCol;

            switch (action.type) {
                case NoOp:
                    break;

                case Move:
                    destinationRows[agent] = agentRow + action.agentRowDelta;
                    destinationCols[agent] = agentCol + action.agentColDelta;
                    boxRows[agent] = agentRow; // Distinct dummy value
                    boxCols[agent] = agentCol; // Distinct dummy value
                    break;

                case Push:
                    destinationRows[agent] = agentRow + action.agentRowDelta;
                    destinationCols[agent] = agentCol + action.agentColDelta;

                    boxRow = destinationRows[agent] + action.boxRowDelta;
                    boxCol = destinationCols[agent] + action.boxColDelta;
                    boxRows[agent] = boxRow;
                    boxCols[agent] = boxCol;

                    break;

                case Pull:
                    destinationRows[agent] = agentRow + action.agentRowDelta;
                    destinationCols[agent] = agentCol + action.agentColDelta;

                    boxRow = agentRow;
                    boxCol = agentCol;
                    boxRows[agent] = boxRow;
                    boxCols[agent] = boxCol;

                    break;
            }
        }

        for (int a1 = 0; a1 < numAgents; ++a1) {
            if (jointAction[a1] == Action.NoOp) {
                continue;
            }

            for (int a2 = a1 + 1; a2 < numAgents; ++a2) {
                if (jointAction[a2] == Action.NoOp) {
                    continue;
                }

                // Moving into same cell?
                if ((destinationRows[a1] == destinationRows[a2] && destinationCols[a1] == destinationCols[a2]) || (boxRows[a1] == boxRows[a2] && boxCols[a1] == boxCols[a2]) || (destinationRows[a1] == boxRows[a2] && destinationCols[a1] == boxCols[a2]) || (destinationRows[a2] == boxRows[a1] && destinationCols[a2] == boxCols[a1])) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isDeadlocked(int boxRow, int boxCol) {
        // 如果箱子当前位置被墙壁或其他箱子完全围住，说明死锁
        boolean upMayNotOk = !this.cellIsNotWallBoxes(boxRow - 1, boxCol);
        boolean downMayNotOk = !this.cellIsNotWallBoxes(boxRow + 1, boxCol);
        boolean leftMayNotOk = !this.cellIsNotWallBoxes(boxRow, boxCol - 1);
        boolean rightMayNotOk = !this.cellIsNotWallBoxes(boxRow, boxCol + 1);

        // 检查是否被完全围住，若是，则死锁
        return (upMayNotOk && downMayNotOk && leftMayNotOk && rightMayNotOk);
    }

    private boolean cellIsFree(int row, int col) {
        return !this.walls[row][col] && this.boxes[row][col] == 0 && this.agentAt(row, col) == 0;
    }

    private boolean cellIsNotWallBoxes(int row, int col) {
        return !this.walls[row][col] && this.boxes[row][col] == 0;
    }

    public char agentAt(int row, int col) {
        for (int i = 0; i < this.agentRows.length; i++) {
            if (this.agentRows[i] == row && this.agentCols[i] == col) {
                return (char) ('0' + i);
            }
        }
        return 0;
    }

    public Action[][] extractPlan() {
        Action[][] plan = new Action[this.g][];
        State state = this;
        while (state.jointAction != null) {
            plan[state.g - 1] = state.jointAction;
            state = state.parent;
        }
        return plan;
    }

    @Override
    public int hashCode() {
        if (this.hash == 0) {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(this.agentColors);
            result = prime * result + Arrays.hashCode(this.boxColors);
            result = prime * result + Arrays.deepHashCode(this.walls);
            result = prime * result + Arrays.deepHashCode(this.goals);
            result = prime * result + Arrays.hashCode(this.agentRows);
            result = prime * result + Arrays.hashCode(this.agentCols);
            for (int row = 0; row < this.boxes.length; ++row) {
                for (int col = 0; col < this.boxes[row].length; ++col) {
                    char c = this.boxes[row][col];
                    if (c != 0) {
                        result = prime * result + (row * this.boxes[row].length + col) * c;
                    }
                }
            }
            this.hash = result;
        }
        return this.hash;
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
        State other = (State) obj;
        return Arrays.equals(this.agentRows, other.agentRows) && Arrays.equals(this.agentCols, other.agentCols) && Arrays.equals(this.agentColors, other.agentColors) && Arrays.deepEquals(this.walls, other.walls) && Arrays.deepEquals(this.boxes, other.boxes) && Arrays.equals(this.boxColors, other.boxColors) && Arrays.deepEquals(this.goals, other.goals);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int row = 0; row < this.walls.length; row++) {
            for (int col = 0; col < this.walls[row].length; col++) {
                if (this.boxes[row][col] > 0) {
                    s.append(this.boxes[row][col]);
                } else if (this.walls[row][col]) {
                    s.append("+");
                } else if (this.agentAt(row, col) != 0) {
                    s.append(this.agentAt(row, col));
                } else {
                    s.append(" ");
                }
            }
            s.append("\n");
        }
        return s.toString();
    }
}