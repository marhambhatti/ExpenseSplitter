package com.expensesplitter.ui;

import com.expensesplitter.util.Session;
import com.expensesplitter.dao.GroupDAO;
import com.expensesplitter.dao.ExpenseDAO;
import com.expensesplitter.dao.SettlementDAO;
import com.expensesplitter.layouts.WrapLayout;
import com.expensesplitter.models.Expense;
import com.expensesplitter.models.Group;
import com.expensesplitter.models.Settlement;
import com.expensesplitter.models.User;
import com.expensesplitter.validation.InputValidator;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
// Group Frame
public class GroupFrame extends JPanel {

    private JPanel gridPanel;
    private JLabel emptyLabel;
    private JLabel groupCountLabel;
    private JLabel totalOwedLabel;
    private JLabel totalOwingLabel;
    private JTextField searchField;
    private JComboBox<String> sortCombo;
    private JScrollPane scrollPane;

    private List<Group> allGroups = new ArrayList<>();

    private static final Color BG_PAGE        = new Color(248, 250, 252);
    private static final Color CARD_WHITE      = Color.WHITE;
    private static final Color BORDER_LIGHT    = new Color(203, 213, 225);
    private static final Color TEXT_DARK       = new Color(15, 23, 42);
    private static final Color TEXT_MUTED      = new Color(100, 116, 139);
    private static final Color ACCENT_BLUE     = new Color(37, 99, 235);
    private static final Color ACCENT_GREEN    = new Color(5, 150, 105);
    private static final Color ACCENT_RED      = new Color(220, 38, 38);
    private static final Color STAT_BG_BLUE    = new Color(239, 246, 255);
    private static final Color STAT_BG_GREEN   = new Color(240, 253, 244);
    private static final Color STAT_BG_RED     = new Color(254, 242, 242);
    private static final Color STAT_BG_PURPLE  = new Color(245, 243, 255);
    private static final Color ACCENT_PURPLE   = new Color(124, 58, 237);

    private final GroupDAO    groupDAO    = new GroupDAO();
    private final ExpenseDAO  expenseDAO  = new ExpenseDAO();
    private final SettlementDAO settlementDAO = new SettlementDAO();

    public GroupFrame() {
        setLayout(new BorderLayout());
        setOpaque(false);
        add(buildMainContent(), BorderLayout.CENTER);
        refreshGroups();
    }

    private JPanel buildMainContent() {
        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(28, 30, 30, 30));

