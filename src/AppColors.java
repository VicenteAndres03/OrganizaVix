import java.awt.Color;

public class AppColors {

    // --- DARK MODE ---
    public static final Color DARK_BG_PRIMARY = new Color(15, 15, 19);
    public static final Color DARK_BG_SECONDARY = new Color(18, 18, 26);
    public static final Color DARK_BG_CARD = new Color(26, 26, 39);
    public static final Color DARK_BG_HOVER = new Color(30, 30, 46);
    public static final Color DARK_BORDER = new Color(30, 30, 46);
    public static final Color DARK_TEXT_PRIMARY = new Color(226, 232, 240);
    public static final Color DARK_TEXT_SECONDARY = new Color(100, 116, 139);
    public static final Color DARK_TEXT_MUTED = new Color(76, 76, 110);
    public static final Color DARK_SIDEBAR = new Color(10, 10, 16);

    // --- LIGHT MODE ---
    public static final Color LIGHT_BG_PRIMARY = new Color(255, 255, 255);
    public static final Color LIGHT_BG_SECONDARY = new Color(248, 250, 252);
    public static final Color LIGHT_BG_CARD = new Color(255, 255, 255);
    public static final Color LIGHT_BG_HOVER = new Color(241, 245, 249);
    public static final Color LIGHT_BORDER = new Color(226, 232, 240);
    public static final Color LIGHT_TEXT_PRIMARY = new Color(15, 23, 42);
    public static final Color LIGHT_TEXT_SECONDARY = new Color(100, 116, 139);
    public static final Color LIGHT_TEXT_MUTED = new Color(148, 163, 184);
    public static final Color LIGHT_SIDEBAR = new Color(15, 23, 42);

    // --- ACCENT (igual en ambos modos) ---
    public static final Color ACCENT = new Color(124, 58, 237);
    public static final Color ACCENT_HOVER = new Color(109, 40, 217);
    public static final Color ACCENT_SOFT = new Color(59, 7, 100);

    // --- ESTADO KANBAN ---
    public static final Color STATUS_TODO = new Color(248, 113, 113);
    public static final Color STATUS_PROGRESS = new Color(251, 191, 36);
    public static final Color STATUS_DONE = new Color(74, 222, 128);

    // --- PRIORIDAD ---
    public static final Color PRIORITY_HIGH = new Color(124, 58, 237);
    public static final Color PRIORITY_HIGH_BG = new Color(59, 7, 100);
    public static final Color PRIORITY_MED = new Color(56, 189, 248);
    public static final Color PRIORITY_MED_BG = new Color(28, 53, 69);
    public static final Color PRIORITY_LOW = new Color(74, 222, 128);
    public static final Color PRIORITY_LOW_BG = new Color(15, 42, 26);

    // Getters dinámicos según tema
    public static Color bg() {
        return ThemeManager.isDark() ? DARK_BG_PRIMARY : LIGHT_BG_PRIMARY;
    }

    public static Color bgSecondary() {
        return ThemeManager.isDark() ? DARK_BG_SECONDARY : LIGHT_BG_SECONDARY;
    }

    public static Color bgCard() {
        return ThemeManager.isDark() ? DARK_BG_CARD : LIGHT_BG_CARD;
    }

    public static Color bgHover() {
        return ThemeManager.isDark() ? DARK_BG_HOVER : LIGHT_BG_HOVER;
    }

    public static Color border() {
        return ThemeManager.isDark() ? DARK_BORDER : LIGHT_BORDER;
    }

    public static Color textPrimary() {
        return ThemeManager.isDark() ? DARK_TEXT_PRIMARY : LIGHT_TEXT_PRIMARY;
    }

    public static Color textSecondary() {
        return ThemeManager.isDark() ? DARK_TEXT_SECONDARY : LIGHT_TEXT_SECONDARY;
    }

    public static Color textMuted() {
        return ThemeManager.isDark() ? DARK_TEXT_MUTED : LIGHT_TEXT_MUTED;
    }

    public static Color sidebar() {
        return ThemeManager.isDark() ? DARK_SIDEBAR : LIGHT_SIDEBAR;
    }
}