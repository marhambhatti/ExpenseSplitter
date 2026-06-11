// FIXED: Aligned with new schema; addExpense(Expense) with equal split among group members
package com.expensesplitter.dao;

import com.expensesplitter.models.Expense;
import com.expensesplitter.models.Group;
import com.expensesplitter.split.EqualSplit;
import com.expensesplitter.split.SplitStrategy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// DAO class responsible for all CRUD operations on expenses and their splits
public class ExpenseDAO {

    // GroupDAO instance used to fetch group members for split calculations
    private final GroupDAO groupDAO = new GroupDAO();

    // Inserts a new expense and auto-splits the amount equally among group members
    public boolean addExpense(Expense expense) {
        String insertExpenseSql = "INSERT INTO expenses (group_id, description, amount, paid_by, category_id, split_type, date) VALUES (?,?,?,?,?,?,?)";
        String insertSplitSql = "INSERT INTO expense_splits (expense_id, user_id, amount) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            // Use transaction so expense + splits are inserted atomically
            conn.setAutoCommit(false);

            int expenseId = -1;
            // Insert the main expense record and retrieve its auto-generated ID
            try (PreparedStatement ps = conn.prepareStatement(insertExpenseSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, expense.getGroupId());
                ps.setString(2, expense.getDescription());
                ps.setDouble(3, expense.getAmount());
                ps.setInt(4, expense.getPaidBy());
                ps.setInt(5, expense.getCategoryId());
                // Default to EQUAL split if no split type is provided
                ps.setString(6, expense.getSplitType() != null ? expense.getSplitType() : "EQUAL");
                ps.setDate(7, expense.getDate());

                // Rollback if no rows were inserted
                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }

                // Retrieve the generated expense ID for use in expense_splits
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        expenseId = keys.getInt(1);
                    }
                }
            }

            // Rollback if generated key was not returned
            if (expenseId == -1) {
                conn.rollback();
                return false;
            }

            // Fetch all member IDs for this group; fallback to payer alone if group is empty
            List<Integer> members = groupDAO.getMemberIds(expense.getGroupId());
            if (members.isEmpty()) {
                members = List.of(expense.getPaidBy());
            }

            // Exclude the payer from the split — they already covered the full amount
            List<Integer> membersToSplit = new ArrayList<>();
            for (Integer memberId : members) {
                if (memberId != expense.getPaidBy()) {
                    membersToSplit.add(memberId);
                }
            }

            // Edge case: payer is the only member in the group — they owe nothing to anyone
            if (membersToSplit.isEmpty()) {
                membersToSplit.add(expense.getPaidBy());
            }

            // Apply equal split strategy to divide amount among non-payer members
            SplitStrategy strategy = new EqualSplit();
            Map<Integer, Double> splits = strategy.split(expense.getAmount(), membersToSplit);

            // Explicitly set payer's share to 0 since they already paid
            splits.put(expense.getPaidBy(), 0.0);

            // Batch insert all split records into expense_splits table
            try (PreparedStatement ps = conn.prepareStatement(insertSplitSql)) {
                for (Map.Entry<Integer, Double> entry : splits.entrySet()) {
                    ps.setInt(1, expenseId);
                    ps.setInt(2, entry.getKey());
                    ps.setDouble(3, entry.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // Commit both the expense and its splits together
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            // Rollback entire transaction on any SQL error
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            // Always close connection to return it to the pool
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // Updates an existing expense record by its ID (does not recalculate splits)
    public boolean updateExpense(Expense expense) {
        String sql = "UPDATE expenses SET group_id=?, description=?, amount=?, paid_by=?, category_id=?, split_type=?, date=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, expense.getGroupId());
            ps.setString(2, expense.getDescription());
            ps.setDouble(3, expense.getAmount());
            ps.setInt(4, expense.getPaidBy());
            ps.setInt(5, expense.getCategoryId());
            ps.setString(6, expense.getSplitType());
            ps.setDate(7, expense.getDate());
            ps.setInt(8, expense.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Deletes an expense and all its associated splits in a single transaction
    public boolean deleteExpense(int expenseId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Must delete splits first due to foreign key constraint on expense_id
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM expense_splits WHERE expense_id = ?")) {
                ps.setInt(1, expenseId);
                ps.executeUpdate();
            }

            int deleted;
            // Now safe to delete the parent expense record
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM expenses WHERE id = ?")) {
                ps.setInt(1, expenseId);
                deleted = ps.executeUpdate();
            }

            conn.commit();
            return deleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // Retrieves all expenses for a given group, ordered by most recent date first
    public List<Expense> getExpensesByGroup(int groupId) {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT * FROM expenses WHERE group_id = ? ORDER BY date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {

                // Map each result row to an Expense model object
                while (rs.next()) {
                    Expense e = new Expense();
                    e.setId(rs.getInt("id"));
                    e.setGroupId(rs.getInt("group_id"));
                    e.setDescription(rs.getString("description"));
                    e.setAmount(rs.getDouble("amount"));
                    e.setPaidBy(rs.getInt("paid_by"));
                    e.setCategoryId(rs.getInt("category_id"));
                    e.setSplitType(rs.getString("split_type"));
                    e.setDate(rs.getDate("date"));
                    list.add(e);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Retrieves all expenses across every group the given user belongs to
    public List<Expense> getExpensesByUser(int userId) {
        List<Expense> all = new ArrayList<>();
        // Iterate over all groups of the user and aggregate their expenses
        for (Group group : new GroupDAO().getGroupsByUser(userId)) {
            all.addAll(getExpensesByGroup(group.getId()));
        }
        return all;
    }
}