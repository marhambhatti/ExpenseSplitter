package com.expensesplitter.ui;

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
// Register Frame
public class RegisterFrame extends JFrame {

    private static final int CARD_W = 390;
    private static final int FIELD_W = CARD_W - 72;

    private JTextField     txtName;
    private JTextField     txtEmail;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirm;

    public RegisterFrame() {
        setTitle("Expense Splitter - Register");
        setSize(500, 720);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UiStyles.CONTENT_BG);
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(CARD_W, 620));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiStyles.CARD_BORDER),
                new EmptyBorder(36, 36, 32, 36)));

        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.gridx   = 0;
        g.weightx = 1.0;
        int row   = 0;

        JLabel iconLbl = new JLabel(buildWalletIcon(48));
        iconLbl.setHorizontalAlignment(SwingConstants.CENTER);
        g.gridy  = row++;
        g.insets = new Insets(0, 0, 12, 0);
        card.add(iconLbl, g);

        JLabel lblTitle = new JLabel("Create an Account", SwingConstants.CENTER);
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
        card.add(fieldLabel("Full Name"), g);

        txtName = new LoginFrame.PlaceholderField("John Doe");
        styleInput(txtName);
        FontIcon userIcon = FontIcon.of(FontAwesomeSolid.USER, 14, new Color(140, 150, 165));
        g.gridy  = row++;
        g.insets = new Insets(6, 0, 16, 0);
        card.add(wrapWithIcon(userIcon, txtName), g);

        g.gridy  = row++;
        g.insets = new Insets(0, 0, 0, 0);
        card.add(fieldLabel("Email Address"), g);

        txtEmail = new LoginFrame.PlaceholderField("user@domain.com");
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
        FontIcon lockIcon1 = FontIcon.of(FontAwesomeSolid.LOCK, 14, new Color(140, 150, 165));
        g.gridy  = row++;
        g.insets = new Insets(6, 0, 16, 0);
        card.add(wrapWithIcon(lockIcon1, txtPassword), g);

        g.gridy  = row++;
        g.insets = new Insets(0, 0, 0, 0);
        card.add(fieldLabel("Confirm Password"), g);

        txtConfirm = new JPasswordField();
        txtConfirm.setEchoChar('•');
        styleInput(txtConfirm);
        FontIcon lockIcon2 = FontIcon.of(FontAwesomeSolid.LOCK, 14, new Color(140, 150, 165));
        g.gridy  = row++;
        g.insets = new Insets(6, 0, 24, 0);
        card.add(wrapWithIcon(lockIcon2, txtConfirm), g);

        JButton btnRegister = buildPrimaryButton("Register", FontAwesomeSolid.USER_PLUS);
        btnRegister.addActionListener(e -> attemptRegister());
        g.gridy  = row++;
        g.insets = new Insets(0, 0, 22, 0);
        card.add(btnRegister, g);

        g.gridy  = row++;
        g.insets = new Insets(0, 0, 18, 0);
        card.add(hairline(), g);

        JLabel lblLogin = linkLabel("Already have an account? Login", new Color(120, 130, 145));
        lblLogin.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogin.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            }
            @Override public void mouseEntered(MouseEvent e) { lblLogin.setForeground(new Color(33, 37, 41)); }
            @Override public void mouseExited(MouseEvent e)  { lblLogin.setForeground(new Color(120, 130, 145)); }
        });
        g.gridy  = row++;
        g.insets = new Insets(0, 0, 0, 0);
        card.add(lblLogin, g);

        add(card);
        getRootPane().setDefaultButton(btnRegister);
    }

    private void attemptRegister() {
        String name     = txtName.getText().trim();
        String email    = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirm  = new String(txtConfirm.getPassword());

        if (InputValidator.isNullOrEmpty(name)) {
            JOptionPane.showMessageDialog(this, "Full name cannot be empty.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
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
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = new User(name, email, password);
        boolean success = new UserDAO().register(user);

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Account created successfully! You can now log in.",
                    "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        } else {
            JOptionPane.showMessageDialog(this,
                    "Registration failed. This email address may already be in use.",
                    "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
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
}