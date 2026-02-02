package app.granatour.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import app.granatour.config.DatabaseConfig;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            String driverClass = DatabaseConfig.getDriver();
            String url = DatabaseConfig.getUrl();
            String user = DatabaseConfig.getUser();
            String password = DatabaseConfig.getPassword();

            // Carga el driver JDBC para establecer conexiones
            System.out.println("Cargando driver de base de datos...");
            Class.forName(driverClass);
            System.out.println("Driver cargado exitosamente");

            System.out.println("Intentando conectar a: " + url);
            this.connection = DriverManager.getConnection(url, user, password);

            // Verifica que la conexión sea válida
            if (this.connection != null && !this.connection.isClosed()) {
                System.out.println("✓ Conexión a base de datos exitosa");
            }

        } catch (ClassNotFoundException e) {
            System.err.println("✗ Error: JDBC Driver no encontrado");
            System.err.println("  Verifica que el driver PostgreSQL esté en las dependencias");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("✗ Error al conectar con la base de datos");
            System.err.println("  Detalles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Obtiene la instancia única de la conexión, creándola si no existe o reconectando si fue cerrada
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        } else {
            try {
                // Verifica si la conexión está cerrada y la reconecta si es necesario
                if (instance.connection == null || instance.connection.isClosed()) {
                    System.out.println("Reconectando a la base de datos...");
                    instance = new DatabaseConnection();
                }
            } catch (SQLException e) {
                System.out.println("Reconectando por error de conexión...");
                instance = new DatabaseConnection();
            }
        }
        return instance;
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    // Cierra la conexión a la base de datos
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Conexión cerrada");
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión");
                e.printStackTrace();
            }
        }
    }
}