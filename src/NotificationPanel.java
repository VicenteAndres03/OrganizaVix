import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de notificaciones que se desliza desde el lado derecho del HomeFrame.
 * Muestra tareas pendientes de hoy y tareas diarias sin completar.
 */
public class NotificationPanel extends JPanel {

    private final int userId;
    private final Runnable onClose;
    private JPanel listPanel;
    private JLabel countLabel;

    // Contador estático — para mostrar en el badge del topbar
    private static int pendingCount = 0;

    public NotificationPanel(int userId, Runnable onClose) {
        this.userId = userId;
        this.onClose = onClose;
        setLayout(new BorderLayout());
        setBackground(AppColors.bgCard());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, AppColors.border()),
                new EmptyBorder(0, 0, 0, 0)));
        setPreferredSize(new Dimension(320, 0));
        buildUI();
        loadNotifications();
    }

    public static int getPendingCount() {
        return pendingCount;
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppColors.bgCard());
        header.setBorder(new EmptyBorder(20, 20, 16, 16));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);

        JLabel icon = new JLabel("🔔");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        JLabel title = new JLabel("Notificaciones");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(AppColors.textPrimary());

        countLabel = new JLabel("") {
            @Override
            protected void paintComponent(Graphics g) {
                if (getText().isEmpty())
                    return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.ACCENT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(20, 20);
            }
        };

        titleRow.add(icon);
        titleRow.add(title);
        titleRow.add(Box.createHorizontalStrut(4));
        titleRow.add(countLabel);

        // Botón cerrar
        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        closeBtn.setForeground(AppColors.textMuted());
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> {
            if (onClose != null)
                onClose.run();
        });

        header.add(titleRow, BorderLayout.CENTER);
        header.add(closeBtn, BorderLayout.EAST);

        // Separador
        JPanel sep = new JPanel();
        sep.setBackground(AppColors.border());
        sep.setPreferredSize(new Dimension(0, 1));

        // Lista de notificaciones
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(AppColors.bgCard());
        listPanel.setBorder(new EmptyBorder(8, 0, 16, 0));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(AppColors.bgCard());
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setBackground(AppColors.bgCard());
        topSection.add(header, BorderLayout.NORTH);
        topSection.add(sep, BorderLayout.SOUTH);

        add(topSection, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void loadNotifications() {
        new Thread(() -> {
            List<Object[]> items = new ArrayList<>();
            String sql = """
                        SELECT id, titulo, descripcion, estado, fecha, prioridad, es_diaria, hora
                        FROM tareas
                        WHERE usuario_id = ?
                          AND estado != 'terminado'
                          AND (fecha = CURRENT_DATE OR es_diaria = TRUE)
                        ORDER BY prioridad DESC, hora ASC NULLS LAST
                    """;
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    items.add(new Object[] {
                            rs.getInt("id"),
                            rs.getString("titulo"),
                            rs.getString("descripcion"),
                            rs.getString("estado"),
                            rs.getDate("fecha") != null ? rs.getDate("fecha").toString() : "",
                            rs.getString("prioridad"),
                            rs.getBoolean("es_diaria"),
                            rs.getString("hora")
                    });
                }
            } catch (SQLException e) {
                System.err.println("Error notificaciones: " + e.getMessage());
            }

            pendingCount = items.size();

            SwingUtilities.invokeLater(() -> {
                listPanel.removeAll();
                countLabel.setText(items.isEmpty() ? "" : String.valueOf(items.size()));

                if (items.isEmpty()) {
                    JPanel emptyState = buildEmptyState();
                    listPanel.add(emptyState);
                } else {
                    // Sección "Hoy"
                    listPanel.add(buildSectionLabel("Tareas para hoy"));
                    for (Object[] row : items) {
                        listPanel.add(buildNotifCard(
                                (int) row[0], (String) row[1], (String) row[2],
                                (String) row[3], (String) row[5], (boolean) row[6], (String) row[7]));
                        listPanel.add(Box.createVerticalStrut(4));
                    }
                }
                listPanel.revalidate();
                listPanel.repaint();
            });
        }).start();
    }

    private JPanel buildNotifCard(int id, String titulo, String desc, String estado,
            String prio, boolean diaria, String hora) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.bgSecondary());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));

                // Barra de prioridad izquierda
                Color prioColor = switch (prio != null ? prio : "media") {
                    case "alta" -> AppColors.PRIORITY_HIGH;
                    case "baja" -> AppColors.PRIORITY_LOW;
                    default -> AppColors.PRIORITY_MED;
                };
                g2.setColor(prioColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 16, 12, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel outerWrap = new JPanel(new BorderLayout());
        outerWrap.setOpaque(false);
        outerWrap.setBorder(new EmptyBorder(2, 12, 2, 12));
        outerWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        outerWrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Título
        JLabel titleLbl = new JLabel(titulo);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(AppColors.textPrimary());
        card.add(titleLbl);

        // Descripción corta
        if (desc != null && !desc.isEmpty()) {
            String shortDesc = desc.length() > 50 ? desc.substring(0, 47) + "..." : desc;
            JLabel descLbl = new JLabel(shortDesc);
            descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            descLbl.setForeground(AppColors.textSecondary());
            card.add(Box.createVerticalStrut(2));
            card.add(descLbl);
        }

        // Fila de info
        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        infoRow.setOpaque(false);
        infoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(Box.createVerticalStrut(6));

        if (diaria) {
            infoRow.add(buildChip("🔁 Diaria", new Color(139, 92, 246, 40), new Color(139, 92, 246)));
        }
        if (hora != null && !hora.isEmpty()) {
            infoRow.add(buildChip("⏰ " + hora, AppColors.bgHover(), AppColors.textSecondary()));
        }
        String estadoLabel = switch (estado) {
            case "proceso" -> "En proceso";
            default -> "Por hacer";
        };
        Color estadoColor = estado.equals("proceso") ? AppColors.STATUS_PROGRESS : AppColors.STATUS_TODO;
        infoRow.add(buildChip(estadoLabel, new Color(estadoColor.getRed(), estadoColor.getGreen(),
                estadoColor.getBlue(), 30), estadoColor));

        card.add(infoRow);
        outerWrap.add(card);
        return outerWrap;
    }

    private JLabel buildChip(String text, Color bg, Color fg) {
        JLabel chip = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setFont(new Font("Segoe UI", Font.BOLD, 10));
        chip.setForeground(fg);
        chip.setOpaque(false);
        chip.setBorder(new EmptyBorder(2, 6, 2, 6));
        return chip;
    }

    private JLabel buildSectionLabel(String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(AppColors.textMuted());
        lbl.setBorder(new EmptyBorder(8, 16, 6, 16));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel buildEmptyState() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(40, 20, 20, 20));

        JLabel emoji = new JLabel("✅");
        emoji.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        emoji.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel("¡Todo al día!");
        msg.setFont(new Font("Segoe UI", Font.BOLD, 16));
        msg.setForeground(AppColors.textPrimary());
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel(
                "<html><div style='text-align:center'>No tienes tareas pendientes para hoy.</div></html>");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(AppColors.textSecondary());
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(emoji);
        p.add(Box.createVerticalStrut(12));
        p.add(msg);
        p.add(Box.createVerticalStrut(6));
        p.add(sub);
        return p;
    }
}