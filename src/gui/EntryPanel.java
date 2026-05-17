package gui;

import model.*;
import service.ParkingService;
import service.ParkingService.*;
import util.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;

/**
 * Entry gate panel – register incoming vehicles and issue tickets.
 */
public class EntryPanel extends JPanel {

    private final ParkingService service;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy  HH:mm:ss");

    // Form fields
    private JTextField vehicleNumberField;
    private JTextField ownerNameField;
    private JComboBox<String> vehicleTypeCombo;

    // Availability indicators
    private JLabel twAvailLabel, fwAvailLabel, hvAvailLabel;

    // Output area
    private JTextArea resultArea;

    public EntryPanel(ParkingService service) {
        this.service = service;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        build();
    }

    private void build() {
        // ── Header ────────────────────────────────────────────────────────────
        JLabel header = new JLabel("ENTRY GATE");
        header.setFont(UITheme.FONT_HEADER);
        header.setForeground(UITheme.ACCENT_AMBER);

        JLabel sub = new JLabel("Register an incoming vehicle and assign a slot");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_MUTED);

        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setOpaque(false);
        headerPanel.add(header);
        headerPanel.add(sub);
        add(headerPanel, BorderLayout.NORTH);

        // ── Center: form + availability ───────────────────────────────────────
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill      = GridBagConstraints.BOTH;
        gbc.insets    = new Insets(6, 6, 6, 6);
        gbc.weightx   = 1;

        // Left: form card
        UIComponents.RoundedPanel formCard = new UIComponents.RoundedPanel(12, UITheme.BG_CARD);
        formCard.setLayout(new GridBagLayout());
        formCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildForm(formCard);

        // Right: availability + result
        JPanel rightPanel = new JPanel(new BorderLayout(0, 14));
        rightPanel.setOpaque(false);
        rightPanel.add(buildAvailabilityCard(), BorderLayout.NORTH);
        rightPanel.add(buildResultArea(), BorderLayout.CENTER);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.55; gbc.weighty = 1;
        center.add(formCard, gbc);

        gbc.gridx = 1; gbc.weightx = 0.45;
        center.add(rightPanel, gbc);

