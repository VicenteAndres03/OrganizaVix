import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;

public class RegisterFrame extends JFrame {

    private JTextField nombreField, emailField;
    private JPasswordField passField, confirmField;
    private JLabel errorLabel;

    public RegisterFrame() {
        setTitle("OrganizaVix — Crear cuenta");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 580));
        setSize(1050, 660);
        setLocationRelativeTo(null);
        setResizable(true);
        loadIcon();
        initUI();
        // Al cerrar esta ventana, asegurarnos de que login siga visible
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) {
                // Si no hay otra ventana visible, abrir Login
                boolean hayVentana = false;
                for (java.awt.Window w : java.awt.Window.getWindows()) {
                    if (w.isVisible() && w != RegisterFrame.this) {
                        hayVentana = true;
                        break;
                    }
                }
                if (!hayVentana) {
                    new LoginFrame().setVisible(true);
                }
            }
        });
    }

    private void loadIcon() {
        try {
            java.net.URL url = getClass().getResource("assets/logo.png");
            if (url != null)
                setIconImage(new ImageIcon(url).getImage());
        } catch (Exception ignored) {
        }
    }

    private void initUI() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(AppColors.bg());

        // ── LEFT decorative panel ────────────────────────────────────
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
                "<html><div style='width:280px; color:rgba(255,255,255,0.7); font-size:15px; line-height:1.6'>Crea tu cuenta y empieza a organizar tus proyectos de forma visual e intuitiva.</div></html>");
        tagline.setForeground(new Color(200, 180, 240));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel step = new JLabel("<html><div style='color:rgba(200,180,255,0.9); font-size:13px; line-height:1.8'>" +
                "① Completa el formulario<br>" +
                "② Verifica tu correo<br>" +
                "③ ¡Empieza a organizar!</div></html>");
        step.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftContent.add(logoImg);
        leftContent.add(Box.createVerticalStrut(20));
        leftContent.add(brand);
        leftContent.add(Box.createVerticalStrut(14));
        leftContent.add(tagline);
        leftContent.add(Box.createVerticalStrut(28));
        leftContent.add(step);
        left.add(leftContent);

        // ── RIGHT form panel ─────────────────────────────────────────
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(AppColors.bg());

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppColors.bgCard());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.border(), 1, true),
                new EmptyBorder(40, 48, 40, 48)));

        JLabel title = new JLabel("Crear cuenta");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(AppColors.textPrimary());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Completa los datos para registrarte");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(AppColors.textSecondary());
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        nombreField = LoginFrame.buildTextField("Tu nombre completo");
        emailField = LoginFrame.buildTextField("usuario@ejemplo.com");
        passField = new JPasswordField();
        LoginFrame.styleField(passField);
        confirmField = new JPasswordField();
        LoginFrame.styleField(confirmField);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(AppColors.STATUS_TODO);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton registerBtn = LoginFrame.buildAccentButton("Crear cuenta");
        registerBtn.addActionListener(e -> handleRegister());

        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        backRow.setBackground(AppColors.bgCard());
        backRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        backRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JButton backBtn = LoginFrame.buildLinkButton("← Ya tengo cuenta");
        backBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        backRow.add(backBtn);

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(28));
        card.add(LoginFrame.buildLabel("Nombre completo"));
        card.add(Box.createVerticalStrut(6));
        card.add(nombreField);
        card.add(Box.createVerticalStrut(14));
        card.add(LoginFrame.buildLabel("Correo electrónico"));
        card.add(Box.createVerticalStrut(6));
        card.add(emailField);
        card.add(Box.createVerticalStrut(14));
        card.add(LoginFrame.buildLabel("Contraseña"));
        card.add(Box.createVerticalStrut(6));
        card.add(passField);
        card.add(Box.createVerticalStrut(14));
        card.add(LoginFrame.buildLabel("Confirmar contraseña"));
        card.add(Box.createVerticalStrut(6));
        card.add(confirmField);
        card.add(Box.createVerticalStrut(8));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(14));
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

    private void handleRegister() {
        String nombre = nombreField.getText().trim();
        String email = emailField.getText().trim();
        String pass = new String(passField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (nombre.isEmpty() || nombre.equals("Tu nombre completo") ||
                email.isEmpty() || email.equals("usuario@ejemplo.com") ||
                pass.isEmpty()) {
            showError("Completa todos los campos.");
            return;
        }
        if (!pass.equals(confirm)) {
            showError("Las contraseñas no coinciden.");
            return;
        }
        if (pass.length() < 6) {
            showError("La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        errorLabel.setText("Registrando...");
        errorLabel.setForeground(AppColors.textMuted());

        new Thread(() -> {
            boolean ok = AuthService.registrar(email, pass, nombre);
            SwingUtilities.invokeLater(() -> {
                if (ok) {
                    JOptionPane.showMessageDialog(this,
                            "¡Cuenta creada exitosamente!\nYa puedes iniciar sesión.",
                            "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    new LoginFrame().setVisible(true);
                } else {
                    showError("Ese correo ya está registrado.");
                }
            });
        }).start();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setForeground(AppColors.STATUS_TODO);
    }
}