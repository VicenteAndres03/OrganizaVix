public class Main {
    public static void main(String[] args) {
        // Crear tabla en Neon si no existe
        AuthService.initDatabase();

        javax.swing.SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}