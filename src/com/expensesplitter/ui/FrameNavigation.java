// FIXED: Shared navigation helpers for sidebar links and logout
package com.expensesplitter.ui;

import com.expensesplitter.util.Session;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class FrameNavigation {

    private FrameNavigation() {}

    public static void open(JFrame current, JFrame next) {
        current.dispose();
        SwingUtilities.invokeLater(() -> next.setVisible(true));
    }

    public static void logout(JFrame current) {
        Session.clear();
        current.dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
