package searchclient.cbs.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Model for CBS
 *  For high level of CT
 */
public class Solution implements Iterable<SingleAgentPath> {

    private final Map<Integer, SingleAgentPath> agentPlans;

    public Solution(Map<Integer, SingleAgentPath> agentPlans) {
        this.agentPlans = new HashMap<>(agentPlans);
    }

    public SingleAgentPath getPlanForAgent(int agentId) {
        return agentPlans.get(agentId);
    }

    @Override
    public Iterator<SingleAgentPath> iterator() {
        return agentPlans.values().iterator();
    }
}
