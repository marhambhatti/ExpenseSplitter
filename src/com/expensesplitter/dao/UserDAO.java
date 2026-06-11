package com.expensesplitter.dao;

import com.expensesplitter.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

public class UserDAO {

    // User Account
    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String generateToken() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    public boolean register(User user) {
        if (emailExists(user.getEmail())) {
            return false;
        }

        String hashedPassword = sha256(user.getPasswordHash());

        String sql = "INSERT INTO users (name, email, password_hash) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, hashedPassword);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User login(String email, String password) {
        String hashedPassword = sha256(password);

        String sql = "SELECT * FROM users WHERE email = ? AND password_hash = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, hashedPassword);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password_hash")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String generatePasswordResetToken(String email) {
        if (!emailExists(email)) {
            return null;
        }

        String token = generateToken();
        long expiresAt = System.currentTimeMillis() + (60 * 60 * 1000);

        String deleteSql = "DELETE FROM password_reset_tokens WHERE email = ?";
        String insertSql = "INSERT INTO password_reset_tokens (email, token, expires_at) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setString(1, email);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, email);
                ps.setString(2, token);
                ps.setLong(3, expiresAt);
                ps.executeUpdate();
            }
            return token;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean resetPasswordWithToken(String token, String newPassword) {
        String selectSql = "SELECT email, expires_at FROM password_reset_tokens WHERE token = ?";
        String updateSql = "UPDATE users SET password_hash = ? WHERE email = ?";
        String deleteSql = "DELETE FROM password_reset_tokens WHERE token = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            String email;
            long expiresAt;

            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setString(1, token);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return false;
                    email     = rs.getString("email");
                    expiresAt = rs.getLong("expires_at");
                }
            }

            if (System.currentTimeMillis() > expiresAt) {
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setString(1, token);
                    ps.executeUpdate();
                }
                return false;
            }

            String hashed = sha256(newPassword);
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, hashed);
                ps.setString(2, email);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setString(1, token);
                ps.executeUpdate();
            }
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Integer getFirstOtherUserId(int excludeId) {
        String sql = "SELECT id FROM users WHERE id != ? ORDER BY id LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password_hash")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password_hash")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}