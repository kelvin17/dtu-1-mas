package searchclient.cbs.model;

import java.io.Serializable;
import java.util.*;

/**
 * Model for CBS
 * For high level of CT
 */
public class Solution implements AbstractDeepCopy<Solution>, Serializable {

    private final Map<Integer, SingleAgentPlan> agentPlans = new TreeMap<>();
    private boolean valid = true;
    private int maxSinglePath = 0;

    public SingleAgentPlan getPlanForAgent(int agentId) {
        return this.agentPlans.get(agentId);
    }

    public Solution() {
    }

    public Solution(Map<Integer, SingleAgentPlan> agentPlans, boolean valid, int maxSinglePath) {
        this.agentPlans.putAll(agentPlans);
        this.valid = valid;
        this.maxSinglePath = maxSinglePath;
    }

    public int getMaxSinglePath() {
        return maxSinglePath;
    }

    public void addOrUpdateAgentPlan(int agentId, SingleAgentPlan agentPlan) {
        if (agentPlan.getMoveSize() > this.maxSinglePath) {
            this.maxSinglePath = agentPlan.getMoveSize();
        }
        this.agentPlans.put(agentId, agentPlan);
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
