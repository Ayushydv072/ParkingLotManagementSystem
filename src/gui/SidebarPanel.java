package gui;

import util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Left sidebar navigation panel with nav items and a header logo.
 */
public class SidebarPanel extends JPanel {

    public record NavItem(String icon, String label, int index) {}

    private final List<JPanel> navButtons = new ArrayList<>();
    private int selectedIndex = 0;
    private Consumer<Integer> onSelect;

    public SidebarPanel() {
        setPreferredSize(new Dimension(UITheme.SIDEBAR_W, 0));
        setBackground(UITheme.BG_SIDEBAR);
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        // Logo area
        JPanel logo = new JPanel(new BorderLayout());
        logo.setBackground(UITheme.BG_SIDEBAR);
        logo.setBorder(BorderFactory.createEmptyBorder(28, 16, 28, 16));

        JLabel icon = new JLabel("🅿");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

        JLabel name = new JLabel("<html><b>PARK</b><br><span style='color:#8B949E;font-size:10px'>MANAGEMENT SYSTEM</span></html>");
        name.setFont(UITheme.FONT_BODY);
        name.setForeground(UITheme.ACCENT_AMBER);

        logo.add(icon, BorderLayout.WEST);
        logo.add(name, BorderLayout.CENTER);

        // Divider
        JSeparator sep = UIComponents.divider();

        // Nav items
        JPanel nav = new JPanel();
        nav.setBackground(UITheme.BG_SIDEBAR);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        NavItem[] items = {
            new NavItem("🏠", "Dashboard",   0),
            new NavItem("🚗", "Entry Gate",  1),
            new NavItem("🚪", "Exit Gate",   2),
            new NavItem("⚙",  "Admin Panel", 3),
        };

        for (NavItem item : items) {
            JPanel btn = createNavButton(item);
            navButtons.add(btn);
            nav.add(btn);
            nav.add(Box.createVerticalStrut(4));
        }

        // Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UITheme.BG_SIDEBAR);
        footer.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        JLabel versionLbl = new JLabel("v1.0.0  •  ParkSys");
        versionLbl.setFont(UITheme.FONT_SMALL);
        versionLbl.setForeground(UITheme.TEXT_MUTED);

        footer.add(versionLbl, BorderLayout.SOUTH);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UITheme.BG_SIDEBAR);
        top.add(logo, BorderLayout.CENTER);
        top.add(sep,  BorderLayout.SOUTH);

        add(top,   BorderLayout.NORTH);
        add(nav,   BorderLayout.CENTER);
        add(footer,BorderLayout.SOUTH);

        setSelected(0);
    }

    private JPanel createNavButton(NavItem item) {
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel = (navButtons.indexOf(this) == selectedIndex);
                boolean hov = getMousePosition() != null;
                if (sel) {
                    g2.setColor(new Color(0xF59E0B, true));
                    g2.setColor(new Color(0x1E1600));
                    g2.fillRoundRect(6, 2, getWidth()-12, getHeight()-4, 8, 8);
                    // left accent bar
                    g2.setColor(UITheme.ACCENT_AMBER);
                    g2.fillRoundRect(0, 4, 4, getHeight()-8, 4, 4);
                } else if (hov) {
                    g2.setColor(UITheme.HOVER);
                    g2.fillRoundRect(6, 2, getWidth()-12, getHeight()-4, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLbl = new JLabel(item.icon());
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        JLabel textLbl = new JLabel(item.label());
        textLbl.setFont(UITheme.FONT_NAV);
        textLbl.setForeground(UITheme.TEXT_PRIMARY);

        btn.add(iconLbl);
        btn.add(textLbl);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                setSelected(item.index());
                if (onSelect != null) onSelect.accept(item.index());
            }
            @Override public void mouseEntered(MouseEvent e) { btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.repaint(); }
        });

        return btn;
    }

    public void setSelected(int index) {
        this.selectedIndex = index;
        navButtons.forEach(JPanel::repaint);
    }

    public void setOnSelect(Consumer<Integer> handler) { this.onSelect = handler; }
}
