package searchclient.cbs.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Model for CBS
 *  For high level of CT
 */
public class Solution implements Iterable<SingleAgentPlan> {

    private final Map<Integer, SingleAgentPlan> agentPlans;

    public Solution(Map<Integer, SingleAgentPlan> agentPlans) {
        this.agentPlans = new HashMap<>(agentPlans);
    }

    public SingleAgentPlan getPlanForAgent(int agentId) {
        return agentPlans.get(agentId);
    }

    @Override
    public Iterator<SingleAgentPlan> iterator() {
        return agentPlans.values().iterator();
    }
}
