package searchclient.cbs.model;

import java.io.Serializable;
import java.util.*;

/**
 * Model for CBS
 * For high level of CT
 */
public class Solution implements AbstractDeepCopy<Solution>, Serializable {

    private final Map<Character, SingleAgentPlan> agentPlans = new TreeMap<>();
    private boolean valid = true;
    private int maxSinglePath = 0;

    public SingleAgentPlan getPlanForAgent(Character agentId) {
        return this.agentPlans.get(agentId);
    }

    public Solution() {
    }

    public int getMaxSinglePath() {
        return maxSinglePath;
    }

    public void addAgentPlan(Character agentId, SingleAgentPlan agentPlan) {
        if (agentPlan.getMoveSize() > this.maxSinglePath) {
            this.maxSinglePath = agentPlan.getMoveSize();
        }
        this.agentPlans.put(agentId, agentPlan);
    }

    public void updateMaxSinglePath() {
        int newMaxSinglePath = 0;
        for (SingleAgentPlan singleAgentPlan : this.agentPlans.values()) {
            if (singleAgentPlan.getMoveSize() > newMaxSinglePath) {
                newMaxSinglePath = singleAgentPlan.getMoveSize();
            }
        }
        this.maxSinglePath = newMaxSinglePath;
    }

    public List<SingleAgentPlan> getAgentPlansInOrder() {
        return new ArrayList<>(this.agentPlans.values());
    }

    @Override
    public String toString() {
        return "Solution{" +
                "agentPlans=" + agentPlans +
                ", valid=" + valid +
                ", maxSinglePath=" + maxSinglePath +
                '}';
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
