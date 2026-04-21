import java.security.*;
import java.sql.*;
import java.util.UUID;

public class AuthService {

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void initDatabase() {
        String[] sqls = {
                """
                        CREATE TABLE IF NOT EXISTS usuarios (
                            id       SERIAL PRIMARY KEY,
                            email    VARCHAR(255) UNIQUE NOT NULL,
                            password VARCHAR(255) NOT NULL,
                            nombre   VARCHAR(255),
                            creado   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        )
                        """,
                """
                        CREATE TABLE IF NOT EXISTS reset_tokens (
                            id         SERIAL PRIMARY KEY,
                            email      VARCHAR(255) NOT NULL,
                            token      VARCHAR(255) NOT NULL,
                            expira     TIMESTAMP NOT NULL,
                            usado      BOOLEAN DEFAULT FALSE
                        )
                        """,
                """
                        CREATE TABLE IF NOT EXISTS tareas (
                            id          SERIAL PRIMARY KEY,
                            usuario_id  INTEGER REFERENCES usuarios(id),
                            titulo      VARCHAR(255) NOT NULL,
                            descripcion TEXT,
                            estado      VARCHAR(50) DEFAULT 'pendiente',
                            fecha       DATE,
                            creado      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        )
                        """
        };
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            for (String sql : sqls)
                stmt.execute(sql);
            System.out.println("Base de datos lista.");
        } catch (SQLException e) {
            System.err.println("Error al iniciar BD: " + e.getMessage());
        }
    }

    public static boolean login(String email, String password) {
        String sql = "SELECT password FROM usuarios WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getString("password").equals(hashPassword(password));
        } catch (SQLException e) {
            System.err.println("Error en login: " + e.getMessage());
        }
        return false;
    }

    public static int getUserId(String email) {
        String sql = "SELECT id FROM usuarios WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt("id");
        } catch (SQLException e) {
            System.err.println("Error al obtener ID: " + e.getMessage());
        }
        return -1;
    }

    public static String getNombre(String email) {
        String sql = "SELECT nombre FROM usuarios WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getString("nombre");
        } catch (SQLException e) {
            System.err.println("Error al obtener nombre: " + e.getMessage());
        }
        return email;
    }

    public static boolean registrar(String email, String password, String nombre) {
        String sql = "INSERT INTO usuarios (email, password, nombre) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, hashPassword(password));
            ps.setString(3, nombre);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al registrar: " + e.getMessage());
            return false;
        }
    }

    // Genera token de recuperación y lo guarda en BD
    public static String generarTokenRecuperacion(String email) {
        // Verificar que el email existe
        String checkSql = "SELECT id FROM usuarios WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                return null; // email no existe
        } catch (SQLException e) {
            return null;
        }

        String token = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String sql = """
                    INSERT INTO reset_tokens (email, token, expira)
                    VALUES (?, ?, NOW() + INTERVAL '15 minutes')
                """;
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, token);
            ps.executeUpdate();
            return token;
        } catch (SQLException e) {
            System.err.println("Error al generar token: " + e.getMessage());
            return null;
        }
    }

    // Verifica el código y cambia la contraseña
    public static boolean resetPassword(String email, String token, String nuevaPassword) {
        String checkSql = """
                    SELECT id FROM reset_tokens
                    WHERE email = ? AND token = ? AND usado = FALSE AND expira > NOW()
                """;
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, email);
            ps.setString(2, token.toUpperCase());
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                return false;
            int tokenId = rs.getInt("id");

            // Marcar token como usado
            String markSql = "UPDATE reset_tokens SET usado = TRUE WHERE id = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(markSql)) {
                ps2.setInt(1, tokenId);
                ps2.executeUpdate();
            }

            // Cambiar contraseña
            String updateSql = "UPDATE usuarios SET password = ? WHERE email = ?";
            try (PreparedStatement ps3 = conn.prepareStatement(updateSql)) {
                ps3.setString(1, hashPassword(nuevaPassword));
                ps3.setString(2, email);
                ps3.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error al resetear password: " + e.getMessage());
            return false;
        }
    }
}