package gui;

import model.*;
import service.ParkingService;
import service.ParkingService.*;
import util.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Exit gate panel – retrieve ticket, compute fee, process payment.
 */
public class ExitPanel extends JPanel {

    private final ParkingService service;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy  HH:mm:ss");

    private JTextField identifierField;
    private JComboBox<String> paymentCombo;
    private JTextArea feeArea;
    private JTextArea receiptArea;
    private UIComponents.AccentButton payBtn;

    private CheckoutResult pendingCheckout;

    public ExitPanel(ParkingService service) {
        this.service = service;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        build();
    }

    private void build() {
        // Header
        JLabel header = new JLabel("EXIT GATE");
        header.setFont(UITheme.FONT_HEADER);
        header.setForeground(UITheme.ACCENT_TEAL);

        JLabel sub = new JLabel("Process vehicle checkout and collect payment");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_MUTED);

        JPanel hp = new JPanel(new GridLayout(2, 1, 0, 4));
        hp.setOpaque(false);
        hp.add(header); hp.add(sub);
        add(hp, BorderLayout.NORTH);

        // Center: two cards
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weighty = 1;

        gbc.gridx = 0; gbc.weightx = 0.45;
        center.add(buildCheckoutCard(), gbc);

        gbc.gridx = 1; gbc.weightx = 0.55;
        center.add(buildReceiptCard(), gbc);