        main.add(buildTopSection(),    BorderLayout.NORTH);
        main.add(buildScrollArea(),    BorderLayout.CENTER);
        return main;
    }

    private JPanel buildTopSection() {
        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 16, 0));

        top.add(buildTitleRow(),  BorderLayout.NORTH);
        top.add(buildStatsRow(),  BorderLayout.CENTER);
        top.add(buildToolbar(),   BorderLayout.SOUTH);
        return top;
    }

    private JPanel buildTitleRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JPanel titleArea = new JPanel();
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));
        titleArea.setOpaque(false);

        JLabel title = new JLabel("My Groups");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_DARK);

        JPanel subtitleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        subtitleRow.setOpaque(false);
        groupCountLabel = new JLabel("0 groups");
        groupCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        groupCountLabel.setForeground(TEXT_MUTED);
        subtitleRow.add(groupCountLabel);

        titleArea.add(title);
        titleArea.add(Box.createVerticalStrut(3));
        titleArea.add(subtitleRow);

        JButton createBtn = buildPrimaryButton("+ Create Group", ACCENT_BLUE);
        createBtn.addActionListener(e -> showCreateGroupDialog());

        row.add(titleArea, BorderLayout.WEST);
        row.add(createBtn, BorderLayout.EAST);
        return row;
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);

        totalOwedLabel  = new JLabel("Rs. 0");
        totalOwingLabel = new JLabel("Rs. 0");
        JLabel netLabel = new JLabel("Rs. 0");
        JLabel grpLabel = new JLabel("0");

        row.add(buildStatCard("OWED TO YOU",  totalOwedLabel,  FontAwesomeSolid.ARROW_DOWN,  ACCENT_GREEN,  STAT_BG_GREEN));
        row.add(buildStatCard("YOU OWE",      totalOwingLabel, FontAwesomeSolid.ARROW_UP,    ACCENT_RED,    STAT_BG_RED));
        row.add(buildStatCard("NET BALANCE",  netLabel,        FontAwesomeSolid.WALLET,      ACCENT_BLUE,   STAT_BG_BLUE));
        row.add(buildStatCard("TOTAL GROUPS", grpLabel,        FontAwesomeSolid.LAYER_GROUP, ACCENT_PURPLE, STAT_BG_PURPLE));
        return row;
    }

    private JPanel buildStatCard(String label, JLabel valueLabel, org.kordamp.ikonli.Ikon icon, Color fg, Color bg) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 40), 1),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        top.setOpaque(false);
        top.add(new JLabel(FontIcon.of(icon, 13, fg)));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(TEXT_MUTED);
        top.add(lbl);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(fg);

        card.add(top,        BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);

        JPanel searchWrap = new JPanel(new BorderLayout(8, 0));
        searchWrap.setBackground(CARD_WHITE);
        searchWrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(6, 10, 6, 10)));
        searchWrap.add(new JLabel(FontIcon.of(FontAwesomeSolid.SEARCH, 13, TEXT_MUTED)), BorderLayout.WEST);

        searchField = new JTextField();
        searchField.setBorder(null);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBackground(CARD_WHITE);
        searchField.putClientProperty("JTextField.placeholderText", "Search groups...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { filterAndRender(); }
            public void removeUpdate(DocumentEvent e)  { filterAndRender(); }
            public void changedUpdate(DocumentEvent e) { filterAndRender(); }
        });
        searchWrap.add(searchField, BorderLayout.CENTER);

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBar.setOpaque(false);

        JLabel sortLbl = new JLabel("Sort:");
        sortLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sortLbl.setForeground(TEXT_MUTED);

        sortCombo = new JComboBox<>(new String[]{"Default", "Name A-Z", "Name Z-A", "Balance High", "Balance Low", "Members"});
        sortCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sortCombo.addActionListener(e -> filterAndRender());

        JButton refreshBtn = buildIconButton(FontAwesomeSolid.SYNC_ALT, "Refresh", TEXT_MUTED);
        refreshBtn.addActionListener(e -> {
            animateRefreshIcon(refreshBtn);
            refreshGroups();
        });

        rightBar.add(sortLbl);
        rightBar.add(sortCombo);
        rightBar.add(refreshBtn);

        bar.add(searchWrap, BorderLayout.CENTER);
        bar.add(rightBar,   BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildScrollArea() {
        gridPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 16, 16));
        gridPanel.setOpaque(false);

        emptyLabel = new JLabel("No groups yet. Click \"+ Create Group\" to get started.", SwingConstants.CENTER);
        emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        emptyLabel.setForeground(TEXT_MUTED);
        emptyLabel.setBorder(new EmptyBorder(60, 0, 60, 0));
        emptyLabel.setVisible(false);

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.add(emptyLabel, BorderLayout.NORTH);
        gridWrapper.add(gridPanel,  BorderLayout.CENTER);

        scrollPane = new JScrollPane(gridWrapper);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(BG_PAGE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getVerticalScrollBar().setBlockIncrement(80);

        scrollPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"),    "scrollUp");
        scrollPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"),  "scrollDown");
        scrollPane.getActionMap().put("scrollUp",   new AbstractAction() { public void actionPerformed(ActionEvent e) { scrollBy(-40); } });
        scrollPane.getActionMap().put("scrollDown", new AbstractAction() { public void actionPerformed(ActionEvent e) { scrollBy(40);  } });

        return scrollPane;
    }

    private void scrollBy(int delta) {
        JScrollBar bar = scrollPane.getVerticalScrollBar();
        bar.setValue(bar.getValue() + delta);
    }

    public void refreshGroups() {
        User user = Session.getCurrentUser();
        if (user == null) {
            allGroups = new ArrayList<>();
            updateStats();
            filterAndRender();
            return;
        }
        allGroups = groupDAO.getGroupsByUser(user.getId());
        updateStats();
        filterAndRender();
    }

    private void updateStats() {
        User user = Session.getCurrentUser();
        if (user == null) {
            totalOwedLabel.setText("Rs. 0");
            totalOwingLabel.setText("Rs. 0");
            groupCountLabel.setText("0 groups");
            return;
        }

        double totalOwed = 0, totalOwing = 0;
        for (Group g : allGroups) {
            double bal = computeBalance(g);
            if (bal > 0) totalOwed  += bal;
            else         totalOwing += Math.abs(bal);
        }

        double net = totalOwed - totalOwing;
        totalOwedLabel.setText("Rs. " + String.format("%.0f", totalOwed));
        totalOwingLabel.setText("Rs. " + String.format("%.0f", totalOwing));
        groupCountLabel.setText(allGroups.size() + (allGroups.size() == 1 ? " group" : " groups"));

        Component[] statCards = ((JPanel) totalOwedLabel.getParent().getParent()).getComponents();
        if (statCards.length >= 3) {
            JLabel netLbl = findValueLabel((JPanel) statCards[2]);
            if (netLbl != null) {
                netLbl.setText((net >= 0 ? "+" : "") + "Rs. " + String.format("%.0f", Math.abs(net)));
                netLbl.setForeground(net >= 0 ? ACCENT_GREEN : ACCENT_RED);
            }
            JLabel grpLbl = findValueLabel((JPanel) statCards[3]);
            if (grpLbl != null) grpLbl.setText(String.valueOf(allGroups.size()));
        }
    }

    private JLabel findValueLabel(JPanel card) {
        for (Component c : card.getComponents()) {
            if (c instanceof JLabel lbl && !(lbl.getFont().getSize() == 10)) return lbl;
        }
        return null;
    }

    private void filterAndRender() {
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        String sort  = sortCombo  != null ? (String) sortCombo.getSelectedItem() : "Default";
        User user    = Session.getCurrentUser();

        List<Group> filtered = allGroups.stream()
                .filter(g -> query.isEmpty() || g.getName().toLowerCase().contains(query))
                .collect(Collectors.toList());

        if ("Name A-Z".equals(sort))     filtered.sort(Comparator.comparing(Group::getName));
        else if ("Name Z-A".equals(sort)) filtered.sort(Comparator.comparing(Group::getName).reversed());
        else if ("Balance High".equals(sort)) filtered.sort((a, b) -> Double.compare(computeBalance(b), computeBalance(a)));
        else if ("Balance Low".equals(sort))  filtered.sort(Comparator.comparingDouble(this::computeBalance));
        else if ("Members".equals(sort))      filtered.sort((a, b) -> Integer.compare(groupDAO.getMemberCount(b.getId()), groupDAO.getMemberCount(a.getId())));

        gridPanel.removeAll();
        emptyLabel.setVisible(filtered.isEmpty() && !query.isEmpty());

        if (filtered.isEmpty() && query.isEmpty()) {
            emptyLabel.setVisible(true);
        }

        for (Group group : filtered) {
            int members = groupDAO.getMemberCount(group.getId());
            gridPanel.add(buildGroupCard(group, members));
        }

        if (query.isEmpty()) gridPanel.add(buildCreateCard());

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private double computeBalance(Group group) {
        User user = Session.getCurrentUser();
        if (user == null) return 0;
        int memberCount = groupDAO.getMemberCount(group.getId());
        List<Expense> expenses = expenseDAO.getExpensesByGroup(group.getId());
        double balance = 0;
        for (Expense e : expenses) {
            if (e.getPaidBy() == user.getId()) balance += e.getAmount();
            else { int s = memberCount > 1 ? memberCount - 1 : 1; balance -= e.getAmount() / s; }
        }
        return balance;
    }

    private int computeSettlementPct(Group group) {
        List<Expense> expenses = expenseDAO.getExpensesByGroup(group.getId());
        if (expenses.isEmpty()) return 100;
        double total   = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double settled = settlementDAO.getSettlementsByGroup(group.getId()).stream().mapToDouble(Settlement::getAmount).sum();
        return settled >= total ? 100 : (int)((settled / total) * 100);
    }

    private JPanel buildGroupCard(Group group, int memberCount) {
        double balance = computeBalance(group);
        int    pct     = computeSettlementPct(group);

        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        card.setPreferredSize(new Dimension(310, 170));
        card.setMinimumSize(new Dimension(280, 170));
        card.setMaximumSize(new Dimension(320, 170));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER_LIGHT, 1, 12),
                new EmptyBorder(14, 16, 14, 16)));

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { card.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(ACCENT_BLUE, 1, 12), new EmptyBorder(14, 16, 14, 16))); }
            @Override public void mouseExited(MouseEvent e)  { card.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(BORDER_LIGHT, 1, 12), new EmptyBorder(14, 16, 14, 16))); }
        });

        String initials = group.getName().length() >= 2
                ? group.getName().substring(0, 2).toUpperCase()
                : group.getName().substring(0, 1).toUpperCase();

        Color[][] palette = {
                {new Color(0, 119, 212),   Color.WHITE,              new Color(0, 100, 190)},
                {new Color(124, 58, 237),  Color.WHITE,              new Color(109, 40, 217)},
                {new Color(191, 100, 14),  Color.WHITE,              new Color(170, 80, 10)},
                {new Color(5, 150, 105),   Color.WHITE,              new Color(4, 120, 87)},
                {new Color(220, 38, 38),   Color.WHITE,              new Color(185, 28, 28)},
        };
        Color[] p = palette[Math.abs(group.getName().hashCode()) % palette.length];
        Color avatarBg = p[0], avatarFg = p[1], avatarBorder = p[2];

        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(avatarBg);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.setColor(avatarBorder);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.setColor(avatarFg);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials, (getWidth()-fm.stringWidth(initials))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(44, 44));
        avatar.setOpaque(false);

        JLabel nameLbl = new JLabel(group.getName());
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLbl.setForeground(TEXT_DARK);

        JLabel membersLbl = new JLabel(memberCount + (memberCount == 1 ? " member" : " members"));
        membersLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        membersLbl.setForeground(TEXT_MUTED);
        membersLbl.setIcon(FontIcon.of(FontAwesomeSolid.USERS, 11, TEXT_MUTED));
        membersLbl.setIconTextGap(5);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);
        info.add(nameLbl);
        info.add(membersLbl);

        JPanel topInfoPanel = new JPanel(new BorderLayout(12, 0));
        topInfoPanel.setOpaque(false);
        topInfoPanel.add(avatar, BorderLayout.WEST);
        topInfoPanel.add(info,   BorderLayout.CENTER);

        JLabel menuIcon = new JLabel(FontIcon.of(FontAwesomeSolid.ELLIPSIS_V, 16, TEXT_MUTED));
        menuIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        menuIcon.setBorder(new EmptyBorder(0, 10, 0, 0));

        JPopupMenu popup = new JPopupMenu();
        addPopupItem(popup, "View Details",    FontAwesomeSolid.EYE,       new Color(37, 99, 235),  e -> showGroupDetailsDialog(group));
        addPopupItem(popup, "Manage Members",  FontAwesomeSolid.USER_COG,  new Color(60, 70, 85),   e -> showManageGroupDialog(group));
        addPopupItem(popup, "Add Expense",     FontAwesomeSolid.PLUS,      ACCENT_GREEN,            e -> showAddExpenseDialog(group));
        popup.addSeparator();
        addPopupItem(popup, "Rename Group",    FontAwesomeSolid.EDIT,      TEXT_MUTED,              e -> showRenameGroupDialog(group));
        addPopupItem(popup, "Delete Group",    FontAwesomeSolid.TRASH_ALT, ACCENT_RED,              e -> deleteGroup(group));

        menuIcon.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { popup.show(menuIcon, e.getX() - 120, e.getY()); }
        });

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(topInfoPanel, BorderLayout.CENTER);
        topSection.add(menuIcon,     BorderLayout.EAST);
        card.add(topSection, BorderLayout.NORTH);

        JPanel divider = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(226, 232, 240));
                g.drawLine(0, getHeight()/2, getWidth(), getHeight()/2);
            }
        };
        divider.setPreferredSize(new Dimension(100, 20));
        divider.setOpaque(false);
        card.add(divider, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 4));
        bottom.setOpaque(false);

        JPanel balanceRow = new JPanel(new BorderLayout());
        balanceRow.setOpaque(false);

        JPanel balanceLeft = new JPanel(new GridLayout(2, 1, 0, 2));
        balanceLeft.setOpaque(false);

        JLabel balTitle = new JLabel("Your Balance");
        balTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        balTitle.setForeground(TEXT_MUTED);
        balTitle.setIcon(FontIcon.of(FontAwesomeSolid.WALLET, 10, TEXT_MUTED));
        balTitle.setIconTextGap(4);

        JLabel balAmt = new JLabel();
        balAmt.setFont(new Font("Consolas", Font.BOLD, 14));

        balanceLeft.add(balTitle);
        balanceLeft.add(balAmt);

        String badgeText; Color badgeBg, badgeFg; FontIcon badgeIcon;
        if (balance < -0.01) {
            balAmt.setText(String.format("-Rs. %.0f", Math.abs(balance)));
            balAmt.setForeground(ACCENT_RED);
            badgeText = "YOU OWE"; badgeBg = new Color(254, 226, 226); badgeFg = new Color(185, 28, 28);
            badgeIcon = FontIcon.of(FontAwesomeSolid.ARROW_UP, 9, badgeFg);
        } else if (balance > 0.01) {
            balAmt.setText(String.format("+Rs. %.0f", balance));
            balAmt.setForeground(ACCENT_GREEN);
            badgeText = "OWED TO YOU"; badgeBg = new Color(209, 250, 229); badgeFg = new Color(4, 120, 87);
            badgeIcon = FontIcon.of(FontAwesomeSolid.ARROW_DOWN, 9, badgeFg);
        } else {
            balAmt.setText("Rs. 0");
            balAmt.setForeground(TEXT_MUTED);
            badgeText = "SETTLED UP"; badgeBg = new Color(226, 232, 240); badgeFg = new Color(71, 85, 105);
            badgeIcon = FontIcon.of(FontAwesomeSolid.CHECK, 9, badgeFg);
        }

        final Color fBg = badgeBg, fFg = badgeFg;
        JLabel badge = new JLabel(badgeText, badgeIcon, SwingConstants.LEFT) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setForeground(fFg);
        badge.setIconTextGap(4);
        badge.setBorder(new EmptyBorder(4, 8, 4, 8));

        JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgeWrap.setOpaque(false);
        badgeWrap.add(badge);

        balanceRow.add(balanceLeft, BorderLayout.WEST);
        balanceRow.add(badgeWrap,   BorderLayout.EAST);

        JPanel progressRow = new JPanel(new BorderLayout(0, 3));
        progressRow.setOpaque(false);
        JLabel pctLbl = new JLabel("Settled " + pct + "%");
        pctLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        pctLbl.setForeground(TEXT_MUTED);
        JPanel progressBar = buildProgressBar(pct, pct == 100 ? ACCENT_GREEN : (balance < 0 ? ACCENT_RED : ACCENT_BLUE));
        progressRow.add(pctLbl,     BorderLayout.NORTH);
        progressRow.add(progressBar, BorderLayout.SOUTH);

        bottom.add(balanceRow,  BorderLayout.CENTER);
        bottom.add(progressRow, BorderLayout.SOUTH);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildProgressBar(int pct, Color fillColor) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(226, 232, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                int fw = (int)(getWidth() * pct / 100.0);
                if (fw > 0) { g2.setColor(fillColor); g2.fillRoundRect(0, 0, fw, getHeight(), 4, 4); }
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(super.getPreferredSize().width, 5); }
        };
    }

    private JPanel buildCreateCard() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(sortCombo.getModel() != null && ((JPanel)this).getMousePosition() != null
                        ? new Color(248, 250, 252) : CARD_WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                float[] dash = {6f, 4f};
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, dash, 0f));
                g2.setColor(new Color(180, 190, 200));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 12, 12));
                g2.dispose();
            }
        };
        card.setPreferredSize(new Dimension(310, 170));
        card.setMinimumSize(new Dimension(280, 170));
        card.setOpaque(false);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        JLabel icon = new JLabel(FontIcon.of(FontAwesomeSolid.PLUS_CIRCLE, 30, new Color(150, 160, 175)));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel text = new JLabel("Create New Group");
        text.setFont(new Font("Segoe UI", Font.BOLD, 13));
        text.setForeground(new Color(150, 160, 175));
        text.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(icon);
        inner.add(Box.createVerticalStrut(8));
        inner.add(text);
        card.add(inner);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showCreateGroupDialog(); }
            @Override public void mouseEntered(MouseEvent e) { card.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { card.repaint(); }
        });
        return card;
    }

    private void showCreateGroupDialog() {
        User user = Session.getCurrentUser();
        if (user == null) {
            showError("Please log in first.");
            return;
        }

        JDialog dialog = buildStyledDialog("Create New Group", 420, 200);
        JPanel body = buildDialogBody(dialog);

        JLabel titleLbl = dialogTitle("Create New Group");
        JTextField nameField = buildDialogField("Group Name", "e.g. Trip to Lahore");

        JButton create = buildPrimaryButton("Create Group", ACCENT_BLUE);
        JButton cancel = buildSecondaryButton("Cancel");
        cancel.addActionListener(e -> dialog.dispose());

        create.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (InputValidator.isNullOrEmpty(name)) { showFieldError(nameField, "Name cannot be empty."); return; }
            if (name.length() > 80)                 { showFieldError(nameField, "Name too long (max 80 chars)."); return; }
            if (groupDAO.groupNameExists(name, user.getId())) { showFieldError(nameField, "You already have a group with this name."); return; }

            if (groupDAO.createGroup(new Group(name, user.getId()))) {
                dialog.dispose();
                showSuccess("Group \"" + name + "\" created!");
                refreshGroups();
            } else {
                showError("Failed to create group. Try again.");
            }
        });

        nameField.addActionListener(e -> create.doClick());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(cancel);
        btnRow.add(create);

        body.add(titleLbl);
        body.add(Box.createVerticalStrut(16));
        body.add(nameField);
        body.add(Box.createVerticalStrut(20));
        body.add(btnRow);

        dialog.add(body);
        dialog.setVisible(true);
        nameField.requestFocusInWindow();
    }

    private void showRenameGroupDialog(Group group) {
        User user = Session.getCurrentUser();
        if (user == null) return;

        JDialog dialog = buildStyledDialog("Rename Group", 420, 200);
        JPanel body = buildDialogBody(dialog);

        JLabel titleLbl = dialogTitle("Rename: " + group.getName());
        JTextField nameField = buildDialogField("New Group Name", group.getName());
        nameField.setText(group.getName());

        JButton save   = buildPrimaryButton("Save", ACCENT_BLUE);
        JButton cancel = buildSecondaryButton("Cancel");
        cancel.addActionListener(e -> dialog.dispose());

        save.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (InputValidator.isNullOrEmpty(name)) { showFieldError(nameField, "Name cannot be empty."); return; }
            if (name.equals(group.getName()))       { dialog.dispose(); return; }
            if (groupDAO.groupNameExists(name, user.getId())) { showFieldError(nameField, "You already have a group with this name."); return; }

            if (groupDAO.renameGroup(group.getId(), name)) {
                dialog.dispose();
                showSuccess("Group renamed to \"" + name + "\".");
                refreshGroups();
            } else {
                showError("Failed to rename. Try again.");
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(cancel);
        btnRow.add(save);

        body.add(titleLbl);
        body.add(Box.createVerticalStrut(16));
        body.add(nameField);
        body.add(Box.createVerticalStrut(20));
        body.add(btnRow);

        dialog.add(body);
        dialog.setVisible(true);
    }

    private void showGroupDetailsDialog(Group group) {
        JDialog dialog = buildStyledDialog("Group Details — " + group.getName(), 520, 440);
        JPanel body = buildDialogBody(dialog);
        body.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel titleLbl = dialogTitle(group.getName());

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 10, 0));
        statsRow.setOpaque(false);
        int members = groupDAO.getMemberCount(group.getId());
        double balance = computeBalance(group);
        int pct = computeSettlementPct(group);

        statsRow.add(buildMiniStat("Members",    String.valueOf(members),                    ACCENT_BLUE));
        statsRow.add(buildMiniStat("Balance",    "Rs. " + String.format("%.0f", Math.abs(balance)), balance >= 0 ? ACCENT_GREEN : ACCENT_RED));
        statsRow.add(buildMiniStat("Settled",    pct + "%",                                  pct == 100 ? ACCENT_GREEN : ACCENT_BLUE));

        JLabel membersTitle = new JLabel("Members");
        membersTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        membersTitle.setForeground(TEXT_DARK);

        JPanel membersList = buildMembersListPanel(group.getId());
        JScrollPane membersScroll = new JScrollPane(membersList);
        membersScroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        membersScroll.setPreferredSize(new Dimension(460, 140));
        membersScroll.getVerticalScrollBar().setUnitIncrement(16);

        JLabel expTitle = new JLabel("Recent Expenses");
        expTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        expTitle.setForeground(TEXT_DARK);
        expTitle.setBorder(new EmptyBorder(10, 0, 4, 0));

        JPanel expList = buildRecentExpensesPanel(group.getId());
        JScrollPane expScroll = new JScrollPane(expList);
        expScroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        expScroll.setPreferredSize(new Dimension(460, 100));
        expScroll.getVerticalScrollBar().setUnitIncrement(16);

        JButton close = buildSecondaryButton("Close");
        close.addActionListener(e -> dialog.dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false);
        btnRow.add(close);

        body.add(titleLbl);
        body.add(Box.createVerticalStrut(12));
        body.add(statsRow);
        body.add(Box.createVerticalStrut(14));
        body.add(membersTitle);
        body.add(Box.createVerticalStrut(6));
        body.add(membersScroll);
        body.add(expTitle);
        body.add(expScroll);
        body.add(Box.createVerticalStrut(10));
        body.add(btnRow);

        dialog.add(body);
        dialog.setVisible(true);
    }

    private JPanel buildMembersListPanel(int groupId) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_WHITE);
        List<User> members = groupDAO.getGroupMembers(groupId);
        if (members.isEmpty()) {
            JLabel lbl = new JLabel("No members.");
            lbl.setBorder(new EmptyBorder(10, 12, 10, 12));
            panel.add(lbl);
        }
        for (User m : members) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(CARD_WHITE);
            row.setBorder(new EmptyBorder(8, 12, 8, 12));
            JLabel name = new JLabel(m.getName());
            name.setFont(new Font("Segoe UI", Font.BOLD, 13));
            JLabel email = new JLabel(m.getEmail());
            email.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            email.setForeground(TEXT_MUTED);
            row.add(name,  BorderLayout.WEST);
            row.add(email, BorderLayout.EAST);
            row.add(new JSeparator(), BorderLayout.SOUTH);
            panel.add(row);
        }
        return panel;
    }

    private JPanel buildRecentExpensesPanel(int groupId) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_WHITE);
        List<Expense> expenses = expenseDAO.getExpensesByGroup(groupId);
        if (expenses.isEmpty()) {
            JLabel lbl = new JLabel("No expenses yet.");
            lbl.setBorder(new EmptyBorder(10, 12, 10, 12));
            panel.add(lbl);
        }
        int limit = Math.min(5, expenses.size());
        for (int i = 0; i < limit; i++) {
            Expense ex = expenses.get(i);
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(CARD_WHITE);
            row.setBorder(new EmptyBorder(6, 12, 6, 12));
            JLabel desc = new JLabel(ex.getDescription());
            desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JLabel amt = new JLabel("Rs. " + String.format("%.0f", ex.getAmount()));
            amt.setFont(new Font("Segoe UI", Font.BOLD, 13));
            amt.setForeground(ACCENT_BLUE);
            String dateStr = ex.getDate() != null ? ex.getDate().toString() : "";
            JLabel dateLbl = new JLabel(dateStr);
            dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            dateLbl.setForeground(TEXT_MUTED);
            JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            rightPanel.setOpaque(false);
            rightPanel.add(amt);
            rightPanel.add(dateLbl);
            row.add(desc, BorderLayout.WEST);
            row.add(rightPanel, BorderLayout.EAST);
            row.add(new JSeparator(), BorderLayout.SOUTH);
            panel.add(row);
        }
        return panel;
    }

    private void showManageGroupDialog(Group group) {
        JDialog dialog = buildStyledDialog("Manage Members — " + group.getName(), 500, 420);
        JPanel body = buildDialogBody(dialog);

        JLabel titleLbl = dialogTitle("Members of " + group.getName());

        JTextArea membersArea = new JTextArea(getMembersText(group.getId()));
        membersArea.setEditable(false);
        membersArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        membersArea.setBackground(new Color(248, 250, 252));
        membersArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane scroll = new JScrollPane(membersArea);
        scroll.setPreferredSize(new Dimension(440, 160));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JTextField emailField = buildDialogField("Add member by email", "user@example.com");
        JButton addBtn = buildPrimaryButton("Add Member", ACCENT_GREEN);
        addBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            if (InputValidator.isNullOrEmpty(email)) { showFieldError(emailField, "Enter an email."); return; }
            addMemberByEmail(group.getId(), email, membersArea);
            emailField.setText("");
            refreshGroups();
        });
        emailField.addActionListener(e -> addBtn.doClick());

        JPanel addRow = new JPanel(new BorderLayout(8, 0));
        addRow.setOpaque(false);
        addRow.add(emailField, BorderLayout.CENTER);
        addRow.add(addBtn,     BorderLayout.EAST);

        JButton close = buildSecondaryButton("Close");
        close.addActionListener(e -> dialog.dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false);
        btnRow.add(close);

        body.add(titleLbl);
        body.add(Box.createVerticalStrut(12));
        body.add(scroll);
        body.add(Box.createVerticalStrut(12));
        body.add(addRow);
        body.add(Box.createVerticalStrut(12));
        body.add(btnRow);

        dialog.add(body);
        dialog.setVisible(true);
    }

    private void showAddExpenseDialog(Group group) {
        JDialog dialog = buildStyledDialog("Add Expense — " + group.getName(), 440, 340);
        JPanel body = buildDialogBody(dialog);

        JLabel titleLbl = dialogTitle("New Expense");

        JTextField descField = buildDialogField("Description", "e.g. Dinner at cafe");
        JTextField amtField  = buildDialogField("Amount (Rs.)", "0.00");

        JLabel splitLbl = new JLabel("Split Type");
        splitLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        splitLbl.setForeground(TEXT_MUTED);
        splitLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> splitCombo = new JComboBox<>(new String[]{"EQUAL", "PERCENTAGE", "EXACT"});
        splitCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        splitCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        splitCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton add    = buildPrimaryButton("Add Expense", ACCENT_GREEN);
        JButton cancel = buildSecondaryButton("Cancel");
        cancel.addActionListener(e -> dialog.dispose());

        add.addActionListener(e -> {
            String desc   = descField.getText().trim();
            String amtTxt = amtField.getText().trim();
            if (InputValidator.isNullOrEmpty(desc)) { showFieldError(descField, "Description required."); return; }
            double amt;
            try { amt = Double.parseDouble(amtTxt); if (amt <= 0) throw new NumberFormatException(); }
            catch (NumberFormatException ex) { showFieldError(amtField, "Enter a valid positive amount."); return; }

            User user = Session.getCurrentUser();
            if (user == null) return;

            String splitType = (String) splitCombo.getSelectedItem();
            Expense expense = new Expense(
                    group.getId(),
                    desc,
                    amt,
                    user.getId(),
                    0,
                    splitType != null ? splitType : "EQUAL",
                    Date.valueOf(LocalDate.now())
            );
            if (expenseDAO.addExpense(expense)) {
                dialog.dispose();
                showSuccess("Expense added!");
                refreshGroups();
            } else {
                showError("Failed to add expense.");
            }
        });

        amtField.addActionListener(e -> add.doClick());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(cancel);
        btnRow.add(add);

        body.add(titleLbl);
        body.add(Box.createVerticalStrut(12));
        body.add(descField);
        body.add(Box.createVerticalStrut(8));
        body.add(amtField);
        body.add(Box.createVerticalStrut(8));
        body.add(splitLbl);
        body.add(Box.createVerticalStrut(4));
        body.add(splitCombo);
        body.add(Box.createVerticalStrut(20));
        body.add(btnRow);

        dialog.add(body);
        dialog.setVisible(true);
        descField.requestFocusInWindow();
    }

    private void deleteGroup(Group group) {
        JDialog dialog = buildStyledDialog("Delete Group", 400, 280);
        JPanel body = buildDialogBody(dialog);

        JLabel titleLbl = dialogTitle("Delete \"" + group.getName() + "\"?");
        JLabel warn = new JLabel("<html><center>This will permanently delete the group<br>and all its expenses. This cannot be undone.</center></html>");
        warn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        warn.setForeground(TEXT_MUTED);
        warn.setHorizontalAlignment(SwingConstants.CENTER);

        JButton del    = buildPrimaryButton("Delete", ACCENT_RED);
        JButton cancel = buildSecondaryButton("Cancel");
        cancel.addActionListener(e -> dialog.dispose());
        del.addActionListener(e -> {
            if (groupDAO.deleteGroup(group.getId())) {
                dialog.dispose();
                showSuccess("Group deleted.");
                refreshGroups();
            } else {
                showError("Failed to delete group.");
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(cancel);
        btnRow.add(del);

        body.add(titleLbl);
        body.add(Box.createVerticalStrut(10));
        body.add(warn);
        body.add(Box.createVerticalStrut(20));
        body.add(btnRow);

        dialog.add(body);
        dialog.setVisible(true);
    }

    private String getMembersText(int groupId) {
        List<User> members = groupDAO.getGroupMembers(groupId);
        if (members.isEmpty()) return "No members yet.";
        StringBuilder sb = new StringBuilder();
        for (User m : members)
            sb.append("• ").append(m.getName()).append(" (").append(m.getEmail()).append(")\n");
        return sb.toString();
    }

    private void addMemberByEmail(int groupId, String email, JTextArea membersArea) {
        com.expensesplitter.dao.UserDAO userDAO = new com.expensesplitter.dao.UserDAO();
        User found = userDAO.getUserByEmail(email);
        if (found == null) { showError("User with email '" + email + "' not found."); return; }
        if (groupDAO.addMember(groupId, found.getId())) {
            showSuccess("Member added!");
            membersArea.setText(getMembersText(groupId));
        } else {
            showError("Member already in group or error.");
        }
    }

    private JDialog buildStyledDialog(String title, int w, int h) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = owner instanceof Frame
                ? new JDialog((Frame) owner, title, true)
                : new JDialog((Dialog) owner, title, true);
        dialog.setSize(w, h);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);
        return dialog;
    }

    private JPanel buildDialogBody(JDialog dialog) {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CARD_WHITE);
        body.setBorder(new EmptyBorder(24, 28, 24, 28));
        return body;
    }

    private JLabel dialogTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField buildDialogField(String label, String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(8, 10, 8, 10)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private JPanel buildMiniStat(String label, String value, Color color) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 4));
        p.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
        p.setBorder(new EmptyBorder(10, 12, 10, 12));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(TEXT_MUTED);
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 16));
        val.setForeground(color);
        p.add(lbl);
        p.add(val);
        return p;
    }

    private JButton buildPrimaryButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() : getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        return btn;
    }

    private JButton buildSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(TEXT_DARK);
        btn.setBackground(new Color(241, 245, 249));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(8, 16, 8, 16)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton buildIconButton(org.kordamp.ikonli.Ikon icon, String tooltip, Color color) {
        JButton btn = new JButton(FontIcon.of(icon, 14, color));
        btn.setToolTipText(tooltip);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 8, 6, 8));
        return btn;
    }

    private void addPopupItem(JPopupMenu menu, String text, org.kordamp.ikonli.Ikon icon, Color color, ActionListener listener) {
        JMenuItem item = new JMenuItem(text, FontIcon.of(icon, 13, color));
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (color.equals(ACCENT_RED)) item.setForeground(ACCENT_RED);
        item.addActionListener(listener);
        menu.add(item);
    }

    private void showFieldError(JTextField field, String msg) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_RED),
                new EmptyBorder(8, 10, 8, 10)));
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.ERROR_MESSAGE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(8, 10, 8, 10)));
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void animateRefreshIcon(JButton btn) {
        Timer timer = new Timer(600, e -> btn.setIcon(FontIcon.of(FontAwesomeSolid.SYNC_ALT, 14, TEXT_MUTED)));
        timer.setRepeats(false);
        btn.setIcon(FontIcon.of(FontAwesomeSolid.SYNC_ALT, 14, ACCENT_BLUE));
        timer.start();
    }

    private static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int thickness, radius;
        RoundedBorder(Color color, int thickness, int radius) { this.color = color; this.thickness = thickness; this.radius = radius; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(x, y, w-1, h-1, radius, radius));
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(thickness, thickness, thickness, thickness); }
    }
}