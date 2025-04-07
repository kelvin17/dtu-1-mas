package searchclient.cbs.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Base Model and For CBS
 * For low level of an agent
 */
public class SingleAgentPath implements Iterable<Move> {

    private final int agentId;

    private final List<Move> moves = new ArrayList<>();

    private final Location initLocation;

    private final Location goalLocation;

    public SingleAgentPath(int agentId, Location goalLocation, Location initLocation) {
        this.agentId = agentId;
        this.goalLocation = goalLocation;
        this.initLocation = initLocation;
    }

    public void addMove(Move move) {
        this.moves.add(move);
    }

    /**
     * The cost of the plan is the size of the move List.
     *
     * @return
     */
    public int getCost() {
        if (moves.isEmpty()) return 0;
        return moves.size();
    }

    @Override
    public Iterator<Move> iterator() {
        return this.moves.iterator();
    }

    public int getAgentId() {
        return agentId;
    }

    public Location getInitLocation() {
        return initLocation;
    }

    public Location getGoalLocation() {
        return goalLocation;
    }
}
