package app.granatour.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Carga y gestiona la configuración de la base de datos desde un archivo de propiedades.
 * Esto permite separar la configuración del código y facilita el uso en diferentes entornos
 * (desarrollo, producción, Docker).
 */
public class DatabaseConfig {

    private static final String CONFIG_FILE = "/config/database.properties";
    private static Properties properties;

    static {
        loadProperties();
    }

    /**
     * Carga las propiedades del archivo de configuración
     */
    private static void loadProperties() {
        properties = new Properties();
        try (InputStream input = DatabaseConfig.class.getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("✗ No se encontró el archivo de configuración: " + CONFIG_FILE);
                System.err.println("  Asegúrate de que database.properties esté en app/src/main/resources/config/");
                throw new IOException("Archivo de configuración no encontrado");
            }
            properties.load(input);
            System.out.println("✓ Configuración de base de datos cargada exitosamente");
        } catch (IOException e) {
            System.err.println("✗ Error al cargar la configuración de base de datos");
            e.printStackTrace();
        }
    }

    /**
     * Obtiene la URL de conexión a la base de datos
     */
    public static String getUrl() {
        return properties.getProperty("db.url", "");
    }

    /**
     * Obtiene el usuario de la base de datos
     */
    public static String getUser() {
        return properties.getProperty("db.user", "");
    }

    /**
     * Obtiene la contraseña de la base de datos
     */
    public static String getPassword() {
        return properties.getProperty("db.password", "");
    }

    /**
     * Obtiene el driver JDBC
     */
    public static String getDriver() {
        return properties.getProperty("db.driver", "org.postgresql.Driver");
    }

    /**
     * Obtiene el tamaño del pool de conexiones
     */
    public static int getPoolSize() {
        return Integer.parseInt(properties.getProperty("db.pool.size", "10"));
    }

    /**
     * Obtiene el timeout de conexión en milisegundos
     */
    public static int getConnectionTimeout() {
        return Integer.parseInt(properties.getProperty("db.connection.timeout", "30000"));
    }

    /**
     * Obtiene una propiedad genérica
     */
    public static String getProperty(String key) {
        return properties.getProperty(key, "");
    }

    /**
     * Obtiene una propiedad genérica con valor por defecto
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
