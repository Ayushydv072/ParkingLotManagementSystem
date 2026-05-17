package gui;

import service.ParkingService;
import util.*;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window – hosts the sidebar and the card panel
 * that switches between dashboard, entry, exit, and admin views.
 */
public class MainWindow extends JFrame {

    private final ParkingService service;

    private SidebarPanel sidebar;
    private JPanel cardPanel;
    private CardLayout cardLayout;

    private DashboardPanel dashboardPanel;
    private EntryPanel     entryPanel;
    private ExitPanel      exitPanel;
    private AdminPanel     adminPanel;

    // Card names
    private static final String CARD_DASHBOARD = "dashboard";
    private static final String CARD_ENTRY     = "entry";
    private static final String CARD_EXIT      = "exit";
    private static final String CARD_ADMIN     = "admin";

    public MainWindow() {
        this.service = new ParkingService();
        initUI();
    }

    private void initUI() {
        setTitle("Parking Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setPreferredSize(new Dimension(1280, 800));
        setLocationRelativeTo(null);

        // Try to set dark title bar on modern JDKs
        try {
            getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
        } catch (Exception ignored) {}

        getContentPane().setBackground(UITheme.BG_DARK);
        getContentPane().setLayout(new BorderLayout());

        // Sidebar
        sidebar = new SidebarPanel();
        sidebar.setOnSelect(this::switchPanel);

        // Card area
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(UITheme.BG_DARK);

        dashboardPanel = new DashboardPanel(service);
        entryPanel     = new EntryPanel(service);
        exitPanel      = new ExitPanel(service);
        adminPanel     = new AdminPanel(service);

        cardPanel.add(dashboardPanel, CARD_DASHBOARD);
        cardPanel.add(entryPanel,     CARD_ENTRY);
        cardPanel.add(exitPanel,      CARD_EXIT);
        cardPanel.add(adminPanel,     CARD_ADMIN);

        getContentPane().add(sidebar,   BorderLayout.WEST);
        getContentPane().add(cardPanel, BorderLayout.CENTER);

        switchPanel(0);
        pack();
        setVisible(true);
    }

    private void switchPanel(int index) {
        sidebar.setSelected(index);
        switch (index) {
            case 0 -> {
                dashboardPanel.refresh();
                cardLayout.show(cardPanel, CARD_DASHBOARD);
            }
            case 1 -> {
                entryPanel.refreshAvailability();
                cardLayout.show(cardPanel, CARD_ENTRY);
            }
            case 2 -> cardLayout.show(cardPanel, CARD_EXIT);
            case 3 -> cardLayout.show(cardPanel, CARD_ADMIN);
        }
    }
}
