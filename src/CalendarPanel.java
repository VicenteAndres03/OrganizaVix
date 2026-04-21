import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class CalendarPanel extends JPanel {

    private final int userId;
    private final HomeFrame homeFrame;
    private JPanel diasPanel;
    private JLabel mesLabel;
    private YearMonth mesActual;
    private Map<LocalDate, java.util.List<String>> tareasPorFecha = new HashMap<>();

    public CalendarPanel(int userId, HomeFrame homeFrame) {
        this.userId = userId;
        this.homeFrame = homeFrame;
        this.mesActual = YearMonth.now();
        setLayout(new BorderLayout());
        setBackground(AppColors.bg());
        initUI();
        cargarTareas();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppColors.bg());
        header.setBorder(new EmptyBorder(16, 24, 8, 24));

        JButton prevBtn = new JButton("←");
        prevBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        prevBtn.setForeground(AppColors.textPrimary());
        prevBtn.setBackground(AppColors.bgCard());
        prevBtn.setBorderPainted(false);
        prevBtn.setFocusPainted(false);
        prevBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        prevBtn.addActionListener(e -> {
            mesActual = mesActual.minusMonths(1);
            cargarTareas();
        });

        JButton nextBtn = new JButton("→");
        nextBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nextBtn.setForeground(AppColors.textPrimary());
        nextBtn.setBackground(AppColors.bgCard());
        nextBtn.setBorderPainted(false);
        nextBtn.setFocusPainted(false);
        nextBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nextBtn.addActionListener(e -> {
            mesActual = mesActual.plusMonths(1);
            cargarTareas();
        });

        mesLabel = new JLabel("", SwingConstants.CENTER);
        mesLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        mesLabel.setForeground(AppColors.textPrimary());

        header.add(prevBtn, BorderLayout.WEST);
        header.add(mesLabel, BorderLayout.CENTER);
        header.add(nextBtn, BorderLayout.EAST);

        JPanel diasSemana = new JPanel(new GridLayout(1, 7));
        diasSemana.setBackground(AppColors.bgSecondary());
        diasSemana.setBorder(new EmptyBorder(6, 24, 6, 24));
        String[] dias = { "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom" };
        for (String d : dias) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(AppColors.textSecondary());
            diasSemana.add(lbl);
        }

        diasPanel = new JPanel(new GridLayout(0, 7, 4, 4));
        diasPanel.setBackground(AppColors.bg());
        diasPanel.setBorder(new EmptyBorder(8, 24, 24, 24));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(AppColors.bg());
        top.add(header, BorderLayout.NORTH);
        top.add(diasSemana, BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(diasPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(AppColors.bg());

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void cargarTareas() {
        new Thread(() -> {
            Map<LocalDate, java.util.List<String>> tareasNuevas = new HashMap<>();
            String sql = "SELECT titulo, fecha FROM tareas WHERE usuario_id = ? AND fecha IS NOT NULL";

            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    LocalDate fecha = rs.getDate("fecha").toLocalDate();
                    tareasNuevas.computeIfAbsent(fecha, k -> new ArrayList<>()).add(rs.getString("titulo"));
                }
            } catch (SQLException e) {
                System.err.println("Error al cargar tareas calendario: " + e.getMessage());
            }

            SwingUtilities.invokeLater(() -> {
                tareasPorFecha.clear();
                tareasPorFecha.putAll(tareasNuevas);
                construirCalendario();
            });
        }).start();
    }

    private void construirCalendario() {
        diasPanel.removeAll();

        mesLabel.setText(mesActual.getMonth().getDisplayName(
                java.time.format.TextStyle.FULL, new Locale("es")) + " " + mesActual.getYear());

        LocalDate primerDia = mesActual.atDay(1);
        int inicioDow = primerDia.getDayOfWeek().getValue();

        for (int i = 1; i < inicioDow; i++) {
            diasPanel.add(new JPanel() {
                {
                    setOpaque(false);
                }
            });
        }

        for (int dia = 1; dia <= mesActual.lengthOfMonth(); dia++) {
            LocalDate fechaFinal = mesActual.atDay(dia);
            JPanel celda = crearCelda(fechaFinal);
            celda.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    // ¡MAGIA! En lugar de abrir un diálogo, viajamos al Kanban de esa fecha
                    homeFrame.abrirKanbanEnFecha(fechaFinal.toString());
                }

                public void mouseEntered(MouseEvent e) {
                    celda.setBackground(AppColors.bgHover());
                    celda.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }

                public void mouseExited(MouseEvent e) {
                    celda.setBackground(fechaFinal.equals(LocalDate.now())
                            ? new Color(59, 7, 100)
                            : AppColors.bgCard());
                }
            });
            diasPanel.add(celda);
        }

        diasPanel.revalidate();
        diasPanel.repaint();
    }

    private JPanel crearCelda(LocalDate fecha) {
        JPanel celda = new JPanel();
        celda.setLayout(new BoxLayout(celda, BoxLayout.Y_AXIS));
        boolean esHoy = fecha.equals(LocalDate.now());
        celda.setBackground(esHoy ? new Color(59, 7, 100) : AppColors.bgCard());
        celda.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.border(), 1, true),
                new EmptyBorder(6, 8, 6, 8)));

        JLabel numLabel = new JLabel(String.valueOf(fecha.getDayOfMonth()));
        numLabel.setFont(new Font("Segoe UI", esHoy ? Font.BOLD : Font.PLAIN, 13));
        numLabel.setForeground(esHoy ? new Color(167, 139, 250) : AppColors.textPrimary());
        celda.add(numLabel);

        java.util.List<String> tareas = tareasPorFecha.getOrDefault(fecha, Collections.emptyList());
        int max = Math.min(tareas.size(), 2);
        for (int i = 0; i < max; i++) {
            JLabel t = new JLabel("• " + tareas.get(i));
            t.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            t.setForeground(AppColors.ACCENT);
            celda.add(t);
        }
        if (tareas.size() > 2) {
            JLabel mas = new JLabel("+" + (tareas.size() - 2) + " más");
            mas.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            mas.setForeground(AppColors.textMuted());
            celda.add(mas);
        }

        return celda;
    }
}