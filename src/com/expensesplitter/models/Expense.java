// FIXED: Aligned fields with DB schema (categoryId, description, id, date as sql.Date)
package com.expensesplitter.models;

import java.sql.Date;

public class Expense {

    private int    id;
    private int    groupId;
    private String description;
    private double amount;
    private int    paidBy;
    private int    categoryId;
    private String splitType;
    private Date   date;

    public Expense() {
        this.splitType = "EQUAL";
    }

    public Expense(int id, int groupId, String description, double amount,
                   int paidBy, int categoryId, String splitType, Date date) {
        this.id = id;
        this.groupId = groupId;
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy;
        this.categoryId = categoryId;
        this.splitType = splitType;
        this.date = date;
    }

    public Expense(int groupId, String description, double amount,
                   int paidBy, int categoryId, String splitType, Date date) {
        this.groupId = groupId;
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy;
        this.categoryId = categoryId;
        this.splitType = splitType != null ? splitType : "EQUAL";
        this.date = date;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public int getPaidBy() { return paidBy; }
    public void setPaidBy(int paidBy) { this.paidBy = paidBy; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getSplitType() { return splitType; }
    public void setSplitType(String splitType) { this.splitType = splitType; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    @Override
    public String toString() {
        return "Expense{id=" + id + ", description='" + description + "', amount=" + amount + "}";
    }
}
