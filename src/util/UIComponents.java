package util;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Reusable styled Swing components that follow UITheme.
 */
public final class UIComponents {

    private UIComponents() {}

    // ─── Rounded Panel ───────────────────────────────────────────────────────

    public static class RoundedPanel extends JPanel {
        private final int radius;
        private Color background;

        public RoundedPanel(int radius, Color bg) {
            super();
            this.radius = radius;
            this.background = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(background);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
            g2.dispose();
            super.paintComponent(g);
        }

        public void setBackground2(Color c) { this.background = c; repaint(); }
    }

    // ─── Accent Button ───────────────────────────────────────────────────────

    public static class AccentButton extends JButton {
        private Color normal, hover;

        public AccentButton(String text, Color normal, Color hover) {
            super(text);
            this.normal = normal;
            this.hover  = hover;
            setFont(UITheme.FONT_NAV);
            setForeground(UITheme.TEXT_PRIMARY);
            setBackground(normal);
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { repaint(); }
                @Override public void mouseExited(MouseEvent e)  { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean hovered = getMousePosition() != null;
            g2.setColor(hovered ? hover : normal);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ─── Stat Card ───────────────────────────────────────────────────────────

    public static JPanel statCard(String title, String value, Color accent) {
        RoundedPanel card = new RoundedPanel(12, UITheme.BG_CARD);
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Accent stripe at top
        JPanel stripe = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 40, 4, 4, 4);
                g2.dispose();
            }
        };
        stripe.setOpaque(false);
        stripe.setPreferredSize(new Dimension(40, 8));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_SMALL);
        titleLabel.setForeground(UITheme.TEXT_MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setName("value");
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accent);

        card.add(stripe, BorderLayout.NORTH);
        card.add(titleLabel, BorderLayout.CENTER);
        card.add(valueLabel, BorderLayout.SOUTH);

        return card;
    }

    // ─── Styled text field ───────────────────────────────────────────────────

    public static JTextField styledField(String placeholder) {
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_DARK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setFont(UITheme.FONT_BODY);
        field.setForeground(UITheme.TEXT_PRIMARY);
        field.setBackground(UITheme.BG_DARK);
        field.setCaretColor(UITheme.ACCENT_AMBER);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        field.setOpaque(false);
        return field;
    }

    // ─── Styled combo box ─────────────────────────────────────────────────────

    public static <T> JComboBox<T> styledCombo(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(UITheme.FONT_BODY);
        combo.setForeground(UITheme.TEXT_PRIMARY);
        combo.setBackground(UITheme.BG_DARK);
        combo.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        ((JLabel) combo.getRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        return combo;
    }

    // ─── Section label ───────────────────────────────────────────────────────

    public static JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(UITheme.FONT_LABEL);
        lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    // ─── Divider ─────────────────────────────────────────────────────────────

    public static JSeparator divider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER);
        sep.setBackground(UITheme.BORDER);
        return sep;
    }
}
