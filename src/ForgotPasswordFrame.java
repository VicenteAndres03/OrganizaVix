import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;

public class ForgotPasswordFrame extends JFrame {

    private JTextField emailField, codigoField;
    private JPasswordField nuevaPassField, confirmPassField;
    private JLabel statusLabel, errorLabel;
    private JButton enviarBtn;
    private JPanel paso2Panel;
    private JPanel card;
    private String emailActual;

    public ForgotPasswordFrame() {
        setTitle("OrganizaVix — Recuperar contraseña");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 580));
        setSize(1050, 660);
        setLocationRelativeTo(null);
        setResizable(true);
        loadIcon();
        initUI();
    }

    private void loadIcon() {
        try {
            java.net.URL url = getClass().getResource("/assets/logo.png");
            if (url != null)
                setIconImage(new ImageIcon(url).getImage());
        } catch (Exception ignored) {
        }
    }

    private void initUI() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(AppColors.bg());

        // ── LEFT panel ───────────────────────────────────────────────
        JPanel left = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0,
                        new Color(30, 10, 60), getWidth(), getHeight(), new Color(80, 20, 140));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
                g2.setColor(new Color(200, 150, 255));
                g2.fillOval(-60, -60, 320, 320);
                g2.fillOval(getWidth() - 180, getHeight() - 200, 300, 300);
                g2.dispose();
            }
        };
        left.setLayout(new GridBagLayout());

        JPanel leftContent = new JPanel();
        leftContent.setLayout(new BoxLayout(leftContent, BoxLayout.Y_AXIS));
        leftContent.setOpaque(false);
        leftContent.setBorder(new EmptyBorder(0, 50, 0, 50));

        JLabel logoImg = LoginFrame.buildLogoImage(64);
        logoImg.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brand = new JLabel("OrganizaVix");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 36));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel(
                "<html><div style='width:280px; color:rgba(255,255,255,0.7); font-size:15px; line-height:1.6'>Recupera el acceso a tu cuenta en pocos pasos.</div></html>");
        tagline.setForeground(new Color(200, 180, 240));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel steps = new JLabel("<html><div style='color:rgba(200,180,255,0.9); font-size:13px; line-height:2'>" +
                "① Ingresa tu correo registrado<br>" +
                "② Revisa tu bandeja de entrada<br>" +
                "③ Ingresa el código de 6 dígitos<br>" +
                "④ Define tu nueva contraseña</div></html>");
        steps.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftContent.add(logoImg);
        leftContent.add(Box.createVerticalStrut(20));
        leftContent.add(brand);
        leftContent.add(Box.createVerticalStrut(14));
        leftContent.add(tagline);
        leftContent.add(Box.createVerticalStrut(28));
        leftContent.add(steps);
        left.add(leftContent);

        // ── RIGHT panel ──────────────────────────────────────────────
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(AppColors.bg());

        card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppColors.bgCard());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.border(), 1, true),
                new EmptyBorder(40, 48, 40, 48)));

        JLabel title = new JLabel("Recuperar contraseña");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(AppColors.textPrimary());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Te enviaremos un código a tu correo");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(AppColors.textSecondary());
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Paso 1: email ────────────────────────────────────────────
        emailField = LoginFrame.buildTextField("usuario@ejemplo.com");

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(AppColors.textMuted());
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        enviarBtn = LoginFrame.buildAccentButton("Enviar código");
        enviarBtn.addActionListener(e -> handleEnviarCodigo());

        // ── Paso 2: código + nueva pass ──────────────────────────────
        paso2Panel = new JPanel();
        paso2Panel.setLayout(new BoxLayout(paso2Panel, BoxLayout.Y_AXIS));
        paso2Panel.setBackground(AppColors.bgCard());
        paso2Panel.setVisible(false);

        // Separador visual
        JSeparator sep = new JSeparator();
        sep.setForeground(AppColors.border());
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JLabel paso2Title = new JLabel("Ingresa el código recibido");
        paso2Title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        paso2Title.setForeground(AppColors.textPrimary());
        paso2Title.setAlignmentX(Component.LEFT_ALIGNMENT);

        codigoField = new JTextField();
        codigoField.setFont(new Font("Segoe UI", Font.BOLD, 22));
        codigoField.setHorizontalAlignment(SwingConstants.CENTER);
        LoginFrame.styleField(codigoField);

        nuevaPassField = new JPasswordField();
        LoginFrame.styleField(nuevaPassField);
        confirmPassField = new JPasswordField();
        LoginFrame.styleField(confirmPassField);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(AppColors.STATUS_TODO);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton resetBtn = LoginFrame.buildAccentButton("Cambiar contraseña");
        resetBtn.addActionListener(e -> handleResetPassword());

        paso2Panel.add(Box.createVerticalStrut(20));
        paso2Panel.add(sep);
        paso2Panel.add(Box.createVerticalStrut(20));
        paso2Panel.add(paso2Title);
        paso2Panel.add(Box.createVerticalStrut(14));
        paso2Panel.add(LoginFrame.buildLabel("Código de 6 dígitos"));
        paso2Panel.add(Box.createVerticalStrut(6));
        paso2Panel.add(codigoField);
        paso2Panel.add(Box.createVerticalStrut(14));
        paso2Panel.add(LoginFrame.buildLabel("Nueva contraseña"));
        paso2Panel.add(Box.createVerticalStrut(6));
        paso2Panel.add(nuevaPassField);
        paso2Panel.add(Box.createVerticalStrut(14));
        paso2Panel.add(LoginFrame.buildLabel("Confirmar contraseña"));
        paso2Panel.add(Box.createVerticalStrut(6));
        paso2Panel.add(confirmPassField);
        paso2Panel.add(Box.createVerticalStrut(8));
        paso2Panel.add(errorLabel);
        paso2Panel.add(Box.createVerticalStrut(8));
        paso2Panel.add(resetBtn);

        // Volver al login
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        backRow.setBackground(AppColors.bgCard());
        backRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        backRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JButton backBtn = LoginFrame.buildLinkButton("← Volver al inicio de sesión");
        backBtn.addActionListener(e -> dispose());
        backRow.add(backBtn);

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(28));
        card.add(LoginFrame.buildLabel("Correo electrónico"));
        card.add(Box.createVerticalStrut(6));
        card.add(emailField);
        card.add(Box.createVerticalStrut(8));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(enviarBtn);
        card.add(paso2Panel);
        card.add(Box.createVerticalStrut(16));
        card.add(backRow);

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

    private void handleEnviarCodigo() {
        String email = emailField.getText().trim();
        if (email.isEmpty() || email.equals("usuario@ejemplo.com")) {
            statusLabel.setText("Ingresa tu correo.");
            statusLabel.setForeground(AppColors.STATUS_TODO);
            return;
        }
        enviarBtn.setEnabled(false);
        statusLabel.setText("Enviando código...");
        statusLabel.setForeground(AppColors.textMuted());

        new Thread(() -> {
            String token = AuthService.generarTokenRecuperacion(email);
            if (token == null) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Ese correo no está registrado.");
                    statusLabel.setForeground(AppColors.STATUS_TODO);
                    enviarBtn.setEnabled(true);
                });
                return;
            }
            boolean enviado = EmailService.enviarCodigoRecuperacion(email, token);
            SwingUtilities.invokeLater(() -> {
                if (enviado) {
                    emailActual = email;
                    statusLabel.setText("✓ Código enviado a " + email);
                    statusLabel.setForeground(AppColors.STATUS_DONE);
                    emailField.setEnabled(false);
                    paso2Panel.setVisible(true);
                    card.revalidate();
                    card.repaint();
                } else {
                    statusLabel.setText("Error al enviar. Revisa EmailService.java");
                    statusLabel.setForeground(AppColors.STATUS_TODO);
                    enviarBtn.setEnabled(true);
                }
            });
        }).start();
    }

    private void handleResetPassword() {
        String codigo = codigoField.getText().trim();
        String nueva = new String(nuevaPassField.getPassword());
        String confirm = new String(confirmPassField.getPassword());

        if (codigo.isEmpty() || nueva.isEmpty()) {
            showError("Completa todos los campos.");
            return;
        }
        if (!nueva.equals(confirm)) {
            showError("Las contraseñas no coinciden.");
            return;
        }
        if (nueva.length() < 6) {
            showError("Mínimo 6 caracteres.");
            return;
        }

        new Thread(() -> {
            boolean ok = AuthService.resetPassword(emailActual, codigo, nueva);
            SwingUtilities.invokeLater(() -> {
                if (ok) {
                    JOptionPane.showMessageDialog(this,
                            "¡Contraseña cambiada exitosamente!\nYa puedes iniciar sesión.",
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    showError("Código incorrecto o expirado.");
                }
            });
        }).start();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setForeground(AppColors.STATUS_TODO);
    }
}