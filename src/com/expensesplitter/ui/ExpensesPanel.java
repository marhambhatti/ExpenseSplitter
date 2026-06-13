package com.expensesplitter.ui;

import com.expensesplitter.util.Session;
import com.expensesplitter.dao.ExpenseDAO;
import com.expensesplitter.dao.GroupDAO;
import com.expensesplitter.dao.UserDAO;
import com.expensesplitter.models.Expense;
import com.expensesplitter.models.Group;
import com.expensesplitter.models.User;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// Expense Pannel
public class ExpensesPanel extends JPanel {

    private JPanel expensesPanel;
    private JLabel emptyLabel;
    private final GroupDAO groupDAO = new GroupDAO();
    private final ExpenseDAO expenseDAO = new ExpenseDAO();

    public ExpensesPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        add(buildMainContent(), BorderLayout.CENTER);
        refreshExpenses();
    }

    private JPanel buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(28, 30, 30, 30));

        JLabel title = new JLabel("All Expenses");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(UiStyles.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("View and manage expenses from all your groups with calculated splits.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(UiStyles.TEXT_MUTED);

        JPanel titleArea = new JPanel();
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));
        titleArea.setOpaque(false);
        titleArea.add(title);
        titleArea.add(Box.createVerticalStrut(4));
        titleArea.add(subtitle);

        JButton addBtn = buildAddButton();

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 22, 0));
        header.add(titleArea, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        expensesPanel = new JPanel();
        expensesPanel.setLayout(new BoxLayout(expensesPanel, BoxLayout.Y_AXIS));
        expensesPanel.setOpaque(false);
        expensesPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        emptyLabel = buildEmptyState();

        JScrollPane scrollPane = new JScrollPane(expensesPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UiStyles.CONTENT_BG);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        main.add(header, BorderLayout.NORTH);
        main.add(scrollPane, BorderLayout.CENTER);
        return main;
    }

    private JButton buildAddButton() {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? UiStyles.ACCENT_PRIMARY_HOVER : UiStyles.ACCENT_PRIMARY;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setText("Add Expense");
        btn.setIcon(FontIcon.of(FontAwesomeSolid.PLUS, 13, Color.WHITE));
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setIconTextGap(8);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(11, 24, 11, 24));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> showAddExpenseDialog());
        return btn;
    }

    private JLabel buildEmptyState() {
        JLabel lbl = new JLabel(
                "<html><div style='text-align:center'>" +
                        "<div style='font-size:16px; color:#64748B'>No expenses yet.</div>" +
                        "<div style='font-size:12px; color:#94A3B8; margin-top:4px'>Click Add Expense to record one.</div>" +
                        "</div></html>"
        );
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(UiStyles.TEXT_LABEL);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(40, 0, 0, 0));
        return lbl;
    }

    public void refreshExpenses() {
        expensesPanel.removeAll();
        User user = Session.getCurrentUser();
        if (user == null) {
            expensesPanel.add(emptyLabel);
            expensesPanel.revalidate();
            expensesPanel.repaint();
            return;
        }

        List<Group> userGroups = groupDAO.getGroupsByUser(user.getId());
        List<Expense> allExpenses = new java.util.ArrayList<>();
        Map<Integer, String> groupNameMap = new HashMap<>();
        Map<Integer, Integer> groupMemberCounts = new HashMap<>();

        for (Group g : userGroups) {
            groupNameMap.put(g.getId(), g.getName());
            groupMemberCounts.put(g.getId(), groupDAO.getMemberCount(g.getId()));
            allExpenses.addAll(expenseDAO.getExpensesByGroup(g.getId()));
        }

        if (allExpenses.isEmpty()) {
            expensesPanel.add(emptyLabel);
        } else {
            for (Expense expense : allExpenses) {
                expensesPanel.add(createExpenseCard(expense, groupNameMap, groupMemberCounts, user.getId()));
                expensesPanel.add(Box.createVerticalStrut(14));
            }
        }

        expensesPanel.revalidate();
        expensesPanel.repaint();
    }

    private JPanel createExpenseCard(Expense expense, Map<Integer, String> groupNames,
                                     Map<Integer, Integer> memberCounts, int currentUserId) {
        String groupName = groupNames.getOrDefault(expense.getGroupId(), "Group");
        User payer = new UserDAO().getUserById(expense.getPaidBy());
        String payerName = payer != null ? payer.getName() : "Unknown";
        int memberCount = memberCounts.getOrDefault(expense.getGroupId(), 1);
        double perPersonShare = expense.getAmount() / memberCount;
        boolean iOwed = expense.getPaidBy() == currentUserId;
        String category = expense.getDescription() != null ? expense.getDescription().toLowerCase() : "";

        org.kordamp.ikonli.Ikon catIkon;
        Color catColor;
        Color catBg;
        if (category.contains("rent") || category.contains("house") || category.contains("hostel")) {
            catIkon = FontAwesomeSolid.HOME; catColor = new Color(124, 58, 237); catBg = new Color(237, 233, 254);
        } else if (category.contains("food") || category.contains("dinner") || category.contains("lunch") || category.contains("eat")) {
            catIkon = FontAwesomeSolid.UTENSILS; catColor = new Color(234, 88, 12); catBg = new Color(255, 237, 213);
        } else if (category.contains("travel") || category.contains("uber") || category.contains("transport") || category.contains("fuel")) {
            catIkon = FontAwesomeSolid.CAR; catColor = new Color(37, 99, 235); catBg = new Color(219, 234, 254);
        } else if (category.contains("electric") || category.contains("bill") || category.contains("utility")) {
            catIkon = FontAwesomeSolid.BOLT; catColor = new Color(217, 119, 6); catBg = new Color(254, 243, 199);
        } else if (category.contains("grocery") || category.contains("market")) {
            catIkon = FontAwesomeSolid.SHOPPING_CART; catColor = new Color(5, 150, 105); catBg = new Color(209, 250, 229);
        } else {
            catIkon = FontAwesomeSolid.MONEY_BILL_WAVE; catColor = new Color(100, 116, 139); catBg = new Color(241, 245, 249);
        }

        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UiStyles.CARD_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        final Color fCatColor = catColor;
        JPanel stripe = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fCatColor);
                g2.fillRoundRect(0, 0, getWidth() + 12, getHeight(), 14, 14);
                g2.dispose();
            }
        };
        stripe.setPreferredSize(new Dimension(6, 0));
        stripe.setOpaque(false);

        JPanel inner = new JPanel(new BorderLayout(16, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(18, 20, 18, 22));

        final Color fCatBg = catBg;
        JLabel iconBubble = new JLabel(FontIcon.of(catIkon, 20, catColor)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fCatBg);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconBubble.setHorizontalAlignment(SwingConstants.CENTER);
        iconBubble.setVerticalAlignment(SwingConstants.CENTER);
        iconBubble.setPreferredSize(new Dimension(52, 52));
        iconBubble.setMinimumSize(new Dimension(52, 52));
        iconBubble.setOpaque(false);

        JPanel iconWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        iconWrap.setOpaque(false);
        iconWrap.add(iconBubble);

        JPanel centerBlock = new JPanel();
        centerBlock.setLayout(new BoxLayout(centerBlock, BoxLayout.Y_AXIS));
        centerBlock.setOpaque(false);

        JLabel descLbl = new JLabel(expense.getDescription());
        descLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        descLbl.setForeground(UiStyles.TEXT_PRIMARY);
        descLbl.setAlignmentX(LEFT_ALIGNMENT);

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badgeRow.setOpaque(false);
        badgeRow.setAlignmentX(LEFT_ALIGNMENT);
        badgeRow.add(makeBadge(FontAwesomeSolid.USERS, groupName, new Color(224, 231, 255), new Color(67, 56, 202)));
        if (expense.getDate() != null)
            badgeRow.add(makeBadge(FontAwesomeSolid.CALENDAR_ALT, expense.getDate().toString(), new Color(226, 232, 240), new Color(71, 85, 105)));
        if (expense.getSplitType() != null && !expense.getSplitType().isEmpty())
            badgeRow.add(makeBadge(FontAwesomeSolid.BALANCE_SCALE, expense.getSplitType(), new Color(209, 250, 229), new Color(4, 120, 87)));

        JLabel payerLbl = new JLabel("Paid by  " + payerName);
        payerLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        payerLbl.setForeground(UiStyles.TEXT_MUTED);
        payerLbl.setIcon(FontIcon.of(FontAwesomeSolid.USER, 11, UiStyles.TEXT_MUTED));
        payerLbl.setIconTextGap(5);
        payerLbl.setAlignmentX(LEFT_ALIGNMENT);

        centerBlock.add(descLbl);
        centerBlock.add(Box.createVerticalStrut(6));
        centerBlock.add(badgeRow);
        centerBlock.add(Box.createVerticalStrut(5));
        centerBlock.add(payerLbl);

        JPanel rightBlock = new JPanel();
        rightBlock.setLayout(new BoxLayout(rightBlock, BoxLayout.Y_AXIS));
        rightBlock.setOpaque(false);

        JLabel amountLbl = new JLabel(String.format("Rs. %.2f", expense.getAmount()));
        amountLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        amountLbl.setForeground(catColor);
        amountLbl.setAlignmentX(RIGHT_ALIGNMENT);

        JLabel perLbl = new JLabel(String.format("Rs. %.2f / person", perPersonShare));
        perLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        perLbl.setForeground(UiStyles.TEXT_MUTED);
        perLbl.setAlignmentX(RIGHT_ALIGNMENT);

        org.kordamp.ikonli.Ikon pillIkon = iOwed ? FontAwesomeSolid.CHECK : FontAwesomeSolid.ARROW_UP;
        String pillText = iOwed ? "You Paid" : "You Owe";
        Color pillBg = iOwed ? new Color(209, 250, 229) : new Color(254, 226, 226);
        Color pillFg = iOwed ? new Color(4, 120, 87) : new Color(185, 28, 28);

        final Color fPillBg = pillBg;
        JLabel pill = new JLabel(pillText, FontIcon.of(pillIkon, 10, pillFg), SwingConstants.LEFT) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fPillBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setFont(new Font("Segoe UI", Font.BOLD, 11));
        pill.setForeground(pillFg);
        pill.setIconTextGap(5);
        pill.setBorder(new EmptyBorder(4, 12, 4, 12));
        pill.setOpaque(false);
        pill.setAlignmentX(RIGHT_ALIGNMENT);

        rightBlock.add(amountLbl);
        rightBlock.add(Box.createVerticalStrut(4));
        rightBlock.add(perLbl);
        rightBlock.add(Box.createVerticalStrut(8));
        rightBlock.add(pill);

        inner.add(iconWrap, BorderLayout.WEST);
        inner.add(centerBlock, BorderLayout.CENTER);
        inner.add(rightBlock, BorderLayout.EAST);

        JPanel splitSection = buildSplitSection(expense, memberCount, perPersonShare);

        JPanel cardContent = new JPanel(new BorderLayout());
        cardContent.setOpaque(false);
        cardContent.add(inner, BorderLayout.NORTH);
        cardContent.add(splitSection, BorderLayout.CENTER);

        card.add(stripe, BorderLayout.WEST);
        card.add(cardContent, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildSplitSection(Expense expense, int memberCount, double perPersonShare) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);

        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(UiStyles.CARD_BORDER);
                g.fillRect(20, 0, getWidth() - 40, 1);
            }
        };
        divider.setPreferredSize(new Dimension(0, 1));
        divider.setOpaque(false);

        JPanel members = new JPanel();
        members.setLayout(new BoxLayout(members, BoxLayout.Y_AXIS));
        members.setOpaque(false);
        members.setBorder(new EmptyBorder(10, 20, 14, 22));

        JLabel splitHeader = new JLabel("Split among " + memberCount + " member" + (memberCount != 1 ? "s" : ""));
        splitHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        splitHeader.setForeground(UiStyles.TEXT_LABEL);
        splitHeader.setIcon(FontIcon.of(FontAwesomeSolid.DIVIDE, 10, UiStyles.TEXT_LABEL));
        splitHeader.setIconTextGap(5);
        splitHeader.setAlignmentX(LEFT_ALIGNMENT);
        members.add(splitHeader);
        members.add(Box.createVerticalStrut(8));

        List<com.expensesplitter.models.User> memberList = groupDAO.getGroupMembers(expense.getGroupId());
        for (com.expensesplitter.models.User member : memberList) {
            boolean isPayer = member.getId() == expense.getPaidBy();
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(3, 0, 3, 0));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
            row.setAlignmentX(LEFT_ALIGNMENT);

            JLabel dot = new JLabel(FontIcon.of(
                    isPayer ? FontAwesomeSolid.CHECK_CIRCLE : FontAwesomeSolid.CIRCLE,
                    12, isPayer ? UiStyles.ACCENT_PRIMARY : new Color(203, 213, 225)
            ));
            dot.setPreferredSize(new Dimension(18, 16));

            JLabel nameLbl = new JLabel(member.getName() + (isPayer ? "  (paid)" : ""));
            nameLbl.setFont(new Font("Segoe UI", isPayer ? Font.BOLD : Font.PLAIN, 12));
            nameLbl.setForeground(isPayer ? UiStyles.ACCENT_PRIMARY : UiStyles.TEXT_SECONDARY);

            JLabel amtLbl = new JLabel(String.format("Rs. %.2f", perPersonShare));
            amtLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            amtLbl.setForeground(isPayer ? UiStyles.ACCENT_PRIMARY : UiStyles.TEXT_MUTED);

            JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            leftSide.setOpaque(false);
            leftSide.add(dot);
            leftSide.add(nameLbl);

            row.add(leftSide, BorderLayout.WEST);
            row.add(amtLbl, BorderLayout.EAST);
            members.add(row);
        }

        wrap.add(divider, BorderLayout.NORTH);
        wrap.add(members, BorderLayout.CENTER);
        return wrap;
    }

    private JLabel makeBadge(org.kordamp.ikonli.Ikon icon, String text, Color bg, Color fg) {
        final Color fBg = bg;
        JLabel lbl = new JLabel(text, FontIcon.of(icon, 10, fg), SwingConstants.LEFT) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(fg);
        lbl.setIconTextGap(4);
        lbl.setBorder(new EmptyBorder(3, 8, 3, 9));
        lbl.setOpaque(false);
        return lbl;
    }

    private void showAddExpenseDialog() {
        java.awt.Window ancestor = SwingUtilities.getWindowAncestor(this);
        java.awt.Frame owner = ancestor instanceof java.awt.Frame f ? f : null;
        ExpenseDialog dialog = new ExpenseDialog(owner);
        dialog.setVisible(true);
        refreshExpenses();
    }
}