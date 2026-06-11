package com.expensesplitter.ui;

import com.expensesplitter.util.Session;
import com.expensesplitter.dao.ExpenseDAO;
import com.expensesplitter.dao.GroupDAO;
import com.expensesplitter.dao.SettlementDAO;
import com.expensesplitter.models.Expense;
import com.expensesplitter.models.Group;
import com.expensesplitter.models.Settlement;
import com.expensesplitter.models.User;
import com.expensesplitter.uifactory.SideBar;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    public enum Page {
        DASHBOARD, GROUPS, EXPENSES, LEDGER_DETAILS, SETTLEMENTS, SEARCH_FILTER, SETTINGS
    }

    private SideBar sidebar;

    public MainFrame() {
        setTitle("Expense Splitter");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        User user = Session.getCurrentUser();

        sidebar = new SideBar(e -> {
            Session.getCurrentUser();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });

        if (user != null) {
            sidebar.setUserProfile(user.getName(), "Online");
        }

        sidebar.addMenuItem("Dashboard",   "blue",   FontAwesomeSolid.CHART_PIE,  new Color(37, 99, 235),   buildDashboardPanel());
        sidebar.addMenuItem("Groups",      "green",  FontAwesomeSolid.USERS,      new Color(5, 150, 105),  new  GroupFrame());
        sidebar.addMenuItem("Expenses",    "orange", FontAwesomeSolid.RECEIPT,    new Color(217, 119, 6),   new ExpensesPanel());
        sidebar.addMenuItem("Ledger",      "purple", FontAwesomeSolid.BOOK,       new Color(124, 58, 237),  new LedgerDetailsFrame());
        sidebar.addMenuItem("Settlements", "teal",   FontAwesomeSolid.HANDSHAKE,  new Color(13, 148, 136),  new SettlementFrame());
        sidebar.addMenuItem("Search",      "gray",   FontAwesomeSolid.SEARCH,     new Color(100, 116, 139), new SearchFilterFrame());

        setContentPane(sidebar);
    }

    public void showPage(Page page) {
        String name = switch (page) {
            case DASHBOARD      -> "Dashboard";
            case GROUPS         -> "Groups";
            case EXPENSES       -> "Expenses";
            case LEDGER_DETAILS -> "Ledger";
            case SETTLEMENTS    -> "Settlements";
            case SEARCH_FILTER  -> "Search";
            case SETTINGS       -> "Dashboard";
        };
        sidebar.selectMenuItemByName(name);
    }

    public void refreshCurrentPage() {
        revalidate();
        repaint();
    }

    private JPanel buildDashboardPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(22, 24, 22, 24));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.setBorder(new EmptyBorder(0, 0, 16, 0));
        JLabel title = new JLabel("Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel sub = new JLabel("Your financial snapshot across all active groups.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(UiStyles.TEXT_MUTED);
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(3));
        titleBlock.add(sub);

        double totalOwes = 0, totalOwedTo = 0;
        User user = Session.getCurrentUser();
        if (user != null) {
            List<Group> groups = new GroupDAO().getGroupsByUser(user.getId());
            for (Group g : groups) {
                double balance = calculateGroupBalance(g.getId(), user.getId());
                if (balance > 0) totalOwedTo += balance;
                else totalOwes += Math.abs(balance);
            }
        }

        JPanel metrics = new JPanel(new GridLayout(1, 3, 16, 0));
        metrics.setOpaque(false);
        metrics.setBorder(new EmptyBorder(0, 0, 18, 0));
        metrics.add(metricCard("TOTAL YOU OWE",     "Rs. " + String.format("%.0f", totalOwes),    UiStyles.METRIC_DANGER_FG,  FontAwesomeSolid.ARROW_UP,   false));
        metrics.add(metricCard("TOTAL OWED TO YOU", "Rs. " + String.format("%.0f", totalOwedTo),  UiStyles.METRIC_SUCCESS_FG, FontAwesomeSolid.ARROW_DOWN, false));
        double net = totalOwedTo - totalOwes;
        metrics.add(metricCard("OVERALL NET BALANCE", (net >= 0 ? "+ " : "") + "Rs. " + String.format("%.0f", net), UiStyles.METRIC_NEUTRAL_FG, FontAwesomeSolid.WALLET, true));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(titleBlock, BorderLayout.NORTH);
        top.add(metrics,    BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(16, 0));
        bottom.setOpaque(false);
        bottom.add(createGroupSummaryPanel(), BorderLayout.CENTER);
        bottom.add(createRecentActivityPanel(), BorderLayout.EAST);

        main.add(top,    BorderLayout.NORTH);
        main.add(bottom, BorderLayout.CENTER);
        return main;
    }

    private JPanel createGroupSummaryPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(UiStyles.CARD_BG);
        outer.setBorder(BorderFactory.createLineBorder(UiStyles.CARD_BORDER));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiStyles.CARD_BG);
        header.setBorder(new EmptyBorder(14, 16, 12, 16));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        FontIcon groupsIcon = FontIcon.of(FontAwesomeSolid.LAYER_GROUP, 14, UiStyles.TEXT_PRIMARY);
        JLabel hTitle = new JLabel("Group Summary");
        hTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleRow.add(new JLabel(groupsIcon));
        titleRow.add(hTitle);

        JLabel viewAll = new JLabel("View All");
        viewAll.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewAll.setForeground(UiStyles.ACCENT_LINK);
        viewAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewAll.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { sidebar.selectMenuItemByName("Groups"); }
        });

        header.add(titleRow, BorderLayout.WEST);
        header.add(viewAll,  BorderLayout.EAST);

        JPanel cards = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 14));
        cards.setBackground(UiStyles.CARD_BG);

        User user = Session.getCurrentUser();
        if (user != null) {
            List<Group> groups = new GroupDAO().getGroupsByUser(user.getId());
            if (groups.isEmpty()) {
                JLabel empty = new JLabel("No groups yet — create one to get started.");
                empty.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                empty.setForeground(Color.GRAY);
                empty.setBorder(new EmptyBorder(20, 16, 20, 16));
                cards.add(empty);
            } else {
                GroupDAO gdao = new GroupDAO();
                for (Group g : groups) {
                    int count = gdao.getMemberCount(g.getId());
                    double balance = calculateGroupBalance(g.getId(), user.getId());
                    int pct = calculateSettlementPercentage(g.getId());
                    Color amountColor = balance >= 0 ? UiStyles.METRIC_SUCCESS_FG : UiStyles.METRIC_DANGER_FG;
                    String amountStr = (balance >= 0 ? "+" : "") + "Rs. " + String.format("%.0f", Math.abs(balance));
                    cards.add(groupCard(g.getName(), count + " Members", amountStr, amountColor, pct));
                }
            }
        }

        JButton newGroupBtn = createNewGroupCard();
        newGroupBtn.addActionListener(e -> sidebar.selectMenuItemByName("Groups"));
        cards.add(newGroupBtn);

        outer.add(header, BorderLayout.NORTH);
        outer.add(cards,  BorderLayout.CENTER);
        return outer;
    }

    private JPanel createRecentActivityPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setPreferredSize(new Dimension(240, 0));
        outer.setBackground(UiStyles.CARD_BG);
        outer.setBorder(BorderFactory.createLineBorder(UiStyles.CARD_BORDER));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        header.setBackground(UiStyles.CARD_BG);
        header.setBorder(new EmptyBorder(14, 14, 10, 14));
        FontIcon clockIcon = FontIcon.of(FontAwesomeSolid.HISTORY, 13, UiStyles.TEXT_PRIMARY);
        JLabel rTitle = new JLabel("Recent Activity");
        rTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.add(new JLabel(clockIcon));
        header.add(rTitle);

        JPanel items = new JPanel();
        items.setLayout(new BoxLayout(items, BoxLayout.Y_AXIS));
        items.setBackground(UiStyles.CARD_BG);

        User user = Session.getCurrentUser();
        if (user != null) {
            List<ActivityEvent> allEvents = getRecentActivities(user.getId());
            if (allEvents.isEmpty()) {
                items.add(activityItem(FontAwesomeSolid.INFO_CIRCLE, "No recent activity yet.", null, ""));
            } else {
                int limit = Math.min(4, allEvents.size());
                for (int i = 0; i < limit; i++) {
                    ActivityEvent event = allEvents.get(i);
                    items.add(activityItem(event.icon, event.description, event.highlight, event.date));
                }
            }
        }

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(UiStyles.CARD_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UiStyles.CARD_BORDER));
        JLabel viewHist = new JLabel("View Full History");
        viewHist.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewHist.setForeground(UiStyles.ACCENT_LINK);
        viewHist.setIcon(FontIcon.of(FontAwesomeSolid.ANGLE_LEFT, 11, UiStyles.ACCENT_LINK));
        viewHist.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        footer.add(viewHist);

        JScrollPane sp = new JScrollPane(items);
        sp.setBorder(null);
        sp.getViewport().setBackground(UiStyles.CARD_BG);

        outer.add(header, BorderLayout.NORTH);
        outer.add(sp,     BorderLayout.CENTER);
        outer.add(footer, BorderLayout.SOUTH);
        return outer;
    }

    private JPanel buildGroupsPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(28, 30, 30, 30));

        JPanel titleArea = new JPanel();
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));
        titleArea.setOpaque(false);
        JLabel title = new JLabel("Manage Groups");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel subtitle = new JLabel("View and manage your expense sharing groups.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 110, 125));
        titleArea.add(title);
        titleArea.add(Box.createVerticalStrut(4));
        titleArea.add(subtitle);

        JPanel gridPanel = new JPanel(new com.expensesplitter.layouts.WrapLayout(FlowLayout.LEFT, 16, 16));
        gridPanel.setOpaque(false);

        JLabel emptyLabel = new JLabel("No groups yet. Click \"+ Create New Group\" to get started.", SwingConstants.CENTER);
        emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        emptyLabel.setForeground(new Color(120, 130, 145));
        emptyLabel.setBorder(new EmptyBorder(60, 0, 60, 0));

        GroupDAO groupDAO = new GroupDAO();
        ExpenseDAO expenseDAO = new ExpenseDAO();
        User user = Session.getCurrentUser();

        Runnable refresh = () -> {
            gridPanel.removeAll();
            if (user == null) { emptyLabel.setVisible(true); gridPanel.revalidate(); gridPanel.repaint(); return; }
            List<Group> groups = groupDAO.getGroupsByUser(user.getId());
            emptyLabel.setVisible(groups.isEmpty());
            for (Group group : groups) {
                int members = groupDAO.getMemberCount(group.getId());
                gridPanel.add(buildGroupCard(group, members, groupDAO, expenseDAO, wrapper));
            }
            gridPanel.add(buildCreateGroupCard(wrapper, groupDAO, user));
            gridPanel.revalidate();
            gridPanel.repaint();
        };

        JButton createBtn = new JButton("+ Create New Group");
        createBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        createBtn.setBackground(UiStyles.ACCENT_PRIMARY);
        createBtn.setForeground(Color.WHITE);
        createBtn.setFocusPainted(false);
        createBtn.setBorderPainted(false);
        createBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        createBtn.addActionListener(e -> {
            if (user == null) return;
            String name = JOptionPane.showInputDialog(this, "Enter group name:", "Create New Group", JOptionPane.PLAIN_MESSAGE);
            if (name == null || name.trim().isEmpty()) return;
            groupDAO.createGroup(new Group(name.trim(), user.getId()));
            refresh.run();
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(titleArea, BorderLayout.WEST);
        header.add(createBtn, BorderLayout.EAST);

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.add(emptyLabel, BorderLayout.NORTH);
        gridWrapper.add(gridPanel,  BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(gridWrapper);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        wrapper.add(header, BorderLayout.NORTH);
        wrapper.add(scroll, BorderLayout.CENTER);

        SwingUtilities.invokeLater(refresh::run);
        return wrapper;
    }

    private JPanel buildGroupCard(Group group, int memberCount, GroupDAO groupDAO, ExpenseDAO expenseDAO, JPanel parentPanel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(300, 145));
        card.setMaximumSize(new Dimension(300, 145));
        card.setMinimumSize(new Dimension(260, 145));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(12, 14, 12, 14)));

        String initials = group.getName().length() >= 2
                ? group.getName().substring(0, 2).toUpperCase()
                : group.getName().substring(0, 1).toUpperCase();

        Color[] bgColors     = {new Color(0, 119, 212), new Color(212, 222, 235), new Color(191, 100, 14)};
        Color[] fgColors     = {Color.WHITE, new Color(80, 90, 100), Color.WHITE};
        Color[] borderColors = {new Color(0, 100, 190), new Color(180, 195, 210), new Color(170, 80, 10)};
        int colorIdx = group.getId() % 3;
        Color avatarBg = bgColors[colorIdx], avatarFg = fgColors[colorIdx], avatarBorder = borderColors[colorIdx];

        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(avatarBg);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.setColor(avatarBorder);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.setColor(avatarFg);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials, (getWidth()-fm.stringWidth(initials))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(42, 42));
        avatar.setOpaque(false);

        JLabel nameLbl = new JLabel(group.getName());
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLbl.setForeground(new Color(15, 23, 42));

        JLabel membersLbl = new JLabel(memberCount + (memberCount == 1 ? " member" : " members"));
        membersLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        membersLbl.setForeground(new Color(100, 116, 139));
        membersLbl.setIcon(FontIcon.of(FontAwesomeSolid.USERS, 11, new Color(100, 116, 139)));
        membersLbl.setIconTextGap(5);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);
        info.add(nameLbl);
        info.add(membersLbl);

        JPanel topInfoPanel = new JPanel(new BorderLayout(12, 0));
        topInfoPanel.setOpaque(false);
        topInfoPanel.add(avatar, BorderLayout.WEST);
        topInfoPanel.add(info,   BorderLayout.CENTER);

        JLabel menuIcon = new JLabel(FontIcon.of(FontAwesomeSolid.ELLIPSIS_V, 16, new Color(100, 116, 139)));
        menuIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        menuIcon.setBorder(new EmptyBorder(0, 10, 0, 4));

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem manageItem = new JMenuItem("Manage Group");
        manageItem.setIcon(FontIcon.of(FontAwesomeSolid.COG, 13, new Color(60, 70, 85)));
        manageItem.addActionListener(e -> showManageGroupDialog(group, groupDAO));

        JMenuItem deleteItem = new JMenuItem("Delete Group");
        deleteItem.setIcon(FontIcon.of(FontAwesomeSolid.TRASH_ALT, 13, new Color(220, 38, 38)));
        deleteItem.setForeground(new Color(220, 38, 38));
        deleteItem.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Delete group \"" + group.getName() + "\" and all its expenses?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) { groupDAO.deleteGroup(group.getId()); parentPanel.revalidate(); parentPanel.repaint(); }
        });
        popupMenu.add(manageItem);
        popupMenu.add(deleteItem);

        menuIcon.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { popupMenu.show(menuIcon, e.getX()-100, e.getY()); }
        });

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(topInfoPanel, BorderLayout.CENTER);
        topSection.add(menuIcon,     BorderLayout.EAST);
        card.add(topSection, BorderLayout.NORTH);

        JPanel dividerPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(226, 232, 240));
                g.drawLine(0, getHeight()/2, getWidth(), getHeight()/2);
            }
        };
        dividerPanel.setPreferredSize(new Dimension(100, 24));
        dividerPanel.setOpaque(false);
        card.add(dividerPanel, BorderLayout.CENTER);

        JPanel bottomSection = new JPanel(new BorderLayout());
        bottomSection.setOpaque(false);

        JLabel balanceTitle = new JLabel("Your Balance");
        balanceTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        balanceTitle.setForeground(new Color(100, 116, 139));
        balanceTitle.setIcon(FontIcon.of(FontAwesomeSolid.WALLET, 11, new Color(100, 116, 139)));
        balanceTitle.setIconTextGap(5);

        JLabel balanceAmt = new JLabel();
        balanceAmt.setFont(new Font("Consolas", Font.BOLD, 13));

        JPanel balanceTextPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        balanceTextPanel.setOpaque(false);
        balanceTextPanel.add(balanceTitle);
        balanceTextPanel.add(balanceAmt);

        double balance = 0.0;
        User currentUser = Session.getCurrentUser();
        if (currentUser != null) {
            List<Expense> expenses = expenseDAO.getExpensesByGroup(group.getId());
            for (Expense e : expenses) {
                if (e.getPaidBy() == currentUser.getId()) balance += e.getAmount();
                else { int s = memberCount > 1 ? memberCount-1 : 1; balance -= (e.getAmount() / s); }
            }
        }

        String badgeText; Color badgeBg; Color badgeFg; FontIcon badgeIcon;
        if (balance < -0.01) {
            balanceAmt.setText(String.format("-$%.2f", Math.abs(balance)));
            balanceAmt.setForeground(new Color(220, 38, 38));
            badgeText = "YOU OWE"; badgeBg = new Color(254, 226, 226); badgeFg = new Color(185, 28, 28);
            badgeIcon = FontIcon.of(FontAwesomeSolid.ARROW_UP, 9, new Color(185, 28, 28));
        } else if (balance > 0.01) {
            balanceAmt.setText(String.format("+$%.2f", balance));
            balanceAmt.setForeground(new Color(5, 150, 105));
            badgeText = "OWED TO YOU"; badgeBg = new Color(209, 250, 229); badgeFg = new Color(4, 120, 87);
            badgeIcon = FontIcon.of(FontAwesomeSolid.ARROW_DOWN, 9, new Color(4, 120, 87));
        } else {
            balanceAmt.setText("$0.00");
            balanceAmt.setForeground(new Color(100, 116, 139));
            badgeText = "SETTLED UP"; badgeBg = new Color(226, 232, 240); badgeFg = new Color(71, 85, 105);
            badgeIcon = FontIcon.of(FontAwesomeSolid.CHECK, 9, new Color(71, 85, 105));
        }

        final Color fBadgeBg = badgeBg, fBadgeFg = badgeFg;
        JLabel badge = new JLabel(badgeText, badgeIcon, SwingConstants.LEFT) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fBadgeBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setForeground(fBadgeFg);
        badge.setIconTextGap(4);
        badge.setBorder(new EmptyBorder(4, 7, 4, 8));

        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(badge);

        bottomSection.add(balanceTextPanel, BorderLayout.WEST);
        bottomSection.add(badgeWrapper,     BorderLayout.EAST);
        card.add(bottomSection, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildCreateGroupCard(JPanel parentPanel, GroupDAO groupDAO, User user) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(300, 145));
        card.setMaximumSize(new Dimension(300, 145));
        card.setMinimumSize(new Dimension(260, 145));
        card.setBackground(Color.WHITE);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setBorder(BorderFactory.createDashedBorder(new Color(180, 190, 200), 1, 6, 4, false));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel iconLbl = new JLabel(FontIcon.of(FontAwesomeSolid.PLUS_CIRCLE, 28, new Color(120, 130, 143)));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel textLbl = new JLabel("Create New Group");
        textLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textLbl.setForeground(new Color(120, 130, 143));
        textLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(iconLbl);
        inner.add(Box.createVerticalStrut(8));
        inner.add(textLbl);
        card.add(inner);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (user == null) return;
                String name = JOptionPane.showInputDialog(MainFrame.this, "Enter group name:", "Create New Group", JOptionPane.PLAIN_MESSAGE);
                if (name == null || name.trim().isEmpty()) return;
                groupDAO.createGroup(new Group(name.trim(), user.getId()));
                parentPanel.revalidate();
                parentPanel.repaint();
            }
        });
        return card;
    }

    private void showManageGroupDialog(Group group, GroupDAO groupDAO) {
        JDialog dialog = new JDialog(this, "Manage Group: " + group.getName(), true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Members of " + group.getName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        main.add(title, BorderLayout.NORTH);

        JTextArea membersArea = new JTextArea();
        membersArea.setEditable(false);
        membersArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        membersArea.setText(getMembersText(group.getId(), groupDAO));
        main.add(new JScrollPane(membersArea), BorderLayout.CENTER);

        JPanel addPanel = new JPanel(new BorderLayout(8, 0));
        JTextField emailField = new JTextField();
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JButton addBtn = new JButton("Add Member");
        addBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            if (email.isEmpty()) { JOptionPane.showMessageDialog(dialog, "Enter member email."); return; }
            com.expensesplitter.dao.UserDAO userDAO = new com.expensesplitter.dao.UserDAO();
            com.expensesplitter.models.User found = userDAO.getUserByEmail(email);
            if (found == null) { JOptionPane.showMessageDialog(dialog, "User not found."); return; }
            if (groupDAO.addMember(group.getId(), found.getId())) {
                membersArea.setText(getMembersText(group.getId(), groupDAO));
                emailField.setText("");
            } else { JOptionPane.showMessageDialog(dialog, "Member already in group or error."); }
        });
        addPanel.add(new JLabel("Email:"), BorderLayout.WEST);
        addPanel.add(emailField, BorderLayout.CENTER);
        addPanel.add(addBtn, BorderLayout.EAST);
        main.add(addPanel, BorderLayout.SOUTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(closeBtn);

        dialog.add(main, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private String getMembersText(int groupId, GroupDAO groupDAO) {
        List<com.expensesplitter.models.User> members = groupDAO.getGroupMembers(groupId);
        if (members.isEmpty()) return "No members yet.";
        StringBuilder sb = new StringBuilder();
        for (com.expensesplitter.models.User m : members)
            sb.append("- ").append(m.getName()).append(" (").append(m.getEmail()).append(")\n");
        return sb.toString();
    }

    private JPanel metricCard(String label, String value, Color valueColor, org.kordamp.ikonli.Ikon icon, boolean rightBorder) {
        Color cardBg = valueColor.equals(UiStyles.METRIC_DANGER_FG)  ? UiStyles.METRIC_DANGER_BG
                : valueColor.equals(UiStyles.METRIC_SUCCESS_FG) ? UiStyles.METRIC_SUCCESS_BG
                : UiStyles.METRIC_NEUTRAL_BG;

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(cardBg);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, UiStyles.CARD_BORDER),
                new EmptyBorder(20, 20, 20, 20)));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel iconLabel = new JLabel(FontIcon.of(icon, 18, valueColor));
        JLabel lbl = new JLabel("  " + label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(UiStyles.TEXT_LABEL);
        JPanel labelRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        labelRow.setOpaque(false);
        labelRow.add(iconLabel);
        labelRow.add(lbl);
        topRow.add(labelRow, BorderLayout.WEST);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 28));
        val.setForeground(valueColor);
        val.setBorder(new EmptyBorder(12, 0, 0, 0));

        card.add(topRow, BorderLayout.NORTH);
        card.add(val,    BorderLayout.CENTER);
        return card;
    }

    private JPanel groupCard(String name, String members, String balance, Color color, int pct) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UiStyles.CARD_BG != null ? UiStyles.CARD_BG : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        card.setPreferredSize(new Dimension(210, 145));
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel topPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        topPanel.setOpaque(false);
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(UiStyles.TEXT_PRIMARY);
        JLabel membersLabel = new JLabel(members);
        membersLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        membersLabel.setForeground(UiStyles.TEXT_LABEL);
        membersLabel.setIcon(FontIcon.of(FontAwesomeSolid.USERS, 10, UiStyles.TEXT_LABEL));
        membersLabel.setIconTextGap(4);
        topPanel.add(nameLabel);
        topPanel.add(membersLabel);

        JLabel amountLabel = new JLabel(balance);
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        amountLabel.setForeground(color);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 6));
        bottomPanel.setOpaque(false);
        JLabel settledLabel = new JLabel("Settled " + pct + "%");
        settledLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        settledLabel.setForeground(UiStyles.TEXT_MUTED);
        JPanel progressLine = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(235, 240, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                int fw = (int)(getWidth() * (pct / 100.0));
                if (fw > 0) { g2.setColor(pct == 100 ? new Color(34, 197, 94) : color); g2.fillRoundRect(0, 0, fw, getHeight(), 4, 4); }
                g2.dispose();
            }
        };
        progressLine.setPreferredSize(new Dimension(100, 5));
        progressLine.setOpaque(false);
        bottomPanel.add(settledLabel,  BorderLayout.NORTH);
        bottomPanel.add(progressLine,  BorderLayout.SOUTH);

        card.add(topPanel,    BorderLayout.NORTH);
        card.add(amountLabel, BorderLayout.CENTER);
        card.add(bottomPanel, BorderLayout.SOUTH);
        return card;
    }

    private JButton createNewGroupCard() {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(241, 245, 249) : (UiStyles.CARD_BG != null ? UiStyles.CARD_BG : Color.WHITE));
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                float[] dash = {5.0f, 4.0f};
                g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, dash, 0.0f));
                g2.setColor(new Color(180, 185, 195));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 12, 12);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(210, 145));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0, 0, 4, 0);
        JLabel plusIcon = new JLabel(FontIcon.of(FontAwesomeSolid.PLUS_CIRCLE, 22, UiStyles.TEXT_MUTED));
        btn.add(plusIcon, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 0, 0);
        JLabel label = new JLabel("Create New Group");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(UiStyles.TEXT_MUTED);
        btn.add(label, gbc);
        return btn;
    }

    private JPanel activityItem(org.kordamp.ikonli.Ikon icon, String text, String highlight, String time) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(UiStyles.CARD_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiStyles.TABLE_GRID),
                new EmptyBorder(12, 16, 12, 16)));

        JLabel av = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(236, 253, 245));
                g2.fillOval(0, 0, getWidth(), getHeight());
                FontIcon.of(icon, 14, UiStyles.ACCENT_PRIMARY).paintIcon(this, g2, (getWidth()-14)/2, (getHeight()-14)/2);
                g2.dispose();
            }
        };
        av.setPreferredSize(new Dimension(36, 36));
        av.setMinimumSize(new Dimension(36, 36));

        String html = "<html><div style='width:160px'>" + text;
        if (highlight != null) html += " <font color='#2563eb'><b>" + highlight + "</b></font>";
        html += "</div></html>";
        JLabel desc = new JLabel(html);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        desc.setForeground(UiStyles.TEXT_PRIMARY);

        JLabel timeLbl = new JLabel(time);
        timeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLbl.setForeground(UiStyles.TEXT_MUTED);

        JPanel txt = new JPanel();
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        txt.setOpaque(false);
        txt.add(desc);
        txt.add(Box.createVerticalStrut(4));
        txt.add(timeLbl);

        row.add(av,  BorderLayout.WEST);
        row.add(txt, BorderLayout.CENTER);
        return row;
    }

    private double calculateGroupBalance(int groupId, int userId) {
        List<Expense> expenses = new ExpenseDAO().getExpensesByGroup(groupId);
        double paid = 0, share = 0;
        int memberCount = new GroupDAO().getMemberCount(groupId);
        for (Expense e : expenses) {
            if (e.getPaidBy() == userId) paid += e.getAmount();
            else { int m = memberCount - 1; if (m > 0) share += e.getAmount() / (double) m; }
        }
        return paid - share;
    }

    private int calculateSettlementPercentage(int groupId) {
        List<Expense> expenses = new ExpenseDAO().getExpensesByGroup(groupId);
        if (expenses.isEmpty()) return 100;
        double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double settled = new SettlementDAO().getSettlementsByGroup(groupId).stream().mapToDouble(Settlement::getAmount).sum();
        return settled >= total ? 100 : (int)((settled / total) * 100);
    }

    private static class ActivityEvent {
        org.kordamp.ikonli.Ikon icon; String description, highlight, date;
        ActivityEvent(org.kordamp.ikonli.Ikon icon, String description, String highlight, String date) {
            this.icon = icon; this.description = description; this.highlight = highlight; this.date = date;
        }
    }

    private List<ActivityEvent> getRecentActivities(int userId) {
        List<ActivityEvent> events = new ArrayList<>();
        GroupDAO groupDAO = new GroupDAO();
        ExpenseDAO expenseDAO = new ExpenseDAO();
        SettlementDAO settlementDAO = new SettlementDAO();
        com.expensesplitter.dao.UserDAO userDAO = new com.expensesplitter.dao.UserDAO();

        for (Group g : groupDAO.getGroupsByUser(userId)) {
            for (Expense e : expenseDAO.getExpensesByGroup(g.getId())) {
                User u = userDAO.getUserById(e.getPaidBy());
                String uName = u != null ? u.getName() : "Someone";
                events.add(new ActivityEvent(FontAwesomeSolid.FILE_INVOICE_DOLLAR, uName + " added '" + e.getDescription() + "' in " + g.getName(), null, e.getDate() != null ? formatDate(e.getDate().toString()) : "Unknown"));
            }
            for (Settlement s : settlementDAO.getSettlementsByGroup(g.getId())) {
                User payer = userDAO.getUserById(s.getPayerId());
                User payee = userDAO.getUserById(s.getPayeeId());
                String pName = payer != null ? payer.getName() : "Someone";
                String eName = payee != null ? payee.getName() : "Someone";
                events.add(new ActivityEvent(FontAwesomeSolid.CHECK_CIRCLE, pName + " settled Rs. " + String.format("%.0f", s.getAmount()) + " to " + eName, null, s.getDate() != null ? formatDate(s.getDate().toString()) : "Unknown"));
            }
        }
        events.sort((a, b) -> {
            try { return java.time.LocalDate.parse(b.date).compareTo(java.time.LocalDate.parse(a.date)); }
            catch (Exception ex) { return 0; }
        });
        return events;
    }

    private String formatDate(String dateStr) {
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
            long d = java.time.temporal.ChronoUnit.DAYS.between(date, java.time.LocalDate.now());
            if (d == 0) return "Today";
            if (d == 1) return "Yesterday";
            if (d < 7)  return d + " days ago";
            if (d < 30) return (d/7) + " weeks ago";
            return (d/30) + " months ago";
        } catch (Exception e) { return dateStr; }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) { e.printStackTrace(); }
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}