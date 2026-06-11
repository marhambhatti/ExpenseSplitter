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

public class ExpenseDAO {

    private final GroupDAO groupDAO = new GroupDAO();

    public boolean addExpense(Expense expense) {
        String insertExpenseSql = "INSERT INTO expenses (group_id, description, amount, paid_by, category_id, split_type, date) VALUES (?,?,?,?,?,?,?)";
        String insertSplitSql = "INSERT INTO expense_splits (expense_id, user_id, amount) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int expenseId = -1;
            try (PreparedStatement ps = conn.prepareStatement(insertExpenseSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, expense.getGroupId());
                ps.setString(2, expense.getDescription());
                ps.setDouble(3, expense.getAmount());
                ps.setInt(4, expense.getPaidBy());
                ps.setInt(5, expense.getCategoryId());
                ps.setString(6, expense.getSplitType() != null ? expense.getSplitType() : "EQUAL");
                ps.setDate(7, expense.getDate());

                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        expenseId = keys.getInt(1);
                    }
                }
            }

            if (expenseId == -1) {
                conn.rollback();
                return false;
            }

            List<Integer> members = groupDAO.getMemberIds(expense.getGroupId());
            if (members.isEmpty()) {
                members = List.of(expense.getPaidBy());
            }

            // Exclude payer from the split - they already paid the full amount
            List<Integer> membersToSplit = new ArrayList<>();
            for (Integer memberId : members) {
                if (memberId != expense.getPaidBy()) {
                    membersToSplit.add(memberId);
                }
            }

            // If only payer is in group, they owe nothing
            if (membersToSplit.isEmpty()) {
                membersToSplit.add(expense.getPaidBy());
            }

            SplitStrategy strategy = new EqualSplit();
            Map<Integer, Double> splits = strategy.split(expense.getAmount(), membersToSplit);
            
            // Set payer's share to 0 since they already paid the full amount
            splits.put(expense.getPaidBy(), 0.0);

            try (PreparedStatement ps = conn.prepareStatement(insertSplitSql)) {
                for (Map.Entry<Integer, Double> entry : splits.entrySet()) {
                    ps.setInt(1, expenseId);
                    ps.setInt(2, entry.getKey());
                    ps.setDouble(3, entry.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return true;
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

    public boolean deleteExpense(int expenseId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM expense_splits WHERE expense_id = ?")) {
                ps.setInt(1, expenseId);
                ps.executeUpdate();
            }

            int deleted;
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

    public List<Expense> getExpensesByGroup(int groupId) {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT * FROM expenses WHERE group_id = ? ORDER BY date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
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

    public List<Expense> getExpensesByUser(int userId) {
        List<Expense> all = new ArrayList<>();
        for (Group group : new GroupDAO().getGroupsByUser(userId)) {
            all.addAll(getExpensesByGroup(group.getId()));
        }
        return all;
    }
}
