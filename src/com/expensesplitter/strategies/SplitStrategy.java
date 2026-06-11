package com.expensesplitter.strategies;

import com.expensesplitter.models.User;
import java.util.List;
import java.util.Map;

// Abstract class — defines the contract for all split types
public abstract class SplitStrategy {
    public abstract Map<User, Double> calculateSplit(double totalAmount, List<User> members);

    @Override
    public String toString() {
        return "SplitStrategy: " + this.getClass().getSimpleName();
    }
}
