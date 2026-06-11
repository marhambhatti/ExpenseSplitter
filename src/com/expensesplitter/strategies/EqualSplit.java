package com.expensesplitter.strategies;

import com.expensesplitter.models.User;
import java.util.*;

public class EqualSplit extends SplitStrategy {

    @Override
    public Map<User, Double> calculateSplit(double totalAmount, List<User> members) {
        Map<User, Double> splitMap = new LinkedHashMap<>();
        double share = totalAmount / members.size();
        for (User member : members) {
            splitMap.put(member, Math.round(share * 100.0) / 100.0);
        }
        return splitMap;
    }
}
