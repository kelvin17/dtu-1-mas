package searchclient;

public class HeuristicsForSimple {
    public static int hGoal(State s) {
        int misplacedGoals = 0;

        for (int row = 0; row < State.goals.length; row++) {
            for (int col = 0; col < State.goals[row].length; col++) {
                char goal = State.goals[row][col];
                //if (goal == '\0') continue; // 没有目标的格子跳过
                if (!((goal <= '9' && goal >= '0') || (goal <= 'Z' && goal >= 'A'))) {
                    continue;
                }

                // **调试：打印目标和对应**
                char currentAgent = s.agentAt(row, col);
                if (currentAgent == '\0') {
                    char currentBox = s.boxes[row][col];
                    if (currentBox == '\0') {
                        //没有东西
                        misplacedGoals++;
                    } else {
                        if (currentBox != goal) {
                            misplacedGoals++;
                        }
                    }
                } else {
                    if (currentAgent != goal) {
                        misplacedGoals++;
                    }
                }
            }
        }

        return misplacedGoals;
    }

    public static int hDistance(State s) {
        int heuristicValue = 0;
        // 遍历所有格子，计算每个箱子和代理与目标之间的曼哈顿距离
        for (int row = 0; row < s.goals.length; row++) {
            for (int col = 0; col < s.goals[row].length; col++) {
                char goal = s.goals[row][col]; // 获取当前格子

                // 如果目标是一个箱子
                if ('A' <= goal && goal <= 'Z') {
                    // 找到这个箱子
                    int boxRow2 = s.boxLocation.get(goal).getRow();
                    int boxCol2 = s.boxLocation.get(goal).getCol();

                    //**Deadlock detection**
                    if (s.isDeadlocked(boxRow2, boxCol2)) {
                        return Integer.MAX_VALUE; // Dead state, not solvable
                    }
                    // 计算箱子当前位置与目标位置的曼哈顿距离
                    int manhattanDistance = Math.abs(boxRow2 - row) + Math.abs(boxCol2 - col);
                    heuristicValue = manhattanDistance + heuristicValue;
                }
                // 如果目标是一个代理
                if ('0' <= goal && goal <= '9') {
                    int agentIdx = goal - '0'; // 计算代理的索引
                    int agentRow = s.agentRows[agentIdx];
                    int agentCol = s.agentCols[agentIdx];

                    // 计算代理当前位置与目标位置的曼哈顿距离
                    int manhattanDistance = Math.abs(agentRow - row) + Math.abs(agentCol - col);
                    heuristicValue = Math.max(manhattanDistance, heuristicValue);
                }
            }
        }
        return heuristicValue;
    }

}
