package com.expensesplitter.ui;

import com.expensesplitter.util.Session;
import com.expensesplitter.dao.DatabaseConnection;
import com.expensesplitter.dao.ExpenseDAO;
import com.expensesplitter.dao.GroupDAO;
import com.expensesplitter.dao.UserDAO;
import com.expensesplitter.models.Category;
import com.expensesplitter.models.Expense;
import com.expensesplitter.models.Group;
import com.expensesplitter.models.User;
import com.expensesplitter.validation.InputValidator;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

// Ledger Details

public class LedgerDetailsFrame extends JPanel {

    private JTable            ledgerTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> groupCombo;
    private DatePickerField   startPicker;
    private DatePickerField   endPicker;
    private JComboBox<String> categoryCombo;
    private JComboBox<String> splitTypeCombo;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> rowsPerPageCombo;
    private JLabel            pageLabel;
    private JLabel            totalAmountLabel;
    private JLabel            totalCountLabel;
    private JLabel            settledCountLabel;
    private JLabel            unsettledAmtLabel;
    private JTextField        searchField;

    // Action buttons in toolbar — enabled only when a row is selected
    private JButton viewBtn;
    private JButton editBtn;
    private JButton delBtn;
    private JLabel  selectionHint;

    private int     currentPage = 1;
    private int     rowsPerPage = 10;
    private String  sortColumn  = "Date";
    private boolean sortAsc     = false;
    private String  searchQuery = "";

    private List<Expense> allExpenses      = new ArrayList<>();
    private List<Expense> filteredExpenses = new ArrayList<>();
    private List<Group>   userGroups       = new ArrayList<>();

    static final Color SETTLED_FG   = new Color(5,   122,  85);
    static final Color SETTLED_BG   = new Color(209, 250, 229);
    static final Color PARTIAL_FG   = new Color(146,  64,  14);
    static final Color PARTIAL_BG   = new Color(254, 243, 199);
    static final Color UNSETTLED_FG = new Color(153,  27,  27);
    static final Color UNSETTLED_BG = new Color(254, 226, 226);
    static final Color EQUAL_FG     = new Color(6,    95,  70);
    static final Color EQUAL_BG     = new Color(209, 250, 229);
    static final Color CUSTOM_FG    = new Color(30,   64, 175);
    static final Color CUSTOM_BG    = new Color(219, 234, 254);
    static final Color PCT_FG       = new Color(91,   33, 182);
    static final Color PCT_BG       = new Color(237, 233, 254);
    static final Color ROW_ALT      = new Color(249, 250, 252);
    static final Color ROW_SEL      = new Color(235, 244, 255);
    static final Color BORDER_C     = new Color(226, 232, 240);

    public LedgerDetailsFrame() {
        setLayout(new BorderLayout());
        setOpaque(false);
        add(buildRoot(), BorderLayout.CENTER);
        loadExpenses();
    }

