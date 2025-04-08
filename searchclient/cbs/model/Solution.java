package searchclient.cbs.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Model for CBS
 * For high level of CT
 */
public class Solution implements Iterable<SingleAgentPath> {

    private final Map<Integer, SingleAgentPath> agentPlans;
    private boolean valid = true;

    public Solution() {
        this.agentPlans = new HashMap<>();
    }

    public SingleAgentPath getPlanForAgent(int agentId) {
        return agentPlans.get(agentId);
    }

    public void addOrUpdateAgentPlan(int agentId, SingleAgentPath agentPlan) {
        agentPlans.put(agentId, agentPlan);
    }

    @Override
    public Iterator<SingleAgentPath> iterator() {
        return agentPlans.values().iterator();
    }

    public Solution copy() {
        Solution copy = new Solution();
        for (SingleAgentPath agentPlan : agentPlans.values()) {
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
