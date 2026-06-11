// FIXED: Renamed fields to id and name to match DB schema and spec
package com.expensesplitter.models;

public class Category {

    public static final String[] ALL_NAMES = {
            "Infrastructure", "Meals", "Software", "Travel",
            "Hardware", "Office", "Events", "Other"
    };

    private int    id;
    private String name;

    public Category() {}

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Category(String name) {
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return name;
    }
}
