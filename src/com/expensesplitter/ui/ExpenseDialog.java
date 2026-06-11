package com.expensesplitter.ui;

import com.expensesplitter.util.Session;
import com.expensesplitter.dao.ExpenseDAO;
import com.expensesplitter.dao.GroupDAO;
import com.expensesplitter.dao.UserDAO;
import com.expensesplitter.models.Category;
import com.expensesplitter.models.Expense;
import com.expensesplitter.models.Group;
import com.expensesplitter.models.User;
import com.expensesplitter.validation.InputValidator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExpenseDialog extends JDialog {

    private JTextField txtDescription;
    private JTextField txtAmount;
    private JComboBox<String> cmbCategory;
    private JComboBox<Group> cmbGroup;
    private JSpinner dateSpinner;
    private JComboBox<User> cmbPayer;
    private JLabel lblCalculation;
    private JTextArea txtParticipants;

    private final List<Group> userGroups = new ArrayList<>();
    private final GroupDAO groupDAO = new GroupDAO();
    private final UserDAO userDAO = new UserDAO();

    public ExpenseDialog(Frame parent) {
        super(parent, "Add New Expense", true);
        setSize(520, 680);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        loadGroups();
        if (userGroups.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "Create a group first before adding expenses.",
                    "No Groups", JOptionPane.WARNING_MESSAGE);
        }

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.add(createHeader(), BorderLayout.NORTH);
        mainPanel.add(createFormPanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void loadGroups() {
        User user = Session.getCurrentUser();
        if (user != null) {
            userGroups.addAll(new GroupDAO().getGroupsByUser(user.getId()));
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                new EmptyBorder(18, 24, 18, 24)));

        JLabel title = new JLabel("Add New Expense");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel subtitle = new JLabel("Fill in the details below to record a new expense.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(UiStyles.TEXT_MUTED);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitle);
        header.add(textPanel, BorderLayout.CENTER);
        return header;
    }

    private JPanel createFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 10, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        gbc.gridy = 0;
        form.add(createLabel("Description *"), gbc);
        gbc.gridy = 1;
        txtDescription = createTextField("e.g. AWS Cloud Hosting - Q3");
        form.add(txtDescription, gbc);

        gbc.gridy = 2;
        form.add(createLabel("Amount *"), gbc);
        gbc.gridy = 3;
        JPanel amountRow = new JPanel(new GridLayout(1, 2, 10, 0));
        amountRow.setOpaque(false);
        txtAmount = createTextField("0.00");
        txtAmount.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                updateCalculation();
            }
        });
        cmbCategory = new JComboBox<>(Category.ALL_NAMES);
        styleComboBox(cmbCategory);
        amountRow.add(txtAmount);
        amountRow.add(cmbCategory);
        form.add(amountRow, gbc);

        gbc.gridy = 4;
        form.add(createLabel("Group / Workspace *"), gbc);
        gbc.gridy = 5;
        cmbGroup = new JComboBox<>(userGroups.toArray(new Group[0]));
        cmbGroup.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Group g) setText(g.getName());
                return this;
            }
        });
        cmbGroup.addActionListener(e -> updatePayerList());
        styleComboBox(cmbGroup);
        form.add(cmbGroup, gbc);

        gbc.gridy = 6;
        JPanel labelRow = new JPanel(new GridLayout(1, 2, 10, 0));
        labelRow.setOpaque(false);
        labelRow.add(createLabel("Date *"));
        labelRow.add(createLabel("Paid By *"));
        form.add(labelRow, gbc);

        gbc.gridy = 7;
        JPanel datePayerRow = new JPanel(new GridLayout(1, 2, 10, 0));
        datePayerRow.setOpaque(false);

        Calendar cal = Calendar.getInstance();
        cal.setTime(java.sql.Date.valueOf(LocalDate.now()));
        dateSpinner = new JSpinner(new SpinnerDateModel(cal.getTime(), null, null, Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setBackground(Color.WHITE);
        ((JSpinner.DefaultEditor) dateSpinner.getEditor()).getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) dateSpinner.getEditor()).getTextField().setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(210, 210, 210)),
                        new EmptyBorder(8, 10, 8, 10)));

        cmbPayer = new JComboBox<>();
        cmbPayer.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User u) setText(u.getName());
                return this;
            }
        });
        cmbPayer.addActionListener(e -> updateCalculation());
        styleComboBox(cmbPayer);
        datePayerRow.add(dateSpinner);
        datePayerRow.add(cmbPayer);
        form.add(datePayerRow, gbc);

        gbc.gridy = 8;
        form.add(createLabel("Expense Breakdown"), gbc);
        gbc.gridy = 9;
        lblCalculation = new JLabel("Select group and amount to see breakdown");
        lblCalculation.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCalculation.setForeground(new Color(100, 100, 100));
        lblCalculation.setVerticalAlignment(JLabel.TOP);
        form.add(lblCalculation, gbc);

        gbc.gridy = 10;
        form.add(createLabel("Participants"), gbc);
        gbc.gridy = 11;
        txtParticipants = new JTextArea(2, 0);
        txtParticipants.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtParticipants.setEditable(false);
        txtParticipants.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                new EmptyBorder(8, 10, 8, 10)));
        form.add(new JScrollPane(txtParticipants), gbc);

        SwingUtilities.invokeLater(this::updatePayerList);
        return form;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JButton cancelBtn = createStyledButton("Cancel", Color.WHITE, new Color(80, 80, 80), new Color(210, 210, 210));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = createStyledButton("Save Expense", UiStyles.ACCENT_PRIMARY, Color.WHITE, new Color(0, 82, 184));
        saveBtn.addActionListener(e -> handleSave());

        panel.add(cancelBtn);
        panel.add(saveBtn);
        return panel;
    }

    private void handleSave() {
        String description = txtDescription.getText().trim();
        String amountStr   = txtAmount.getText().trim();

        if (InputValidator.isNullOrEmpty(description) || InputValidator.isNullOrEmpty(amountStr)
                || cmbGroup.getSelectedItem() == null || cmbPayer.getSelectedItem() == null
                || dateSpinner.getValue() == null) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields (marked with *).",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!InputValidator.isPositiveDouble(amountStr)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive amount.",
                    "Invalid Amount", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date sqlDate;
        try {
            java.util.Date utilDate = (java.util.Date) dateSpinner.getValue();
            sqlDate = new Date(utilDate.getTime());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date selected.",
                    "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Group selectedGroup = (Group) cmbGroup.getSelectedItem();
        User  selectedPayer = (User)  cmbPayer.getSelectedItem();

        if (selectedPayer == null) {
            JOptionPane.showMessageDialog(this, "Please select who paid.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int categoryIndex = cmbCategory.getSelectedIndex();
        int categoryId    = categoryIndex >= 0 ? categoryIndex + 1 : 8;

        Expense expense = new Expense(
                selectedGroup.getId(),
                description,
                Double.parseDouble(amountStr),
                selectedPayer.getId(),
                categoryId,
                "EQUAL",
                sqlDate
        );

        if (new ExpenseDAO().addExpense(expense)) {
            JOptionPane.showMessageDialog(this, "Expense saved successfully!", "Expense Added",
                    JOptionPane.INFORMATION_MESSAGE);
            notifyExpensesPanel();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save expense. Check database connection.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void notifyExpensesPanel() {
        Window owner = getOwner();
        if (owner == null) return;
        findAndRefreshExpensesPanel(owner);
    }

    private void findAndRefreshExpensesPanel(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof ExpensesPanel ep) {
                ep.refreshExpenses();
                return;
            }
            if (c instanceof Container child) {
                findAndRefreshExpensesPanel(child);
            }
        }
    }

    private void updatePayerList() {
        cmbPayer.removeAllItems();
        Group selectedGroup = (Group) cmbGroup.getSelectedItem();
        if (selectedGroup != null) {
            for (User member : groupDAO.getGroupMembers(selectedGroup.getId())) {
                cmbPayer.addItem(member);
            }
        }
        updateCalculation();
    }

    private void updateCalculation() {
        String amountStr    = txtAmount.getText().trim();
        Group  selectedGroup = (Group) cmbGroup.getSelectedItem();
        User   selectedPayer = (User)  cmbPayer.getSelectedItem();

        if (InputValidator.isNullOrEmpty(amountStr) || selectedGroup == null || selectedPayer == null) {
            lblCalculation.setText("Enter amount to see breakdown");
            txtParticipants.setText("");
            return;
        }

        if (!InputValidator.isPositiveDouble(amountStr)) {
            lblCalculation.setText("Invalid amount");
            txtParticipants.setText("");
            return;
        }

        double amount      = Double.parseDouble(amountStr);
        List<User> members = groupDAO.getGroupMembers(selectedGroup.getId());

        if (members.isEmpty()) {
            lblCalculation.setText("No members in group");
            txtParticipants.setText("");
            return;
        }

        int    membersToDivide = members.size() - 1;
        double perPersonShare  = membersToDivide > 0 ? amount / membersToDivide : 0;

        StringBuilder breakdown    = new StringBuilder();
        StringBuilder participants = new StringBuilder();
        double totalOwed = 0;

        breakdown.append("<html><b>").append(selectedPayer.getName()).append(" paid Rs. ")
                .append(String.format("%.2f", amount)).append("</b><br><br>");

        for (User member : members) {
            if (member.getId() == selectedPayer.getId()) {
                breakdown.append("✓ ").append(member.getName()).append(" (paid full amount)<br>");
                participants.append("✓ ").append(member.getName())
                        .append(" - Paid: Rs. ").append(String.format("%.2f", amount))
                        .append(", Owes: Rs. 0.00\n");
            } else {
                breakdown.append("◦ ").append(member.getName()).append(" owes Rs. ")
                        .append(String.format("%.2f", perPersonShare)).append("<br>");
                participants.append("◦ ").append(member.getName())
                        .append(" - Paid: Rs. 0.00, Owes: Rs. ")
                        .append(String.format("%.2f", perPersonShare)).append("\n");
                totalOwed += perPersonShare;
            }
        }

        breakdown.append("<br><b>Summary:</b><br>");
        breakdown.append("Total Amount: Rs. ").append(String.format("%.2f", amount)).append("<br>");
        breakdown.append("Total Owed by Others: Rs. ").append(String.format("%.2f", totalOwed)).append("<br>");
        if (membersToDivide > 0)
            breakdown.append("Share per Person: Rs. ").append(String.format("%.2f", perPersonShare)).append("<br>");
        breakdown.append("</html>");

        lblCalculation.setText(breakdown.toString());
        txtParticipants.setText(participants.toString());
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(60, 60, 60));
        return lbl;
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        if (!InputValidator.isNullOrEmpty(placeholder)) {
            field.setText("");
            field.setToolTipText(placeholder);
        }
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(0, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                new EmptyBorder(6, 10, 6, 10)));
        return field;
    }

    private void styleComboBox(JComboBox<?> cmb) {
        cmb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmb.setBackground(Color.WHITE);
        cmb.setPreferredSize(new Dimension(0, 36));
    }

    private JButton createStyledButton(String text, Color bg, Color fg, Color border) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                new EmptyBorder(8, 20, 8, 20)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}