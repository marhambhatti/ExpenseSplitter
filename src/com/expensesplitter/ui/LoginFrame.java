package com.expensesplitter.ui;

import com.expensesplitter.util.Session;
import com.expensesplitter.dao.UserDAO;
import com.expensesplitter.models.User;
import com.expensesplitter.validation.InputValidator;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class LoginFrame extends JFrame {

    private static final int CARD_W = 390;
    private static final int FIELD_W = CARD_W - 72;

    private JTextField    txtEmail;
    private JPasswordField txtPassword;

    public LoginFrame() {
        setTitle("Expense Splitter - Login");
        setSize(500, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UiStyles.CONTENT_BG);
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(CARD_W, 520));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiStyles.CARD_BORDER),
                new EmptyBorder(36, 36, 32, 36)));

        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.gridx   = 0;
        g.weightx = 1.0;
        int row = 0;

        JLabel iconLbl = new JLabel(buildWalletIcon(48));
        iconLbl.setHorizontalAlignment(SwingConstants.CENTER);
        g.gridy  = row++;
        g.insets = new Insets(0, 0, 12, 0);
        card.add(iconLbl, g);

        JLabel lblTitle = new JLabel("Expense Splitter Application", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(UiStyles.TEXT_PRIMARY);
        g.gridy  = row++;
        g.insets = new Insets(0, 0, 20, 0);
        card.add(lblTitle, g);

        g.gridy  = row++;
        g.insets = new Insets(0, 0, 22, 0);
        card.add(hairline(), g);

        g.gridy  = row++;
        g.insets = new Insets(0, 0, 0, 0);
        card.add(fieldLabel("Email Address"), g);

        txtEmail = new PlaceholderField("user@domain.com");
        styleInput(txtEmail);
        FontIcon emailIcon = FontIcon.of(FontAwesomeSolid.ENVELOPE, 14, new Color(140, 150, 165));
        g.gridy  = row++;
        g.insets = new Insets(6, 0, 16, 0);
        card.add(wrapWithIcon(emailIcon, txtEmail), g);

        g.gridy  = row++;
        g.insets = new Insets(0, 0, 0, 0);
        card.add(fieldLabel("Password"), g);

        txtPassword = new JPasswordField();
        txtPassword.setEchoChar('•');
        styleInput(txtPassword);
        FontIcon lockIcon = FontIcon.of(FontAwesomeSolid.LOCK, 14, new Color(140, 150, 165));
        g.gridy  = row++;
        g.insets = new Insets(6, 0, 24, 0);
        card.add(wrapWithIcon(lockIcon, txtPassword), g);

        JButton btnLogin = buildPrimaryButton("Login", FontAwesomeSolid.ARROW_RIGHT);
        btnLogin.addActionListener(e -> attemptLogin());
        g.gridy  = row++;
        g.insets = new Insets(0, 0, 22, 0);
        card.add(btnLogin, g);

        g.gridy  = row++;
        g.insets = new Insets(0, 0, 18, 0);
        card.add(hairline(), g);

        JLabel lblForgot = linkLabel("Forgot Password?", UiStyles.ACCENT_PRIMARY);
        lblForgot.setHorizontalAlignment(SwingConstants.CENTER);
        lblForgot.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showForgotPasswordDialog(); }
            @Override public void mouseEntered(MouseEvent e) { lblForgot.setForeground(UiStyles.ACCENT_PRIMARY.darker()); }
            @Override public void mouseExited(MouseEvent e)  { lblForgot.setForeground(UiStyles.ACCENT_PRIMARY); }
        });
        g.gridy  = row++;
        g.insets = new Insets(0, 0, 10, 0);
        card.add(lblForgot, g);

        JLabel lblRegister = linkLabel("Register New Account", new Color(120, 130, 145));
        lblRegister.setHorizontalAlignment(SwingConstants.CENTER);
        lblRegister.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                dispose();
                SwingUtilities.invokeLater(() -> new RegisterFrame().setVisible(true));
            }
            @Override public void mouseEntered(MouseEvent e) { lblRegister.setForeground(new Color(33, 37, 41)); }
            @Override public void mouseExited(MouseEvent e)  { lblRegister.setForeground(new Color(120, 130, 145)); }
        });
        g.gridy  = row++;
        g.insets = new Insets(0, 0, 0, 0);
        card.add(lblRegister, g);

        add(card);

        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        txtEmail.getInputMap(JComponent.WHEN_FOCUSED).put(enterKey, "next");
        txtEmail.getActionMap().put("next", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { txtPassword.requestFocusInWindow(); }
        });
        txtPassword.getInputMap(JComponent.WHEN_FOCUSED).put(enterKey, "login");
        txtPassword.getActionMap().put("login", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { attemptLogin(); }
        });

        getRootPane().setDefaultButton(btnLogin);
    }

    private void showForgotPasswordDialog() {
        JDialog dialog = new JDialog(this, "Forgot Password", true);
        dialog.setSize(420, 260);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.getContentPane().setBackground(Color.WHITE);
        dialog.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.gridx   = 0;
        g.weightx = 1.0;
        int row   = 0;

        JPanel inner = new JPanel(new GridBagLayout());
        inner.setBackground(Color.WHITE);
        inner.setBorder(new EmptyBorder(28, 32, 28, 32));

        g.gridy  = row++;
        g.insets = new Insets(0, 0, 8, 0);
        JLabel heading = new JLabel("Reset your password");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 17));
        heading.setForeground(UiStyles.TEXT_PRIMARY);
        inner.add(heading, g);

        g.gridy  = row++;
        g.insets = new Insets(0, 0, 14, 0);
        JLabel sub = new JLabel("Enter your registered email to receive a reset token.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(110, 120, 135));
        inner.add(sub, g);

        JTextField txtResetEmail = new PlaceholderField("user@domain.com");
        styleInput(txtResetEmail);
        FontIcon ei = FontIcon.of(FontAwesomeSolid.ENVELOPE, 14, new Color(140, 150, 165));
        g.gridy  = row++;
        g.insets = new Insets(0, 0, 20, 0);
        inner.add(wrapWithIcon(ei, txtResetEmail), g);

        JButton btnSend = buildPrimaryButton("Send Reset Token", FontAwesomeSolid.PAPER_PLANE);
        g.gridy  = row++;
        g.insets = new Insets(0, 0, 0, 0);
        inner.add(btnSend, g);

        dialog.add(inner);

        btnSend.addActionListener(ev -> {
            String email = txtResetEmail.getText().trim();
            if (!InputValidator.isValidEmail(email)) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid email address.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            UserDAO dao   = new UserDAO();
            String token  = dao.generatePasswordResetToken(email);

            if (token == null) {
                JOptionPane.showMessageDialog(dialog,
                        "No account found with that email address.",
                        "Not Found", JOptionPane.WARNING_MESSAGE);
                return;
            }

            dialog.dispose();
            showResetTokenDialog(token);
        });

        dialog.setVisible(true);
    }

    private void showResetTokenDialog(String prefilledToken) {
        JDialog dialog = new JDialog(this, "Reset Password", true);
        dialog.setSize(440, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.getContentPane().setBackground(Color.WHITE);
        dialog.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.gridx   = 0;
        g.weightx = 1.0;
        int row   = 0;

        JPanel inner = new JPanel(new GridBagLayout());
        inner.setBackground(Color.WHITE);
        inner.setBorder(new EmptyBorder(28, 32, 28, 32));

        g.gridy  = row++;
        g.insets = new Insets(0, 0, 6, 0);
        JLabel heading = new JLabel("Enter new password");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 17));
        heading.setForeground(UiStyles.TEXT_PRIMARY);
        inner.add(heading, g);

        g.gridy  = row++;
        g.insets = new Insets(0, 0, 16, 0);
        JLabel tokenDisplay = new JLabel("Token: " + prefilledToken);
        tokenDisplay.setFont(new Font("Segoe UI Mono", Font.PLAIN, 11));
        tokenDisplay.setForeground(new Color(100, 110, 125));
        inner.add(tokenDisplay, g);

        g.gridy  = row++;
        g.insets = new Insets(0, 0, 0, 0);
        inner.add(fieldLabel("Reset Token"), g);

        JTextField txtToken = new JTextField(prefilledToken);
        styleInput(txtToken);
        FontIcon keyIcon = FontIcon.of(FontAwesomeSolid.KEY, 14, new Color(140, 150, 165));
        g.gridy  = row++;
        g.insets = new Insets(6, 0, 14, 0);
        inner.add(wrapWithIcon(keyIcon, txtToken), g);

        g.gridy  = row++;
        g.insets = new Insets(0, 0, 0, 0);
        inner.add(fieldLabel("New Password"), g);

        JPasswordField txtNewPass = new JPasswordField();
        txtNewPass.setEchoChar('•');
        styleInput(txtNewPass);
        FontIcon lock1 = FontIcon.of(FontAwesomeSolid.LOCK, 14, new Color(140, 150, 165));
        g.gridy  = row++;
        g.insets = new Insets(6, 0, 14, 0);
        inner.add(wrapWithIcon(lock1, txtNewPass), g);

        g.gridy  = row++;
        g.insets = new Insets(0, 0, 0, 0);
        inner.add(fieldLabel("Confirm Password"), g);

        JPasswordField txtConfirmPass = new JPasswordField();
        txtConfirmPass.setEchoChar('•');
        styleInput(txtConfirmPass);
        FontIcon lock2 = FontIcon.of(FontAwesomeSolid.LOCK, 14, new Color(140, 150, 165));
        g.gridy  = row++;
        g.insets = new Insets(6, 0, 20, 0);
        inner.add(wrapWithIcon(lock2, txtConfirmPass), g);

        JButton btnReset = buildPrimaryButton("Reset Password", FontAwesomeSolid.CHECK);
        g.gridy  = row++;
        g.insets = new Insets(0, 0, 0, 0);
        inner.add(btnReset, g);

        dialog.add(inner);

        btnReset.addActionListener(ev -> {
            String token    = txtToken.getText().trim();
            String newPass  = new String(txtNewPass.getPassword());
            String confPass = new String(txtConfirmPass.getPassword());

            if (token.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter the reset token.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (InputValidator.isNullOrEmpty(newPass)) {
                JOptionPane.showMessageDialog(dialog, "New password cannot be empty.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (newPass.length() < 6) {
                JOptionPane.showMessageDialog(dialog, "Password must be at least 6 characters.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!newPass.equals(confPass)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = new UserDAO().resetPasswordWithToken(token, newPass);
            if (success) {
                JOptionPane.showMessageDialog(dialog,
                        "Password reset successfully. You can now log in with your new password.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Invalid or expired reset token. Please request a new one.",
                        "Reset Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(60, 70, 85));
        return lbl;
    }

    private void styleInput(JComponent field) {
        field.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 10));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(Color.WHITE);
    }

    private JPanel wrapWithIcon(FontIcon icon, JComponent field) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setPreferredSize(new Dimension(FIELD_W, 40));
        wrapper.setMaximumSize(new Dimension(FIELD_W, 40));
        wrapper.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 225)));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setBorder(new EmptyBorder(0, 12, 0, 4));
        iconLbl.setPreferredSize(new Dimension(36, 40));

        wrapper.add(iconLbl, BorderLayout.WEST);
        wrapper.add(field,   BorderLayout.CENTER);
        return wrapper;
    }

    private JButton buildPrimaryButton(String label, FontAwesomeSolid iconType) {
        FontIcon icon = FontIcon.of(iconType, 14, Color.WHITE);
        JButton btn = new JButton(label, icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? UiStyles.ACCENT_PRIMARY.darker() : UiStyles.ACCENT_PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setHorizontalTextPosition(SwingConstants.LEFT);
        btn.setIconTextGap(10);
        btn.setPreferredSize(new Dimension(FIELD_W, 44));
        btn.setMaximumSize(new Dimension(FIELD_W, 44));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(UiStyles.ACCENT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel linkLabel(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(color);
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return lbl;
    }

    private JSeparator hairline() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(220, 226, 235));
        return sep;
    }

    private ImageIcon buildWalletIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(190, 216, 255));
        g2.fillRoundRect(0, 0, size, size, 8, 8);

        g2.setColor(UiStyles.ACCENT_PRIMARY);
        g2.setFont(new Font("Segoe UI", Font.BOLD, size - 12));
        FontMetrics fm = g2.getFontMetrics();
        String text = "C";
        int x = (size - fm.stringWidth(text)) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, x, y);

        g2.dispose();
        return new ImageIcon(img);
    }

    private void attemptLogin() {
        String email    = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (!InputValidator.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (InputValidator.isNullOrEmpty(password)) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User loggedInUser = new UserDAO().login(email, password);
        if (loggedInUser != null) {
            Session.setCurrentUser(loggedInUser);
            dispose();
            SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
        } else {
            JOptionPane.showMessageDialog(this, "Invalid email or password. Please try again.",
                    "Authentication Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class PlaceholderField extends JTextField {
        private final String hint;

        PlaceholderField(String hint) {
            this.hint = hint;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(170, 178, 192));
                g2.setFont(getFont());
                Insets ins = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(hint, ins.left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        }
    }
}