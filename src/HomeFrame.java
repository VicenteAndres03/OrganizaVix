import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;

public class HomeFrame extends JFrame {

    private final String email;
    private final int userId;

    private JPanel sidebar, mainContent, topbar, centerPanel, logoRow, userCard;
    private KanbanPanel kanbanPanel;
    private CalendarPanel calendarPanel;
    private StatsPanel statsPanel;
    private ProfilePanel profilePanel; // Añadido

    private CardLayout cardLayout;
    private JLabel pageTitle, appLabel;
    private JButton activeNav, themeBtn;
    private JButton kanbanBtn, calBtn, statsBtn, profileBtn; // Añadido
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
            profilePanel = new ProfilePanel(userId, email, this); // Añadido

            SwingUtilities.invokeLater(() -> {
                mainContent.add(kanbanPanel, "kanban");
                mainContent.add(calendarPanel, "calendar");
                mainContent.add(statsPanel, "stats");
                mainContent.add(profilePanel, "profile"); // Añadido
                cardLayout.show(mainContent, "kanban");
            });
        }).start();
    }

    private void loadIcon() {
        try {
            URL url = getClass().getResource("/assets/logo.png");
            if (url == null)
                url = getClass().getResource("assets/logo.png");
            if (url != null) {
                ImageIcon rawIcon = new ImageIcon(url);
                Image rawImg = rawIcon.getImage();
                int w = rawIcon.getIconWidth();
                int h = rawIcon.getIconHeight();

                if (w > 0 && h > 0) {
                    int size = Math.max(w, h);
                    BufferedImage sq = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = sq.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g2.drawImage(rawImg, (size - w) / 2, (size - h) / 2, w, h, null);
                    g2.dispose();
                    setIconImage(sq);
                } else {
                    setIconImage(rawImg);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private Color sidebarBg() {
        return ThemeManager.isDark() ? AppColors.DARK_SIDEBAR : new Color(250, 250, 252);
    }

    private Color sidebarText() {
        return ThemeManager.isDark() ? new Color(148, 163, 184) : new Color(55, 65, 81);
    }

    private Color sidebarBorder() {
        return ThemeManager.isDark() ? new Color(30, 30, 46) : new Color(220, 225, 235);
    }

    private Color navActiveBg() {
        return ThemeManager.isDark() ? AppColors.ACCENT_SOFT : new Color(237, 228, 255);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppColors.bg());

        sidebar = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(sidebarBorder());
                g.fillRect(getWidth() - 1, 0, 1, getHeight());
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(sidebarBg());
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(new EmptyBorder(20, 14, 20, 14));

        logoRow = new JPanel();
        logoRow.setLayout(new BoxLayout(logoRow, BoxLayout.Y_AXIS));
        logoRow.setOpaque(false);
        logoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        logoImgLabel = buildLogoLabel(130);
        logoImgLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        appLabel = new JLabel("OrganizaVix");
        appLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        appLabel.setForeground(ThemeManager.isDark() ? Color.WHITE : new Color(20, 25, 40));
        appLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoRow.add(logoImgLabel);
        logoRow.add(Box.createVerticalStrut(8));
        logoRow.add(appLabel);

        String nombre = AuthService.getNombre(email);
        userCard = buildUserCard(nombre);

        kanbanBtn = buildNavBtn("Kanban", "kanban");
        calBtn = buildNavBtn("Calendario", "calendar");
        statsBtn = buildNavBtn("Estadísticas", "stats");
        profileBtn = buildNavBtn("Mi Perfil", "profile"); // Añadido

        activeNav = kanbanBtn;
        setNavActive(kanbanBtn);

        kanbanBtn.addActionListener(e -> {
            if (kanbanPanel != null)
                kanbanPanel.setFechaFiltro(null);
            navigate("kanban", kanbanBtn);
        });
        calBtn.addActionListener(e -> navigate("calendar", calBtn));
        statsBtn.addActionListener(e -> navigate("stats", statsBtn));
        profileBtn.addActionListener(e -> navigate("profile", profileBtn)); // Añadido

        boolean isDark = ThemeManager.isDark();
        themeBtn = buildSecondaryBtn(isDark ? "Modo claro" : "Modo oscuro", isDark ? "sun" : "moon", false);
        themeBtn.addActionListener(e -> {
            ThemeManager.toggle();
            boolean darkNow = ThemeManager.isDark();
            themeBtn.setText(darkNow ? "Modo claro" : "Modo oscuro");
            themeBtn.setIcon(new VectorIcon(darkNow ? "sun" : "moon"));
            refreshTheme();
        });

        logoutBtn = buildSecondaryBtn("Cerrar sesión", "logout", true);
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        sidebar.add(logoRow);
        sidebar.add(Box.createVerticalStrut(24));
        sidebar.add(userCard);
        sidebar.add(Box.createVerticalStrut(24));
        sidebar.add(kanbanBtn);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(calBtn);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(statsBtn);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(profileBtn); // Añadido
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(themeBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(logoutBtn);

        topbar = new JPanel(new BorderLayout());
        topbar.setBackground(AppColors.bg());
        topbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.border()),
                new EmptyBorder(14, 24, 14, 24)));

        pageTitle = new JLabel("Tablero Kanban");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pageTitle.setForeground(AppColors.textPrimary());
        topbar.add(pageTitle, BorderLayout.WEST);

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

    public void abrirKanbanEnFecha(String fecha) {
        if (kanbanPanel != null) {
            kanbanPanel.setFechaFiltro(fecha);
        }
        navigate("kanban", kanbanBtn);
    }

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
            case "profile" -> {
                pageTitle.setText("Mi Perfil");
                if (profilePanel != null)
                    profilePanel.refreshColors();
            }
        }
    }

    private void setNavActive(JButton btn) {
        btn.putClientProperty("active", true);
        btn.setForeground(new Color(124, 58, 237));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.repaint();
    }

    private void setNavInactive(JButton btn) {
        btn.putClientProperty("active", false);
        btn.setForeground(sidebarText());
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.repaint();
    }

    private void refreshTheme() {
        getContentPane().setBackground(AppColors.bg());
        sidebar.setBackground(sidebarBg());
        sidebar.repaint();

        appLabel.setForeground(ThemeManager.isDark() ? Color.WHITE : new Color(20, 25, 40));

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

        for (JButton btn : new JButton[] { kanbanBtn, calBtn, statsBtn, profileBtn }) {
            if (btn == activeNav)
                setNavActive(btn);
            else
                setNavInactive(btn);
        }

        themeBtn.setForeground(sidebarText());

        topbar.setBackground(AppColors.bg());
        topbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.border()),
                new EmptyBorder(14, 24, 14, 24)));
        pageTitle.setForeground(AppColors.textPrimary());

        centerPanel.setBackground(AppColors.bg());
        mainContent.setBackground(AppColors.bg());
        topbar.repaint();

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
        if (profilePanel != null) {
            profilePanel.setBackground(AppColors.bg());
            profilePanel.refreshColors();
        }

        sidebar.revalidate();
        sidebar.repaint();
        repaint();
        revalidate();
    }

    private JPanel buildUserCard(String nombre) {
        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBackground(ThemeManager.isDark()
                ? new Color(255, 255, 255, 12)
                : new Color(124, 58, 237, 15));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel avatar = buildAvatar(nombre);

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        JLabel welcomeLbl = new JLabel("Hola de vuelta,");
        welcomeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        welcomeLbl.setForeground(ThemeManager.isDark() ? new Color(150, 150, 170) : new Color(100, 110, 120));

        JLabel userLbl = new JLabel(nombre);
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userLbl.setForeground(ThemeManager.isDark() ? new Color(240, 240, 250) : new Color(30, 40, 50));

        textCol.add(welcomeLbl);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(userLbl);

        card.add(avatar);
        card.add(textCol);
        return card;
    }

    private JLabel buildLogoLabel(int targetHeight) {
        try {
            URL url = getClass().getResource("/assets/logo.png");
            if (url == null)
                url = getClass().getResource("assets/logo.png");
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

                int finalH = targetHeight;
                int finalW = targetHeight;

                if (iw > 0 && ih > 0) {
                    finalW = (int) (iw * ((double) finalH / ih));
                }

                BufferedImage hq = new BufferedImage(finalW, finalH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = hq.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.drawImage(original, 0, 0, finalW, finalH, null);
                g2.dispose();

                JLabel lbl = new JLabel(new ImageIcon(hq));

                lbl.setMaximumSize(new Dimension(finalW, finalH));
                lbl.setPreferredSize(new Dimension(finalW, finalH));
                lbl.setMinimumSize(new Dimension(finalW, finalH));
                lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                lbl.setBorder(null);

                return lbl;
            }
        } catch (Exception ignored) {
        }

        return new JLabel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, targetHeight, targetHeight, targetHeight / 4f,
                        targetHeight / 4f));
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, targetHeight / 3));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (targetHeight - fm.stringWidth("OV")) / 2;
                int ty = targetHeight / 2 + fm.getAscent() / 2 - 2;
                g2.drawString("OV", tx, ty);
                g2.dispose();
            }

            public Dimension getPreferredSize() {
                return new Dimension(targetHeight, targetHeight);
            }

            public Dimension getMinimumSize() {
                return new Dimension(targetHeight, targetHeight);
            }
        };
    }

    private JButton buildNavBtn(String text, String iconType) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Boolean active = (Boolean) getClientProperty("active");
                boolean isActive = (active != null && active);
                boolean isHover = getModel().isRollover();

                if (isActive) {
                    g2.setColor(navActiveBg());
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                    g2.setColor(new Color(124, 58, 237));
                    g2.fill(new RoundRectangle2D.Float(0, getHeight() * 0.25f, 4, getHeight() * 0.5f, 4, 4));
                } else if (isHover) {
                    g2.setColor(ThemeManager.isDark() ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 10));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                }

                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(sidebarText());
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.setIcon(new VectorIcon(iconType));
        btn.setIconTextGap(14);
        btn.setBorder(new EmptyBorder(10, 20, 10, 16));

        return btn;
    }

    private JButton buildSecondaryBtn(String text, String iconType, boolean isAlert) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (isAlert) {
                        g2.setColor(new Color(239, 68, 68, 25));
                    } else {
                        g2.setColor(ThemeManager.isDark() ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 10));
                    }
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(isAlert ? AppColors.STATUS_TODO : sidebarText());
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.setIcon(new VectorIcon(iconType));
        btn.setIconTextGap(14);
        btn.setBorder(new EmptyBorder(8, 20, 8, 16));

        return btn;
    }

    private JPanel buildAvatar(String nombre) {
        return new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.ACCENT);
                g2.fillOval(0, 0, 36, 36);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String initials = nombre.length() >= 2
                        ? nombre.substring(0, 2).toUpperCase()
                        : nombre.substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials, (36 - fm.stringWidth(initials)) / 2, 23);
                g2.dispose();
            }

            public Dimension getPreferredSize() {
                return new Dimension(36, 36);
            }

            public Dimension getMinimumSize() {
                return new Dimension(36, 36);
            }
        };
    }

    private class VectorIcon implements Icon {
        private final String type;

        public VectorIcon(String type) {
            this.type = type;
        }

        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground());
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = x + 2;
            int cy = y + 2;

            switch (type) {
                case "kanban" -> {
                    g2.drawRoundRect(cx, cy + 1, 14, 12, 3, 3);
                    g2.drawLine(cx + 4, cy + 1, cx + 4, cy + 13);
                    g2.drawLine(cx + 10, cy + 1, cx + 10, cy + 13);
                }
                case "calendar" -> {
                    g2.drawRoundRect(cx, cy + 2, 14, 12, 3, 3);
                    g2.drawLine(cx, cy + 6, cx + 14, cy + 6);
                    g2.drawLine(cx + 4, cy, cx + 4, cy + 4);
                    g2.drawLine(cx + 10, cy, cx + 10, cy + 4);
                }
                case "stats" -> {
                    g2.drawLine(cx + 1, cy + 14, cx + 15, cy + 14);
                    g2.drawRoundRect(cx + 2, cy + 8, 3, 6, 1, 1);
                    g2.drawRoundRect(cx + 7, cy + 3, 3, 11, 1, 1);
                    g2.drawRoundRect(cx + 12, cy + 10, 3, 4, 1, 1);
                }
                case "profile" -> {
                    g2.drawOval(cx + 4, cy + 1, 6, 6);
                    Path2D p = new Path2D.Float();
                    p.moveTo(cx + 1, cy + 14);
                    p.curveTo(cx + 1, cy + 9, cx + 13, cy + 9, cx + 13, cy + 14);
                    g2.draw(p);
                }
                case "sun" -> {
                    g2.drawOval(cx + 3, cy + 3, 8, 8);
                    g2.drawLine(cx + 7, cy - 2, cx + 7, cy);
                    g2.drawLine(cx + 7, cy + 14, cx + 7, cy + 16);
                    g2.drawLine(cx - 2, cy + 7, cx, cy + 7);
                    g2.drawLine(cx + 14, cy + 7, cx + 16, cy + 7);
                    g2.drawLine(cx + 1, cy + 1, cx + 2, cy + 2);
                    g2.drawLine(cx + 13, cy + 13, cx + 12, cy + 12);
                    g2.drawLine(cx + 1, cy + 13, cx + 2, cy + 12);
                    g2.drawLine(cx + 13, cy + 1, cx + 12, cy + 2);
                }
                case "moon" -> {
                    Path2D p = new Path2D.Float();
                    p.moveTo(cx + 10, cy + 1);
                    p.curveTo(cx + 1, cy + 3, cx + 1, cy + 13, cx + 10, cy + 15);
                    p.curveTo(cx + 5, cy + 11, cx + 5, cy + 5, cx + 10, cy + 1);
                    g2.draw(p);
                }
                case "logout" -> {
                    Path2D box = new Path2D.Float();
                    box.moveTo(cx + 6, cy + 1);
                    box.lineTo(cx + 2, cy + 1);
                    box.lineTo(cx + 2, cy + 13);
                    box.lineTo(cx + 6, cy + 13);
                    g2.draw(box);
                    g2.drawLine(cx + 6, cy + 7, cx + 14, cy + 7);
                    g2.drawLine(cx + 11, cy + 4, cx + 14, cy + 7);
                    g2.drawLine(cx + 11, cy + 10, cx + 14, cy + 7);
                }
            }
            g2.dispose();
        }
    }
}