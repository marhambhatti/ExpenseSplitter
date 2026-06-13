package com.expensesplitter;

import com.expensesplitter.ui.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
// Main Class
public class Main {
    public static void main(String[] args) {
        // Running UI (Log In Screen Shows First)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
