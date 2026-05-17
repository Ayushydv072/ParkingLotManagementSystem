package gui;

import db.ParkingDatabase;
import model.*;
import service.ParkingService;
import util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Administration panel: pricing policies, slot management, and reports.
 */
public class AdminPanel extends JPanel {

    private final ParkingService service;
    private final ParkingDatabase db;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private JTabbedPane tabs;

    // Pricing tab fields
    private JTextField twBase, twHourly, twPenalty;
    private JTextField fwBase, fwHourly, fwPenalty;
    private JTextField hvBase, hvHourly, hvPenalty;

    // Report tab
    private DefaultTableModel reportModel;
    private JLabel reportRevLbl, reportCountLbl;

    public AdminPanel(ParkingService service) {
        this.service = service;
        this.db      = service.getDb();
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        build();
    }

    private void build() {
        JLabel header = new JLabel("ADMIN PANEL");
        header.setFont(UITheme.FONT_HEADER);
        header.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Manage pricing, slots, and generate reports");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_MUTED);

        JPanel hp = new JPanel(new GridLayout(2, 1, 0, 4));
        hp.setOpaque(false);
        hp.add(header); hp.add(sub);
        add(hp, BorderLayout.NORTH);

        tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_TITLE);
        tabs.setBackground(UITheme.BG_CARD);
        tabs.setForeground(UITheme.TEXT_PRIMARY);
        tabs.addTab("💰  Pricing Policies", buildPricingTab());
        tabs.addTab("🅿  Slot Management",  buildSlotTab());
        tabs.addTab("📊  Reports",          buildReportTab());

