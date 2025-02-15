package searchclient;

import java.util.Comparator;

public abstract class Heuristic implements Comparator<State> {
    public Heuristic(State initialState) {
        // Here's a chance to pre-process the static parts of the level.
    }

    public int h(State s) {
        return 0;
    }

    public abstract int f(State s);

    @Override
    public int compare(State s1, State s2) {
        return this.f(s1) - this.f(s2);
    }
}

class HeuristicAStar extends Heuristic {
    String hType;

    public HeuristicAStar(State initialState, String hType) {
        super(initialState);
        this.hType = hType;
    }

    @Override
    public int f(State s) {
        return s.g() + this.h(s);
    }

    @Override
    public int h(State s) {
        if ("-distance".equals(hType)) {
            return HeuristicsForSimple.hDistance(s);
        } else if ("-newdistance".equals(hType)) {
            return HeuristicsForFull.hDistance(s);
        } else {
            return HeuristicsForSimple.hGoal(s);
        }
    }

    @Override
    public String toString() {
        return "A* evaluation";
    }
}

class HeuristicWeightedAStar extends Heuristic {
    private int w;

    public HeuristicWeightedAStar(State initialState, int w) {
        super(initialState);
        this.w = w;
    }

    @Override
    public int f(State s) {
        return s.g() + this.w * this.h(s);
    }

    @Override
    public String toString() {
        return String.format("WA*(%d) evaluation", this.w);
    }
}

class HeuristicGreedy extends Heuristic {
    String hType;

    public HeuristicGreedy(State initialState, String hType) {
        super(initialState);
        this.hType = hType;
    }

    @Override
    public int f(State s) {
        return h(s);
    }

    @Override
    public int h(State s) {
        if ("-distance".equals(hType)) {
            return HeuristicsForSimple.hDistance(s);
        } else if ("-newdistance".equals(hType)) {
            return HeuristicsForFull.hDistance(s);
        } else {
            return HeuristicsForSimple.hGoal(s);
        }
    }

    @Override
    public String toString() {
        return "greedy evaluation";
    }
}
