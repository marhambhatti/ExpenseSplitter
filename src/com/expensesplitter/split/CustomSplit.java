// FIXED: Uses Map<Integer,Double> custom amounts with sum validation per spec
package com.expensesplitter.split;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomSplit extends SplitStrategy {

    private final Map<Integer, Double> customAmounts;

    public CustomSplit(Map<Integer, Double> customAmounts) {
        this.customAmounts = customAmounts != null ? customAmounts : new HashMap<>();
    }

    @Override
    public Map<Integer, Double> split(double totalAmount, List<Integer> participantIds) {
        Map<Integer, Double> result = new HashMap<>();
        if (participantIds == null || participantIds.isEmpty()) {
            return result;
        }

        double sum = 0.0;
        for (Integer userId : participantIds) {
            double amount = Math.round(customAmounts.getOrDefault(userId, 0.0) * 100.0) / 100.0;
            result.put(userId, amount);
            sum += amount;
        }

        if (Math.abs(sum - totalAmount) > 0.05) {
            throw new IllegalArgumentException(
                    "Custom split amounts (" + sum + ") do not match total (" + totalAmount + ")");
        }
        return result;
    }
}
