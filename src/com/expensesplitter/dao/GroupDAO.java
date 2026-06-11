package com.expensesplitter.dao;

import com.expensesplitter.models.Group;
import com.expensesplitter.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO {

    public boolean createGroup(Group group) {
        String sql = "INSERT INTO groups_table (name, created_by) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, group.getName());
            ps.setInt(2, group.getCreatedBy());

            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return addMember(keys.getInt(1), group.getCreatedBy());
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean groupNameExists(String name, int userId) {
        String sql = "SELECT COUNT(*) FROM groups_table g " +
                "INNER JOIN group_members gm ON g.id = gm.group_id " +
                "WHERE LOWER(TRIM(g.name)) = LOWER(TRIM(?)) AND gm.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean groupNameExistsExcluding(String name, int userId, int excludeGroupId) {
        String sql = "SELECT COUNT(*) FROM groups_table g " +
                "INNER JOIN group_members gm ON g.id = gm.group_id " +
                "WHERE LOWER(TRIM(g.name)) = LOWER(TRIM(?)) AND gm.user_id = ? AND g.id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, userId);
            ps.setInt(3, excludeGroupId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean renameGroup(int groupId, String newName) {
        String sql = "UPDATE groups_table SET name = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newName.trim());
            ps.setInt(2, groupId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addMember(int groupId, int userId) {
        String sql = "INSERT IGNORE INTO group_members (group_id, user_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeMember(int groupId, int userId) {
        String sql = "DELETE FROM group_members WHERE group_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Group> getGroupsByUser(int userId) {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT g.id, g.name, g.created_by FROM groups_table g " +
                "INNER JOIN group_members gm ON g.id = gm.group_id " +
                "WHERE gm.user_id = ? ORDER BY g.name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    groups.add(new Group(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("created_by")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }

    public Group getGroupById(int groupId) {
        String sql = "SELECT id, name, created_by FROM groups_table WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Group(rs.getInt("id"), rs.getString("name"), rs.getInt("created_by"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Integer> getMemberIds(int groupId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT user_id FROM group_members WHERE group_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    public int getMemberCount(int groupId) {
        String sql = "SELECT COUNT(*) FROM group_members WHERE group_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<User> getGroupMembers(int groupId) {
        List<User> members = new ArrayList<>();
        String sql = "SELECT u.id, u.name, u.email, u.password_hash FROM users u " +
                "INNER JOIN group_members gm ON u.id = gm.user_id " +
                "WHERE gm.group_id = ? ORDER BY u.name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(new User(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password_hash")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    public boolean deleteGroup(int groupId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM expense_splits WHERE expense_id IN (SELECT id FROM expenses WHERE group_id = ?)")) {
                ps.setInt(1, groupId); ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM expenses WHERE group_id = ?")) {
                ps.setInt(1, groupId); ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM settlements WHERE group_id = ?")) {
                ps.setInt(1, groupId); ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM group_members WHERE group_id = ?")) {
                ps.setInt(1, groupId); ps.executeUpdate();
            }
            int deleted;
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM groups_table WHERE id = ?")) {
                ps.setInt(1, groupId); deleted = ps.executeUpdate();
            }

            conn.commit();
            return deleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            return false;
        } finally {
            if (conn != null) { try { conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }
}