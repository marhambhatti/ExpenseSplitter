// FIXED: Aligned column names (id, payee_id, date) with new schema
package com.expensesplitter.dao;

import com.expensesplitter.models.Settlement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SettlementDAO {

    public boolean addSettlement(Settlement settlement) {
        String err = addSettlementWithMessage(settlement);
        return err == null;
    }

    /** Returns null on success, or an error message on failure. */
    public String addSettlementWithMessage(Settlement settlement) {
        String sql = "INSERT INTO settlements (group_id, payer_id, payee_id, amount, date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            System.out.println(" ");
            ps.setInt(1, settlement.getGroupId());
            ps.setInt(2, settlement.getPayerId());
            ps.setInt(3, settlement.getPayeeId());
            ps.setDouble(4, settlement.getAmount());
            ps.setDate(5, settlement.getDate());

            if (ps.executeUpdate() > 0) {
                return null;
            }
            return "Insert returned no rows.";
        } catch (SQLException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public List<Settlement> getSettlementsByGroup(int groupId) {
        List<Settlement> list = new ArrayList<>();
        String sql = "SELECT * FROM settlements WHERE group_id = ? ORDER BY date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Settlement s = new Settlement();
                    s.setId(rs.getInt("id"));
                    s.setGroupId(rs.getInt("group_id"));
                    s.setPayerId(rs.getInt("payer_id"));
                    s.setPayeeId(rs.getInt("payee_id"));
                    s.setAmount(rs.getDouble("amount"));
                    s.setDate(rs.getDate("date"));
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
