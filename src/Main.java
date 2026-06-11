// ============================================================
//  Expense Splitter — Entry Point
//  Course: Software Construction & Development
// ============================================================

import com.expensesplitter.ui.LoginFrame;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

public class Main {

    public static void main(String[] args) {
        FlatMacLightLaf.setup();
         LoginFrame loginFrame = new LoginFrame();
         loginFrame.setVisible(true);
        System.out.println("Expense Splitter Application Starting...");
    }
}
