import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KanbanPanel extends JPanel {

    private final int userId;
    private final HomeFrame homeFrame;
    private JPanel pendientePanel, procesoPanel, terminadoPanel;
    private String fechaFiltro = null;

    public KanbanPanel(int userId, HomeFrame homeFrame) {
        this.userId = userId;
        this.homeFrame = homeFrame;
        setLayout(new BorderLayout());
        setBackground(AppColors.bg());
        initUI();
        cargarTareas();
    }

    public void setFechaFiltro(String fecha) {
        this.fechaFiltro = fecha;
        cargarTareas();
    }

    private void initUI() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        header.setBackground(AppColors.bg());

        JButton addBtn = LoginFrame.buildAccentButton("+ Nueva tarea");
        addBtn.setMaximumSize(new Dimension(140, 36));
        addBtn.addActionListener(e -> mostrarDialogoNuevaTarea());

        JButton clearFilterBtn = new JButton("Volver a Hoy");
        clearFilterBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        clearFilterBtn.setForeground(AppColors.ACCENT);
        clearFilterBtn.setContentAreaFilled(false);
        clearFilterBtn.setBorderPainted(false);
        clearFilterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearFilterBtn.addActionListener(e -> setFechaFiltro(null));

        header.add(addBtn);
        header.add(clearFilterBtn);

        JPanel columnas = new JPanel(new GridLayout(1, 3, 16, 0));
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
        sp.getViewport().setBackground(ThemeManager.isDark() ? new Color(30, 30, 40) : new Color(243, 244, 246));
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JPanel crearColumna(String titulo, Color color) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(ThemeManager.isDark() ? new Color(30, 30, 40) : new Color(243, 244, 246));
        col.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel dot = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 4, 10, 10);
                g2.dispose();
            }

            public Dimension getPreferredSize() {
                return new Dimension(16, 18);
            }
        };
        dot.setOpaque(false);

        JLabel label = new JLabel(titulo);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(AppColors.textPrimary());

        headerPanel.add(dot);
        headerPanel.add(label);

        col.add(headerPanel);
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
                    scroll.getViewport()
                            .setBackground(ThemeManager.isDark() ? new Color(30, 30, 40) : new Color(243, 244, 246));
                    if (scroll.getViewport().getView() instanceof JPanel col) {
                        col.setBackground(ThemeManager.isDark() ? new Color(30, 30, 40) : new Color(243, 244, 246));
                        if (col.getComponentCount() > 0 && col.getComponent(0) instanceof JPanel headerPanel) {
                            for (Component c : headerPanel.getComponents()) {
                                if (c instanceof JLabel lbl) {
                                    lbl.setForeground(AppColors.textPrimary());
                                }
                            }
                        }
                    }
                }
            }
        }
        cargarTareas();
    }

    public void cargarTareas() {
        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                java.util.List<Object[]> listaTareas = new java.util.ArrayList<>();
                String sql = "SELECT * FROM tareas WHERE usuario_id = ? ";
                if (fechaFiltro != null) {
                    sql += "AND fecha = ? ";
                } else {
                    sql += "AND (fecha = CURRENT_DATE OR es_diaria = FALSE OR fecha IS NULL) ";
                }
                sql += "ORDER BY creado DESC";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, userId);
                    if (fechaFiltro != null) {
                        ps.setDate(2, java.sql.Date.valueOf(fechaFiltro));
                    }
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        listaTareas.add(new Object[] {
                                rs.getInt("id"), rs.getString("titulo"), rs.getString("descripcion"),
                                rs.getString("estado"),
                                rs.getDate("fecha") != null ? rs.getDate("fecha").toString() : "",
                                rs.getString("prioridad"), rs.getBoolean("es_diaria"), rs.getString("hora")
                        });
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    limpiarColumna(pendientePanel);
                    limpiarColumna(procesoPanel);
                    limpiarColumna(terminadoPanel);

                    for (Object[] row : listaTareas) {
                        JPanel card = crearTarjeta((int) row[0], (String) row[1], (String) row[2], (String) row[3],
                                (String) row[4], (String) row[5], (boolean) row[6], (String) row[7]);
                        switch ((String) row[3]) {
                            case "pendiente" -> {
                                pendientePanel.add(card);
                                pendientePanel.add(Box.createVerticalStrut(10));
                            }
                            case "proceso" -> {
                                procesoPanel.add(card);
                                procesoPanel.add(Box.createVerticalStrut(10));
                            }
                            case "terminado" -> {
                                terminadoPanel.add(card);
                                terminadoPanel.add(Box.createVerticalStrut(10));
                            }
                        }
                    }
                    revalidate();
                    repaint();
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void limpiarColumna(JPanel col) {
        Component[] comps = col.getComponents();
        for (int i = 1; i < comps.length; i++)
            col.remove(comps[i]);
    }

    private JPanel crearTarjeta(int id, String titulo, String desc, String estado, String fecha, String prioridad,
            boolean esDiaria, String hora) {
        JPanel card = new JPanel() {
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
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppColors.bgCard());
        card.setBorder(new EmptyBorder(14, 16, 10, 16)); // Reducido el margen inferior
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel hr = new JPanel(new BorderLayout());
        hr.setOpaque(false);
        JLabel titleLbl = new JLabel("<html><b>" + titulo + "</b></html>");
        titleLbl.setForeground(AppColors.textPrimary());
        hr.add(titleLbl, BorderLayout.WEST);
        hr.add(buildPriorityBadge(prioridad), BorderLayout.EAST);
        card.add(hr);
        card.add(Box.createVerticalStrut(8));

        if (desc != null && !desc.isEmpty()) {
            JLabel dLbl = new JLabel("<html><body style='width:180px; color:" + hex(AppColors.textSecondary()) + "'>"
                    + desc + "</body></html>");
            card.add(dLbl);
            card.add(Box.createVerticalStrut(10));
        }

        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        infoRow.setOpaque(false);
        if (esDiaria) {
            JLabel lbl = new JLabel("🔁 Diaria " + (hora.isEmpty() ? "" : "• ⏰ " + hora));
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setForeground(new Color(139, 92, 246));
            infoRow.add(lbl);
        } else if (!fecha.isEmpty()) {
            JLabel fLbl = new JLabel("📅 " + fecha);
            fLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            fLbl.setForeground(AppColors.textMuted());
            infoRow.add(fLbl);
        }
        card.add(infoRow);
        card.add(Box.createVerticalStrut(12));

        JPanel sep = new JPanel() {
            {
                setBackground(AppColors.border());
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            }
        };
        card.add(sep);
        card.add(Box.createVerticalStrut(6)); // Menos espacio antes de botones

        // CORRECCIÓN: Contenedor de botones con FlowLayout y separación real
        JPanel acc = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acc.setOpaque(false);
        acc.setAlignmentX(Component.RIGHT_ALIGNMENT);

        if (!estado.equals("pendiente"))
            addBtnVectorial(acc, id, "pendiente", "left", "Mover a Por hacer");
        if (!estado.equals("proceso"))
            addBtnVectorial(acc, id, "proceso", "right", "Mover a En proceso");
        if (!estado.equals("terminado"))
            addBtnVectorial(acc, id, "terminado", "check", "Completar tarea");
        addBtnDelete(acc, id);

        card.add(acc);
        return card;
    }

    private void addBtnVectorial(JPanel p, int id, String st, String type, String tooltip) {
        JButton b = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(AppColors.bgHover());
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                }
                g2.setColor(AppColors.textSecondary());
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int w = getWidth();
                int h = getHeight();
                if (type.equals("left"))
                    g2.drawPolyline(new int[] { w / 2 + 2, w / 2 - 2, w / 2 + 2 },
                            new int[] { h / 2 - 3, h / 2, h / 2 + 3 }, 3);
                else if (type.equals("right"))
                    g2.drawPolyline(new int[] { w / 2 - 2, w / 2 + 2, w / 2 - 2 },
                            new int[] { h / 2 - 3, h / 2, h / 2 + 3 }, 3);
                else if (type.equals("check"))
                    g2.drawPolyline(new int[] { w / 2 - 3, w / 2 - 1, w / 2 + 4 },
                            new int[] { h / 2 + 1, h / 2 + 3, h / 2 - 3 }, 3);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(24, 24)); // Un poco más pequeños
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setToolTipText(tooltip);
        b.addActionListener(e -> cambiarEstado(id, st));
        p.add(b);
    }

    private void addBtnDelete(JPanel p, int id) {
        JButton b = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(239, 68, 68, 30));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                }
                g2.setColor(AppColors.STATUS_TODO);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int w = getWidth();
                int h = getHeight();
                g2.drawLine(w / 2 - 3, h / 2 - 3, w / 2 + 3, h / 2 + 3);
                g2.drawLine(w / 2 + 3, h / 2 - 3, w / 2 - 3, h / 2 + 3);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(24, 24));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setToolTipText("Eliminar tarea");
        b.addActionListener(e -> eliminarTarea(id));
        p.add(b);
    }

    private void cambiarEstado(int id, String st) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("UPDATE tareas SET estado = ? WHERE id = ?")) {
            ps.setString(1, st);
            ps.setInt(2, id);
            ps.executeUpdate();
            cargarTareas();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void eliminarTarea(int id) {
        boolean confirmar = CustomDialog.showConfirm(homeFrame,
                "Eliminar Tarea", "¿Estás seguro de que quieres eliminar esta tarea?", true);

        if (confirmar) {
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM tareas WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                cargarTareas();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void mostrarDialogoNuevaTarea() {
        JDialog dialog = new JDialog(homeFrame, "Crear nueva tarea", true);
        dialog.setSize(420, 640);
        dialog.setLocationRelativeTo(homeFrame);
        dialog.getContentPane().setBackground(AppColors.bgCard());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AppColors.bgCard());
        panel.setBorder(new EmptyBorder(30, 36, 30, 36));

        JLabel modalTitle = new JLabel("Añadir tarea");
        modalTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        modalTitle.setForeground(AppColors.textPrimary());
        modalTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(modalTitle);
        panel.add(Box.createVerticalStrut(24));

        JTextField tituloF = LoginFrame.buildTextField("Ej. Tarea nueva");
        JTextField descF = LoginFrame.buildTextField("Descripción...");
        JTextField fechaF = LoginFrame.buildTextField("YYYY-MM-DD");

        JCheckBox chkDiaria = new JCheckBox(" 🔁 Tarea diaria");
        chkDiaria.setBackground(AppColors.bgCard());
        chkDiaria.setForeground(AppColors.textPrimary());
        chkDiaria.setFocusPainted(false);

        JTextField horaF = LoginFrame.buildTextField("Hora (Ej. 09:00)");
        horaF.setVisible(false);

        chkDiaria.addActionListener(e -> {
            horaF.setVisible(chkDiaria.isSelected());
            fechaF.setEnabled(!chkDiaria.isSelected());
            panel.revalidate();
        });

        String[] estados = { "Por hacer", "En proceso", "Terminado" };
        JComboBox<String> estadoBox = new JComboBox<>(estados);
        estilizarComboBox(estadoBox);

        String[] prioridades = { "alta", "media", "baja" };
        JComboBox<String> prioBox = new JComboBox<>(prioridades);
        prioBox.setSelectedIndex(1);
        estilizarComboBox(prioBox);

        JButton guardar = LoginFrame.buildAccentButton("Guardar");
        guardar.addActionListener(e -> {
            String t = tituloF.getText().trim();
            if (t.isEmpty() || t.equals("Ej. Tarea nueva"))
                return;

            String selection = (String) estadoBox.getSelectedItem();
            String es = selection.equals("Por hacer") ? "pendiente"
                    : selection.equals("En proceso") ? "proceso" : "terminado";

            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO tareas (usuario_id, titulo, descripcion, estado, fecha, prioridad, es_diaria, hora) VALUES (?,?,?,?,?,?,?,?)")) {
                ps.setInt(1, userId);
                ps.setString(2, t);
                ps.setString(3, descF.getText().trim());
                ps.setString(4, es);
                ps.setDate(5, chkDiaria.isSelected() ? new java.sql.Date(System.currentTimeMillis())
                        : (fechaF.getText().contains("-") ? java.sql.Date.valueOf(fechaF.getText()) : null));
                ps.setString(6, (String) prioBox.getSelectedItem());
                ps.setBoolean(7, chkDiaria.isSelected());
                ps.setString(8, chkDiaria.isSelected() ? horaF.getText() : "");
                ps.executeUpdate();
                dialog.dispose();
                cargarTareas();
            } catch (Exception ex) {
                CustomDialog.showMessage(homeFrame, "Error", "No se pudo guardar la tarea.", true);
            }
        });

        panel.add(LoginFrame.buildLabel("Título"));
        panel.add(tituloF);
        panel.add(Box.createVerticalStrut(10));
        panel.add(LoginFrame.buildLabel("Descripción"));
        panel.add(descF);
        panel.add(Box.createVerticalStrut(10));
        panel.add(LoginFrame.buildLabel("Fecha"));
        panel.add(fechaF);
        panel.add(Box.createVerticalStrut(10));
        panel.add(chkDiaria);
        panel.add(horaF);
        panel.add(Box.createVerticalStrut(10));

        JPanel comboRow = new JPanel(new GridLayout(1, 2, 16, 0));
        comboRow.setOpaque(false);
        comboRow.add(estadoBox);
        comboRow.add(prioBox);
        panel.add(comboRow);
        panel.add(Box.createVerticalStrut(20));
        panel.add(guardar);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void estilizarComboBox(JComboBox<String> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.setBackground(AppColors.bgSecondary());
        box.setForeground(AppColors.textPrimary());
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    }

    private JPanel buildPriorityBadge(String prioridad) {
        String p = (prioridad == null) ? "media" : prioridad;
        JPanel badge = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                switch (p) {
                    case "alta" -> g2.setColor(AppColors.PRIORITY_HIGH_BG);
                    case "baja" -> g2.setColor(AppColors.PRIORITY_LOW_BG);
                    default -> g2.setColor(AppColors.PRIORITY_MED_BG);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        badge.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 2));
        badge.setOpaque(false);
        JLabel lbl = new JLabel(p.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        switch (p) {
            case "alta" -> lbl.setForeground(AppColors.PRIORITY_HIGH);
            case "baja" -> lbl.setForeground(AppColors.PRIORITY_LOW);
            default -> lbl.setForeground(AppColors.PRIORITY_MED);
        }
        badge.add(lbl);
        return badge;
    }

    private String hex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}