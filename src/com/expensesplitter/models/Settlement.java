package com.expensesplitter.models;
// Settlement Model
import java.sql.Date;

public class Settlement {

    private int    id;
    private int    groupId;
    private int    payerId;
    private int    payeeId;
    private double amount;
    private Date   date;

    public Settlement() {}

    public Settlement(int id, int groupId, int payerId, int payeeId, double amount, Date date) {
        this.id = id;
        this.groupId = groupId;
        this.payerId = payerId;
        this.payeeId = payeeId;
        this.amount = amount;
        this.date = date;
    }

    public Settlement(int groupId, int payerId, int payeeId, double amount, Date date) {
        this.groupId = groupId;
        this.payerId = payerId;
        this.payeeId = payeeId;
        this.amount = amount;
        this.date = date;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public int getPayerId() { return payerId; }
    public void setPayerId(int payerId) { this.payerId = payerId; }

    public int getPayeeId() { return payeeId; }
    public void setPayeeId(int payeeId) { this.payeeId = payeeId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    @Override
    public String toString() {
        return "Settlement{id=" + id + ", payerId=" + payerId + ", payeeId=" + payeeId + ", amount=" + amount + "}";
    }
}