    private JPanel buildRoot() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(24, 26, 18, 26));
        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        return root;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 18, 0));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel title = new JLabel("Ledger Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(UiStyles.TEXT_PRIMARY);

        JLabel sub = new JLabel("Track, filter and manage all expenses across your active groups.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(UiStyles.TEXT_LABEL);

        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(sub);

        bar.add(left,          BorderLayout.WEST);
        bar.add(buildKpiRow(), BorderLayout.EAST);
        return bar;
    }

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        row.setOpaque(false);

        JPanel totalCard     = kpiCard("Total Amount",   "Rs. 0.00", new Color(240, 249, 255), new Color(14,  116, 144));
        JPanel countCard     = kpiCard("Total Expenses", "0",        new Color(240, 253, 244), new Color(22,  101,  52));
        JPanel settledCard   = kpiCard("Settled",        "0",        new Color(240, 253, 244), new Color(5,   150, 105));
        JPanel unsettledCard = kpiCard("Unsettled Amt",  "Rs. 0.00", new Color(254, 242, 242), new Color(185,  28,  28));

        totalAmountLabel  = findLabel(totalCard);
        totalCountLabel   = findLabel(countCard);
        settledCountLabel = findLabel(settledCard);
        unsettledAmtLabel = findLabel(unsettledCard);

        row.add(totalCard); row.add(countCard); row.add(settledCard); row.add(unsettledCard);
        return row;
    }

    private JPanel kpiCard(String label, String val, Color bg, Color fg) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 45));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(11, 16, 11, 16));
        card.setPreferredSize(new Dimension(162, 62));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lbl.setForeground(UiStyles.TEXT_LABEL);

        JLabel valLbl = new JLabel(val);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        valLbl.setForeground(fg);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.add(lbl);
        inner.add(Box.createVerticalStrut(1));
        inner.add(valLbl);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JLabel findLabel(JPanel card) {
        for (Component c : ((JPanel) card.getComponent(0)).getComponents())
            if (c instanceof JLabel l && l.getFont().isBold()) return l;
        return new JLabel();
    }

    private JPanel buildCenter() {
        JPanel c = new JPanel(new BorderLayout(0, 12));
        c.setOpaque(false);

        JPanel filterCard = buildFilterPanel();
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C, 1),
                new EmptyBorder(14, 18, 12, 18)));

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(new LineBorder(BORDER_C, 1));
        tableCard.add(buildTablePanel(), BorderLayout.CENTER);
        tableCard.add(buildPagination(), BorderLayout.SOUTH);

        c.add(filterCard, BorderLayout.NORTH);
        c.add(tableCard,  BorderLayout.CENTER);
        return c;
    }

    // Filter panel with search + action buttons all in row2
    private JPanel buildFilterPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        // Row 1: filter combos
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        row1.setOpaque(false);

        groupCombo = styledCombo(160);
        groupCombo.addItem("All Groups");
        User u = Session.getCurrentUser();
        if (u != null)
            for (Group g : new GroupDAO().getGroupsByUser(u.getId()))
                groupCombo.addItem(g.getName());

        startPicker = new DatePickerField(148);
        endPicker   = new DatePickerField(148);

        categoryCombo = styledCombo(138);
        categoryCombo.addItem("All Categories");
        for (String cat : Category.ALL_NAMES) categoryCombo.addItem(cat);

        splitTypeCombo = styledCombo(128);
        splitTypeCombo.addItem("All Splits");
        splitTypeCombo.addItem("EQUAL");
        splitTypeCombo.addItem("CUSTOM");
        splitTypeCombo.addItem("PERCENTAGE");

        statusFilterCombo = styledCombo(128);
        statusFilterCombo.addItem("All Statuses");
        statusFilterCombo.addItem("SETTLED");
        statusFilterCombo.addItem("PARTIAL");
        statusFilterCombo.addItem("UNSETTLED");

        row1.add(filterLbl("Group:"));    row1.add(groupCombo);
        row1.add(sep());
        row1.add(filterLbl("From:"));     row1.add(startPicker);
        row1.add(filterLbl("To:"));       row1.add(endPicker);
        row1.add(sep());
        row1.add(filterLbl("Category:")); row1.add(categoryCombo);
        row1.add(sep());
        row1.add(filterLbl("Split:"));    row1.add(splitTypeCombo);
        row1.add(filterLbl("Status:"));   row1.add(statusFilterCombo);

        // Row 2: search + filter buttons (LEFT) | action buttons (RIGHT)
        JPanel row2 = new JPanel(new BorderLayout());
        row2.setOpaque(false);

        // LEFT side: search, clear, apply, export
        JPanel leftPart = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        leftPart.setOpaque(false);

        searchField = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(BORDER_C);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setOpaque(false);
        searchField.setBorder(new EmptyBorder(4, 10, 4, 10));
        searchField.setPreferredSize(new Dimension(220, 32));
        searchField.putClientProperty("placeholder", "Search description, payer, category...");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { onSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { onSearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { onSearch(); }
        });

        JButton clearBtn = UiStyles.outlineButton("Clear", () -> {
            groupCombo.setSelectedIndex(0);
            startPicker.setDate(null);
            endPicker.setDate(null);
            categoryCombo.setSelectedIndex(0);
            splitTypeCombo.setSelectedIndex(0);
            statusFilterCombo.setSelectedIndex(0);
            searchField.setText("");
            loadExpenses();
        });
        JButton applyBtn  = UiStyles.primaryButton("Apply Filters", this::applyAndRefresh);
        JButton exportBtn = buildExportBtn();

        leftPart.add(filterLbl("Search:"));
        leftPart.add(searchField);
        leftPart.add(Box.createHorizontalStrut(4));
        leftPart.add(clearBtn);
        leftPart.add(applyBtn);
        leftPart.add(Box.createHorizontalStrut(6));
        leftPart.add(exportBtn);

        // RIGHT side: View / Edit / Delete action buttons + hint label
        JPanel rightPart = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        rightPart.setOpaque(false);

        selectionHint = new JLabel("Select a row to act");
        selectionHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        selectionHint.setForeground(new Color(148, 163, 184));

        viewBtn = toolbarBtn("View",   new Color(71,  85, 105), new Color(241, 245, 249), new Color(203, 213, 225));
        editBtn = toolbarBtn("Edit",   new Color(37,  99, 235), new Color(219, 234, 254), new Color(147, 197, 253));
        delBtn  = toolbarBtn("Delete", new Color(185, 28,  28), new Color(254, 226, 226), new Color(252, 165, 165));

        viewBtn.addActionListener(e -> onViewAction());
        editBtn.addActionListener(e -> onEditAction());
        delBtn.addActionListener(e  -> onDeleteAction());

        // Start disabled — no row selected yet
        setActionButtonsEnabled(false);

        rightPart.add(selectionHint);
        rightPart.add(Box.createHorizontalStrut(4));
        rightPart.add(viewBtn);
        rightPart.add(editBtn);
        rightPart.add(delBtn);

        row2.add(leftPart,  BorderLayout.WEST);
        row2.add(rightPart, BorderLayout.EAST);

        p.add(row1, BorderLayout.NORTH);
        p.add(row2, BorderLayout.SOUTH);
        return p;
    }

    // Enable/disable the three action buttons and the hint label
    private void setActionButtonsEnabled(boolean enabled) {
        viewBtn.setEnabled(enabled);
        editBtn.setEnabled(enabled);
        delBtn.setEnabled(enabled);
        selectionHint.setVisible(!enabled);
    }

    private void onViewAction() {
        int row = ledgerTable.getSelectedRow();
        if (row < 0) return;
        int idx = (currentPage - 1) * rowsPerPage + row;
        if (idx < filteredExpenses.size()) showViewDialog(filteredExpenses.get(idx));
    }

    private void onEditAction() {
        int row = ledgerTable.getSelectedRow();
        if (row < 0) return;
        int idx = (currentPage - 1) * rowsPerPage + row;
        if (idx < filteredExpenses.size()) showEditDialog(filteredExpenses.get(idx));
    }

    private void onDeleteAction() {
        int row = ledgerTable.getSelectedRow();
        if (row < 0) return;
        int idx = (currentPage - 1) * rowsPerPage + row;
        if (idx < filteredExpenses.size()) deleteExpense(filteredExpenses.get(idx));
    }

    private void onSearch() {
        searchQuery = searchField.getText().trim().toLowerCase();
        currentPage = 1;
        applyAndRefresh();
    }

    private void applyAndRefresh() {
        filteredExpenses = new ArrayList<>(allExpenses);
        applyFilters();
        sortExpenses();
        currentPage = 1;
        updateTable();
        updateKpi();
    }

    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        // No "Actions" column — actions moved to toolbar above
        String[] cols = {"#", "Date", "Description", "Category", "Amount", "Payer", "Group", "Split", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        ledgerTable = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row))
                    c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                return c;
            }
            @Override public String getToolTipText(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row >= 0 && col == 2) {
                    Object v = getValueAt(row, col);
                    return v != null ? v.toString() : null;
                }
                return null;
            }
        };
        ledgerTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ledgerTable.setRowHeight(46);
        ledgerTable.setShowHorizontalLines(true);
        ledgerTable.setShowVerticalLines(false);
        ledgerTable.setGridColor(new Color(240, 242, 247));
        ledgerTable.setIntercellSpacing(new Dimension(0, 0));
        ledgerTable.setSelectionBackground(ROW_SEL);
        ledgerTable.setSelectionForeground(UiStyles.TEXT_PRIMARY);
        ledgerTable.setFillsViewportHeight(true);
        ledgerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ToolTipManager.sharedInstance().setInitialDelay(300);

        // When a row is selected, enable the toolbar action buttons
        ledgerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                setActionButtonsEnabled(ledgerTable.getSelectedRow() >= 0);
        });

        JTableHeader header = ledgerTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_C));
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                                                                     boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setBackground(new Color(248, 250, 252));
                lbl.setForeground(new Color(71, 85, 105));
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
                String col = (String) v;
                boolean sortable = col.equals("Date") || col.equals("Amount");
                if (sortable) {
                    String arrow = sortColumn.equals(col) ? (sortAsc ? " \u2191" : " \u2193") : " \u2195";
                    lbl.setText(col + arrow);
                    lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
                return lbl;
            }
        });

        header.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int col = header.columnAtPoint(e.getPoint());
                String[] cols2 = {"#", "Date", "Description", "Category", "Amount", "Payer", "Group", "Split", "Status"};
                String name = cols2[col];
                if (name.equals("Date") || name.equals("Amount")) {
                    if (sortColumn.equals(name)) sortAsc = !sortAsc;
                    else { sortColumn = name; sortAsc = true; }
                    applyAndRefresh();
                }
            }
        });

        // 9 columns now (no Actions) — widths sum nicely
        int[] widths = {34, 98, 165, 100, 115, 90, 100, 90, 100};
        for (int i = 0; i < widths.length; i++)
            ledgerTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        ledgerTable.getColumnModel().getColumn(0).setCellRenderer(new RowNumRenderer());
        ledgerTable.getColumnModel().getColumn(4).setCellRenderer(new AmountRenderer());
        ledgerTable.getColumnModel().getColumn(7).setCellRenderer(new BadgeRenderer(false));
        ledgerTable.getColumnModel().getColumn(8).setCellRenderer(new BadgeRenderer(true));

        JScrollPane sp = new JScrollPane(ledgerTable);
        sp.setBorder(null);
        sp.getViewport().setBackground(Color.WHITE);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildPagination() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_C),
                new EmptyBorder(8, 16, 8, 16)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JLabel rl = new JLabel("Rows per page:");
        rl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rl.setForeground(UiStyles.TEXT_SECONDARY);
        rowsPerPageCombo = styledCombo(62);
        for (String s : new String[]{"5", "10", "20", "50"}) rowsPerPageCombo.addItem(s);
        rowsPerPageCombo.setSelectedItem("10");
        rowsPerPageCombo.addActionListener(e -> {
            rowsPerPage = Integer.parseInt((String) rowsPerPageCombo.getSelectedItem());
            currentPage = 1;
            updateTable();
        });
        left.add(rl);
        left.add(rowsPerPageCombo);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        center.setOpaque(false);

        JButton first = navBtn("\u00AB");
        JButton prev  = navBtn("\u2039");
        pageLabel = new JLabel("Page 1 of 1");
        pageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pageLabel.setForeground(UiStyles.TEXT_SECONDARY);
        pageLabel.setBorder(new EmptyBorder(0, 10, 0, 10));
        JButton next = navBtn("\u203A");
        JButton last = navBtn("\u00BB");

        first.addActionListener(e -> { currentPage = 1; updateTable(); });
        prev.addActionListener(e  -> { if (currentPage > 1) { currentPage--; updateTable(); } });
        next.addActionListener(e  -> { int tp = totalPages(); if (currentPage < tp) { currentPage++; updateTable(); } });
        last.addActionListener(e  -> { currentPage = totalPages(); updateTable(); });

        center.add(first); center.add(prev); center.add(pageLabel); center.add(next); center.add(last);
        p.add(left,   BorderLayout.WEST);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private int totalPages() {
        return Math.max(1, (int) Math.ceil((double) filteredExpenses.size() / rowsPerPage));
    }

    private void loadExpenses() {
        User user = Session.getCurrentUser();
        if (user == null) { allExpenses.clear(); filteredExpenses.clear(); updateTable(); updateKpi(); return; }

        userGroups  = new GroupDAO().getGroupsByUser(user.getId());
        allExpenses = new ArrayList<>();
        for (Group g : userGroups)
            allExpenses.addAll(new ExpenseDAO().getExpensesByGroup(g.getId()));

        applyAndRefresh();
    }

    private void applyFilters() {
        String    selGroup    = (String) groupCombo.getSelectedItem();
        String    selCategory = (String) categoryCombo.getSelectedItem();
        String    selSplit    = (String) splitTypeCombo.getSelectedItem();
        String    selStatus   = (String) statusFilterCombo.getSelectedItem();
        LocalDate startD      = startPicker.getDate();
        LocalDate endD        = endPicker.getDate();

        List<Expense> src = new ArrayList<>(filteredExpenses);
        filteredExpenses.clear();

        for (Expense ex : src) {
            if (selGroup != null && !selGroup.equals("All Groups")) {
                Group grp = userGroups.stream()
                        .filter(g -> g.getId() == ex.getGroupId()).findFirst().orElse(null);
                if (grp == null || !grp.getName().equals(selGroup)) continue;
            }
            if (selCategory != null && !selCategory.equals("All Categories"))
                if (!getCategoryName(ex.getCategoryId()).equals(selCategory)) continue;

            if (selSplit != null && !selSplit.equals("All Splits")) {
                String st = ex.getSplitType() != null ? ex.getSplitType() : "EQUAL";
                if (!st.equals(selSplit)) continue;
            }
            if (selStatus != null && !selStatus.equals("All Statuses"))
                if (!computeStatus(ex).equals(selStatus)) continue;

            if (startD != null && ex.getDate() != null
                    && ex.getDate().toLocalDate().isBefore(startD)) continue;
            if (endD != null && ex.getDate() != null
                    && ex.getDate().toLocalDate().isAfter(endD)) continue;

            if (!searchQuery.isEmpty()) {
                String desc   = ex.getDescription() != null ? ex.getDescription().toLowerCase() : "";
                String catNm  = getCategoryName(ex.getCategoryId()).toLowerCase();
                User   payer  = new UserDAO().getUserById(ex.getPaidBy());
                String payerN = payer != null ? payer.getName().toLowerCase() : "";
                if (!desc.contains(searchQuery) && !catNm.contains(searchQuery)
                        && !payerN.contains(searchQuery)) continue;
            }
            filteredExpenses.add(ex);
        }
    }

    private void sortExpenses() {
        filteredExpenses.sort((a, b) -> {
            int cmp;
            if ("Amount".equals(sortColumn)) {
                cmp = Double.compare(a.getAmount(), b.getAmount());
            } else {
                if      (a.getDate() == null && b.getDate() == null) cmp = 0;
                else if (a.getDate() == null) cmp = -1;
                else if (b.getDate() == null) cmp =  1;
                else cmp = a.getDate().compareTo(b.getDate());
            }
            return sortAsc ? cmp : -cmp;
        });
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        int start = (currentPage - 1) * rowsPerPage;
        int end   = Math.min(start + rowsPerPage, filteredExpenses.size());

        UserDAO          uDAO = new UserDAO();
        SimpleDateFormat df   = new SimpleDateFormat("MMM dd, yyyy");

        for (int i = start; i < end; i++) {
            Expense ex    = filteredExpenses.get(i);
            User    payer = uDAO.getUserById(ex.getPaidBy());
            Group   grp   = userGroups.stream()
                    .filter(g -> g.getId() == ex.getGroupId()).findFirst().orElse(null);

            String dateStr   = ex.getDate()      != null ? df.format(ex.getDate()) : "N/A";
            String payerName = payer             != null ? payer.getName()          : "Unknown";
            String groupName = grp               != null ? grp.getName()            : "\u2014";
            String catName   = getCategoryName(ex.getCategoryId());
            String amtStr    = String.format("Rs. %,.2f", ex.getAmount());
            String splitType = ex.getSplitType() != null ? ex.getSplitType()        : "EQUAL";
            String status    = computeStatus(ex);

            tableModel.addRow(new Object[]{
                    i + 1, dateStr,
                    ex.getDescription() != null ? ex.getDescription() : "\u2014",
                    catName, amtStr, payerName, groupName, splitType, status
            });
        }

        int total     = filteredExpenses.size();
        int totPages  = totalPages();
        int dispStart = total == 0 ? 0 : start + 1;
        pageLabel.setText("Page " + currentPage + " of " + totPages
                + "  \u2014  " + dispStart + "\u2013" + end + " of " + total);

        // Clear selection when table reloads
        ledgerTable.clearSelection();
        setActionButtonsEnabled(false);

        if (filteredExpenses.isEmpty()) showEmptyState();
    }

    private void showEmptyState() {
    }

    private void updateKpi() {
        double totalAmt  = filteredExpenses.stream().mapToDouble(Expense::getAmount).sum();
        long   settled   = filteredExpenses.stream().filter(e -> computeStatus(e).equals("SETTLED")).count();
        double unsettAmt = filteredExpenses.stream()
                .filter(e -> !computeStatus(e).equals("SETTLED"))
                .mapToDouble(Expense::getAmount).sum();

        if (totalAmountLabel  != null) totalAmountLabel.setText(String.format("Rs. %,.2f", totalAmt));
        if (totalCountLabel   != null) totalCountLabel.setText(String.valueOf(filteredExpenses.size()));
        if (settledCountLabel != null) settledCountLabel.setText(String.valueOf(settled));
        if (unsettledAmtLabel != null) unsettledAmtLabel.setText(String.format("Rs. %,.2f", unsettAmt));
    }

    private String getCategoryName(int id) {
        if (id >= 1 && id <= Category.ALL_NAMES.length) return Category.ALL_NAMES[id - 1];
        if (id == 0 && Category.ALL_NAMES.length > 0)   return Category.ALL_NAMES[0];
        return "Other";
    }

    private String computeStatus(Expense ex) {
        double total = ex.getAmount();
        if (total <= 0) return "SETTLED";
        double settled = getSettledTotal(ex.getId(), ex.getPaidBy());
        if (settled >= total - 0.005) return "SETTLED";
        if (settled > 0)              return "PARTIAL";
        return "UNSETTLED";
    }

    private double getSettledTotal(int expenseId, int payerId) {
        String sql =
                "SELECT COALESCE(SUM(s.amount),0) FROM settlements s " +
                        "INNER JOIN expenses e ON e.group_id = s.group_id " +
                        "WHERE e.id = ? AND s.payee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            ps.setInt(2, payerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private void showEditDialog(Expense expense) {
        Window  owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg   = new JDialog(owner instanceof Frame f ? f : null, "Edit Expense", true);
        dlg.setSize(520, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(new Color(248, 250, 252));
        hdr.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_C),
                new EmptyBorder(18, 24, 16, 24)));

        JPanel ht = new JPanel();
        ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.setOpaque(false);
        JLabel tl = new JLabel("Edit Expense");
        tl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tl.setForeground(UiStyles.TEXT_PRIMARY);
        JLabel sl = new JLabel("Update the details below. Changes will recalculate splits.");
        sl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sl.setForeground(UiStyles.TEXT_LABEL);
        ht.add(tl); ht.add(Box.createVerticalStrut(3)); ht.add(sl);
        hdr.add(ht);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 28, 10, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 4, 8, 4);
        g.anchor = GridBagConstraints.WEST;
        g.fill   = GridBagConstraints.HORIZONTAL;

        JTextField descF = dlgField(expense.getDescription() != null ? expense.getDescription() : "");
        JTextField amtF  = dlgField(String.valueOf(expense.getAmount()));

        DatePickerField datePicker = new DatePickerField(280);
        datePicker.setPreferredSize(new Dimension(280, 36));
        if (expense.getDate() != null)
            datePicker.setDate(expense.getDate().toLocalDate());

        JComboBox<String> catCmb = new JComboBox<>();
        catCmb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        catCmb.setPreferredSize(new Dimension(280, 36));
        for (String c : Category.ALL_NAMES) catCmb.addItem(c);
        int ci = expense.getCategoryId() - 1;
        if (ci >= 0 && ci < Category.ALL_NAMES.length) catCmb.setSelectedIndex(ci);

        JComboBox<String> splitCmb = new JComboBox<>(new String[]{"EQUAL", "CUSTOM", "PERCENTAGE"});
        splitCmb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        splitCmb.setPreferredSize(new Dimension(280, 36));
        splitCmb.setSelectedItem(expense.getSplitType() != null ? expense.getSplitType() : "EQUAL");

        JPanel splitInfoPanel = new JPanel(new BorderLayout());
        splitInfoPanel.setOpaque(false);
        JLabel splitInfo = new JLabel("EQUAL: amount divided equally among all group members.");
        splitInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        splitInfo.setForeground(UiStyles.TEXT_MUTED);
        splitInfoPanel.add(splitInfo);
        splitCmb.addActionListener(e -> {
            String sel = (String) splitCmb.getSelectedItem();
            splitInfo.setText(switch (sel) {
                case "CUSTOM"     -> "CUSTOM: each member gets a manually defined share.";
                case "PERCENTAGE" -> "PERCENTAGE: each member pays a percent of the total.";
                default           -> "EQUAL: amount divided equally among all group members.";
            });
        });

        addRow(form, g, 0, "Description",  descF);
        addRow(form, g, 1, "Amount (Rs.)", amtF);
        addRow(form, g, 2, "Category",     catCmb);
        addRow(form, g, 3, "Split Type",   splitCmb);
        addRow(form, g, 4, "",             splitInfoPanel);
        addRow(form, g, 5, "Date",         datePicker);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnRow.setBackground(new Color(248, 250, 252));
        btnRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_C));

        JButton save = UiStyles.primaryButton("Save Changes", () -> {
            String desc   = descF.getText().trim();
            String amtStr = amtF.getText().trim();
            if (desc.isEmpty())                           { showErr(dlg, "Description cannot be empty."); return; }
            if (!InputValidator.isPositiveDouble(amtStr)) { showErr(dlg, "Enter a valid positive amount."); return; }
            String ds = datePicker.getDateString();
            expense.setDescription(desc);
            expense.setAmount(Double.parseDouble(amtStr));
            expense.setCategoryId(catCmb.getSelectedIndex() + 1);
            expense.setSplitType((String) splitCmb.getSelectedItem());
            if (!ds.isEmpty()) expense.setDate(Date.valueOf(ds));
            if (new ExpenseDAO().updateExpense(expense)) {
                JOptionPane.showMessageDialog(dlg, "Expense updated successfully.", "Updated", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                loadExpenses();
            } else {
                showErr(dlg, "Update failed. Please try again.");
            }
        });

        JButton cancel = UiStyles.outlineButton("Cancel", dlg::dispose);
        btnRow.add(cancel);
        btnRow.add(save);

        dlg.add(hdr,    BorderLayout.NORTH);
        dlg.add(form,   BorderLayout.CENTER);
        dlg.add(btnRow, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showViewDialog(Expense expense) {
        Window  owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg   = new JDialog(owner instanceof Frame f ? f : null, "Expense Details", true);
        dlg.setSize(460, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(new Color(248, 250, 252));
        hdr.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_C),
                new EmptyBorder(16, 24, 16, 24)));
        JLabel tl = new JLabel("Expense Details");
        tl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        tl.setForeground(UiStyles.TEXT_PRIMARY);
        hdr.add(tl);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 28, 10, 28));

        UserDAO    uDAO    = new UserDAO();
        GroupDAO   gDAO    = new GroupDAO();
        User       payer   = uDAO.getUserById(expense.getPaidBy());
        Group      grp     = userGroups.stream().filter(g -> g.getId() == expense.getGroupId()).findFirst().orElse(null);
        List<User> members = gDAO.getGroupMembers(expense.getGroupId());
        String     status  = computeStatus(expense);

        SimpleDateFormat df      = new SimpleDateFormat("EEEE, MMMM dd yyyy");
        String           dateStr = expense.getDate() != null ? df.format(expense.getDate()) : "N/A";

        Object[][] rows = {
                {"Description", expense.getDescription() != null ? expense.getDescription() : "\u2014"},
                {"Amount",      String.format("Rs. %,.2f", expense.getAmount())},
                {"Category",    getCategoryName(expense.getCategoryId())},
                {"Paid By",     payer != null ? payer.getName() : "Unknown"},
                {"Group",       grp   != null ? grp.getName()   : "\u2014"},
                {"Date",        dateStr},
                {"Split Type",  expense.getSplitType() != null ? expense.getSplitType() : "EQUAL"},
                {"Status",      status},
        };

        for (Object[] row : rows) {
            JPanel line = new JPanel(new BorderLayout());
            line.setOpaque(false);
            line.setBorder(new EmptyBorder(5, 0, 5, 0));
            line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            JLabel key = new JLabel((String) row[0] + ":");
            key.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            key.setForeground(UiStyles.TEXT_LABEL);
            key.setPreferredSize(new Dimension(120, 20));

            JLabel val = new JLabel((String) row[1]);
            val.setFont(new Font("Segoe UI", Font.BOLD, 13));
            val.setForeground(UiStyles.TEXT_PRIMARY);
            if ("Status".equals(row[0])) {
                val.setForeground(switch (status) {
                    case "SETTLED" -> SETTLED_FG;
                    case "PARTIAL" -> PARTIAL_FG;
                    default        -> UNSETTLED_FG;
                });
            }

            line.add(key, BorderLayout.WEST);
            line.add(val, BorderLayout.CENTER);
            body.add(line);

            JSeparator sep2 = new JSeparator();
            sep2.setForeground(new Color(240, 242, 247));
            sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            body.add(sep2);
        }

        if (!members.isEmpty()) {
            JPanel mLine = new JPanel(new BorderLayout());
            mLine.setOpaque(false);
            mLine.setBorder(new EmptyBorder(5, 0, 5, 0));
            mLine.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            JLabel mKey = new JLabel("Participants:");
            mKey.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            mKey.setForeground(UiStyles.TEXT_LABEL);
            mKey.setPreferredSize(new Dimension(120, 20));
            String mVal = members.stream().map(User::getName)
                    .reduce((a, b2) -> a + ", " + b2).orElse("\u2014");
            JLabel mValLbl = new JLabel(mVal);
            mValLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            mValLbl.setForeground(UiStyles.TEXT_PRIMARY);
            mLine.add(mKey,    BorderLayout.WEST);
            mLine.add(mValLbl, BorderLayout.CENTER);
            body.add(mLine);
        }

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnRow.setBackground(new Color(248, 250, 252));
        btnRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_C));
        btnRow.add(UiStyles.outlineButton("Close", dlg::dispose));

        dlg.add(hdr,    BorderLayout.NORTH);
        dlg.add(new JScrollPane(body) {{ setBorder(null); }}, BorderLayout.CENTER);
        dlg.add(btnRow, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void deleteExpense(Expense expense) {
        String msg = "<html>Delete expense <b>\"" +
                (expense.getDescription() != null ? expense.getDescription() : "Untitled") +
                "\"</b>?<br><small>This cannot be undone. All split records will also be removed.</small></html>";
        int r = JOptionPane.showConfirmDialog(this, msg, "Confirm Deletion",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            if (new ExpenseDAO().deleteExpense(expense.getId())) {
                JOptionPane.showMessageDialog(this, "Expense deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadExpenses();
            } else {
                JOptionPane.showMessageDialog(this, "Delete failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("ledger_export.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (java.io.PrintWriter pw = new java.io.PrintWriter(fc.getSelectedFile())) {
            pw.println("Date,Description,Category,Amount,Payer,Group,SplitType,Status");
            SimpleDateFormat df   = new SimpleDateFormat("yyyy-MM-dd");
            UserDAO          uDAO = new UserDAO();
            for (Expense ex : filteredExpenses) {
                User  payer = uDAO.getUserById(ex.getPaidBy());
                Group grp   = userGroups.stream()
                        .filter(g -> g.getId() == ex.getGroupId()).findFirst().orElse(null);
                pw.printf("%s,%s,%s,%.2f,%s,%s,%s,%s%n",
                        ex.getDate() != null ? df.format(ex.getDate()) : "",
                        (ex.getDescription() != null ? ex.getDescription() : "").replace(",", ";"),
                        getCategoryName(ex.getCategoryId()),
                        ex.getAmount(),
                        payer != null ? payer.getName() : "Unknown",
                        grp   != null ? grp.getName()   : "\u2014",
                        ex.getSplitType() != null ? ex.getSplitType() : "EQUAL",
                        computeStatus(ex));
            }
            JOptionPane.showMessageDialog(this,
                    "Exported " + filteredExpenses.size() + " records.", "Done", JOptionPane.INFORMATION_MESSAGE);
        } catch (java.io.IOException e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Toolbar action button — full text, no clipping issues since space is not constrained
    private static JButton toolbarBtn(String text, Color fg, Color bg, Color border) {
        JButton b = new JButton(text) {
            boolean hov = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                if (!isEnabled()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(241, 245, 249));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new Color(203, 213, 225));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                    return;
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(fg);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setPreferredSize(new Dimension(72, 30));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static void showErr(Component p, String msg) {
        JOptionPane.showMessageDialog(p, msg, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    private static void addRow(JPanel p, GridBagConstraints g, int row, String txt, JComponent field) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        if (!txt.isEmpty()) {
            JLabel lbl = new JLabel(txt + ":");
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setForeground(UiStyles.TEXT_SECONDARY);
            lbl.setPreferredSize(new Dimension(140, 22));
            p.add(lbl, g);
        } else {
            p.add(new JLabel(""), g);
        }
        g.gridx = 1; g.weightx = 1.0;
        p.add(field, g);
    }

    private JTextField dlgField(String val) {
        JTextField f = new JTextField(val);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(280, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(209, 213, 219), 1, true),
                new EmptyBorder(4, 10, 4, 10)));
        return f;
    }

    private JComboBox<String> styledCombo(int width) {
        JComboBox<String> c = new JComboBox<>();
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        c.setPreferredSize(new Dimension(width, 32));
        return c;
    }

    private JLabel filterLbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(UiStyles.TEXT_SECONDARY);
        return l;
    }

    private JSeparator sep() {
        JSeparator s = new JSeparator(SwingConstants.VERTICAL);
        s.setPreferredSize(new Dimension(1, 28));
        s.setForeground(BORDER_C);
        return s;
    }

    private JButton navBtn(String txt) {
        JButton b = new JButton(txt);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setPreferredSize(new Dimension(32, 28));
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(BORDER_C));
        b.setBackground(Color.WHITE);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton buildExportBtn() {
        JButton b = new JButton("Export CSV") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(236, 253, 245) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(UiStyles.ACCENT_PRIMARY);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(UiStyles.ACCENT_PRIMARY);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(7, 14, 7, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> exportCSV());
        return b;
    }

    // ─── Inner Renderers ─────────────────────────────────────────────────────

    static class RowNumRenderer extends DefaultTableCellRenderer {
        RowNumRenderer() { setHorizontalAlignment(CENTER); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                                                                 boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            setFont(new Font("Segoe UI", Font.PLAIN, 11));
            setForeground(new Color(148, 163, 184));
            return this;
        }
    }

    static class AmountRenderer extends DefaultTableCellRenderer {
        AmountRenderer() { setHorizontalAlignment(RIGHT); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                                                                 boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(new Color(15, 23, 42));
            setBorder(new EmptyBorder(0, 0, 0, 12));
            return this;
        }
    }

    static class BadgeRenderer implements TableCellRenderer {
        private final boolean isStatus;
        BadgeRenderer(boolean s) { this.isStatus = s; }

        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                                                                 boolean sel, boolean foc, int row, int col) {
            String val  = v != null ? v.toString() : "";
            JPanel wrap = new JPanel(new GridBagLayout());
            wrap.setOpaque(true);
            wrap.setBackground(sel ? ROW_SEL : (row % 2 == 0 ? Color.WHITE : ROW_ALT));
            wrap.add(new RoundedBadge(val, isStatus));
            return wrap;
        }
    }

    static class RoundedBadge extends JPanel {
        private final String text;
        private final Color  fg, bg;

        RoundedBadge(String text, boolean isStatus) {
            this.text = text;
            setOpaque(false);
            setPreferredSize(new Dimension(82, 24));
            if (isStatus) {
                switch (text) {
                    case "SETTLED" -> { fg = SETTLED_FG;   bg = SETTLED_BG; }
                    case "PARTIAL" -> { fg = PARTIAL_FG;   bg = PARTIAL_BG; }
                    default        -> { fg = UNSETTLED_FG; bg = UNSETTLED_BG; }
                }
            } else {
                switch (text) {
                    case "CUSTOM"     -> { fg = CUSTOM_FG; bg = CUSTOM_BG; }
                    case "PERCENTAGE" -> { fg = PCT_FG;    bg = PCT_BG; }
                    default           -> { fg = EQUAL_FG;  bg = EQUAL_BG; }
                }
            }
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, h, h);
            g2.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 70));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.setColor(fg);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, (w - fm.stringWidth(text)) / 2,
                    (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}