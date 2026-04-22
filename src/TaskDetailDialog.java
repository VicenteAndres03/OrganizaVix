import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;

/**
 * Modal para VER y EDITAR una tarea existente.
 * Se abre haciendo clic en el título de cualquier tarjeta Kanban.
 */
public class TaskDetailDialog extends JDialog {

    private final int taskId;
    private final HomeFrame homeFrame;
    private final Runnable onSave;

    private JTextField tituloF, fechaF, horaF;
    private JTextArea descF;
    private JComboBox<String> estadoBox, prioBox;
    private JCheckBox chkDiaria;
    private JLabel errorLbl;

    public TaskDetailDialog(HomeFrame parent, int taskId, Runnable onSave) {
        super(parent, "Detalle de tarea", true);
        this.taskId = taskId;
        this.homeFrame = parent;
        this.onSave = onSave;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(480, 620);
        setLocationRelativeTo(parent);

        buildUI();
        loadTask();
    }

    private void buildUI() {
        // Panel con sombra y bordes redondeados
        JPanel root = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Sombra
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fill(new RoundRectangle2D.Float(4, 4, getWidth() - 4, getHeight() - 4, 20, 20));
                // Fondo
                g2.setColor(AppColors.bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 4, 20, 20));
                // Borde
                g2.setColor(AppColors.border());
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 5, getHeight() - 5, 20, 20));
                g2.dispose();
            }
        };
        root.setLayout(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(0, 0, 4, 4));

        // ── Topbar del modal ─────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(20, 28, 16, 20));

        JLabel titleLabel = new JLabel("Editar tarea");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(AppColors.textPrimary());

        // Botón X para cerrar
        JButton closeBtn = new JButton("✕") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(239, 68, 68, 40));
                    g2.fillOval(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeBtn.setForeground(AppColors.textMuted());
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setPreferredSize(new Dimension(32, 32));
        closeBtn.addActionListener(e -> dispose());

        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(closeBtn, BorderLayout.EAST);

        // ── Contenido ────────────────────────────────────────────────
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 28, 24, 28));

        // Título
        content.add(buildLabel("Título"));
        content.add(Box.createVerticalStrut(6));
        tituloF = new JTextField();
        LoginFrame.styleField(tituloF);
        content.add(tituloF);
        content.add(Box.createVerticalStrut(14));

        // Descripción — TextArea con scroll
        content.add(buildLabel("Descripción"));
        content.add(Box.createVerticalStrut(6));
        descF = new JTextArea(3, 20);
        descF.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descF.setBackground(AppColors.bgSecondary());
        descF.setForeground(AppColors.textPrimary());
        descF.setLineWrap(true);
        descF.setWrapStyleWord(true);
        descF.setCaretColor(AppColors.textPrimary());
        JScrollPane descScroll = new JScrollPane(descF);
        descScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.border(), 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(descScroll);
        content.add(Box.createVerticalStrut(14));

        // Fecha
        content.add(buildLabel("Fecha (YYYY-MM-DD)"));
        content.add(Box.createVerticalStrut(6));
        fechaF = new JTextField();
        LoginFrame.styleField(fechaF);
        content.add(fechaF);
        content.add(Box.createVerticalStrut(10));

        // Tarea diaria
        chkDiaria = new JCheckBox(" 🔁 Tarea diaria (se repite cada día)");
        chkDiaria.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkDiaria.setOpaque(false);
        chkDiaria.setForeground(AppColors.textPrimary());
        chkDiaria.setFocusPainted(false);
        chkDiaria.setAlignmentX(Component.LEFT_ALIGNMENT);

        horaF = new JTextField();
        LoginFrame.styleField(horaF);
        horaF.setVisible(false);

        chkDiaria.addActionListener(e -> {
            horaF.setVisible(chkDiaria.isSelected());
            fechaF.setEnabled(!chkDiaria.isSelected());
            content.revalidate();
            content.repaint();
        });

        content.add(chkDiaria);
        content.add(Box.createVerticalStrut(4));
        content.add(buildLabel("Hora (ej. 09:00)"));
        content.add(Box.createVerticalStrut(4));
        content.add(horaF);
        content.add(Box.createVerticalStrut(14));

        // Estado y Prioridad — fila
        content.add(buildLabel("Estado y Prioridad"));
        content.add(Box.createVerticalStrut(6));

        String[] estados = { "Por hacer", "En proceso", "Terminado" };
        String[] prioridades = { "alta", "media", "baja" };
        estadoBox = new JComboBox<>(estados);
        prioBox = new JComboBox<>(prioridades);
        estilizarCombo(estadoBox);
        estilizarCombo(prioBox);

        JPanel comboRow = new JPanel(new GridLayout(1, 2, 10, 0));
        comboRow.setOpaque(false);
        comboRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        comboRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboRow.add(estadoBox);
        comboRow.add(prioBox);
        content.add(comboRow);
        content.add(Box.createVerticalStrut(8));

        // Error label
        errorLbl = new JLabel(" ");
        errorLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLbl.setForeground(AppColors.STATUS_TODO);
        errorLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(errorLbl);
        content.add(Box.createVerticalStrut(16));

        // Botones
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton cancelBtn = buildGhostButton("Cancelar");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = LoginFrame.buildAccentButton("Guardar cambios");
        saveBtn.addActionListener(e -> saveTask());

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);
        content.add(btnRow);

        JScrollPane scrollContent = new JScrollPane(content);
        scrollContent.setBorder(null);
        scrollContent.setOpaque(false);
        scrollContent.getViewport().setOpaque(false);
        scrollContent.getVerticalScrollBar().setUnitIncrement(12);

        root.add(topBar, BorderLayout.NORTH);
        root.add(scrollContent, BorderLayout.CENTER);

        setContentPane(root);
    }

    private void loadTask() {
        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement("SELECT * FROM tareas WHERE id = ?")) {
                ps.setInt(1, taskId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String titulo = rs.getString("titulo");
                    String desc = rs.getString("descripcion");
                    String estado = rs.getString("estado");
                    String prio = rs.getString("prioridad");
                    String fecha = rs.getDate("fecha") != null ? rs.getDate("fecha").toString() : "";
                    boolean diaria = rs.getBoolean("es_diaria");
                    String hora = rs.getString("hora");

                    SwingUtilities.invokeLater(() -> {
                        tituloF.setText(titulo != null ? titulo : "");
                        descF.setText(desc != null ? desc : "");
                        fechaF.setText(fecha);
                        chkDiaria.setSelected(diaria);
                        horaF.setVisible(diaria);
                        horaF.setText(hora != null ? hora : "");
                        fechaF.setEnabled(!diaria);

                        estadoBox.setSelectedItem(switch (estado != null ? estado : "pendiente") {
                            case "proceso" -> "En proceso";
                            case "terminado" -> "Terminado";
                            default -> "Por hacer";
                        });
                        prioBox.setSelectedItem(prio != null ? prio : "media");
                    });
                }
            } catch (SQLException e) {
                System.err.println("Error cargando tarea: " + e.getMessage());
            }
        }).start();
    }

    private void saveTask() {
        String titulo = tituloF.getText().trim();
        if (titulo.isEmpty()) {
            errorLbl.setText("El título no puede estar vacío.");
            return;
        }

        String estadoSel = switch ((String) estadoBox.getSelectedItem()) {
            case "En proceso" -> "proceso";
            case "Terminado" -> "terminado";
            default -> "pendiente";
        };

        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(
                            "UPDATE tareas SET titulo=?, descripcion=?, estado=?, fecha=?, prioridad=?, es_diaria=?, hora=? WHERE id=?")) {
                ps.setString(1, titulo);
                ps.setString(2, descF.getText().trim());
                ps.setString(3, estadoSel);

                if (chkDiaria.isSelected()) {
                    ps.setDate(4, new java.sql.Date(System.currentTimeMillis()));
                } else {
                    String fechaTxt = fechaF.getText().trim();
                    try {
                        ps.setDate(4, fechaTxt.isEmpty() ? null : java.sql.Date.valueOf(fechaTxt));
                    } catch (IllegalArgumentException ex) {
                        SwingUtilities.invokeLater(() -> errorLbl.setText("Fecha inválida. Usa YYYY-MM-DD."));
                        return;
                    }
                }

                ps.setString(5, (String) prioBox.getSelectedItem());
                ps.setBoolean(6, chkDiaria.isSelected());
                ps.setString(7, chkDiaria.isSelected() ? horaF.getText().trim() : "");
                ps.setInt(8, taskId);
                ps.executeUpdate();

                SwingUtilities.invokeLater(() -> {
                    dispose();
                    if (onSave != null)
                        onSave.run();
                });
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> errorLbl.setText("Error al guardar: " + ex.getMessage()));
            }
        }).start();
    }

    private JLabel buildLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(AppColors.textSecondary());
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void estilizarCombo(JComboBox<String> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        box.setBackground(AppColors.bgSecondary());
        box.setForeground(AppColors.textPrimary());
        box.setBorder(BorderFactory.createLineBorder(AppColors.border(), 1, true));
    }

    private JButton buildGhostButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? AppColors.bgHover() : AppColors.bgSecondary());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(AppColors.border());
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.setColor(AppColors.textSecondary());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }
}