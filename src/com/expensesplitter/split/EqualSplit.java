// FIXED: Uses Integer participant IDs and equal division per spec
package com.expensesplitter.split;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EqualSplit extends SplitStrategy {

    @Override
    public Map<Integer, Double> split(double totalAmount, List<Integer> participantIds) {
        Map<Integer, Double> result = new HashMap<>();
        if (participantIds == null || participantIds.isEmpty()) {
            return result;
        }
        double share = Math.round((totalAmount / participantIds.size()) * 100.0) / 100.0;
        for (Integer userId : participantIds) {
            result.put(userId, share);
        }
        return result;
    }
}
