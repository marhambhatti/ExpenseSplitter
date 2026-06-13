package com.expensesplitter.ui;

import com.expensesplitter.util.Session;
import com.expensesplitter.dao.ExpenseDAO;
import com.expensesplitter.dao.GroupDAO;
import com.expensesplitter.dao.UserDAO;
import com.expensesplitter.models.Category;
import com.expensesplitter.models.Expense;
import com.expensesplitter.models.Group;
import com.expensesplitter.models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchFilterFrame extends JPanel {
// Search Frame
    private JTextField txtSearch;
    private JComboBox<String> cmbGroup;
    private JComboBox<String> cmbCategory;
    private JTextField txtStartDate;
    private JTextField txtEndDate;
    private JComboBox<String> cmbPayer;

    private DefaultTableModel tableModel;
    private JLabel lblResultCount;

    private final GroupDAO groupDAO = new GroupDAO();
    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final UserDAO userDAO = new UserDAO();

    private List<Group> userGroups = new ArrayList<>();
    private List<Expense> allExpenses = new ArrayList<>();
    private Map<Integer, String> groupNames = new HashMap<>();
    private Map<Integer, String> userNames = new HashMap<>();

    public SearchFilterFrame() {
        setLayout(new BorderLayout());
        setOpaque(false);
        add(buildMainContent(), BorderLayout.CENTER);
        loadAllData();
        applyFilters();
    }

    private JPanel buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(28, 30, 28, 30));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Search & Filter");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UiStyles.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Search expenses by keyword, filter by group, category, date, or payer.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(UiStyles.TEXT_LABEL);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitle);

        JPanel filterCard = buildFilterPanel();
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiStyles.CARD_BORDER),
                new EmptyBorder(18, 20, 18, 20)));

        JPanel resultsPanel = buildResultsPanel();

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(titleBlock, BorderLayout.NORTH);
        top.add(filterCard, BorderLayout.CENTER);

        main.add(top, BorderLayout.NORTH);
        main.add(resultsPanel, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JPanel row1 = new JPanel(new BorderLayout(10, 0));
        row1.setOpaque(false);
        row1.setBorder(new EmptyBorder(0, 0, 14, 0));

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setPreferredSize(new Dimension(0, 38));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 225)),
                new EmptyBorder(4, 10, 4, 10)));

        JButton btnSearch = UiStyles.primaryButton("🔍  Search", this::applyFilters);
        btnSearch.setPreferredSize(new Dimension(130, 38));

        JLabel searchLbl = new JLabel("Search:  ");
        searchLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchLbl.setPreferredSize(new Dimension(80, 38));

        row1.add(searchLbl, BorderLayout.WEST);
        row1.add(txtSearch, BorderLayout.CENTER);
        row1.add(btnSearch, BorderLayout.EAST);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        row2.setOpaque(false);
        row2.setBorder(new EmptyBorder(0, 0, 12, 0));

        cmbGroup = buildCombo("All Groups");
        cmbCategory = buildCombo("All Categories");
        cmbPayer = buildCombo("All Payers");

        row2.add(labeledField("Group:", cmbGroup));
        row2.add(labeledField("Category:", cmbCategory));
        row2.add(labeledField("Payer:", cmbPayer));

        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        row3.setOpaque(false);

        txtStartDate = buildDateField("YYYY-MM-DD");
        txtEndDate = buildDateField("YYYY-MM-DD");

        row3.add(labeledField("From:", txtStartDate));
        row3.add(labeledField("To:", txtEndDate));

        JButton btnApply = UiStyles.primaryButton("Apply Filters", this::applyFilters);
        JButton btnClear = UiStyles.outlineButton("Clear All", this::clearFilters);
        btnApply.setPreferredSize(new Dimension(130, 34));
        btnClear.setPreferredSize(new Dimension(100, 34));

        row3.add(Box.createHorizontalStrut(8));
        row3.add(btnApply);
        row3.add(btnClear);

        panel.add(row1);
        panel.add(row2);
        panel.add(row3);
        return panel;
    }

    private JPanel buildResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 0, 0, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel resultsTitle = new JLabel("Results");
        resultsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

        lblResultCount = new JLabel("0 expenses found");
        lblResultCount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblResultCount.setForeground(UiStyles.TEXT_LABEL);

        header.add(resultsTitle, BorderLayout.WEST);
        header.add(lblResultCount, BorderLayout.EAST);

        String[] cols = {"Date", "Description", "Group", "Category", "Amount (Rs.)", "Paid By", "Split"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(UiStyles.TABLE_GRID);
        table.setShowGrid(true);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(UiStyles.TABLE_HEADER_BG);
        table.getTableHeader().setForeground(UiStyles.TEXT_SECONDARY);

        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(4).setCellRenderer(rightAlign);

        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                String val = v != null ? v.toString() : "";
                switch (val) {
                    case "EQUAL" -> setForeground(new Color(37, 99, 235));
                    case "CUSTOM" -> setForeground(new Color(22, 163, 74));
                    case "PERCENTAGE" -> setForeground(new Color(217, 119, 6));
                    default -> setForeground(UiStyles.TEXT_SECONDARY);
                }
                return comp;
            }
        });

        int[] widths = {100, 220, 140, 110, 110, 130, 90};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UiStyles.CARD_BORDER));
        scroll.getViewport().setBackground(Color.WHITE);

        panel.add(header, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void loadAllData() {
        User user = Session.getCurrentUser();
        if (user == null) return;

        userGroups = groupDAO.getGroupsByUser(user.getId());
        allExpenses.clear();
        groupNames.clear();
        userNames.clear();

        cmbGroup.removeAllItems();
        cmbGroup.addItem("All Groups");
        for (Group g : userGroups) {
            groupNames.put(g.getId(), g.getName());
            cmbGroup.addItem(g.getName());
            allExpenses.addAll(expenseDAO.getExpensesByGroup(g.getId()));
        }

        cmbCategory.removeAllItems();
        cmbCategory.addItem("All Categories");
        for (String cat : Category.ALL_NAMES) {
            cmbCategory.addItem(cat);
        }

        cmbPayer.removeAllItems();
        cmbPayer.addItem("All Payers");
        java.util.Set<Integer> payerIds = new java.util.LinkedHashSet<>();
        for (Expense e : allExpenses) payerIds.add(e.getPaidBy());
        for (int payerId : payerIds) {
            User payer = userDAO.getUserById(payerId);
            if (payer != null) {
                userNames.put(payerId, payer.getName());
                cmbPayer.addItem(payer.getName());
            }
        }
    }

    private void applyFilters() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);

        String keyword = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        String selGroup = cmbGroup != null ? (String) cmbGroup.getSelectedItem() : "All Groups";
        String selCategory = cmbCategory != null ? (String) cmbCategory.getSelectedItem() : "All Categories";
        String selPayer = cmbPayer != null ? (String) cmbPayer.getSelectedItem() : "All Payers";
        String startDateStr = txtStartDate != null ? txtStartDate.getText().trim() : "";
        String endDateStr = txtEndDate != null ? txtEndDate.getText().trim() : "";

        LocalDate startDate = parseDate(startDateStr);
        LocalDate endDate = parseDate(endDateStr);

        int count = 0;

        for (Expense e : allExpenses) {
            if (!keyword.isEmpty()) {
                String desc = e.getDescription() != null ? e.getDescription().toLowerCase() : "";
                if (!desc.contains(keyword)) continue;
            }

            if (selGroup != null && !selGroup.equals("All Groups")) {
                String gName = groupNames.getOrDefault(e.getGroupId(), "");
                if (!gName.equals(selGroup)) continue;
            }

            if (selCategory != null && !selCategory.equals("All Categories")) {
                String catName = getCategoryName(e.getCategoryId());
                if (!catName.equals(selCategory)) continue;
            }

            if (selPayer != null && !selPayer.equals("All Payers")) {
                String payerName = userNames.getOrDefault(e.getPaidBy(), "");
                if (!payerName.equals(selPayer)) continue;
            }

            if (e.getDate() != null) {
                LocalDate expDate = e.getDate().toLocalDate();
                if (startDate != null && expDate.isBefore(startDate)) continue;
                if (endDate != null && expDate.isAfter(endDate)) continue;
            }

            String dateStr = e.getDate() != null ? e.getDate().toString() : "—";
            String groupName = groupNames.getOrDefault(e.getGroupId(), "Unknown");
            String catName = getCategoryName(e.getCategoryId());
            String payerName = userNames.getOrDefault(e.getPaidBy(), "Unknown");
            String amountStr = String.format("%.2f", e.getAmount());

            tableModel.addRow(new Object[]{
                    dateStr,
                    e.getDescription() != null ? e.getDescription() : "—",
                    groupName,
                    catName,
                    amountStr,
                    payerName,
                    e.getSplitType() != null ? e.getSplitType() : "EQUAL"
            });
            count++;
        }

        if (lblResultCount != null) {
            lblResultCount.setText(count + " expense" + (count == 1 ? "" : "s") + " found");
        }
    }

    private void clearFilters() {
        txtSearch.setText("");
        txtStartDate.setText("YYYY-MM-DD");
        txtEndDate.setText("YYYY-MM-DD");
        cmbGroup.setSelectedIndex(0);
        cmbCategory.setSelectedIndex(0);
        cmbPayer.setSelectedIndex(0);
        applyFilters();
    }

    private String getCategoryName(int id) {
        if (id >= 1 && id <= Category.ALL_NAMES.length) {
            return Category.ALL_NAMES[id - 1];
        }
        return "Other";
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isEmpty() || s.equals("YYYY-MM-DD")) return null;
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private JComboBox<String> buildCombo(String defaultItem) {
        JComboBox<String> combo = new JComboBox<>();
        combo.addItem(defaultItem);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setPreferredSize(new Dimension(160, 34));
        return combo;
    }

    private JTextField buildDateField(String hint) {
        JTextField field = new JTextField(hint);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(120, 34));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 225)),
                new EmptyBorder(4, 8, 4, 8)));
        field.setForeground(new Color(130, 140, 155));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(hint)) {
                    field.setText("");
                    field.setForeground(UiStyles.TEXT_PRIMARY);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(hint);
                    field.setForeground(new Color(130, 140, 155));
                }
            }
        });
        return field;
    }

    private JPanel labeledField(String labelText, JComponent field) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(UiStyles.TEXT_SECONDARY);
        row.add(lbl);
        row.add(field);
        return row;
    }
}