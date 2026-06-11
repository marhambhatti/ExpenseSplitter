package com.expensesplitter.ui;

import com.expensesplitter.util.Session;
import com.expensesplitter.dao.ExpenseDAO;
import com.expensesplitter.dao.GroupDAO;
import com.expensesplitter.dao.SettlementDAO;
import com.expensesplitter.dao.UserDAO;
import com.expensesplitter.models.Expense;
import com.expensesplitter.models.Group;
import com.expensesplitter.models.Settlement;
import com.expensesplitter.models.User;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import java.util.*;
import java.util.List;

public class SettlementFrame extends JPanel {

    private JPanel settlementsPanel;
    private JLabel totalOwedLabel;
    private JLabel totalOwingLabel;
    private JLabel pendingCountLabel;

    private final GroupDAO      groupDAO      = new GroupDAO();
    private final ExpenseDAO    expenseDAO    = new ExpenseDAO();
    private final SettlementDAO settlementDAO = new SettlementDAO();
    private final UserDAO       userDAO       = new UserDAO();

    public SettlementFrame() {
        setLayout(new BorderLayout());
        setOpaque(false);
        add(createMainContent(), BorderLayout.CENTER);
        refreshSettlements();
    }

    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(28, 30, 28, 30));
        main.add(createHeader(), BorderLayout.NORTH);
        main.add(createBody(),   BorderLayout.CENTER);
        return main;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 16));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 24, 0));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JPanel titleLeft = new JPanel();
        titleLeft.setLayout(new BoxLayout(titleLeft, BoxLayout.Y_AXIS));
        titleLeft.setOpaque(false);

        JLabel title = new JLabel("Settlements");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(UiStyles.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Track and resolve outstanding balances across all groups");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(UiStyles.TEXT_MUTED);

        titleLeft.add(title);
        titleLeft.add(Box.createVerticalStrut(3));
        titleLeft.add(subtitle);

        JButton refreshBtn = new JButton();
        refreshBtn.setIcon(FontIcon.of(FontAwesomeSolid.SYNC_ALT, 14, UiStyles.TEXT_SECONDARY));
        refreshBtn.setToolTipText("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.setForeground(UiStyles.TEXT_SECONDARY);
        refreshBtn.setBackground(UiStyles.CARD_BG);
        refreshBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiStyles.CARD_BORDER),
                new EmptyBorder(6, 10, 6, 10)));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshSettlements());

        titleRow.add(titleLeft,  BorderLayout.WEST);
        titleRow.add(refreshBtn, BorderLayout.EAST);

        header.add(titleRow,          BorderLayout.NORTH);
        header.add(createSummaryCards(), BorderLayout.CENTER);
        return header;
    }

    private JPanel createSummaryCards() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(12, 0, 0, 0));

        totalOwingLabel   = new JLabel("Rs. 0.00");
        totalOwedLabel    = new JLabel("Rs. 0.00");
        pendingCountLabel = new JLabel("0 items");

        row.add(summaryCard("YOU OWE",     totalOwingLabel,   FontAwesomeSolid.ARROW_CIRCLE_UP,
                new Color(254, 242, 242), new Color(220, 38, 38),  new Color(254, 202, 202)));
        row.add(summaryCard("OWED TO YOU", totalOwedLabel,    FontAwesomeSolid.ARROW_CIRCLE_DOWN,
                new Color(236, 253, 245), new Color(5, 150, 105),  new Color(167, 243, 208)));
        row.add(summaryCard("PENDING",     pendingCountLabel, FontAwesomeSolid.CLOCK,
                new Color(255, 251, 235), new Color(217, 119, 6),  new Color(253, 230, 138)));
        return row;
    }

    private JPanel summaryCard(String label, JLabel valueLabel, org.kordamp.ikonli.Ikon icon,
                               Color bg, Color fg, Color iconBg) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(iconBg, 1),
                new EmptyBorder(16, 18, 16, 18)));

        JPanel iconCircle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconCircle.setPreferredSize(new Dimension(40, 40));
        iconCircle.setOpaque(false);
        iconCircle.setLayout(new GridBagLayout());
        iconCircle.add(new JLabel(FontIcon.of(icon, 16, fg)));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(fg);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(fg);

        textPanel.add(lbl);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(valueLabel);

        card.add(iconCircle, BorderLayout.WEST);
        card.add(textPanel,  BorderLayout.CENTER);
        return card;
    }

    private JPanel createBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);

        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setOpaque(false);
        sectionHeader.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        left.add(new JLabel(FontIcon.of(FontAwesomeSolid.LIST_ALT, 14, UiStyles.TEXT_PRIMARY)));
        JLabel sectionTitle = new JLabel("Outstanding Balances");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        sectionTitle.setForeground(UiStyles.TEXT_PRIMARY);
        left.add(sectionTitle);

        sectionHeader.add(left, BorderLayout.WEST);

        settlementsPanel = new JPanel();
        settlementsPanel.setLayout(new BoxLayout(settlementsPanel, BoxLayout.Y_AXIS));
        settlementsPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(settlementsPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UiStyles.TABLE_HEADER_BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        body.add(sectionHeader, BorderLayout.NORTH);
        body.add(scroll,        BorderLayout.CENTER);
        return body;
    }

    public void refreshSettlements() {
        settlementsPanel.removeAll();

        User user = Session.getCurrentUser();
        if (user == null) {
            settlementsPanel.add(createEmptyState("Please log in to view settlements.", FontAwesomeSolid.LOCK));
            finish();
            return;
        }

        List<Group> userGroups = groupDAO.getGroupsByUser(user.getId());
        if (userGroups.isEmpty()) {
            settlementsPanel.add(createEmptyState("No groups yet. Create a group to get started.", FontAwesomeSolid.USERS));
            finish();
            return;
        }

        double totalOwing = 0, totalOwed = 0;
        int pendingCount = 0;
        boolean anyCard = false;

        for (Group group : userGroups) {
            Map<Integer, Double> balances = calculateNetBalances(group.getId(), user.getId());
            if (balances.isEmpty()) continue;

            List<User> members = groupDAO.getGroupMembers(group.getId());
            List<JPanel> cards = new ArrayList<>();

            for (User member : members) {
                if (member.getId() == user.getId()) continue;
                Double balance = balances.get(member.getId());
                if (balance == null || Math.abs(balance) <= 0.01) continue;

                cards.add(createSettlementCard(member, balance, user.getId(), group.getId()));
                pendingCount++;
                if (balance > 0) totalOwed  += balance;
                else             totalOwing += Math.abs(balance);
            }

            if (!cards.isEmpty()) {
                anyCard = true;
                settlementsPanel.add(createGroupSection(group, cards));
                settlementsPanel.add(Box.createVerticalStrut(16));
            }
        }

        if (!anyCard) {
            settlementsPanel.add(createEmptyState("All settled up! No outstanding balances.", FontAwesomeSolid.CHECK_CIRCLE));
        }

        totalOwingLabel.setText("Rs. " + String.format("%.2f", totalOwing));
        totalOwedLabel.setText("Rs. "  + String.format("%.2f", totalOwed));
        pendingCountLabel.setText(pendingCount + " item" + (pendingCount != 1 ? "s" : ""));

        finish();
    }

    private void finish() {
        settlementsPanel.revalidate();
        settlementsPanel.repaint();
    }

    private JPanel createGroupSection(Group group, List<JPanel> cards) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel groupHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        groupHeader.setOpaque(false);
        groupHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        groupHeader.add(new JLabel(FontIcon.of(FontAwesomeSolid.FOLDER_OPEN, 13, UiStyles.STATUS_WARNING)));
        JLabel groupName = new JLabel(group.getName());
        groupName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        groupName.setForeground(UiStyles.TEXT_PRIMARY);
        groupHeader.add(groupName);
        groupHeader.add(new JLabel(FontIcon.of(FontAwesomeSolid.CIRCLE, 4, UiStyles.TEXT_MUTED)));
        JLabel cnt = new JLabel(cards.size() + " pending");
        cnt.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cnt.setForeground(UiStyles.TEXT_MUTED);
        groupHeader.add(cnt);

        section.add(groupHeader);
        section.add(Box.createVerticalStrut(6));
        for (JPanel card : cards) {
            card.setAlignmentX(Component.LEFT_ALIGNMENT);
            section.add(card);
            section.add(Box.createVerticalStrut(8));
        }
        return section;
    }

    private Map<Integer, Double> calculateNetBalances(int groupId, int userId) {
        List<User>       members  = groupDAO.getGroupMembers(groupId);
        List<Expense>    expenses = expenseDAO.getExpensesByGroup(groupId);
        List<Settlement> settled  = settlementDAO.getSettlementsByGroup(groupId);

        Map<Integer, Double> balances = new HashMap<>();
        for (User m : members) balances.put(m.getId(), 0.0);

        int memberCount = members.size();
        if (memberCount == 0) return balances;

        for (Expense expense : expenses) {
            double share  = expense.getAmount() / memberCount;
            int    paidBy = expense.getPaidBy();

            if (paidBy == userId) {
                for (User m : members)
                    if (m.getId() != userId)
                        balances.merge(m.getId(), share, Double::sum);
            } else {
                if (balances.containsKey(paidBy))
                    balances.merge(paidBy, -share, Double::sum);
            }
        }

        for (Settlement s : settled) {
            int payer = s.getPayerId(), payee = s.getPayeeId();
            double amt = s.getAmount();
            if (payee == userId && balances.containsKey(payer))
                balances.merge(payer, -amt, Double::sum);
            else if (payer == userId && balances.containsKey(payee))
                balances.merge(payee,  amt, Double::sum);
        }

        balances.entrySet().removeIf(e -> Math.abs(e.getValue()) <= 0.01);
        return balances;
    }

    private JPanel createSettlementCard(User otherUser, double balance, int userId, int groupId) {
        boolean userOwes  = balance < 0;
        double  absAmount = Math.abs(balance);
        Color accentColor = userOwes ? new Color(220, 38, 38) : new Color(5, 150, 105);
        Color stripColor  = userOwes ? new Color(254, 202, 202) : new Color(167, 243, 208);
        Color cardBg      = userOwes ? new Color(255, 252, 252) : new Color(252, 255, 253);

        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cardBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(stripColor);
                g2.fillRoundRect(0, 0, 5, getHeight(), 3, 3);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(stripColor, 1),
                new EmptyBorder(14, 20, 14, 14)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JPanel avatarPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(userOwes ? new Color(254, 226, 226) : new Color(209, 250, 229));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(accentColor);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                String ch = String.valueOf(otherUser.getName().charAt(0)).toUpperCase();
                g2.drawString(ch, (getWidth() - fm.stringWidth(ch)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatarPanel.setPreferredSize(new Dimension(42, 42));
        avatarPanel.setOpaque(false);

        JPanel avatarWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        avatarWrapper.setOpaque(false);
        avatarWrapper.setBorder(new EmptyBorder(0, 0, 0, 14));
        avatarWrapper.add(avatarPanel);

        JLabel nameLabel = new JLabel(otherUser.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(UiStyles.TEXT_PRIMARY);

        JPanel dirRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        dirRow.setOpaque(false);
        dirRow.add(new JLabel(FontIcon.of(
                userOwes ? FontAwesomeSolid.LONG_ARROW_ALT_RIGHT : FontAwesomeSolid.LONG_ARROW_ALT_LEFT,
                11, accentColor)));
        JLabel dirLabel = new JLabel(userOwes
                ? "You owe " + otherUser.getName()
                : otherUser.getName() + " owes you");
        dirLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dirLabel.setForeground(UiStyles.TEXT_MUTED);
        dirRow.add(dirLabel);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(dirRow);

        JLabel amountLabel = new JLabel("Rs. " + String.format("%.2f", absAmount));
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        amountLabel.setForeground(accentColor);

        JPanel rightPanel = new JPanel(new BorderLayout(0, 6));
        rightPanel.setOpaque(false);

        JPanel amountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        amountPanel.setOpaque(false);
        amountPanel.add(amountLabel);
        rightPanel.add(amountPanel, BorderLayout.NORTH);

        if (!userOwes) {
            JButton settleBtn = new JButton("  Mark Settled") {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover() ? new Color(4, 120, 87) : accentColor);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            settleBtn.setIcon(FontIcon.of(FontAwesomeSolid.CHECK_CIRCLE, 12, Color.WHITE));
            settleBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            settleBtn.setForeground(Color.WHITE);
            settleBtn.setContentAreaFilled(false);
            settleBtn.setBorderPainted(false);
            settleBtn.setFocusPainted(false);
            settleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            settleBtn.setBorder(new EmptyBorder(5, 10, 5, 12));
            settleBtn.addActionListener(e -> recordSettlement(userId, otherUser.getId(), groupId, absAmount));

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            btnPanel.setOpaque(false);
            btnPanel.add(settleBtn);
            rightPanel.add(btnPanel, BorderLayout.SOUTH);
        } else {
            JLabel badge = new JLabel("  PENDING");
            badge.setIcon(FontIcon.of(FontAwesomeSolid.HOURGLASS_HALF, 9, accentColor));
            badge.setFont(new Font("Segoe UI", Font.BOLD, 9));
            badge.setForeground(accentColor);
            badge.setOpaque(true);
            badge.setBackground(new Color(254, 226, 226));
            badge.setBorder(new EmptyBorder(3, 7, 3, 7));
            JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            badgePanel.setOpaque(false);
            badgePanel.add(badge);
            rightPanel.add(badgePanel, BorderLayout.SOUTH);
        }

        card.add(avatarWrapper, BorderLayout.WEST);
        card.add(textPanel,     BorderLayout.CENTER);
        card.add(rightPanel,    BorderLayout.EAST);
        return card;
    }

    private void recordSettlement(int payerId, int payeeId, int groupId, double amount) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Record settlement of Rs. " + String.format("%.2f", amount) + "?",
                "Confirm Settlement", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        Settlement s = new Settlement(groupId, payerId, payeeId, amount, new Date(System.currentTimeMillis()));
        String error = settlementDAO.addSettlementWithMessage(s);
        if (error == null) {
            JOptionPane.showMessageDialog(this, "Settlement recorded successfully!", "Done", JOptionPane.INFORMATION_MESSAGE);
            refreshSettlements();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to record settlement.\n" + error, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createEmptyState(String text, org.kordamp.ikonli.Ikon icon) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(60, 0, 60, 0));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconLabel = new JLabel(FontIcon.of(icon, 48, new Color(203, 213, 225)));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel(text);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msg.setForeground(new Color(148, 163, 184));
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);
        msg.setBorder(new EmptyBorder(12, 0, 0, 0));

        panel.add(iconLabel);
        panel.add(msg);
        return panel;
    }
}