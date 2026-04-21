import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KanbanPanel extends JPanel {

    private final int userId;
    private final HomeFrame homeFrame;
    private JPanel pendientePanel, procesoPanel, terminadoPanel;

    public KanbanPanel(int userId, HomeFrame homeFrame) {
        this.userId = userId;
        this.homeFrame = homeFrame;
        setLayout(new BorderLayout());
        setBackground(AppColors.bg());
        initUI();
        cargarTareas();
    }

    private void initUI() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        header.setBackground(AppColors.bg());

        JButton addBtn = LoginFrame.buildAccentButton("+ Nueva tarea");
        addBtn.setMaximumSize(new Dimension(140, 36));
        addBtn.addActionListener(e -> mostrarDialogoNuevaTarea());

        header.add(addBtn);

        JPanel columnas = new JPanel(new GridLayout(1, 3, 12, 0));
        columnas.setBackground(AppColors.bg());
        columnas.setBorder(new EmptyBorder(0, 16, 16, 16));

        pendientePanel = crearColumna("Por hacer", AppColors.STATUS_TODO);
        procesoPanel = crearColumna("En proceso", AppColors.STATUS_PROGRESS);
        terminadoPanel = crearColumna("Terminado", AppColors.STATUS_DONE);

        columnas.add(wrapScroll(pendientePanel));
        columnas.add(wrapScroll(procesoPanel));
        columnas.add(wrapScroll(terminadoPanel));

        add(header, BorderLayout.NORTH);
        add(columnas, BorderLayout.CENTER);
    }

    private JScrollPane wrapScroll(JPanel col) {
        JScrollPane sp = new JScrollPane(col);
        sp.setBorder(null);
        sp.getViewport().setBackground(AppColors.bgSecondary());
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return sp;
    }

    private JPanel crearColumna(String titulo, Color color) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(AppColors.bgSecondary());
        col.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 2, true),
                new EmptyBorder(8, 8, 8, 8)));

        JLabel label = new JLabel(titulo);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(4, 4, 12, 4));

        col.add(label);
        col.putClientProperty("estado",
                titulo.equals("Por hacer") ? "pendiente" : titulo.equals("En proceso") ? "proceso" : "terminado");
        return col;
    }

    public void refreshColors() {
        setBackground(AppColors.bg());
        if (getComponentCount() > 0 && getComponent(0) instanceof JPanel header) {
            header.setBackground(AppColors.bg());
        }
        if (getComponentCount() > 1 && getComponent(1) instanceof JPanel columnas) {
            columnas.setBackground(AppColors.bg());
            for (Component sp : columnas.getComponents()) {
                if (sp instanceof JScrollPane scroll) {
                    scroll.getViewport().setBackground(AppColors.bgSecondary());
                    if (scroll.getViewport().getView() instanceof JPanel col) {
                        col.setBackground(AppColors.bgSecondary());
                    }
                }
            }
        }
        cargarTareas();
    }

    public void cargarTareas() {
        new Thread(() -> {
            java.util.List<Object[]> listaTareas = new java.util.ArrayList<>();
            String sql = "SELECT * FROM tareas WHERE usuario_id = ? ORDER BY creado DESC";

            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String titulo = rs.getString("titulo");
                    String desc = rs.getString("descripcion");
                    String estado = rs.getString("estado");
                    String fecha = rs.getDate("fecha") != null ? rs.getDate("fecha").toString() : "";
                    String prioridad;
                    try {
                        prioridad = rs.getString("prioridad");
                    } catch (SQLException ex) {
                        prioridad = "media";
                    }
                    listaTareas.add(new Object[] { id, titulo, desc, estado, fecha, prioridad });
                }
            } catch (SQLException e) {
                System.err.println("Error al cargar tareas: " + e.getMessage());
            }

            SwingUtilities.invokeLater(() -> {
                limpiarColumna(pendientePanel);
                limpiarColumna(procesoPanel);
                limpiarColumna(terminadoPanel);

                for (Object[] row : listaTareas) {
                    int id = (int) row[0];
                    String titulo = (String) row[1];
                    String desc = (String) row[2];
                    String estado = (String) row[3];
                    String fecha = (String) row[4];
                    String prioridad = (String) row[5];

                    JPanel card = crearTarjeta(id, titulo, desc, estado, fecha, prioridad);
                    switch (estado) {
                        case "pendiente" -> {
                            pendientePanel.add(card);
                            pendientePanel.add(Box.createVerticalStrut(8));
                        }
                        case "proceso" -> {
                            procesoPanel.add(card);
                            procesoPanel.add(Box.createVerticalStrut(8));
                        }
                        case "terminado" -> {
                            terminadoPanel.add(card);
                            terminadoPanel.add(Box.createVerticalStrut(8));
                        }
                    }
                }
                revalidate();
                repaint();
            });
        }).start();
    }

    private void limpiarColumna(JPanel col) {
        Component[] comps = col.getComponents();
        List<Component> toRemove = new ArrayList<>();
        for (int i = 1; i < comps.length; i++)
            toRemove.add(comps[i]);
        for (Component c : toRemove)
            col.remove(c);
    }

    private JPanel crearTarjeta(int id, String titulo, String desc,
            String estado, String fecha, String prioridad) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppColors.bgCard());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.border(), 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tituloLabel = new JLabel(titulo);
        tituloLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tituloLabel.setForeground(AppColors.textPrimary());
        tituloLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(tituloLabel);

        card.add(Box.createVerticalStrut(4));
        card.add(buildPriorityBadge(prioridad));

        if (desc != null && !desc.isEmpty()) {
            JLabel descLabel = new JLabel("<html><body style='width:160px'>" + desc + "</body></html>");
            descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            descLabel.setForeground(AppColors.textSecondary());
            descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(Box.createVerticalStrut(4));
            card.add(descLabel);
        }

        if (!fecha.isEmpty()) {
            JLabel fechaLabel = new JLabel("📅 " + fecha);
            fechaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            fechaLabel.setForeground(AppColors.PRIORITY_MED);
            fechaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(Box.createVerticalStrut(4));
            card.add(fechaLabel);
        }

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        acciones.setBackground(AppColors.bgCard());
        acciones.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (!estado.equals("pendiente"))
            addBotonEstado(acciones, id, "pendiente", "← Volver");
        if (!estado.equals("proceso"))
            addBotonEstado(acciones, id, "proceso", "▶ Proceso");
        if (!estado.equals("terminado"))
            addBotonEstado(acciones, id, "terminado", "✓ Listo");

        JButton delBtn = new JButton("✕");
        delBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        delBtn.setForeground(AppColors.STATUS_TODO);
        delBtn.setBackground(AppColors.bgCard());
        delBtn.setBorderPainted(false);
        delBtn.setFocusPainted(false);
        delBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        delBtn.addActionListener(e -> eliminarTarea(id));
        acciones.add(delBtn);

        card.add(Box.createVerticalStrut(6));
        card.add(acciones);

        return card;
    }

    private void addBotonEstado(JPanel panel, int id, String nuevoEstado, String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(AppColors.bgHover());
        btn.setForeground(AppColors.textSecondary());
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> cambiarEstado(id, nuevoEstado));
        panel.add(btn);
    }

    private void cambiarEstado(int id, String estado) {
        String sql = "UPDATE tareas SET estado = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado: " + e.getMessage());
        }
        cargarTareas();
    }

    private void eliminarTarea(int id) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar esta tarea?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        String sql = "DELETE FROM tareas WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al eliminar: " + e.getMessage());
        }
        cargarTareas();
    }

    private void mostrarDialogoNuevaTarea() {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), "Nueva tarea", true);
        dialog.setSize(380, 460);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(AppColors.bgCard());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AppColors.bgCard());
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JTextField tituloF = LoginFrame.buildTextField("Título");
        LoginFrame.styleField(tituloF);

        JTextField descF = LoginFrame.buildTextField("Descripción (opcional)");
        LoginFrame.styleField(descF);

        JTextField fechaF = LoginFrame.buildTextField("YYYY-MM-DD (opcional)");
        LoginFrame.styleField(fechaF);

        String[] estados = { "pendiente", "proceso", "terminado" };
        JComboBox<String> estadoBox = new JComboBox<>(estados);
        estadoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        String[] prioridades = { "alta", "media", "baja" };
        JComboBox<String> prioBox = new JComboBox<>(prioridades);
        prioBox.setSelectedIndex(1);
        prioBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton guardar = LoginFrame.buildAccentButton("Guardar tarea");
        guardar.addActionListener(e -> {
            String t = tituloF.getText().trim();
            if (t.isEmpty() || t.equals("Título"))
                return;
            String d = descF.getText().trim();
            String f = fechaF.getText().trim();
            String es = (String) estadoBox.getSelectedItem();
            String pr = (String) prioBox.getSelectedItem();

            String sql = "INSERT INTO tareas (usuario_id, titulo, descripcion, estado, fecha, prioridad) VALUES (?,?,?,?,?,?)";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, t);
                ps.setString(3, d);
                ps.setString(4, es);
                ps.setDate(5, (f.isEmpty() || f.equals("YYYY-MM-DD (opcional)"))
                        ? null
                        : java.sql.Date.valueOf(f));
                ps.setString(6, pr);
                ps.executeUpdate();
                dialog.dispose();
                cargarTareas();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        panel.add(LoginFrame.buildLabel("Título"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(tituloF);
        panel.add(Box.createVerticalStrut(12));
        panel.add(LoginFrame.buildLabel("Descripción"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(descF);
        panel.add(Box.createVerticalStrut(12));
        panel.add(LoginFrame.buildLabel("Fecha"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(fechaF);
        panel.add(Box.createVerticalStrut(12));
        panel.add(LoginFrame.buildLabel("Estado inicial"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(estadoBox);
        panel.add(Box.createVerticalStrut(12));
        panel.add(LoginFrame.buildLabel("Prioridad"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(prioBox);
        panel.add(Box.createVerticalStrut(16));
        panel.add(guardar);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private JPanel buildPriorityBadge(String prioridad) {
        String p = (prioridad == null) ? "media" : prioridad;
        JLabel lbl = new JLabel(p);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        Color bg, fg;
        switch (p) {
            case "alta" -> {
                bg = AppColors.PRIORITY_HIGH_BG;
                fg = AppColors.PRIORITY_HIGH;
            }
            case "baja" -> {
                bg = AppColors.PRIORITY_LOW_BG;
                fg = AppColors.PRIORITY_LOW;
            }
            default -> {
                bg = AppColors.PRIORITY_MED_BG;
                fg = AppColors.PRIORITY_MED;
            }
        }
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setOpaque(true);
        lbl.setBorder(new EmptyBorder(3, 8, 3, 8));

        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badge.setOpaque(false);
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        badge.add(lbl);
        return badge;
    }
}