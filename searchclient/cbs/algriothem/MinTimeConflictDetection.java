package searchclient.cbs.algriothem;

import searchclient.cbs.model.AbstractConflict;
import searchclient.cbs.model.Node;
import searchclient.cbs.model.SingleAgentPlan;

import java.util.List;

public class MinTimeConflictDetection {

    public AbstractConflict detect(Node n) {
        AbstractConflict conflict = null;
        List<SingleAgentPlan> paths = n.getSolution().getAgentPlansInOrder();
        for (int i = 0; i < paths.size(); i++) {
            SingleAgentPlan plan1 = paths.get(i);
            for (int j = i + 1; j < paths.size(); j++) {
                SingleAgentPlan plan2 = paths.get(j);
                AbstractConflict currentConflict = plan1.firstConflict(plan2);
                if (conflict == null
                        || (currentConflict != null && currentConflict.getTimeNow() < conflict.getTimeNow())) {
                    conflict = currentConflict;
                }
            }
        }
        return conflict;
    }
}
