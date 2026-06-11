package com.expensesplitter.strategies;

import com.expensesplitter.models.User;
import java.util.*;

public class CustomSplit extends SplitStrategy {

    private Map<User, Double> customAmounts;

    public CustomSplit(Map<User, Double> customAmounts) {
        this.customAmounts = customAmounts;
    }

    @Override
    public Map<User, Double> calculateSplit(double totalAmount, List<User> members) {
        return customAmounts;
    }
}
