package com.expensesplitter.strategies;

import com.expensesplitter.models.User;
import java.util.*;

public class PercentageSplit extends SplitStrategy {

    private Map<User, Double> percentages;

    public PercentageSplit(Map<User, Double> percentages) {
        this.percentages = percentages;
    }

    @Override
    public Map<User, Double> calculateSplit(double totalAmount, List<User> members) {
        Map<User, Double> splitMap = new LinkedHashMap<>();
        for (User member : members) {
            double pct = percentages.getOrDefault(member, 0.0);
            double share = (totalAmount * pct) / 100.0;
            splitMap.put(member, Math.round(share * 100.0) / 100.0);
        }
        return splitMap;
    }
}
