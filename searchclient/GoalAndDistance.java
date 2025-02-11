package searchclient;

public class GoalAndDistance {
    public static int hGoal(State s) {
        System.out.println("#I am Goal count");
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

        System.err.println("[DEBUG] Computed h(n) = " + misplacedGoals);
        return misplacedGoals;
    }

    public static int hDistance(State s) {
        System.out.println("#I am distance");
        int heuristicValue = 0;
        // 遍历所有格子，计算每个箱子和代理与目标之间的曼哈顿距离
        for (int row = 0; row < s.goals.length; row++) {
            for (int col = 0; col < s.goals[row].length; col++) {
                char goal = s.goals[row][col]; // 获取当前格子

                // 如果目标是一个箱子
                if ('A' <= goal && goal <= 'Z') {
                    // 找到这个箱子
                    for (int boxRow = 0; boxRow < s.boxes.length; boxRow++) {
                        for (int boxCol = 0; boxCol < s.boxes[boxRow].length; boxCol++) {
                            if (s.boxes[boxRow][boxCol] == goal) {
                                // 计算箱子当前位置与目标位置的曼哈顿距离
                                int manhattanDistance = Math.abs(boxRow - row) + Math.abs(boxCol - col);
                                heuristicValue = Math.max(manhattanDistance, heuristicValue);
                                break;
                            }
                        }
                    }
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
