// FIXED: Uses Map<Integer,Double> percentages summing to 100 per spec
package com.expensesplitter.split;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PercentageSplit extends SplitStrategy {

    private final Map<Integer, Double> percentages;

    public PercentageSplit(Map<Integer, Double> percentages) {
        this.percentages = percentages != null ? percentages : new HashMap<>();
    }

    @Override
    public Map<Integer, Double> split(double totalAmount, List<Integer> participantIds) {
        Map<Integer, Double> result = new HashMap<>();
        if (participantIds == null || participantIds.isEmpty()) {
            return result;
        }

        double totalPercent = percentages.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(totalPercent - 100.0) > 0.01) {
            throw new IllegalArgumentException(
                    "Percentages must sum to 100. Current sum: " + totalPercent);
        }

        for (Integer userId : participantIds) {
            double pct = percentages.getOrDefault(userId, 0.0);
            double share = Math.round((totalAmount * pct / 100.0) * 100.0) / 100.0;
            result.put(userId, share);
        }
        return result;
    }
}
