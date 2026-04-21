import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;

public class HomeFrame extends JFrame {

    private final String email;
    private final int userId;

    private JPanel sidebar, mainContent, topbar, centerPanel, logoRow, logoText, userCard;
    private KanbanPanel kanbanPanel;
    private CalendarPanel calendarPanel;
    private StatsPanel statsPanel;
    private CardLayout cardLayout;
    private JLabel pageTitle, appLabel;
    private JButton activeNav, themeBtn;
    private JButton kanbanBtn, calBtn, statsBtn;
    private JLabel logoImgLabel;
    private JButton logoutBtn;

    public HomeFrame(String email) {
        this.email = email;
        this.userId = AuthService.getUserId(email);
        setTitle("OrganizaVix");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 550));
        setSize(1100, 680);
        setLocationRelativeTo(null);
        loadIcon();
        initUI();
        ThemeManager.addListener(this::refreshTheme);

        new Thread(() -> {
            kanbanPanel = new KanbanPanel(userId, this);
            calendarPanel = new CalendarPanel(userId, this);
            statsPanel = new StatsPanel(userId);
            SwingUtilities.invokeLater(() -> {
                mainContent.add(kanbanPanel, "kanban");
                mainContent.add(calendarPanel, "calendar");
                mainContent.add(statsPanel, "stats");
                cardLayout.show(mainContent, "kanban");
            });
        }).start();
    }

    private void loadIcon() {
        try {
            URL url = getClass().getResource("assets/logo.png");
            if (url != null)
                setIconImage(new ImageIcon(url).getImage());
        } catch (Exception ignored) {
        }
    }

    // ── Colores según tema ────────────────────────────────────────────

    private Color sidebarBg() {
        return ThemeManager.isDark()
                ? AppColors.DARK_SIDEBAR
                : new Color(250, 250, 252);
    }

    private Color sidebarText() {
        // FIX: en claro, texto más oscuro para mayor contraste
        return ThemeManager.isDark()
                ? new Color(148, 163, 184)
                : new Color(55, 65, 81);
    }

    private Color sidebarBorder() {
        return ThemeManager.isDark()
                ? new Color(30, 30, 46)
                : new Color(220, 225, 235);
    }

    /** Color de fondo activo del nav según tema */
    private Color navActiveBg() {
        return ThemeManager.isDark()
                ? AppColors.ACCENT_SOFT
                : new Color(237, 228, 255);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppColors.bg());

        // ── SIDEBAR ──────────────────────────────────────────────────
        sidebar = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(sidebarBorder());
                g.fillRect(getWidth() - 1, 0, 1, getHeight());
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(sidebarBg());
        sidebar.setPreferredSize(new Dimension(216, 0));
        sidebar.setBorder(new EmptyBorder(20, 14, 20, 14));

        // Logo
        logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoRow.setOpaque(false);
        logoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        logoImgLabel = buildLogoLabel(32);

        logoText = new JPanel();
        logoText.setLayout(new BoxLayout(logoText, BoxLayout.Y_AXIS));
        logoText.setOpaque(false);
        appLabel = new JLabel("OrganizaVix");
        appLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        appLabel.setForeground(ThemeManager.isDark() ? Color.WHITE : new Color(20, 25, 40));
        logoText.add(appLabel);
        logoRow.add(logoImgLabel);
        logoRow.add(logoText);

        // Usuario
        String nombre = AuthService.getNombre(email);
        userCard = buildUserCard(nombre);

        // Nav buttons
        kanbanBtn = buildNavBtn("  ▦  Kanban");
        calBtn = buildNavBtn("  ◫  Calendario");
        statsBtn = buildNavBtn("  ◈  Estadísticas");
        activeNav = kanbanBtn;
        setNavActive(kanbanBtn);

        kanbanBtn.addActionListener(e -> navigate("kanban", kanbanBtn));
        calBtn.addActionListener(e -> navigate("calendar", calBtn));
        statsBtn.addActionListener(e -> navigate("stats", statsBtn));

        // Tema
        themeBtn = new JButton(ThemeManager.isDark() ? "☀  Modo claro" : "☾  Modo oscuro");
        styleSecondaryBtn(themeBtn);
        themeBtn.addActionListener(e -> {
            ThemeManager.toggle();
            themeBtn.setText(ThemeManager.isDark() ? "☀  Modo claro" : "☾  Modo oscuro");
            refreshTheme();
        });

        // Logout
        logoutBtn = new JButton("  ⏻  Cerrar sesión");
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logoutBtn.setForeground(AppColors.STATUS_TODO);
        logoutBtn.setBackground(new Color(0, 0, 0, 0));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        logoutBtn.setHorizontalAlignment(SwingConstants.LEFT);
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        sidebar.add(logoRow);
        sidebar.add(Box.createVerticalStrut(18));
        sidebar.add(userCard);
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(kanbanBtn);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(calBtn);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(statsBtn);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(themeBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(logoutBtn);

        // ── TOPBAR ───────────────────────────────────────────────────
        topbar = new JPanel(new BorderLayout());
        topbar.setBackground(AppColors.bg());
        topbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.border()),
                new EmptyBorder(14, 24, 14, 24)));

        pageTitle = new JLabel("Tablero Kanban");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pageTitle.setForeground(AppColors.textPrimary());
        topbar.add(pageTitle, BorderLayout.WEST);

        // ── MAIN ─────────────────────────────────────────────────────
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(AppColors.bg());

        centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(AppColors.bg());
        centerPanel.add(topbar, BorderLayout.NORTH);
        centerPanel.add(mainContent, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                sidebar.setVisible(getWidth() > 750);
            }
        });
    }

    // ── Navegación ───────────────────────────────────────────────────

    public void navigate(String panel, JButton btn) {
        cardLayout.show(mainContent, panel);
        if (activeNav != null && activeNav != btn)
            setNavInactive(activeNav);
        setNavActive(btn);
        activeNav = btn;
        switch (panel) {
            case "kanban" -> {
                pageTitle.setText("Tablero Kanban");
                if (kanbanPanel != null)
                    kanbanPanel.cargarTareas();
            }
            case "calendar" -> {
                pageTitle.setText("Calendario");
                if (calendarPanel != null)
                    calendarPanel.cargarTareas();
            }
            case "stats" -> {
                pageTitle.setText("Estadísticas");
                if (statsPanel != null)
                    statsPanel.cargar();
            }
        }
    }

    private void setNavActive(JButton btn) {
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBackground(navActiveBg());
        btn.setForeground(new Color(124, 58, 237)); // púrpura consistente en ambos temas
    }

    private void setNavInactive(JButton btn) {
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setForeground(sidebarText());
    }

    // ── refreshTheme ─────────────────────────────────────────────────

    private void refreshTheme() {
        // Raíz
        getContentPane().setBackground(AppColors.bg());

        // Sidebar
        sidebar.setBackground(sidebarBg());
        sidebar.repaint();

        // Logo text
        appLabel.setForeground(ThemeManager.isDark() ? Color.WHITE : new Color(20, 25, 40));

        // FIX: Reconstruir userCard con colores actualizados
        String nombre = AuthService.getNombre(email);
        int userCardIndex = -1;
        Component[] sidebarComps = sidebar.getComponents();
        for (int i = 0; i < sidebarComps.length; i++) {
            if (sidebarComps[i] == userCard) {
                userCardIndex = i;
                break;
            }
        }
        userCard = buildUserCard(nombre);
        if (userCardIndex >= 0) {
            sidebar.remove(userCardIndex);
            sidebar.add(userCard, userCardIndex);
        }

        // Nav buttons
        for (JButton btn : new JButton[] { kanbanBtn, calBtn, statsBtn }) {
            if (btn == activeNav)
                setNavActive(btn);
            else
                setNavInactive(btn);
        }

        themeBtn.setForeground(sidebarText());
        themeBtn.setText(ThemeManager.isDark() ? "☀  Modo claro" : "☾  Modo oscuro");

        // Topbar
        topbar.setBackground(AppColors.bg());
        topbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.border()),
                new EmptyBorder(14, 24, 14, 24)));
        pageTitle.setForeground(AppColors.textPrimary());

        // Centro
        centerPanel.setBackground(AppColors.bg());
        mainContent.setBackground(AppColors.bg());

        // FIX: Forzar repintado del topbar con borde correcto
        topbar.repaint();

        // Sub-paneles
        if (kanbanPanel != null) {
            kanbanPanel.setBackground(AppColors.bg());
            kanbanPanel.refreshColors();
        }
        if (calendarPanel != null) {
            calendarPanel.setBackground(AppColors.bg());
            calendarPanel.cargarTareas();
        }
        if (statsPanel != null) {
            statsPanel.setBackground(AppColors.bg());
        }

        sidebar.revalidate();
        sidebar.repaint();
        repaint();
        revalidate();
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private JPanel buildUserCard(String nombre) {
        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        // FIX: Color de fondo correcto según tema
        card.setBackground(ThemeManager.isDark()
                ? new Color(255, 255, 255, 15)
                : new Color(124, 58, 237, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel avatar = buildAvatar(nombre);
        JLabel userLbl = new JLabel(nombre);
        userLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLbl.setForeground(ThemeManager.isDark() ? new Color(220, 220, 240) : new Color(55, 65, 81));
        card.add(avatar);
        card.add(userLbl);
        return card;
    }

    /**
     * FIX: Usa interpolación bicúbica igual que LoginFrame.buildLogoImage()
     */
    private JLabel buildLogoLabel(int size) {
        try {
            URL url = getClass().getResource("assets/logo.png");
            if (url == null)
                url = getClass().getResource("/assets/logo.png");
            if (url != null) {
                ImageIcon raw = new ImageIcon(url);
                Image original = raw.getImage();
                MediaTracker tracker = new MediaTracker(new JLabel());
                tracker.addImage(original, 0);
                try {
                    tracker.waitForAll();
                } catch (InterruptedException ignored) {
                }

                int iw = raw.getIconWidth();
                int ih = raw.getIconHeight();

                BufferedImage hq = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = hq.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                if (iw > 0 && ih > 0) {
                    int crop = Math.min(iw, ih);
                    int srcX = (iw - crop) / 2;
                    int srcY = (ih - crop) / 2;
                    g2.drawImage(original,
                            0, 0, size, size,
                            srcX, srcY, srcX + crop, srcY + crop,
                            null);
                } else {
                    g2.drawImage(original, 0, 0, size, size, null);
                }
                g2.dispose();
                return new JLabel(new ImageIcon(hq));
            }
        } catch (Exception ignored) {
        }

        // Fallback pintado
        return new JLabel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, size, size, size / 4f, size / 4f));
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, size / 3));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (size - fm.stringWidth("OV")) / 2;
                int ty = size / 2 + fm.getAscent() / 2 - 2;
                g2.drawString("OV", tx, ty);
                g2.dispose();
            }

            public Dimension getPreferredSize() {
                return new Dimension(size, size);
            }

            public Dimension getMinimumSize() {
                return new Dimension(size, size);
            }
        };
    }

    private JButton buildNavBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(sidebarText());
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 12, 8, 12));
        return btn;
    }

    private void styleSecondaryBtn(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(sidebarText());
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
    }

    private JPanel buildAvatar(String nombre) {
        return new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.ACCENT);
                g2.fillOval(0, 0, 32, 32);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                String initials = nombre.length() >= 2
                        ? nombre.substring(0, 2).toUpperCase()
                        : nombre.substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials, (32 - fm.stringWidth(initials)) / 2, 21);
                g2.dispose();
            }

            public Dimension getPreferredSize() {
                return new Dimension(32, 32);
            }

            public Dimension getMinimumSize() {
                return new Dimension(32, 32);
            }
        };
    }
}