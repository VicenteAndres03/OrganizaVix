import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class CustomDialog {

    // Mensajes de alerta normales (Éxito o Error)
    public static void showMessage(Frame parent, String title, String message, boolean isError) {
        JDialog dialog = new JDialog(parent, title, true);
        dialog.setUndecorated(true); // Quita la barra superior fea del sistema operativo
        dialog.setBackground(new Color(0, 0, 0, 0)); // Fondo transparente para esquinas redondeadas

        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(AppColors.border());
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(24, 32, 24, 32));
        panel.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(isError ? AppColors.STATUS_TODO : AppColors.textPrimary());
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msgLbl = new JLabel("<html><div style='text-align: center; width: 250px;'>" + message + "</div></html>");
        msgLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msgLbl.setForeground(AppColors.textSecondary());
        msgLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton okBtn = LoginFrame.buildAccentButton("Aceptar");
        okBtn.setMaximumSize(new Dimension(120, 36));
        okBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        okBtn.addActionListener(e -> dialog.dispose());

        panel.add(titleLbl);
        panel.add(Box.createVerticalStrut(16));
        panel.add(msgLbl);
        panel.add(Box.createVerticalStrut(24));
        panel.add(okBtn);

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    // Mensajes de confirmación (Sí/No) para borrar cosas
    public static boolean showConfirm(Frame parent, String title, String message, boolean isDanger) {
        final boolean[] result = { false };
        JDialog dialog = new JDialog(parent, title, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(AppColors.border());
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(24, 32, 24, 32));
        panel.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(isDanger ? AppColors.STATUS_TODO : AppColors.textPrimary());
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msgLbl = new JLabel("<html><div style='text-align: center; width: 280px;'>" + message + "</div></html>");
        msgLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msgLbl.setForeground(AppColors.textSecondary());
        msgLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnRow.setOpaque(false);

        JButton cancelBtn = new JButton("Cancelar") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(ThemeManager.isDark() ? new Color(255, 255, 255, 20) : new Color(0, 0, 0, 15));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelBtn.setForeground(AppColors.textSecondary());
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setPreferredSize(new Dimension(100, 36));
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton confirmBtn = new JButton(isDanger ? "Eliminar" : "Confirmar") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isDanger ? new Color(239, 68, 68) : AppColors.ACCENT);
                if (getModel().isRollover())
                    g2.setColor(isDanger ? new Color(220, 38, 38) : AppColors.ACCENT_HOVER);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setContentAreaFilled(false);
        confirmBtn.setBorderPainted(false);
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmBtn.setPreferredSize(new Dimension(120, 36));
        confirmBtn.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });

        btnRow.add(cancelBtn);
        btnRow.add(confirmBtn);

        panel.add(titleLbl);
        panel.add(Box.createVerticalStrut(16));
        panel.add(msgLbl);
        panel.add(Box.createVerticalStrut(24));
        panel.add(btnRow);

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return result[0];
    }
}