        add(center, BorderLayout.CENTER);
    }

    private JPanel buildCheckoutCard() {
        UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(12, UITheme.BG_CARD);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(6, 0, 6, 0);

        JLabel title = new JLabel("Checkout Details");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        card.add(title, g);

        g.gridy = 1;
        card.add(UIComponents.divider(), g);

        // Identifier
        g.gridy = 2; g.gridwidth = 1; g.weightx = 0.4;
        card.add(UIComponents.sectionLabel("Ticket ID / Vehicle No."), g);

        identifierField = UIComponents.styledField("TKT-XXXX or Vehicle No.");
        g.gridx = 1; g.weightx = 0.6;
        card.add(identifierField, g);

        // Lookup button
        UIComponents.AccentButton lookupBtn = new UIComponents.AccentButton(
                "LOOK UP", UITheme.HOVER, UITheme.BORDER);
        lookupBtn.setForeground(UITheme.ACCENT_AMBER);
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2; g.insets = new Insets(8, 0, 8, 0);
        card.add(lookupBtn, g);

        // Fee area
        feeArea = new JTextArea(7, 1);
        feeArea.setFont(UITheme.FONT_MONO);
        feeArea.setForeground(UITheme.TEXT_PRIMARY);
        feeArea.setBackground(UITheme.BG_DARK);
        feeArea.setEditable(false);
        feeArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        feeArea.setText("Enter ticket ID or vehicle number\nand click LOOK UP.");

        JScrollPane feeScroll = new JScrollPane(feeArea);
        feeScroll.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        feeScroll.getViewport().setBackground(UITheme.BG_DARK);
        g.gridy = 4; g.weighty = 1; g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(0, 0, 10, 0);
        card.add(feeScroll, g);

        // Payment method
        g.gridy = 5; g.weighty = 0; g.fill = GridBagConstraints.HORIZONTAL;
        g.gridwidth = 1; g.weightx = 0.4; g.insets = new Insets(6, 0, 6, 0);
        card.add(UIComponents.sectionLabel("Payment Method"), g);

        paymentCombo = UIComponents.styledCombo(new String[]{"CASH", "CARD", "UPI / QR"});
        g.gridx = 1; g.weightx = 0.6;
        card.add(paymentCombo, g);

        // Pay button
        payBtn = new UIComponents.AccentButton("CONFIRM PAYMENT & EXIT",
                UITheme.ACCENT_TEAL, new Color(0x0D9488));
        payBtn.setEnabled(false);
        g.gridx = 0; g.gridy = 6; g.gridwidth = 2; g.insets = new Insets(12, 0, 0, 0);
        card.add(payBtn, g);

        lookupBtn.addActionListener(e -> lookupVehicle());
        payBtn.addActionListener(e -> confirmPayment());

        return card;
    }

    private JPanel buildReceiptCard() {
        UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(12, UITheme.BG_CARD);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Payment Receipt");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        receiptArea = new JTextArea();
        receiptArea.setFont(UITheme.FONT_MONO);
        receiptArea.setForeground(UITheme.ACCENT_GREEN);
        receiptArea.setBackground(UITheme.BG_DARK);
        receiptArea.setEditable(false);
        receiptArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        receiptArea.setText("Receipt will appear here after\nsuccessful payment.");

        JScrollPane scroll = new JScrollPane(receiptArea);
        scroll.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        scroll.getViewport().setBackground(UITheme.BG_DARK);

        card.add(title, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ─── Actions ─────────────────────────────────────────────────────────────

    private void lookupVehicle() {
        String id = identifierField.getText().trim();
        if (id.isEmpty()) { showError("Please enter a Ticket ID or Vehicle Number."); return; }

        CheckoutResult result = service.checkout(id);
        if (!result.found) {
            feeArea.setForeground(UITheme.ACCENT_RED);
            feeArea.setText("✖ NOT FOUND\n\nNo active ticket found for:\n" + id);
            payBtn.setEnabled(false);
            pendingCheckout = null;
            return;
        }

        pendingCheckout = result;
        payBtn.setEnabled(true);

        ParkingTicket t = result.ticket;
        long hours   = result.durationMinutes / 60;
        long minutes = result.durationMinutes % 60;

        feeArea.setForeground(UITheme.TEXT_PRIMARY);
        feeArea.setText(
                "✔ TICKET FOUND\n" +
                "────────────────────────\n" +
                "Ticket ID  : " + t.getTicketId() + "\n" +
                "Vehicle    : " + t.getVehicle().getVehicleNumber() + "\n" +
                "Type       : " + t.getVehicle().getVehicleType() + "\n" +
                "Slot       : " + t.getSlot().getSlotId() + "\n" +
                "Entry      : " + t.getEntryTime().format(fmt) + "\n" +
                "Duration   : " + hours + "h " + minutes + "m\n" +
                "────────────────────────\n" +
                "TOTAL FEE  : ₹ " + String.format("%.2f", result.fee) + "\n" +
                "────────────────────────\n" +
                "Select payment method and\nclick CONFIRM PAYMENT."
        );
    }

    private void confirmPayment() {
        if (pendingCheckout == null) return;

        String method = (String) paymentCombo.getSelectedItem();
        service.confirmPayment(pendingCheckout.ticket, method);

        ParkingTicket t  = pendingCheckout.ticket;
        long hours   = pendingCheckout.durationMinutes / 60;
        long minutes = pendingCheckout.durationMinutes % 60;

        receiptArea.setText(
                "  ════════════════════════\n" +
                "       PAYMENT RECEIPT\n" +
                "  ════════════════════════\n" +
                "  Ticket ID  : " + t.getTicketId() + "\n" +
                "  Vehicle    : " + t.getVehicle().getVehicleNumber() + "\n" +
                "  Type       : " + t.getVehicle().getVehicleType() + "\n" +
                "  Owner      : " + t.getVehicle().getOwnerName() + "\n" +
                "  Slot       : " + t.getSlot().getSlotId() + "\n" +
                "  ─────────────────────────\n" +
                "  Entry Time : " + t.getEntryTime().format(fmt) + "\n" +
                "  Exit Time  : " + t.getExitTime().format(fmt) + "\n" +
                "  Duration   : " + hours + "h " + minutes + "m\n" +
                "  ─────────────────────────\n" +
                "  Amount Paid: ₹ " + String.format("%.2f", t.getTotalFee()) + "\n" +
                "  Payment    : " + method + "\n" +
                "  ─────────────────────────\n" +
                "  Status     : ✔ PAID\n" +
                "  ════════════════════════\n\n" +
                "  Thank you! Drive safe.\n" +
                "  Slot " + t.getSlot().getSlotId() + " is now free."
        );

        feeArea.setText("Payment complete.\nEnter next ticket ID to process another vehicle.");
        identifierField.setText("");
        payBtn.setEnabled(false);
        pendingCheckout = null;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