        // Style tabs
        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override protected void installDefaults() {
                super.installDefaults();
                highlight = UITheme.ACCENT_AMBER;
                lightHighlight = UITheme.BG_CARD;
                shadow = UITheme.BORDER;
                darkShadow = UITheme.BG_DARK;
                focus = UITheme.ACCENT_AMBER;
            }
        });

        add(tabs, BorderLayout.CENTER);
    }

    // ─── Pricing Tab ─────────────────────────────────────────────────────────

    private JPanel buildPricingTab() {
        JPanel outer = new JPanel(new GridLayout(3, 1, 14, 14));
        outer.setOpaque(false);
        outer.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        PricingPolicy tw = db.getPricingPolicy(Vehicle.VehicleType.TWO_WHEELER);
        PricingPolicy fw = db.getPricingPolicy(Vehicle.VehicleType.FOUR_WHEELER);
        PricingPolicy hv = db.getPricingPolicy(Vehicle.VehicleType.HEAVY_VEHICLE);

        twBase    = new JTextField(String.valueOf(tw.getBaseCharge()));
        twHourly  = new JTextField(String.valueOf(tw.getHourlyRate()));
        twPenalty = new JTextField(String.valueOf(tw.getPenaltyPerHour()));

        fwBase    = new JTextField(String.valueOf(fw.getBaseCharge()));
        fwHourly  = new JTextField(String.valueOf(fw.getHourlyRate()));
        fwPenalty = new JTextField(String.valueOf(fw.getPenaltyPerHour()));

        hvBase    = new JTextField(String.valueOf(hv.getBaseCharge()));
        hvHourly  = new JTextField(String.valueOf(hv.getHourlyRate()));
        hvPenalty = new JTextField(String.valueOf(hv.getPenaltyPerHour()));

        outer.add(pricingCard("TWO-WHEELER",   UITheme.ACCENT_AMBER, twBase, twHourly, twPenalty));
        outer.add(pricingCard("FOUR-WHEELER",  UITheme.ACCENT_TEAL,  fwBase, fwHourly, fwPenalty));
        outer.add(pricingCard("HEAVY VEHICLE", UITheme.ACCENT_RED,   hvBase, hvHourly, hvPenalty));

        JScrollPane scroll = new JScrollPane(outer);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(UITheme.BG_DARK);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);
        wrapper.add(scroll, BorderLayout.CENTER);

        UIComponents.AccentButton saveBtn = new UIComponents.AccentButton(
                "SAVE ALL PRICING", UITheme.ACCENT_AMBER, UITheme.AMBER_DARK);
        saveBtn.addActionListener(e -> savePricing());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false);
        btnRow.add(saveBtn);
        wrapper.add(btnRow, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel pricingCard(String title, Color accent,
                                JTextField base, JTextField hourly, JTextField penalty) {
        UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(12, UITheme.BG_CARD);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 6, 4, 6);

        JLabel hdr = new JLabel("⬡  " + title);
        hdr.setFont(UITheme.FONT_TITLE);
        hdr.setForeground(accent);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 6; g.weightx = 1;
        card.add(hdr, g);

        g.gridwidth = 1; g.weightx = 0.15;

        String[] labels = {"Base Charge (₹)", "Hourly Rate (₹)", "Penalty/Hr (₹)"};
        JTextField[] fields = {base, hourly, penalty};
        for (int i = 0; i < 3; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(UITheme.FONT_SMALL);
            lbl.setForeground(UITheme.TEXT_MUTED);
            g.gridx = i * 2; g.gridy = 1; g.weightx = 0.2;
            card.add(lbl, g);

            styleInput(fields[i]);
            g.gridx = i * 2 + 1; g.weightx = 0.15;
            card.add(fields[i], g);
        }
        return card;
    }

    private void styleInput(JTextField f) {
        f.setFont(UITheme.FONT_MONO);
        f.setForeground(UITheme.TEXT_PRIMARY);
        f.setBackground(UITheme.BG_DARK);
        f.setCaretColor(UITheme.ACCENT_AMBER);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        f.setColumns(6);
    }

    private void savePricing() {
        try {
            db.getPricingPolicy(Vehicle.VehicleType.TWO_WHEELER)
              .setBaseCharge(Double.parseDouble(twBase.getText()));
            db.getPricingPolicy(Vehicle.VehicleType.TWO_WHEELER)
              .setHourlyRate(Double.parseDouble(twHourly.getText()));
            db.getPricingPolicy(Vehicle.VehicleType.TWO_WHEELER)
              .setPenaltyPerHour(Double.parseDouble(twPenalty.getText()));

            db.getPricingPolicy(Vehicle.VehicleType.FOUR_WHEELER)
              .setBaseCharge(Double.parseDouble(fwBase.getText()));
            db.getPricingPolicy(Vehicle.VehicleType.FOUR_WHEELER)
              .setHourlyRate(Double.parseDouble(fwHourly.getText()));
            db.getPricingPolicy(Vehicle.VehicleType.FOUR_WHEELER)
              .setPenaltyPerHour(Double.parseDouble(fwPenalty.getText()));

            db.getPricingPolicy(Vehicle.VehicleType.HEAVY_VEHICLE)
              .setBaseCharge(Double.parseDouble(hvBase.getText()));
            db.getPricingPolicy(Vehicle.VehicleType.HEAVY_VEHICLE)
              .setHourlyRate(Double.parseDouble(hvHourly.getText()));
            db.getPricingPolicy(Vehicle.VehicleType.HEAVY_VEHICLE)
              .setPenaltyPerHour(Double.parseDouble(hvPenalty.getText()));

            JOptionPane.showMessageDialog(this,
                    "Pricing policies updated successfully.",
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid number format. Please check all fields.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── Slot Management Tab ─────────────────────────────────────────────────

    private JPanel buildSlotTab() {
        JPanel outer = new JPanel(new BorderLayout(0, 14));
        outer.setOpaque(false);
        outer.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        String[] cols = {"Slot ID", "Suitable For", "Status", "Occupied By"};
        DefaultTableModel slotModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (ParkingSlot s : db.getAllSlots()) {
            slotModel.addRow(new Object[]{
                    s.getSlotId(),
                    s.getSuitableFor(),
                    s.getStatus(),
                    s.getOccupiedBy() != null ? s.getOccupiedBy().getVehicleNumber() : "-"
            });
        }

        JTable slotTable = new JTable(slotModel);
        styleTableComponent(slotTable);

        // Color rows by status
        slotTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                String status = (String) table.getValueAt(row, 2);
                if (!isSelected) {
                    c.setBackground("AVAILABLE".equals(status) ?
                            new Color(0x0F2A1A) : new Color(0x2A0F0F));
                    c.setForeground("AVAILABLE".equals(status) ?
                            UITheme.ACCENT_GREEN : UITheme.ACCENT_RED);
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(slotTable);
        scroll.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        scroll.getViewport().setBackground(UITheme.BG_CARD);

        UIComponents.AccentButton refreshBtn = new UIComponents.AccentButton(
                "REFRESH SLOT LIST", UITheme.HOVER, UITheme.BORDER);
        refreshBtn.setForeground(UITheme.ACCENT_TEAL);
        refreshBtn.addActionListener(e -> {
            slotModel.setRowCount(0);
            for (ParkingSlot s : db.getAllSlots()) {
                slotModel.addRow(new Object[]{
                        s.getSlotId(), s.getSuitableFor(), s.getStatus(),
                        s.getOccupiedBy() != null ? s.getOccupiedBy().getVehicleNumber() : "-"
                });
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.setOpaque(false);
        btnRow.add(refreshBtn);

        outer.add(scroll, BorderLayout.CENTER);
        outer.add(btnRow, BorderLayout.SOUTH);
        return outer;
    }

    // ─── Reports Tab ─────────────────────────────────────────────────────────

    private JPanel buildReportTab() {
        JPanel outer = new JPanel(new BorderLayout(0, 14));
        outer.setOpaque(false);
        outer.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // Summary row
        JPanel summary = new JPanel(new GridLayout(1, 2, 12, 0));
        summary.setOpaque(false);
        reportRevLbl   = new JLabel("₹ 0.00", SwingConstants.CENTER);
        reportCountLbl = new JLabel("0",       SwingConstants.CENTER);
        reportRevLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        reportRevLbl.setForeground(UITheme.ACCENT_AMBER);
        reportCountLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        reportCountLbl.setForeground(UITheme.ACCENT_TEAL);

        summary.add(summaryCard("TODAY REVENUE", reportRevLbl, UITheme.ACCENT_AMBER));
        summary.add(summaryCard("VEHICLES SERVED TODAY", reportCountLbl, UITheme.ACCENT_TEAL));
        summary.setPreferredSize(new Dimension(0, 90));

        // History table
        String[] cols = {"Ticket ID", "Vehicle", "Type", "Slot", "Entry", "Exit", "Duration", "Fee", "Payment"};
        reportModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable report = new JTable(reportModel);
        styleTableComponent(report);

        JScrollPane scroll = new JScrollPane(report);
        scroll.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        scroll.getViewport().setBackground(UITheme.BG_CARD);

        UIComponents.AccentButton refreshBtn = new UIComponents.AccentButton(
                "REFRESH REPORT", UITheme.ACCENT_AMBER, UITheme.AMBER_DARK);
        refreshBtn.addActionListener(e -> refreshReport());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.setOpaque(false);
        btnRow.add(refreshBtn);

        outer.add(summary, BorderLayout.NORTH);
        outer.add(scroll, BorderLayout.CENTER);
        outer.add(btnRow, BorderLayout.SOUTH);
        return outer;
    }

    private JPanel summaryCard(String title, JLabel valLbl, Color accent) {
        UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(12, UITheme.BG_CARD);
        card.setLayout(new BorderLayout(0, 4));
        card.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(UITheme.FONT_LABEL);
        t.setForeground(UITheme.TEXT_MUTED);

        card.add(t, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        return card;
    }

    private void refreshReport() {
        reportRevLbl.setText("₹ " + String.format("%.2f", db.getTodayRevenue()));
        reportCountLbl.setText(String.valueOf(db.getTotalVehiclesServedToday()));

        reportModel.setRowCount(0);
        for (ParkingTicket t : db.getHistory()) {
            long dur = t.getDurationMinutes();
            reportModel.addRow(new Object[]{
                    t.getTicketId(),
                    t.getVehicle().getVehicleNumber(),
                    t.getVehicle().getVehicleType(),
                    t.getSlot().getSlotId(),
                    t.getEntryTime() != null ? t.getEntryTime().format(fmt) : "-",
                    t.getExitTime()  != null ? t.getExitTime().format(fmt)  : "-",
                    (dur / 60) + "h " + (dur % 60) + "m",
                    "₹ " + String.format("%.2f", t.getTotalFee()),
                    t.getPaymentMethod()
            });
        }
    }

    private void styleTableComponent(JTable table) {
        table.setFont(UITheme.FONT_BODY);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setBackground(UITheme.BG_CARD);
        table.setGridColor(UITheme.BORDER);
        table.setRowHeight(26);
        table.setSelectionBackground(UITheme.HOVER);
        table.setSelectionForeground(UITheme.ACCENT_AMBER);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(UITheme.FONT_LABEL);
        table.getTableHeader().setBackground(UITheme.BG_SIDEBAR);
        table.getTableHeader().setForeground(UITheme.TEXT_MUTED);
    }
}
