import gui.MainWindow;
import util.UITheme;

import javax.swing.*;

/**
 * Application entry point.
 * Bootstraps the Swing EDT and launches the main window.
 */

public class Main {
    public static void main(String[] args) {
        // Apply FlatLaf or fall back to Nimbus
        try {
            // Try FlatDarkLaf if on classpath
            Class<?> flat = Class.forName("com.formdev.flatlaf.FlatDarkLaf");
            UIManager.setLookAndFeel((javax.swing.LookAndFeel) flat.getDeclaredConstructor().newInstance());
        } catch (Exception ignored) {
            // Fallback: Nimbus with dark tweaks
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ex) {
                // last resort: default L&F
            }
        }
        try {
    db.DBManager.initSlotsIfEmpty();
    System.out.println("DB connected: " + db.DBConnection.getConnection());
     } catch (java.sql.SQLException e) {
    System.err.println("DB Connection Failed: " + e.getMessage());
    e.printStackTrace();
     }

        // Apply global UI defaults for a consistent dark theme
        UIManager.put("Panel.background",          UITheme.BG_DARK);
        UIManager.put("OptionPane.background",     UITheme.BG_CARD);
        UIManager.put("OptionPane.messageForeground", UITheme.TEXT_PRIMARY);
        UIManager.put("Button.background",         UITheme.BG_CARD);
        UIManager.put("Button.foreground",         UITheme.TEXT_PRIMARY);
        UIManager.put("TextField.background",      UITheme.BG_DARK);
        UIManager.put("TextField.foreground",      UITheme.TEXT_PRIMARY);
        UIManager.put("ComboBox.background",       UITheme.BG_DARK);
        UIManager.put("ComboBox.foreground",       UITheme.TEXT_PRIMARY);
        UIManager.put("ScrollPane.background",     UITheme.BG_DARK);
        UIManager.put("TextArea.background",       UITheme.BG_DARK);
        UIManager.put("TextArea.foreground",       UITheme.TEXT_PRIMARY);
        UIManager.put("TabbedPane.background",     UITheme.BG_CARD);
        UIManager.put("TabbedPane.foreground",     UITheme.TEXT_PRIMARY);
        UIManager.put("TabbedPane.selected",       UITheme.BG_DARK);
        UIManager.put("Table.background",          UITheme.BG_CARD);
        UIManager.put("Table.foreground",          UITheme.TEXT_PRIMARY);
        UIManager.put("TableHeader.background",    UITheme.BG_SIDEBAR);
        UIManager.put("TableHeader.foreground",    UITheme.TEXT_MUTED);
        UIManager.put("ScrollBar.thumb",           UITheme.BORDER);
        UIManager.put("ScrollBar.track",           UITheme.BG_DARK);



        SwingUtilities.invokeLater(MainWindow::new);
    }
}
