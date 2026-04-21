import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ProfilePanel extends JPanel {

    private final int userId;
    private final String email;
    private final HomeFrame homeFrame;

    private JPasswordField passActualField;
    private JPasswordField passNuevaField;
    private JPanel cardPanel;
    private JLabel infoNombre, infoEmail, tituloContra, tituloPeligro, warnText;

    public ProfilePanel(int userId, String email, HomeFrame homeFrame) {
        this.userId = userId;
        this.email = email;
        this.homeFrame = homeFrame;
        setLayout(new BorderLayout());
        setBackground(AppColors.bg());
        initUI();
    }

    private void initUI() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(AppColors.bg());
        scrollContent.setBorder(new EmptyBorder(24, 40, 40, 40));

        cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(AppColors.border());
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(AppColors.bgCard());
        cardPanel.setOpaque(false);
        cardPanel.setBorder(new EmptyBorder(32, 40, 40, 40));
        cardPanel.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));
        cardPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel infoHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        infoHeader.setOpaque(false);
        infoHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel avatar = buildAvatar(AuthService.getNombre(email));

        JPanel textInfo = new JPanel();
        textInfo.setLayout(new BoxLayout(textInfo, BoxLayout.Y_AXIS));
        textInfo.setOpaque(false);

        infoNombre = new JLabel(AuthService.getNombre(email));
        infoNombre.setFont(new Font("Segoe UI", Font.BOLD, 22));
        infoNombre.setForeground(AppColors.textPrimary());

        infoEmail = new JLabel(email);
        infoEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoEmail.setForeground(AppColors.textSecondary());

        textInfo.add(infoNombre);
        textInfo.add(Box.createVerticalStrut(4));
        textInfo.add(infoEmail);

        infoHeader.add(avatar);
        infoHeader.add(textInfo);

        tituloContra = new JLabel("Cambiar Contraseña");
        tituloContra.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tituloContra.setForeground(AppColors.textPrimary());
        tituloContra.setAlignmentX(Component.LEFT_ALIGNMENT);

        passActualField = new JPasswordField();
        LoginFrame.styleField(passActualField);

        passNuevaField = new JPasswordField();
        LoginFrame.styleField(passNuevaField);

        JButton btnActualizar = LoginFrame.buildAccentButton("Actualizar Contraseña");
        btnActualizar.setMaximumSize(new Dimension(200, 42));
        btnActualizar.addActionListener(e -> cambiarPassword());

        tituloPeligro = new JLabel("Zona de Peligro");
        tituloPeligro.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tituloPeligro.setForeground(new Color(239, 68, 68));
        tituloPeligro.setAlignmentX(Component.LEFT_ALIGNMENT);

        warnText = new JLabel(
                "<html><p style='width:380px;'>Una vez que elimines tu cuenta, no hay vuelta atrás. Todas tus tareas y datos serán borrados permanentemente.</p></html>");
        warnText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        warnText.setForeground(AppColors.textSecondary());
        warnText.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnEliminar = new JButton("Eliminar mi cuenta") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(220, 38, 38) : new Color(239, 68, 68));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnEliminar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setContentAreaFilled(false);
        btnEliminar.setBorderPainted(false);
        btnEliminar.setMaximumSize(new Dimension(200, 42));
        btnEliminar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnEliminar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEliminar.addActionListener(e -> confirmarEliminacion());

        cardPanel.add(infoHeader);
        cardPanel.add(Box.createVerticalStrut(40));
        cardPanel.add(addSeparator());
        cardPanel.add(Box.createVerticalStrut(24));
        cardPanel.add(tituloContra);
        cardPanel.add(Box.createVerticalStrut(16));
        cardPanel.add(LoginFrame.buildLabel("Contraseña actual"));
        cardPanel.add(Box.createVerticalStrut(6));
        cardPanel.add(passActualField);
        cardPanel.add(Box.createVerticalStrut(16));
        cardPanel.add(LoginFrame.buildLabel("Nueva contraseña"));
        cardPanel.add(Box.createVerticalStrut(6));
        cardPanel.add(passNuevaField);
        cardPanel.add(Box.createVerticalStrut(24));
        cardPanel.add(btnActualizar);

        cardPanel.add(Box.createVerticalStrut(40));
        cardPanel.add(addSeparator());
        cardPanel.add(Box.createVerticalStrut(24));
        cardPanel.add(tituloPeligro);
        cardPanel.add(Box.createVerticalStrut(8));
        cardPanel.add(warnText);
        cardPanel.add(Box.createVerticalStrut(24));
        cardPanel.add(btnEliminar);

        scrollContent.add(cardPanel);

        JScrollPane scroll = new JScrollPane(scrollContent);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(AppColors.bg());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);
    }

    private JPanel addSeparator() {
        JPanel sep = new JPanel() {
            protected void paintComponent(Graphics g) {
                g.setColor(AppColors.border());
                g.fillRect(0, 0, getWidth(), 1);
            }

            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, 1);
            }
        };
        sep.setOpaque(false);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    private void cambiarPassword() {
        String actual = new String(passActualField.getPassword());
        String nueva = new String(passNuevaField.getPassword());

        if (actual.isEmpty() || nueva.isEmpty()) {
            CustomDialog.showMessage(homeFrame, "Aviso", "Por favor llena ambos campos.", true);
            return;
        }

        if (nueva.length() < 6) {
            CustomDialog.showMessage(homeFrame, "Aviso", "La nueva contraseña debe tener al menos 6 caracteres.", true);
            return;
        }

        boolean exito = AuthService.changePassword(userId, actual, nueva);
        if (exito) {
            CustomDialog.showMessage(homeFrame, "Éxito", "¡Contraseña actualizada exitosamente!", false);
            passActualField.setText("");
            passNuevaField.setText("");
        } else {
            CustomDialog.showMessage(homeFrame, "Error", "La contraseña actual es incorrecta.", true);
        }
    }

    private void confirmarEliminacion() {
        String msj = "¿Estás ABSOLUTAMENTE SEGURO de que deseas eliminar tu cuenta? Se perderán todas tus tareas.";
        // ¡Usamos el nuevo diseño bonito para el popup!
        boolean confirmado = CustomDialog.showConfirm(homeFrame, "Eliminar Cuenta", msj, true);

        if (confirmado) {
            if (AuthService.deleteAccount(userId)) {
                CustomDialog.showMessage(homeFrame, "Adiós", "Tu cuenta ha sido eliminada permanentemente.", false);
                homeFrame.dispose();
                new LoginFrame().setVisible(true);
            } else {
                CustomDialog.showMessage(homeFrame, "Error", "Ocurrió un error al eliminar tu cuenta.", true);
            }
        }
    }

    public void refreshColors() {
        setBackground(AppColors.bg());
        cardPanel.setBackground(AppColors.bgCard());
        infoNombre.setForeground(AppColors.textPrimary());
        infoEmail.setForeground(AppColors.textSecondary());
        tituloContra.setForeground(AppColors.textPrimary());
        warnText.setForeground(AppColors.textSecondary());

        LoginFrame.styleField(passActualField);
        LoginFrame.styleField(passNuevaField);

        Component[] comps = cardPanel.getComponents();
        for (Component c : comps) {
            if (c instanceof JLabel lbl && lbl.getText().equals("Contraseña actual")
                    || c instanceof JLabel lbl2 && lbl2.getText().equals("Nueva contraseña")) {
                ((JLabel) c).setForeground(AppColors.textSecondary());
            }
        }
        repaint();
    }

    private JPanel buildAvatar(String nombre) {
        return new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.ACCENT);
                g2.fillOval(0, 0, 60, 60);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
                String initials = nombre.length() >= 2 ? nombre.substring(0, 2).toUpperCase()
                        : nombre.substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials, (60 - fm.stringWidth(initials)) / 2, 38);
                g2.dispose();
            }

            public Dimension getPreferredSize() {
                return new Dimension(60, 60);
            }

            public Dimension getMinimumSize() {
                return new Dimension(60, 60);
            }

            public Dimension getMaximumSize() {
                return new Dimension(60, 60);
            }
        };
    }
}