package util;

import java.awt.*;

/**
 * Centralised design tokens for the parking system UI.
 * Deep midnight-blue palette with amber / teal accents.
 */
public final class UITheme {

    private UITheme() {}

    // ─── Palette ─────────────────────────────────────────────────────────────
    public static final Color BG_DARK      = new Color(0x0D1117);
    public static final Color BG_CARD      = new Color(0x161B22);
    public static final Color BG_SIDEBAR   = new Color(0x0A0E14);
    public static final Color ACCENT_AMBER = new Color(0xF59E0B);
    public static final Color ACCENT_TEAL  = new Color(0x14B8A6);
    public static final Color ACCENT_RED   = new Color(0xEF4444);
    public static final Color ACCENT_GREEN = new Color(0x22C55E);
    public static final Color TEXT_PRIMARY = new Color(0xF0F6FC);
    public static final Color TEXT_MUTED   = new Color(0x8B949E);
    public static final Color BORDER       = new Color(0x30363D);
    public static final Color HOVER        = new Color(0x21262D);
    public static final Color AMBER_DARK   = new Color(0x92400E);

    // ─── Typography ──────────────────────────────────────────────────────────
    public static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Consolas",  Font.PLAIN, 13);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.BOLD,  12);
    public static final Font FONT_NAV     = new Font("Segoe UI", Font.BOLD,  13);

    // ─── Dimensions ──────────────────────────────────────────────────────────
    public static final int SIDEBAR_W  = 200;
    public static final int CORNER_R   = 10;
}
