import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private JPanel root, card;
    private JPanel left;
    private JButton themeBtnRef;

    public LoginFrame() {
        setTitle("OrganizaVix");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 580));
        setSize(1050, 660);
        setLocationRelativeTo(null);
        setResizable(true);
        loadIcon();
        initUI();
        ThemeManager.addListener(this::refreshTheme);
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

    private void initUI() {
        root = new JPanel(new GridLayout(1, 2));
        root.setBackground(AppColors.bg());

        left = buildLeftPanel();

        // ── RIGHT ────────────────────────────────────────────────────
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(AppColors.bg());

        card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppColors.bgCard());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.border(), 1, true),
                new EmptyBorder(44, 48, 44, 48)));

        JLabel title = new JLabel("Bienvenido de vuelta");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(AppColors.textPrimary());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Ingresa tus credenciales para continuar");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(AppColors.textSecondary());
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        emailField = buildTextField("usuario@ejemplo.com");
        passwordField = new JPasswordField();
        styleField(passwordField);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(AppColors.STATUS_TODO);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = buildAccentButton("Iniciar sesión");
        loginBtn.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin());

        JPanel linksCol = new JPanel();
        linksCol.setLayout(new BoxLayout(linksCol, BoxLayout.Y_AXIS));
        linksCol.setBackground(AppColors.bgCard());
        linksCol.setAlignmentX(Component.LEFT_ALIGNMENT);
        linksCol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JButton registerBtn = buildLinkButton("¿No tienes cuenta? Regístrate");
        registerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton forgotBtn = buildLinkButton("Olvidé mi contraseña");
        forgotBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        registerBtn.addActionListener(e -> {
            dispose();
            new RegisterFrame().setVisible(true);
        });
        forgotBtn.addActionListener(e -> new ForgotPasswordFrame().setVisible(true));

        linksCol.add(registerBtn);
        linksCol.add(Box.createVerticalStrut(4));
        linksCol.add(forgotBtn);

        themeBtnRef = buildThemeToggle();

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(32));
        card.add(buildLabel("Correo electrónico"));
        card.add(Box.createVerticalStrut(6));
        card.add(emailField);
        card.add(Box.createVerticalStrut(16));
        card.add(buildLabel("Contraseña"));
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(8));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(16));
        card.add(linksCol);
        card.add(Box.createVerticalStrut(12));
        card.add(themeBtnRef);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(40, 50, 40, 50);
        right.add(card, gbc);

        root.add(left);
        root.add(right);
        setContentPane(root);
        AnimationUtil.fadeIn(card);
    }

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (ThemeManager.isDark()) {
                    GradientPaint gp = new GradientPaint(0, 0,
                            new Color(30, 10, 60), getWidth(), getHeight(), new Color(80, 20, 140));
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
                    g2.setColor(new Color(200, 150, 255));
                    g2.fillOval(-60, -60, 320, 320);
                    g2.fillOval(getWidth() - 180, getHeight() - 200, 300, 300);
                } else {
                    GradientPaint gp = new GradientPaint(0, 0,
                            new Color(237, 228, 255), getWidth(), getHeight(), new Color(255, 250, 255));
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.20f));
                    g2.setColor(new Color(167, 139, 250));
                    g2.fillOval(-40, -40, 260, 260);
                    g2.fillOval(getWidth() - 160, getHeight() - 180, 260, 260);
                }
                g2.dispose();
            }
        };
        panel.setLayout(new GridBagLayout());

        boolean dark = ThemeManager.isDark();
        Color brandColor = dark ? Color.WHITE : new Color(40, 10, 80);
        Color tagColor = dark ? new Color(200, 180, 240) : new Color(100, 60, 160);
        Color bulletColor = dark ? new Color(210, 190, 255) : new Color(124, 58, 237);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 50, 0, 50));

        // Mantenemos el tamaño grande en 130 tal como pediste
        JLabel logoImg = buildLogoImage(130);
        logoImg.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brand = new JLabel("OrganizaVix");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 36));
        brand.setForeground(brandColor);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel(
                "<html><div style='width:280px; font-size:15px; line-height:1.6'>"
                        + "Tu espacio para organizar tareas, gestionar proyectos y alcanzar tus metas."
                        + "</div></html>");
        tagline.setForeground(tagColor);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] features = { "✦  Tablero Kanban visual", "✦  Calendario integrado", "✦  Estadísticas en tiempo real" };
        JPanel featPanel = new JPanel();
        featPanel.setLayout(new BoxLayout(featPanel, BoxLayout.Y_AXIS));
        featPanel.setOpaque(false);
        featPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String f : features) {
            JLabel fl = new JLabel(f);
            fl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            fl.setForeground(bulletColor);
            fl.setBorder(new EmptyBorder(5, 0, 5, 0));
            featPanel.add(fl);
        }

        content.add(logoImg);
        content.add(Box.createVerticalStrut(8));
        content.add(brand);
        content.add(Box.createVerticalStrut(14));
        content.add(tagline);
        content.add(Box.createVerticalStrut(32));
        content.add(featPanel);
        panel.add(content);
        return panel;
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Completa todos los campos.");
            return;
        }
        errorLabel.setText("Conectando...");
        errorLabel.setForeground(AppColors.textMuted());
        new Thread(() -> {
            boolean ok = AuthService.login(email, password);
            SwingUtilities.invokeLater(() -> {
                if (ok) {
                    dispose();
                    new HomeFrame(email).setVisible(true);
                } else {
                    errorLabel.setText("Correo o contraseña incorrectos.");
                    errorLabel.setForeground(AppColors.STATUS_TODO);
                    passwordField.setText("");
                }
            });
        }).start();
    }

    private void refreshTheme() {
        left = buildLeftPanel();
        root.remove(0);
        root.add(left, 0);

        Component rightPanel = root.getComponent(1);
        ((JPanel) rightPanel).setBackground(AppColors.bg());

        card.setBackground(AppColors.bgCard());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.border(), 1, true),
                new EmptyBorder(44, 48, 44, 48)));

        refreshContainer(card);

        if (themeBtnRef != null)
            themeBtnRef.setText(ThemeManager.isDark() ? "☀  Modo claro" : "☾  Modo oscuro");

        root.revalidate();
        root.repaint();
    }

    private void refreshContainer(Container c) {
        for (Component comp : c.getComponents()) {
            if (comp == errorLabel)
                continue;
            if (comp instanceof JTextField f) {
                styleField(f);
            }
            if (comp instanceof JPasswordField p) {
                styleField(p);
            }
            if (comp instanceof JLabel lbl && comp != errorLabel) {
                Font font = lbl.getFont();
                if (font.isBold() && font.getSize() >= 20)
                    lbl.setForeground(AppColors.textPrimary());
                else if (!lbl.getForeground().equals(AppColors.ACCENT))
                    lbl.setForeground(AppColors.textSecondary());
            }
            if (comp instanceof JPanel p && p != card) {
                p.setBackground(AppColors.bgCard());
                refreshContainer(p);
            }
        }
    }

    // ─── Static Helpers ───────────────────────────────────────────────

    public static JLabel buildLogoImage(int targetHeight) {
        try {
            URL url = LoginFrame.class.getResource("/assets/logo.png");
            if (url == null)
                url = LoginFrame.class.getResource("assets/logo.png");
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

                // SOLUCIÓN DEFINITIVA: Candado de tamaño para que Swing no desalinee
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

        JLabel fallbackLabel = new JLabel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, targetHeight, targetHeight, targetHeight / 4f,
                        targetHeight / 4f));
                g2.setColor(Color.WHITE);
                int fs = targetHeight / 3;
                g2.setFont(new Font("Segoe UI", Font.BOLD, fs));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("OV",
                        (targetHeight - fm.stringWidth("OV")) / 2,
                        targetHeight / 2 + fm.getAscent() / 2 - 2);
                g2.dispose();
            }

            public Dimension getPreferredSize() {
                return new Dimension(targetHeight, targetHeight);
            }

            public Dimension getMinimumSize() {
                return new Dimension(targetHeight, targetHeight);
            }
        };

        return fallbackLabel;
    }

    public static JTextField buildTextField(String placeholder) {
        JTextField f = new JTextField();
        styleField(f);
        f.setText(placeholder);
        f.setForeground(AppColors.textMuted());
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) {
                    f.setText("");
                    f.setForeground(AppColors.textPrimary());
                }
            }

            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) {
                    f.setText(placeholder);
                    f.setForeground(AppColors.textMuted());
                }
            }
        });
        return f;
    }

    public static void styleField(JComponent f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBackground(AppColors.bgSecondary());
        f.setForeground(AppColors.textPrimary());
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.border(), 1, true),
                new EmptyBorder(10, 14, 10, 14)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    public static JButton buildAccentButton(String text) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(AppColors.ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        AnimationUtil.addHoverEffect(btn, AppColors.ACCENT, AppColors.ACCENT_HOVER);
        return btn;
    }

    public static JButton buildLinkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(AppColors.ACCENT);
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JLabel buildLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(AppColors.textSecondary());
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JButton buildThemeToggle() {
        JButton btn = new JButton(ThemeManager.isDark() ? "☀  Modo claro" : "☾  Modo oscuro");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(AppColors.textMuted());
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            ThemeManager.toggle();
            refreshTheme();
        });
        return btn;
    }
}