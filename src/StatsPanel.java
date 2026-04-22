import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class StatsPanel extends JPanel {

    private final int userId;
    private JLabel totalVal, doneVal, progressVal, pendingVal;
    private JPanel progressBar;
    private JPanel weekChartPanel;

    public StatsPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        setBackground(AppColors.bg());
        initUI();
        cargar();
    }

    private void initUI() {
        JPanel scroll = new JPanel();
        scroll.setLayout(new BoxLayout(scroll, BoxLayout.Y_AXIS));
        scroll.setBackground(AppColors.bg());
        scroll.setBorder(new EmptyBorder(24, 28, 28, 28));

        // ── Métricas ─────────────────────────────────────────────────
        JPanel metricGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        metricGrid.setOpaque(false);
        metricGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        metricGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalVal = addMetricCard(metricGrid, "Total tareas", "0", AppColors.ACCENT);
        doneVal = addMetricCard(metricGrid, "✓ Completadas", "0", AppColors.STATUS_DONE);
        progressVal = addMetricCard(metricGrid, "⚡ En proceso", "0", AppColors.STATUS_PROGRESS);
        pendingVal = addMetricCard(metricGrid, "◉ Por hacer", "0", AppColors.STATUS_TODO);

        // ── Progreso general ──────────────────────────────────────────
        JPanel progCard = buildCard();
        progCard.setLayout(new BoxLayout(progCard, BoxLayout.Y_AXIS));
        progCard.setBorder(new EmptyBorder(20, 24, 24, 24));

        JLabel progTitle = new JLabel("Progreso general");
        progTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        progTitle.setForeground(AppColors.textPrimary());
        progTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel progSub = new JLabel("Porcentaje de tareas completadas");
        progSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        progSub.setForeground(AppColors.textSecondary());
        progSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel barBg = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.border());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g2);
            }
        };
        barBg.setOpaque(false);
        barBg.setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));
        barBg.setPreferredSize(new Dimension(0, 14));
        barBg.setAlignmentX(Component.LEFT_ALIGNMENT);

        progressBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, AppColors.ACCENT, getWidth(), 0, new Color(167, 100, 255));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
            }
        };
        progressBar.setOpaque(false);
        barBg.add(progressBar, BorderLayout.WEST);

        progCard.add(progTitle);
        progCard.add(Box.createVerticalStrut(4));
        progCard.add(progSub);
        progCard.add(Box.createVerticalStrut(16));
        progCard.add(barBg);

        // ── Gráfico semanal ───────────────────────────────────────────
        JPanel weekCard = buildCard();
        weekCard.setLayout(new BoxLayout(weekCard, BoxLayout.Y_AXIS));
        weekCard.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel weekTitle = new JLabel("Actividad esta semana");
        weekTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        weekTitle.setForeground(AppColors.textPrimary());
        weekTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel weekSub = new JLabel("Tareas creadas o con vencimiento cada día");
        weekSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        weekSub.setForeground(AppColors.textSecondary());
        weekSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        weekChartPanel = new JPanel() {
            // Se pinta dinámicamente en cargar()
        };
        weekChartPanel.setOpaque(false);
        weekChartPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        weekChartPanel.setPreferredSize(new Dimension(0, 160));
        weekChartPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        weekCard.add(weekTitle);
        weekCard.add(Box.createVerticalStrut(4));
        weekCard.add(weekSub);
        weekCard.add(Box.createVerticalStrut(20));
        weekCard.add(weekChartPanel);

        // ── Distribución de prioridades ───────────────────────────────
        JPanel prioCard = buildCard();
        prioCard.setLayout(new BoxLayout(prioCard, BoxLayout.Y_AXIS));
        prioCard.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel prioTitle = new JLabel("Distribución por prioridad");
        prioTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        prioTitle.setForeground(AppColors.textPrimary());
        prioTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        prioCard.add(prioTitle);
        prioCard.add(Box.createVerticalStrut(16));
        // Barras de prioridad — se construyen en cargar()
        prioCard.setName("prioCard");

        // ── Ensamblado ────────────────────────────────────────────────
        scroll.add(metricGrid);
        scroll.add(Box.createVerticalStrut(20));
        scroll.add(progCard);
        scroll.add(Box.createVerticalStrut(20));
        scroll.add(weekCard);
        scroll.add(Box.createVerticalStrut(20));
        scroll.add(prioCard);
        scroll.add(Box.createVerticalStrut(20));

        JScrollPane sp = new JScrollPane(scroll);
        sp.setBorder(null);
        sp.getViewport().setBackground(AppColors.bg());
        sp.getVerticalScrollBar().setUnitIncrement(16);
        add(sp, BorderLayout.CENTER);
    }

    public void cargar() {
        new Thread(() -> {
            // Conteos por estado
            int[] counts = { 0, 0, 0, 0 }; // total, done, proceso, pendiente
            String sql = "SELECT estado, COUNT(*) as c FROM tareas WHERE usuario_id=? GROUP BY estado";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int c = rs.getInt("c");
                    counts[0] += c;
                    switch (rs.getString("estado")) {
                        case "terminado" -> counts[1] = c;
                        case "proceso" -> counts[2] = c;
                        case "pendiente" -> counts[3] = c;
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error stats: " + e.getMessage());
            }

            // Datos semanales (7 días atrás)
            int[] weekData = new int[7];
            String[] weekLabels = new String[7];
            LocalDate today = LocalDate.now();
            String weekSql = """
                        SELECT fecha, COUNT(*) as c FROM tareas
                        WHERE usuario_id=? AND fecha >= ? AND fecha <= ?
                        GROUP BY fecha
                    """;
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(weekSql)) {
                ps.setInt(1, userId);
                ps.setDate(2, java.sql.Date.valueOf(today.minusDays(6)));
                ps.setDate(3, java.sql.Date.valueOf(today));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    LocalDate d = rs.getDate("fecha").toLocalDate();
                    int diff = (int) (d.toEpochDay() - today.minusDays(6).toEpochDay());
                    if (diff >= 0 && diff < 7)
                        weekData[diff] = rs.getInt("c");
                }
            } catch (SQLException e) {
                System.err.println("Error semana: " + e.getMessage());
            }

            for (int i = 0; i < 7; i++) {
                LocalDate d = today.minusDays(6 - i);
                weekLabels[i] = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("es"));
            }

            // Prioridades
            int[] prioData = { 0, 0, 0 }; // alta, media, baja
            String prioSql = "SELECT prioridad, COUNT(*) as c FROM tareas WHERE usuario_id=? GROUP BY prioridad";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(prioSql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int c = rs.getInt("c");
                    switch (rs.getString("prioridad") != null ? rs.getString("prioridad") : "media") {
                        case "alta" -> prioData[0] = c;
                        case "media" -> prioData[1] = c;
                        case "baja" -> prioData[2] = c;
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error prio: " + e.getMessage());
            }

            int[] fc = counts;
            int[] fw = weekData;
            String[] fl = weekLabels;
            int[] fp = prioData;
            SwingUtilities.invokeLater(() -> {
                // Actualizar métricas
                totalVal.setText(String.valueOf(fc[0]));
                doneVal.setText(String.valueOf(fc[1]));
                progressVal.setText(String.valueOf(fc[2]));
                pendingVal.setText(String.valueOf(fc[3]));

                // Barra de progreso animada
                int pct = fc[0] > 0 ? (fc[1] * 100 / fc[0]) : 0;
                Timer t = new Timer(16, null);
                int[] frame = { 0 };
                int targetPct = pct;
                t.addActionListener(e -> {
                    frame[0]++;
                    float ratio = Math.min(1f, frame[0] / 30f);
                    float ease = 1f - (1f - ratio) * (1f - ratio);
                    int w = (int) (progressBar.getParent().getWidth() * (targetPct / 100f) * ease);
                    progressBar.setPreferredSize(new Dimension(Math.max(0, w), 14));
                    progressBar.getParent().revalidate();
                    if (frame[0] >= 30)
                        t.stop();
                });
                t.start();

                // Gráfico semanal — repaint con datos
                int maxWeek = 1;
                for (int v : fw)
                    if (v > maxWeek)
                        maxWeek = v;
                final int maxVal = maxWeek;

                weekChartPanel.removeAll();
                weekChartPanel.setLayout(new GridLayout(1, 7, 8, 0));
                for (int i = 0; i < 7; i++) {
                    final int val = fw[i];
                    final String label = fl[i];
                    final boolean isToday = (i == 6);
                    JPanel barCol = new JPanel(new BorderLayout(0, 4));
                    barCol.setOpaque(false);

                    // Valor numérico
                    JLabel valLbl = new JLabel(val > 0 ? String.valueOf(val) : "", SwingConstants.CENTER);
                    valLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    valLbl.setForeground(isToday ? AppColors.ACCENT : AppColors.textMuted());
                    valLbl.setPreferredSize(new Dimension(0, 18));

                    // Barra
                    JPanel barWrap = new JPanel(new BorderLayout());
                    barWrap.setOpaque(false);
                    JPanel bar = new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            // Fondo
                            g2.setColor(AppColors.border());
                            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                            // Barra
                            if (val > 0) {
                                float ratio = (float) val / maxVal;
                                int barH = (int) (getHeight() * ratio);
                                Color barColor = isToday ? AppColors.ACCENT
                                        : new Color(AppColors.ACCENT.getRed(), AppColors.ACCENT.getGreen(),
                                                AppColors.ACCENT.getBlue(), 120);
                                g2.setColor(barColor);
                                g2.fill(new RoundRectangle2D.Float(0, getHeight() - barH, getWidth(), barH, 8, 8));
                            }
                            g2.dispose();
                        }
                    };
                    bar.setOpaque(false);
                    barWrap.add(bar, BorderLayout.CENTER);

                    // Etiqueta día
                    JLabel dayLbl = new JLabel(label, SwingConstants.CENTER);
                    dayLbl.setFont(new Font("Segoe UI", isToday ? Font.BOLD : Font.PLAIN, 11));
                    dayLbl.setForeground(isToday ? AppColors.ACCENT : AppColors.textSecondary());
                    dayLbl.setPreferredSize(new Dimension(0, 18));

                    barCol.add(valLbl, BorderLayout.NORTH);
                    barCol.add(barWrap, BorderLayout.CENTER);
                    barCol.add(dayLbl, BorderLayout.SOUTH);
                    weekChartPanel.add(barCol);
                }
                weekChartPanel.revalidate();
                weekChartPanel.repaint();

                // Barras de prioridad
                JPanel prioCard = findPrioCard();
                if (prioCard != null) {
                    // Limpiar y reconstruir filas de prioridad
                    Component[] comps = prioCard.getComponents();
                    for (int i = comps.length - 1; i >= 2; i--)
                        prioCard.remove(comps[i]);

                    int total = fp[0] + fp[1] + fp[2];
                    addPrioRow(prioCard, "Alta", fp[0], total, AppColors.PRIORITY_HIGH, AppColors.PRIORITY_HIGH_BG);
                    addPrioRow(prioCard, "Media", fp[1], total, AppColors.PRIORITY_MED, AppColors.PRIORITY_MED_BG);
                    addPrioRow(prioCard, "Baja", fp[2], total, AppColors.PRIORITY_LOW, AppColors.PRIORITY_LOW_BG);
                    prioCard.revalidate();
                    prioCard.repaint();
                }
            });
        }).start();
    }

    private void addPrioRow(JPanel parent, String label, int val, int total, Color fg, Color bg) {
        int pct = total > 0 ? (val * 100 / total) : 0;

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLbl = new JLabel(label);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLbl.setForeground(fg);
        nameLbl.setPreferredSize(new Dimension(50, 20));

        JPanel barBg = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.border());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        barBg.setOpaque(false);
        barBg.setPreferredSize(new Dimension(0, 10));

        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(pct, 10));
        barBg.add(bar, BorderLayout.WEST);

        JLabel pctLbl = new JLabel(pct + "%");
        pctLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pctLbl.setForeground(AppColors.textMuted());
        pctLbl.setPreferredSize(new Dimension(38, 20));
        pctLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(nameLbl, BorderLayout.WEST);
        row.add(barBg, BorderLayout.CENTER);
        row.add(pctLbl, BorderLayout.EAST);

        parent.add(Box.createVerticalStrut(10));
        parent.add(row);
    }

    private JPanel findPrioCard() {
        JScrollPane sp = (JScrollPane) getComponent(0);
        JPanel scroll = (JPanel) sp.getViewport().getView();
        for (Component c : scroll.getComponents()) {
            if (c instanceof JPanel p && "prioCard".equals(p.getName()))
                return p;
        }
        return null;
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private JLabel addMetricCard(JPanel parent, String label, String value, Color color) {
        JPanel card = buildCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel topLabel = new JLabel(label);
        topLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        topLabel.setForeground(AppColors.textSecondary());

        JLabel valLabel = new JLabel(value);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 34));
        valLabel.setForeground(color);

        // Línea de color abajo
        JPanel accent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 4, 4));
                g2.dispose();
            }
        };
        accent.setOpaque(false);
        accent.setMaximumSize(new Dimension(32, 3));
        accent.setPreferredSize(new Dimension(32, 3));

        card.add(topLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(valLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(accent);

        parent.add(card);
        return valLabel;
    }

    private JPanel buildCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(AppColors.border());
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }
}