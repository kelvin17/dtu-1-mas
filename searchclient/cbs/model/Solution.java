package searchclient.cbs.model;

import java.util.*;

/**
 * Model for CBS
 * For high level of CT
 */
public class Solution implements Iterable<SingleAgentPlan> {

    private final Map<Integer, SingleAgentPlan> agentPlans = new TreeMap<>();
    private boolean valid = true;
    private int maxSinglePath = 0;

    public SingleAgentPlan getPlanForAgent(int agentId) {
        return this.agentPlans.get(agentId);
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
    public Iterator<SingleAgentPlan> iterator() {
        return agentPlans.values().iterator();
    }

    public Solution copy() {
        Solution copy = new Solution();
        copy.maxSinglePath = this.maxSinglePath;
        for (SingleAgentPlan agentPlan : this.agentPlans.values()) {
            copy.addOrUpdateAgentPlan(agentPlan.getAgentId(), agentPlan.copy());
        }
        return copy;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
