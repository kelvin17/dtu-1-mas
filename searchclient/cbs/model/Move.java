package searchclient.cbs.model;

import searchclient.Action;
import searchclient.ActionType;

/**
 * Base Model
 * Agent move at timeNow, who is at currentLocation
 */
public class Move implements AbstractDeepCopy<Move> {
    private final Agent agent;
    private final Box box;
    private final int timeNow;
    private final Action action;

    public Move(Agent agent, int timeNow, Action action, Box box) {
        this.agent = agent;
        this.timeNow = timeNow;
        this.action = action;
        this.box = box;
    }

    public Agent getAgent() {
        return agent;
    }

    public int getTimeNow() {
        return timeNow;
    }


    public Action getAction() {
        return action;
    }

    public Move copy() {
        Box box = this.box == null ? null : this.box.deepCopy();
        return new Move(agent, timeNow, action, box);
    }

    /**
     * Get the location to move to
     * if action is NoOp, return null
     * if action is Push, return the box location after the push
     * if action is Move or Pull, return the agent location after the move
     *
     * @return
     */
    public Location getMoveTo() {
        if (this.action == Action.NoOp) {
            //todo check whether this is correct
            return null;
        } else if (this.action.type == ActionType.Push) {
            return new Location(this.box.getCurrentLocation().getRow() + this.action.boxRowDelta,
                    this.box.getCurrentLocation().getCol() + this.action.boxColDelta);
        } else {
            return new Location(this.agent.getCurrentLocation().getRow() + this.action.agentRowDelta,
                    this.agent.getCurrentLocation().getCol() + this.action.agentColDelta);
        }
    }

    /**
     * This function is used for detecting the collision
     *
     * @return
     */
    public Location getCurrentLocation() {
        if (this.action.type == ActionType.Move) {
            return this.agent.getCurrentLocation();
        } else if (this.action.type == ActionType.Pull) {
            return this.agent.getCurrentLocation();
        } else if (this.action.type == ActionType.Push) {
            return this.box.getCurrentLocation();
        } else {
            //NoOp todo check whether this is correct
            return null;
        }
    }

    public Box getBox() {
        return box;
    }

    @Override
    public String toString() {
        return "Move{" +
                "agent=" + agent +
                ", box=" + box +
                ", timeNow=" + timeNow +
                ", action=" + action +
                '}';
    }
}
