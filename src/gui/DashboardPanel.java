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
 * Real-time dashboard showing slot occupancy, live vehicles, and KPIs.
 */
public class DashboardPanel extends JPanel {

    private final ParkingService service;
    private final ParkingDatabase db;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm  dd/MM");

    // Stat labels
    private JLabel totalSlotsLbl, availLbl, occupiedLbl, revenueLbl,
                   todayVehiclesLbl, queueLbl;

    // Slot grid
    private JPanel slotGrid;

    // Active vehicles table
    private DefaultTableModel tableModel;

    public DashboardPanel(ParkingService service) {
        this.service = service;
        this.db      = service.getDb();
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        build();
        startAutoRefresh();
    }

    private void build() {
        // Header
        JLabel header = new JLabel("ADMIN DASHBOARD");
        header.setFont(UITheme.FONT_HEADER);
        header.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Real-time parking lot overview");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_MUTED);

        JPanel hp = new JPanel(new GridLayout(2, 1, 0, 4));
        hp.setOpaque(false);
        hp.add(header); hp.add(sub);
        add(hp, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(14, 14));
        content.setOpaque(false);

        content.add(buildKpiRow(),      BorderLayout.NORTH);
        content.add(buildSlotSection(), BorderLayout.CENTER);
        content.add(buildVehicleTable(),BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);
    }

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 6, 12, 0));
        row.setOpaque(false);

        totalSlotsLbl    = kpiValueLabel("22", UITheme.TEXT_PRIMARY);
        availLbl         = kpiValueLabel("22", UITheme.ACCENT_GREEN);
        occupiedLbl      = kpiValueLabel("0",  UITheme.ACCENT_RED);
        revenueLbl       = kpiValueLabel("₹0", UITheme.ACCENT_AMBER);
        todayVehiclesLbl = kpiValueLabel("0",  UITheme.ACCENT_TEAL);
        queueLbl         = kpiValueLabel("0",  UITheme.ACCENT_RED);

        row.add(kpiCard("TOTAL SLOTS",    totalSlotsLbl,    UITheme.BORDER));
        row.add(kpiCard("AVAILABLE",      availLbl,         UITheme.ACCENT_GREEN));
        row.add(kpiCard("OCCUPIED",       occupiedLbl,      UITheme.ACCENT_RED));
        row.add(kpiCard("TODAY REVENUE",  revenueLbl,       UITheme.ACCENT_AMBER));
        row.add(kpiCard("TODAY VEHICLES", todayVehiclesLbl, UITheme.ACCENT_TEAL));
        row.add(kpiCard("WAITING QUEUE",  queueLbl,         UITheme.ACCENT_RED));

        return row;
    }

    private JLabel kpiValueLabel(String val, Color color) {
        JLabel lbl = new JLabel(val, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lbl.setForeground(color);
        return lbl;
    }

    private JPanel kpiCard(String title, JLabel valLbl, Color accent) {
        UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(12, UITheme.BG_CARD);
        card.setLayout(new GridLayout(3, 1, 0, 2));
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel stripe = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 30, 3, 3, 3);
                g2.dispose();
            }
        };
        stripe.setOpaque(false);
        stripe.setPreferredSize(new Dimension(30, 6));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        card.add(stripe);
        card.add(valLbl);
        card.add(titleLbl);
        return card;
    }

    private JPanel buildSlotSection() {
        UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(12, UITheme.BG_CARD);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Slot Map");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        legend.setOpaque(false);
        legend.add(legendDot(UITheme.ACCENT_GREEN, "Available"));
        legend.add(legendDot(UITheme.ACCENT_RED,   "Occupied"));
        legend.add(legendDot(UITheme.ACCENT_AMBER, "Two-Wheeler"));
        legend.add(legendDot(UITheme.ACCENT_TEAL,  "Four-Wheeler"));
        legend.add(legendDot(UITheme.TEXT_MUTED,   "Heavy"));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(title, BorderLayout.WEST);
        topRow.add(legend, BorderLayout.EAST);

        slotGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 8));
        slotGrid.setOpaque(false);

        JScrollPane scroll = new JScrollPane(slotGrid);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setPreferredSize(new Dimension(0, 160));

        card.add(topRow, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel legendDot(Color color, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, 10, 10);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(10, 10));
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MUTED);
        p.add(dot); p.add(lbl);
        return p;
    }

    private JPanel buildVehicleTable() {
        UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(12, UITheme.BG_CARD);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Active Vehicles");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        String[] cols = {"Ticket ID", "Vehicle No.", "Type", "Owner", "Slot", "Entry Time", "Duration"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel);
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        scroll.getViewport().setBackground(UITheme.BG_CARD);
        scroll.setPreferredSize(new Dimension(0, 180));

        card.add(title, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void styleTable(JTable table) {
        table.setFont(UITheme.FONT_BODY);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setBackground(UITheme.BG_CARD);
        table.setGridColor(UITheme.BORDER);
        table.setRowHeight(28);
        table.setSelectionBackground(UITheme.HOVER);
        table.setSelectionForeground(UITheme.ACCENT_AMBER);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(UITheme.FONT_LABEL);
        table.getTableHeader().setBackground(UITheme.BG_SIDEBAR);
        table.getTableHeader().setForeground(UITheme.TEXT_MUTED);
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
    }

    // ─── Refresh ─────────────────────────────────────────────────────────────

    public void refresh() {
        long total    = db.getAllSlots().size();
        long occupied = db.getAllSlots().stream().filter(s -> !s.isAvailable()).count();
        long avail    = total - occupied;

        totalSlotsLbl.setText(String.valueOf(total));
        availLbl.setText(String.valueOf(avail));
        occupiedLbl.setText(String.valueOf(occupied));
        revenueLbl.setText("₹" + String.format("%.0f", db.getTodayRevenue()));
        todayVehiclesLbl.setText(String.valueOf(db.getTotalVehiclesServedToday()));
        queueLbl.setText(String.valueOf(db.waitingQueueSize()));

        refreshSlotGrid();
        refreshTable();
    }

    private void refreshSlotGrid() {
        slotGrid.removeAll();
        for (ParkingSlot slot : db.getAllSlots()) {
            slotGrid.add(slotTile(slot));
        }
        slotGrid.revalidate();
        slotGrid.repaint();
    }

    private JPanel slotTile(ParkingSlot slot) {
        Color typeColor = switch (slot.getSuitableFor()) {
            case TWO_WHEELER   -> UITheme.ACCENT_AMBER;
            case FOUR_WHEELER  -> UITheme.ACCENT_TEAL;
            case HEAVY_VEHICLE -> UITheme.TEXT_MUTED;
        };
        Color bgColor = slot.isAvailable() ? new Color(0x0F2A1A) : new Color(0x2A0F0F);
        Color borderColor = slot.isAvailable() ? UITheme.ACCENT_GREEN : UITheme.ACCENT_RED;

        UIComponents.RoundedPanel tile = new UIComponents.RoundedPanel(6, bgColor) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                g2.dispose();
            }
        };
        tile.setLayout(new GridLayout(2, 1, 0, 2));
        tile.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        tile.setPreferredSize(new Dimension(70, 44));

        JLabel idLbl = new JLabel(slot.getSlotId(), SwingConstants.CENTER);
        idLbl.setFont(UITheme.FONT_LABEL);
        idLbl.setForeground(typeColor);

        JLabel statusLbl = new JLabel(slot.isAvailable() ? "FREE" : "BUSY", SwingConstants.CENTER);
        statusLbl.setFont(UITheme.FONT_SMALL);
        statusLbl.setForeground(borderColor);

        tile.add(idLbl);
        tile.add(statusLbl);

        if (!slot.isAvailable() && slot.getOccupiedBy() != null) {
            tile.setToolTipText("<html>" + slot.getOccupiedBy().getVehicleNumber() +
                    "<br>Owner: " + slot.getOccupiedBy().getOwnerName() + "</html>");
        }
        return tile;
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (ParkingTicket t : db.getAllActiveTickets()) {
            long dur = t.getDurationMinutes();
            tableModel.addRow(new Object[]{
                    t.getTicketId(),
                    t.getVehicle().getVehicleNumber(),
                    t.getVehicle().getVehicleType(),
                    t.getVehicle().getOwnerName(),
                    t.getSlot().getSlotId(),
                    t.getEntryTime().format(fmt),
                    (dur / 60) + "h " + (dur % 60) + "m"
            });
        }
    }

    private void startAutoRefresh() {
        javax.swing.Timer timer = new javax.swing.Timer(3000, e -> refresh());
        timer.start();
        refresh();
    }

    // Simple wrap-layout helper (used for slot grid)
    static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }
        @Override public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);
                int x = 0, y = insets.top + vgap, rowH = 0;
                for (Component c : target.getComponents()) {
                    if (!c.isVisible()) continue;
                    Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                    if (x + d.width > maxWidth) { y += rowH + vgap; x = 0; rowH = 0; }
                    x += d.width + hgap;
                    rowH = Math.max(rowH, d.height);
                }
                y += rowH + vgap + insets.bottom;
                return new Dimension(targetWidth, y);
            }
        }
    }
}