import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final String URL = "jdbc:postgresql://ep-lucky-boat-ac2w0n5x-pooler.sa-east-1.aws.neon.tech/neondb";

    private static final String USER = "neondb_owner";
    private static final String PASSWORD = "npg_OY2cCIX6gUMj";

    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", USER);
        props.setProperty("password", PASSWORD);

        // SSL — obligatorio en Neon
        props.setProperty("sslmode", "require");
        props.setProperty("channel_binding", "require");

        // Timeouts y keep-alive para evitar "Control plane request failed"
        props.setProperty("connectTimeout", "10"); // segundos
        props.setProperty("socketTimeout", "30"); // segundos
        props.setProperty("loginTimeout", "10");
        props.setProperty("tcpKeepAlive", "true");
        props.setProperty("ApplicationName", "OrganizaVix");

        // Neon cierra conexiones inactivas: reconectar automáticamente
        props.setProperty("autosave", "conservative");
        props.setProperty("cleanupSavepoints", "true");

        return DriverManager.getConnection(URL, props);
    }
}