        add(center, BorderLayout.CENTER);
    }

    private void buildForm(JPanel card) {
        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets  = new Insets(6, 0, 6, 0);

        // Title
        JLabel title = new JLabel("Vehicle Details");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        card.add(title, g);

        g.gridy = 1;
        card.add(UIComponents.divider(), g);

        // Vehicle Number
        g.gridy = 2; g.gridwidth = 1; g.weightx = 0.4;
        card.add(UIComponents.sectionLabel("Vehicle Number"), g);

        vehicleNumberField = UIComponents.styledField("e.g. MH12AB1234");
        g.gridx = 1; g.weightx = 0.6;
        card.add(vehicleNumberField, g);

        // Owner Name
        g.gridx = 0; g.gridy = 3; g.weightx = 0.4;
        card.add(UIComponents.sectionLabel("Owner Name"), g);

        ownerNameField = UIComponents.styledField("Full name");
        g.gridx = 1; g.weightx = 0.6;
        card.add(ownerNameField, g);

        // Vehicle Type
        g.gridx = 0; g.gridy = 4; g.weightx = 0.4;
        card.add(UIComponents.sectionLabel("Vehicle Type"), g);

        vehicleTypeCombo = UIComponents.styledCombo(new String[]{
                "TWO_WHEELER", "FOUR_WHEELER", "HEAVY_VEHICLE"});
        g.gridx = 1; g.weightx = 0.6;
        card.add(vehicleTypeCombo, g);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);

        UIComponents.AccentButton parkBtn  = new UIComponents.AccentButton(
                "PARK VEHICLE", UITheme.ACCENT_AMBER, UITheme.AMBER_DARK);
        UIComponents.AccentButton clearBtn = new UIComponents.AccentButton(
                "CLEAR", UITheme.HOVER, UITheme.BORDER);
        clearBtn.setForeground(UITheme.TEXT_MUTED);

        parkBtn.addActionListener(e -> parkVehicle());
        clearBtn.addActionListener(e -> clearForm());

        btnRow.add(parkBtn);
        btnRow.add(clearBtn);

        g.gridx = 0; g.gridy = 5; g.gridwidth = 2;
        g.insets = new Insets(16, 0, 0, 0);
        card.add(btnRow, g);
    }

    private JPanel buildAvailabilityCard() {
        UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(12, UITheme.BG_CARD);
        card.setLayout(new GridLayout(1, 3, 10, 0));
        card.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        twAvailLabel = availBadge("TWO-WHEELER", "0", UITheme.ACCENT_TEAL);
        fwAvailLabel = availBadge("FOUR-WHEELER","0", UITheme.ACCENT_AMBER);
        hvAvailLabel = availBadge("HEAVY",       "0", UITheme.ACCENT_RED);

        card.add(avlPanel("TWO-WHEELER",  twAvailLabel, UITheme.ACCENT_TEAL));
        card.add(avlPanel("FOUR-WHEELER", fwAvailLabel, UITheme.ACCENT_AMBER));
        card.add(avlPanel("HEAVY",        hvAvailLabel, UITheme.ACCENT_RED));

        refreshAvailability();
        return card;
    }

    private JPanel avlPanel(String title, JLabel valueLabel, Color color) {
        JPanel p = new JPanel(new GridLayout(3, 1, 0, 2));
        p.setOpaque(false);

        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(UITheme.FONT_SMALL);
        t.setForeground(UITheme.TEXT_MUTED);

        JLabel avl = new JLabel("AVAILABLE", SwingConstants.CENTER);
        avl.setFont(UITheme.FONT_SMALL);
        avl.setForeground(color);

        p.add(t);
        p.add(valueLabel);
        p.add(avl);
        return p;
    }

    private JLabel availBadge(String type, String value, Color color) {
        JLabel lbl = new JLabel(value, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lbl.setForeground(color);
        return lbl;
    }

    private JPanel buildResultArea() {
        UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(12, UITheme.BG_CARD);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Ticket / Status");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        resultArea = new JTextArea();
        resultArea.setFont(UITheme.FONT_MONO);
        resultArea.setForeground(UITheme.ACCENT_TEAL);
        resultArea.setBackground(UITheme.BG_DARK);
        resultArea.setEditable(false);
        resultArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        resultArea.setText("No activity yet.\nPark a vehicle to see the ticket details here.");

        JScrollPane scroll = new JScrollPane(resultArea);
        scroll.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        scroll.getViewport().setBackground(UITheme.BG_DARK);

        card.add(title, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ─── Actions ─────────────────────────────────────────────────────────────

    private void parkVehicle() {
        String number = vehicleNumberField.getText().trim();
        String owner  = ownerNameField.getText().trim();
        String typeStr = (String) vehicleTypeCombo.getSelectedItem();

        if (number.isEmpty() || owner.isEmpty()) {
            showError("Please fill all required fields.");
            return;
        }

        Vehicle.VehicleType type = Vehicle.VehicleType.valueOf(typeStr);
        Vehicle vehicle = new Vehicle(number, type, owner);
        ParkingResult result = service.parkVehicle(vehicle);

        switch (result.type) {
            case SUCCESS -> showTicket(result.ticket);
            case QUEUED  -> {
                resultArea.setForeground(UITheme.ACCENT_RED);
                resultArea.setText("⚠ PARKING LOT FULL\n\n" +
                        "Vehicle: " + number + "\n" +
                        "Added to waiting queue.\n" +
                        "Queue position: #" + result.queuePosition + "\n\n" +
                        "You will be notified when a slot becomes available.");
            }
            case ALREADY_PARKED -> {
                resultArea.setForeground(UITheme.ACCENT_AMBER);
                resultArea.setText("⚠ VEHICLE ALREADY PARKED\n\n" +
                        "Vehicle " + number + " is already in the lot.\n" +
                        "Ticket ID: " + result.ticket.getTicketId() + "\n" +
                        "Slot: " + result.ticket.getSlot().getSlotId());
            }
        }
        refreshAvailability();
    }

    private void showTicket(model.ParkingTicket ticket) {
        resultArea.setForeground(UITheme.ACCENT_TEAL);
        resultArea.setText(
                "✔ PARKING TICKET ISSUED\n" +
                "────────────────────────\n" +
                "Ticket ID  : " + ticket.getTicketId() + "\n" +
                "Vehicle    : " + ticket.getVehicle().getVehicleNumber() + "\n" +
                "Type       : " + ticket.getVehicle().getVehicleType() + "\n" +
                "Owner      : " + ticket.getVehicle().getOwnerName() + "\n" +
                "Slot       : " + ticket.getSlot().getSlotId() + "\n" +
                "Entry Time : " + ticket.getEntryTime().format(fmt) + "\n" +
                "────────────────────────\n" +
                "Please retain this ticket.\nPresent at exit gate."
        );
    }

    private void clearForm() {
        vehicleNumberField.setText("");
        ownerNameField.setText("");
        vehicleTypeCombo.setSelectedIndex(0);
        resultArea.setForeground(UITheme.ACCENT_TEAL);
        resultArea.setText("Form cleared. Ready for next vehicle.");
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Input Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public void refreshAvailability() {
        var db = service.getDb();
        twAvailLabel.setText(String.valueOf(db.countAvailable(Vehicle.VehicleType.TWO_WHEELER)));
        fwAvailLabel.setText(String.valueOf(db.countAvailable(Vehicle.VehicleType.FOUR_WHEELER)));
        hvAvailLabel.setText(String.valueOf(db.countAvailable(Vehicle.VehicleType.HEAVY_VEHICLE)));
    }
}
