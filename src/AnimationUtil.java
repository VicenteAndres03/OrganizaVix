import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AnimationUtil {

    // Fade-in de un componente
    public static void fadeIn(JComponent comp) {
        comp.setVisible(true);
        Timer timer = new Timer(16, null);
        float[] alpha = { 0f };
        timer.addActionListener(e -> {
            alpha[0] += 0.07f;
            if (alpha[0] >= 1f) {
                alpha[0] = 1f;
                timer.stop();
            }
            comp.putClientProperty("alpha", alpha[0]);
            comp.repaint();
        });
        timer.start();
    }

    // Hover animado para botones
    public static void addHoverEffect(JComponent comp, Color normal, Color hover) {
        comp.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                animate(comp, comp.getBackground(), hover, 8);
            }

            public void mouseExited(MouseEvent e) {
                animate(comp, comp.getBackground(), normal, 8);
            }
        });
    }

    private static void animate(JComponent comp, Color from, Color to, int steps) {
        Timer t = new Timer(16, null);
        int[] step = { 0 };
        t.addActionListener(e -> {
            step[0]++;
            float ratio = (float) step[0] / steps;
            if (ratio >= 1f) {
                comp.setBackground(to);
                t.stop();
                return;
            }
            int r = (int) (from.getRed() + (to.getRed() - from.getRed()) * ratio);
            int g = (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * ratio);
            int b = (int) (from.getBlue() + (to.getBlue() - from.getBlue()) * ratio);
            comp.setBackground(new Color(
                    Math.max(0, Math.min(255, r)),
                    Math.max(0, Math.min(255, g)),
                    Math.max(0, Math.min(255, b))));
        });
        t.start();
    }

    // Click press effect
    public static void addPressEffect(JComponent comp) {
        comp.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                comp.setBorder(BorderFactory.createEmptyBorder(2, 2, 0, 0));
            }

            public void mouseReleased(MouseEvent e) {
                comp.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            }
        });
    }

    // Slide-in desde abajo
    public static void slideIn(JComponent comp, JComponent parent) {
        int targetY = comp.getY();
        comp.setLocation(comp.getX(), targetY + 30);
        Timer t = new Timer(16, null);
        int[] frames = { 0 };
        t.addActionListener(e -> {
            frames[0]++;
            float ratio = Math.min(1f, frames[0] / 12f);
            float ease = 1f - (1f - ratio) * (1f - ratio);
            int y = (int) (targetY + 30 - 30 * ease);
            comp.setLocation(comp.getX(), y);
            if (ratio >= 1f) {
                comp.setLocation(comp.getX(), targetY);
                t.stop();
            }
        });
        t.start();
    }
}