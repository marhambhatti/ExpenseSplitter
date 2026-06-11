// REDESIGNED: Premium Deep Navy + Emerald + Warm Slate color system
package com.expensesplitter.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class UiStyles {

    private UiStyles() {}

    // ── COLOR PALETTE ─────────────────────────────────────────────────
    // Sidebar
    public static final Color SIDEBAR_BG        = new Color(15, 23, 42);    // Deep navy
    public static final Color SIDEBAR_ACTIVE_BG  = new Color(30, 41, 59);   // Slightly lighter navy
    public static final Color SIDEBAR_ACTIVE_FG  = new Color(52, 211, 153); // Emerald accent
    public static final Color SIDEBAR_FG         = new Color(148, 163, 184);// Slate-400 text
    public static final Color SIDEBAR_HOVER_BG   = new Color(30, 41, 59);   // Hover layer
    public static final Color SIDEBAR_BORDER     = new Color(30, 41, 59);   // Subtle divider
    public static final Color SIDEBAR_LOGO_FG    = new Color(52, 211, 153); // Emerald logo
    public static final Color SIDEBAR_SUB_FG     = new Color(71, 85, 105);  // Muted subtitle

    // Top Bar
    public static final Color TOPBAR_BG         = new Color(255, 255, 255); // Clean white
    public static final Color TOPBAR_BORDER      = new Color(226, 232, 240);// Slate-200
    public static final Color TOPBAR_TITLE_FG    = new Color(15, 23, 42);   // Deep navy text
    public static final Color TOPBAR_SEARCH_BG   = new Color(248, 250, 252);// Near-white input
    public static final Color TOPBAR_SEARCH_BORDER = new Color(203, 213, 225);

    // Main Content & Background
    public static final Color CONTENT_BG         = new Color(248, 250, 252);// Warm off-white
    public static final Color CARD_BG             = new Color(255, 255, 255);
    public static final Color CARD_BORDER         = new Color(226, 232, 240);// Slate-200
    public static final Color CARD_HOVER_BORDER   = new Color(148, 163, 184);

    // Metric Cards
    public static final Color METRIC_DANGER_BG   = new Color(254, 242, 242);// Rose-50
    public static final Color METRIC_DANGER_FG   = new Color(220, 38, 38);  // Rose-600
    public static final Color METRIC_DANGER_ACCENT= new Color(254, 202, 202);
    public static final Color METRIC_SUCCESS_BG  = new Color(236, 253, 245);// Emerald-50
    public static final Color METRIC_SUCCESS_FG  = new Color(5, 150, 105);  // Emerald-600
    public static final Color METRIC_SUCCESS_ACCENT= new Color(167, 243, 208);
    public static final Color METRIC_NEUTRAL_BG  = new Color(241, 245, 249);// Slate-100
    public static final Color METRIC_NEUTRAL_FG  = new Color(15, 23, 42);   // Navy

    // Accent / Primary
    public static final Color ACCENT_PRIMARY     = new Color(5, 150, 105);  // Emerald-600
    public static final Color ACCENT_PRIMARY_HOVER = new Color(4, 120, 87); // Emerald-700
    public static final Color ACCENT_LINK        = new Color(5, 150, 105);

    // Text
    public static final Color TEXT_PRIMARY       = new Color(15, 23, 42);   // Near-black navy
    public static final Color TEXT_SECONDARY     = new Color(71, 85, 105);  // Slate-600
    public static final Color TEXT_MUTED         = new Color(148, 163, 184);// Slate-400
    public static final Color TEXT_LABEL         = new Color(100, 116, 139);// Slate-500

    // Tables
    public static final Color TABLE_HEADER_BG    = new Color(248, 250, 252);
    public static final Color TABLE_GRID         = new Color(226, 232, 240);
    public static final Color TABLE_ROW_ALT      = new Color(249, 250, 251);

    // Status
    public static final Color STATUS_SUCCESS     = new Color(5, 150, 105);
    public static final Color STATUS_DANGER      = new Color(220, 38, 38);
    public static final Color STATUS_WARNING     = new Color(217, 119, 6);
    public static final Color STATUS_INFO        = new Color(37, 99, 235);

    // ── BUTTON FACTORIES ──────────────────────────────────────────────
    public static JButton primaryButton(String text, Runnable action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Subtle drop-shadow
                if (!getModel().isPressed()) {
                    g2.setColor(new Color(5, 150, 105, 40));
                    g2.fillRoundRect(2, 4, getWidth() - 2, getHeight() - 2, 10, 10);
                }
                Color bg = getModel().isPressed() ? ACCENT_PRIMARY_HOVER
                         : getModel().isRollover() ? ACCENT_PRIMARY_HOVER : ACCENT_PRIMARY;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(10, 22, 12, 22));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (action != null) {
            btn.addActionListener(e -> action.run());
        }
        return btn;
    }

    public static JButton outlineButton(String text, Runnable action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(248, 250, 252) : CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(getModel().isRollover() ? TEXT_SECONDARY : CARD_BORDER);
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(TEXT_SECONDARY);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(9, 18, 11, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (action != null) {
            btn.addActionListener(e -> action.run());
        }
        return btn;
    }
}
