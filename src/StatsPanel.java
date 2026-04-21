import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class StatsPanel extends JPanel {

    private final int userId;
    private JLabel totalLbl, doneLbl, progressLbl, pendingLbl;
    private JPanel progressBar;

    public StatsPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        setBackground(AppColors.bg());
        initUI();
        cargar();
    }

    private void initUI() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(AppColors.bg());
        grid.setBorder(new EmptyBorder(24, 24, 24, 24));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.BOTH;
        g.weightx = 1;
        g.weighty = 0;

        // Fila 1 — 4 métricas
        totalLbl = metricCard("Total tareas", "0", AppColors.textPrimary());
        doneLbl = metricCard("Completadas", "0", AppColors.STATUS_DONE);
        progressLbl = metricCard("En proceso", "0", AppColors.STATUS_PROGRESS);
        pendingLbl = metricCard("Por hacer", "0", AppColors.STATUS_TODO);

        g.gridx = 0;
        g.gridy = 0;
        grid.add(totalLbl, g);
        g.gridx = 1;
        grid.add(doneLbl, g);
        g.gridx = 2;
        grid.add(progressLbl, g);
        g.gridx = 3;
        grid.add(pendingLbl, g);

        // Barra de progreso
        JPanel progCard = new JPanel();
        progCard.setLayout(new BoxLayout(progCard, BoxLayout.Y_AXIS));
        progCard.setBackground(AppColors.bgCard());
        progCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.border(), 1, true),
                new EmptyBorder(20, 24, 20, 24)));

        JLabel progTitle = new JLabel("Progreso general");
        progTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        progTitle.setForeground(AppColors.textPrimary());

        JPanel barBg = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g2) {
                Graphics2D g = (Graphics2D) g2.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(AppColors.border());
                g.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g.dispose();
                super.paintComponent(g2);
            }
        };
        barBg.setOpaque(false);
        barBg.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        barBg.setPreferredSize(new Dimension(0, 12));

        progressBar = new JPanel() {
            protected void paintComponent(Graphics g2) {
                Graphics2D g = (Graphics2D) g2.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(AppColors.ACCENT);
                g.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g.dispose();
            }
        };
        progressBar.setOpaque(false);
        barBg.add(progressBar, BorderLayout.WEST);

        progCard.add(progTitle);
        progCard.add(Box.createVerticalStrut(16));
        progCard.add(barBg);

        g.gridx = 0;
        g.gridy = 1;
        g.gridwidth = 4;
        g.weighty = 0;
        grid.add(progCard, g);

        // Spacer
        g.gridy = 2;
        g.weighty = 1;
        grid.add(new JPanel() {
            {
                setOpaque(false);
            }
        }, g);

        add(grid, BorderLayout.CENTER);
    }

    public void cargar() {
        new Thread(() -> {
            int[] counts = { 0, 0, 0, 0 }; // total, done, progress, pending
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
            int[] finalCounts = counts;
            SwingUtilities.invokeLater(() -> {
                setMetric(totalLbl, String.valueOf(finalCounts[0]));
                setMetric(doneLbl, String.valueOf(finalCounts[1]));
                setMetric(progressLbl, String.valueOf(finalCounts[2]));
                setMetric(pendingLbl, String.valueOf(finalCounts[3]));

                int pct = finalCounts[0] > 0 ? (finalCounts[1] * 100 / finalCounts[0]) : 0;
                // Animar barra
                Timer t = new Timer(16, null);
                int[] frame = { 0 };
                int totalFrames = 30;
                int targetPct = pct;
                t.addActionListener(e -> {
                    frame[0]++;
                    float ratio = Math.min(1f, (float) frame[0] / totalFrames);
                    float ease = 1f - (1f - ratio) * (1f - ratio);
                    int w = (int) (progressBar.getParent().getWidth() * (targetPct / 100f) * ease);
                    progressBar.setPreferredSize(new Dimension(Math.max(0, w), 12));
                    progressBar.getParent().revalidate();
                    if (frame[0] >= totalFrames)
                        t.stop();
                });
                t.start();
            });
        }).start();
    }

    private JLabel metricCard(String label, String value, Color valueColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppColors.bgCard());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.border(), 1, true),
                new EmptyBorder(20, 20, 20, 20)));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(AppColors.textSecondary());
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 32));
        val.setForeground(valueColor);
        card.add(lbl);
        card.add(Box.createVerticalStrut(6));
        card.add(val);
        // Guardamos referencia al JLabel del valor
        JLabel ref = new JLabel();
        ref.putClientProperty("card", card);
        ref.putClientProperty("val", val);
        return ref;
    }

    private void setMetric(JLabel ref, String value) {
        JLabel val = (JLabel) ref.getClientProperty("val");
        if (val != null)
            val.setText(value);
    }
}