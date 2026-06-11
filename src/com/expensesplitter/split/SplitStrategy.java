// FIXED: Moved to split package with Map<Integer,Double> split signature per spec
package com.expensesplitter.split;

import java.util.List;
import java.util.Map;

public abstract class SplitStrategy {
    public abstract Map<Integer, Double> split(double totalAmount, List<Integer> participantIds);
}
