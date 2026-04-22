import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;

public class KanbanPanel extends JPanel {

    private final int userId;
    private final HomeFrame homeFrame;
    private JPanel pendientePanel, procesoPanel, terminadoPanel;
    private String fechaFiltro = null;
    private String searchQuery = "";

    // Labels de conteo por columna
    private JLabel pendienteCount, procesoCount, terminadoCount;

    public KanbanPanel(int userId, HomeFrame homeFrame) {
        this.userId = userId;
        this.homeFrame = homeFrame;
        setLayout(new BorderLayout());
        setBackground(AppColors.bg());
        initUI();
        cargarTareas();
    }

    public void setSearchQuery(String q) {
        this.searchQuery = q == null ? "" : q.trim().toLowerCase();
        cargarTareas();
    }

    private void initUI() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        header.setBackground(AppColors.bg());

        JButton addBtn = LoginFrame.buildAccentButton("+ Nueva tarea");
        addBtn.setPreferredSize(new Dimension(140, 36));
        addBtn.addActionListener(e -> mostrarDialogoNuevaTarea());

        JButton clearFilterBtn = new JButton("✕ Limpiar filtro") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(124, 58, 237, 20));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        clearFilterBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clearFilterBtn.setForeground(AppColors.ACCENT);
        clearFilterBtn.setContentAreaFilled(false);
        clearFilterBtn.setBorderPainted(false);
        clearFilterBtn.setFocusPainted(false);
        clearFilterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearFilterBtn.setVisible(false);
        clearFilterBtn.addActionListener(e -> {
            setFechaFiltro(null);
            clearFilterBtn.setVisible(false);
        });

        header.add(addBtn);
        header.add(clearFilterBtn);

        // Escuchar cambios de fecha filtro para mostrar/ocultar botón
        addPropertyChangeListener("fechaFiltro", evt -> clearFilterBtn.setVisible(fechaFiltro != null));

        // Columnas Kanban
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

    public void setFechaFiltro(String fecha) {
        this.fechaFiltro = fecha;
        firePropertyChange("fechaFiltro", null, fecha);
        cargarTareas();
    }

    private JScrollPane wrapScroll(JPanel col) {
        JScrollPane sp = new JScrollPane(col);
        sp.setBorder(null);
        Color colBg = ThemeManager.isDark() ? new Color(22, 22, 34) : new Color(243, 244, 248);
        sp.getViewport().setBackground(colBg);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().setBorder(null);
        return sp;
    }

    private JPanel crearColumna(String titulo, Color accentColor) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        Color colBg = ThemeManager.isDark() ? new Color(22, 22, 34) : new Color(243, 244, 248);
        col.setBackground(colBg);
        col.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Header de columna con count badge
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftSide.setOpaque(false);

        // Dot de color
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fillOval(0, 4, 9, 9);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(14, 18);
            }
        };
        dot.setOpaque(false);

        JLabel titleLbl = new JLabel(titulo);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(AppColors.textPrimary());

        leftSide.add(dot);
        leftSide.add(Box.createHorizontalStrut(6));
        leftSide.add(titleLbl);

        // Badge de cantidad
        JLabel countLbl = new JLabel("0") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 30));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        countLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        countLbl.setForeground(accentColor);
        countLbl.setOpaque(false);
        countLbl.setBorder(new EmptyBorder(2, 7, 2, 7));
        countLbl.setHorizontalAlignment(SwingConstants.CENTER);

        // Guardar referencia al count label según columna
        switch (titulo) {
            case "Por hacer" -> pendienteCount = countLbl;
            case "En proceso" -> procesoCount = countLbl;
            case "Terminado" -> terminadoCount = countLbl;
        }

        headerPanel.add(leftSide, BorderLayout.WEST);
        headerPanel.add(countLbl, BorderLayout.EAST);

        col.add(headerPanel);
        return col;
    }

    public void refreshColors() {
        setBackground(AppColors.bg());
        Color colBg = ThemeManager.isDark() ? new Color(22, 22, 34) : new Color(243, 244, 248);

        // Header
        if (getComponentCount() > 0 && getComponent(0) instanceof JPanel header)
            header.setBackground(AppColors.bg());

        // Columnas
        if (getComponentCount() > 1 && getComponent(1) instanceof JPanel columnas) {
            columnas.setBackground(AppColors.bg());
            for (Component sp : columnas.getComponents()) {
                if (sp instanceof JScrollPane scroll) {
                    scroll.getViewport().setBackground(colBg);
                    if (scroll.getViewport().getView() instanceof JPanel col) {
                        col.setBackground(colBg);
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

                if (!searchQuery.isEmpty()) {
                    sql += "AND (LOWER(titulo) LIKE ? OR LOWER(descripcion) LIKE ?) ";
                }

                sql += "ORDER BY CASE prioridad WHEN 'alta' THEN 1 WHEN 'media' THEN 2 ELSE 3 END, creado DESC";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int idx = 1;
                    ps.setInt(idx++, userId);
                    if (fechaFiltro != null)
                        ps.setDate(idx++, java.sql.Date.valueOf(fechaFiltro));
                    if (!searchQuery.isEmpty()) {
                        String like = "%" + searchQuery + "%";
                        ps.setString(idx++, like);
                        ps.setString(idx++, like);
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

                    int[] counts = { 0, 0, 0 };
                    for (Object[] row : listaTareas) {
                        JPanel card = crearTarjeta(
                                (int) row[0], (String) row[1], (String) row[2], (String) row[3],
                                (String) row[4], (String) row[5], (boolean) row[6], (String) row[7]);
                        switch ((String) row[3]) {
                            case "pendiente" -> {
                                pendientePanel.add(card);
                                pendientePanel.add(Box.createVerticalStrut(10));
                                counts[0]++;
                            }
                            case "proceso" -> {
                                procesoPanel.add(card);
                                procesoPanel.add(Box.createVerticalStrut(10));
                                counts[1]++;
                            }
                            case "terminado" -> {
                                terminadoPanel.add(card);
                                terminadoPanel.add(Box.createVerticalStrut(10));
                                counts[2]++;
                            }
                        }
                    }

                    // Actualizar contadores
                    if (pendienteCount != null)
                        pendienteCount.setText(String.valueOf(counts[0]));
                    if (procesoCount != null)
                        procesoCount.setText(String.valueOf(counts[1]));
                    if (terminadoCount != null)
                        terminadoCount.setText(String.valueOf(counts[2]));

                    // Empty state por columna
                    if (counts[0] == 0)
                        pendientePanel.add(buildEmptyCol("Sin tareas pendientes"));
                    if (counts[1] == 0)
                        procesoPanel.add(buildEmptyCol("Nada en progreso"));
                    if (counts[2] == 0)
                        terminadoPanel.add(buildEmptyCol("Nada completado aún"));

                    revalidate();
                    repaint();
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private JPanel buildEmptyCol(String msg) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(new EmptyBorder(20, 8, 8, 8));

        JLabel lbl = new JLabel("<html><center>" + msg + "</center></html>");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(AppColors.textMuted());
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lbl);
        return p;
    }

    private void limpiarColumna(JPanel col) {
        Component[] comps = col.getComponents();
        for (int i = 1; i < comps.length; i++)
            col.remove(comps[i]);
    }

    private JPanel crearTarjeta(int id, String titulo, String desc, String estado,
            String fecha, String prioridad, boolean esDiaria, String hora) {
        JPanel card = new JPanel() {
            private boolean hovered = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Sombra ligera al hover
                if (hovered) {
                    g2.setColor(new Color(0, 0, 0, ThemeManager.isDark() ? 50 : 20));
                    g2.fill(new RoundRectangle2D.Float(2, 4, getWidth() - 2, getHeight() - 2, 16, 16));
                }
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 2, getHeight() - 2, 16, 16));
                g2.setColor(hovered ? AppColors.ACCENT : AppColors.border());
                g2.setStroke(new java.awt.BasicStroke(hovered ? 1.5f : 1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 3, getHeight() - 3, 16, 16));
                g2.dispose();
            }

            { // instance initializer
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppColors.bgCard());
        card.setBorder(new EmptyBorder(14, 16, 12, 16));
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Click en la tarjeta → abrir editor
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Solo si no fue un botón hijo
                if (e.getSource() == card) {
                    TaskDetailDialog dlg = new TaskDetailDialog(homeFrame, id, () -> cargarTareas());
                    dlg.setVisible(true);
                }
            }
        });

        // ── Título + badge prioridad ──────────────────────────────────
        JPanel hr = new JPanel(new BorderLayout());
        hr.setOpaque(false);
        hr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        hr.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel("<html><b>" + titulo + "</b></html>");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(AppColors.textPrimary());
        hr.add(titleLbl, BorderLayout.WEST);
        hr.add(buildPriorityBadge(prioridad), BorderLayout.EAST);
        card.add(hr);

        // ── Descripción ───────────────────────────────────────────────
        if (desc != null && !desc.isEmpty()) {
            card.add(Box.createVerticalStrut(7));
            String shortDesc = desc.length() > 80 ? desc.substring(0, 77) + "…" : desc;
            JLabel dLbl = new JLabel(
                    "<html><body style='width:170px; color:" + hex(AppColors.textSecondary()) + "'>" + shortDesc
                            + "</body></html>");
            dLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(dLbl);
        }

        // ── Fecha / diaria ────────────────────────────────────────────
        card.add(Box.createVerticalStrut(8));
        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        infoRow.setOpaque(false);
        infoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (esDiaria) {
            infoRow.add(buildChip("🔁 Diaria" + (hora != null && !hora.isEmpty() ? "  ⏰ " + hora : ""),
                    new Color(139, 92, 246, 30), new Color(139, 92, 246)));
        } else if (!fecha.isEmpty()) {
            infoRow.add(buildChip("📅 " + fecha, AppColors.bgSecondary(), AppColors.textMuted()));
        }
        card.add(infoRow);

        // ── Separador ─────────────────────────────────────────────────
        card.add(Box.createVerticalStrut(10));
        JPanel sep = new JPanel();
        sep.setBackground(AppColors.border());
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(sep);
        card.add(Box.createVerticalStrut(8));

        // ── Botones de acción ─────────────────────────────────────────
        JPanel acc = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        acc.setOpaque(false);
        acc.setAlignmentX(Component.LEFT_ALIGNMENT);
        acc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        if (!estado.equals("pendiente"))
            addBtnVectorial(acc, id, "pendiente", "left", "← Por hacer");
        if (!estado.equals("proceso"))
            addBtnVectorial(acc, id, "proceso", "right", "→ En proceso");
        if (!estado.equals("terminado"))
            addBtnVectorial(acc, id, "terminado", "check", "✓ Completar");
        addBtnDelete(acc, id);

        card.add(acc);
        return card;
    }

    // ── Helpers ────────────────────────────────────────────────────────────

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
        chip.setBorder(new EmptyBorder(3, 7, 3, 7));
        return chip;
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
                Color iconColor = type.equals("check") ? AppColors.STATUS_DONE : AppColors.textSecondary();
                g2.setColor(iconColor);
                g2.setStroke(new java.awt.BasicStroke(2.0f, java.awt.BasicStroke.CAP_ROUND,
                        java.awt.BasicStroke.JOIN_ROUND));
                int w = getWidth(), h = getHeight();
                switch (type) {
                    case "left" -> g2.drawPolyline(new int[] { w / 2 + 2, w / 2 - 2, w / 2 + 2 },
                            new int[] { h / 2 - 3, h / 2, h / 2 + 3 }, 3);
                    case "right" -> g2.drawPolyline(new int[] { w / 2 - 2, w / 2 + 2, w / 2 - 2 },
                            new int[] { h / 2 - 3, h / 2, h / 2 + 3 }, 3);
                    case "check" -> g2.drawPolyline(new int[] { w / 2 - 3, w / 2 - 1, w / 2 + 4 },
                            new int[] { h / 2 + 1, h / 2 + 3, h / 2 - 3 }, 3);
                }
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(28, 28));
        b.setMinimumSize(new Dimension(28, 28));
        b.setMaximumSize(new Dimension(28, 28));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setToolTipText(tooltip);
        b.addActionListener(e -> {
            cambiarEstado(id, st);
        });
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
                g2.setStroke(new java.awt.BasicStroke(2.0f, java.awt.BasicStroke.CAP_ROUND,
                        java.awt.BasicStroke.JOIN_ROUND));
                int w = getWidth(), h = getHeight();
                g2.drawLine(w / 2 - 3, h / 2 - 3, w / 2 + 3, h / 2 + 3);
                g2.drawLine(w / 2 + 3, h / 2 - 3, w / 2 - 3, h / 2 + 3);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(28, 28));
        b.setMinimumSize(new Dimension(28, 28));
        b.setMaximumSize(new Dimension(28, 28));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setToolTipText("Eliminar tarea");
        b.addActionListener(e -> eliminarTarea(id));
        p.add(b);
    }

    private void cambiarEstado(int id, String st) {
        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement("UPDATE tareas SET estado=? WHERE id=?")) {
                ps.setString(1, st);
                ps.setInt(2, id);
                ps.executeUpdate();
                SwingUtilities.invokeLater(this::cargarTareas);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void eliminarTarea(int id) {
        boolean confirmar = CustomDialog.showConfirm(homeFrame,
                "Eliminar Tarea", "¿Estás seguro de que quieres eliminar esta tarea?", true);
        if (confirmar) {
            new Thread(() -> {
                try (Connection conn = DatabaseConnection.getConnection();
                        PreparedStatement ps = conn.prepareStatement("DELETE FROM tareas WHERE id=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    SwingUtilities.invokeLater(this::cargarTareas);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void mostrarDialogoNuevaTarea() {
        JDialog dialog = new JDialog(homeFrame, "Crear nueva tarea", true);
        dialog.setSize(440, 580);
        dialog.setResizable(false);
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

        JTextField tituloF = LoginFrame.buildTextField("Ej. Revisar propuesta");
        JTextField descF = LoginFrame.buildTextField("Descripción (opcional)");
        JTextField fechaF = LoginFrame.buildTextField("YYYY-MM-DD");

        JCheckBox chkDiaria = new JCheckBox(" 🔁 Tarea diaria");
        chkDiaria.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkDiaria.setBackground(AppColors.bgCard());
        chkDiaria.setForeground(AppColors.textPrimary());
        chkDiaria.setFocusPainted(false);
        chkDiaria.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField horaF = LoginFrame.buildTextField("Hora (Ej. 09:00)");
        horaF.setVisible(false);

        chkDiaria.addActionListener(e -> {
            horaF.setVisible(chkDiaria.isSelected());
            fechaF.setEnabled(!chkDiaria.isSelected());
            panel.revalidate();
            panel.repaint();
        });

        String[] estados = { "Por hacer", "En proceso", "Terminado" };
        String[] prioridades = { "alta", "media", "baja" };
        JComboBox<String> estadoBox = new JComboBox<>(estados);
        JComboBox<String> prioBox = new JComboBox<>(prioridades);
        prioBox.setSelectedIndex(1);
        estilizarComboBox(estadoBox);
        estilizarComboBox(prioBox);

        JPanel comboRow = new JPanel(new GridLayout(1, 2, 10, 0));
        comboRow.setOpaque(false);
        comboRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        comboRow.add(estadoBox);
        comboRow.add(prioBox);

        JLabel errLbl = new JLabel(" ");
        errLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errLbl.setForeground(AppColors.STATUS_TODO);
        errLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton guardar = LoginFrame.buildAccentButton("Guardar tarea");
        guardar.setAlignmentX(Component.LEFT_ALIGNMENT);
        guardar.addActionListener(e -> {
            String t = tituloF.getText().trim();
            if (t.isEmpty() || t.equals("Ej. Revisar propuesta")) {
                errLbl.setText("El título no puede estar vacío.");
                tituloF.setBorder(BorderFactory.createLineBorder(AppColors.STATUS_TODO, 1, true));
                return;
            }
            String es = switch ((String) estadoBox.getSelectedItem()) {
                case "En proceso" -> "proceso";
                case "Terminado" -> "terminado";
                default -> "pendiente";
            };
            new Thread(() -> {
                try (Connection conn = DatabaseConnection.getConnection();
                        PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO tareas (usuario_id,titulo,descripcion,estado,fecha,prioridad,es_diaria,hora) VALUES(?,?,?,?,?,?,?,?)")) {
                    ps.setInt(1, userId);
                    ps.setString(2, t);
                    ps.setString(3, descF.getText().trim());
                    ps.setString(4, es);
                    if (chkDiaria.isSelected()) {
                        ps.setDate(5, new java.sql.Date(System.currentTimeMillis()));
                    } else {
                        String ft = fechaF.getText().trim();
                        try {
                            ps.setDate(5, ft.contains("-") ? java.sql.Date.valueOf(ft) : null);
                        } catch (IllegalArgumentException ex) {
                            ps.setDate(5, null);
                        }
                    }
                    ps.setString(6, (String) prioBox.getSelectedItem());
                    ps.setBoolean(7, chkDiaria.isSelected());
                    ps.setString(8, chkDiaria.isSelected() ? horaF.getText().trim() : "");
                    ps.executeUpdate();
                    SwingUtilities.invokeLater(() -> {
                        dialog.dispose();
                        cargarTareas();
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> errLbl.setText("Error al guardar."));
                }
            }).start();
        });

        panel.add(LoginFrame.buildLabel("Título *"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(tituloF);
        panel.add(Box.createVerticalStrut(14));
        panel.add(LoginFrame.buildLabel("Descripción"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(descF);
        panel.add(Box.createVerticalStrut(14));
        panel.add(LoginFrame.buildLabel("Fecha"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(fechaF);
        panel.add(Box.createVerticalStrut(10));
        panel.add(chkDiaria);
        panel.add(Box.createVerticalStrut(4));
        panel.add(horaF);
        panel.add(Box.createVerticalStrut(14));
        panel.add(LoginFrame.buildLabel("Estado y Prioridad"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(comboRow);
        panel.add(Box.createVerticalStrut(8));
        panel.add(errLbl);
        panel.add(Box.createVerticalStrut(16));
        panel.add(guardar);

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(AppColors.bgCard());
        dialog.setContentPane(scroll);
        dialog.setVisible(true);
    }

    private void estilizarComboBox(JComboBox<String> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.setBackground(AppColors.bgSecondary());
        box.setForeground(AppColors.textPrimary());
        box.setPreferredSize(new Dimension(150, 40));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        box.setBorder(BorderFactory.createLineBorder(AppColors.border(), 1, true));
    }

    private JPanel buildPriorityBadge(String prioridad) {
        String p = prioridad == null ? "media" : prioridad;
        Color bg = switch (p) {
            case "alta" -> AppColors.PRIORITY_HIGH_BG;
            case "baja" -> AppColors.PRIORITY_LOW_BG;
            default -> AppColors.PRIORITY_MED_BG;
        };
        Color fg = switch (p) {
            case "alta" -> AppColors.PRIORITY_HIGH;
            case "baja" -> AppColors.PRIORITY_LOW;
            default -> AppColors.PRIORITY_MED;
        };
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
            }
        };
        badge.setLayout(new FlowLayout(FlowLayout.CENTER, 7, 2));
        badge.setOpaque(false);
        JLabel lbl = new JLabel(p.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(fg);
        badge.add(lbl);
        return badge;
    }

    private String hex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}