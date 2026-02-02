package app.granatour.reports;

import app.granatour.database.DatabaseConnection;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleHtmlExporterConfiguration;
import net.sf.jasperreports.export.SimpleHtmlReportConfiguration;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase para generar informes JasperReports.
 * Genera el Catálogo de Excursiones (HTML) y el Informe de Reservas (PDF).
 */
public class ReportGenerator {

    private static final String REPORTS_PATH = "/informes/";
    private static final String OUTPUT_DIR = getProjectOutputDirectory();

    /**
     * Obtiene el directorio de salida dentro del proyecto
     * Busca la carpeta del proyecto y crea una subcarpeta 'informes-generados'
     */
    private static String getProjectOutputDirectory() {
        // Intentar obtener la ruta del proyecto desde el classpath
        try {
            String classPath = ReportGenerator.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI().getPath();

            // En Windows, eliminar la barra inicial si existe (ej: /C:/...)
            if (classPath.matches("^/[A-Za-z]:.*")) {
                classPath = classPath.substring(1);
            }

            File classDir = new File(classPath);
            // Subir desde build/classes/java/main o build/resources/main hasta la raíz del proyecto
            File projectDir = classDir;
            while (projectDir != null && !new File(projectDir, "build.gradle").exists()) {
                projectDir = projectDir.getParentFile();
            }

            if (projectDir != null) {
                String outputPath = projectDir.getAbsolutePath() + File.separator + "informes-generados" + File.separator;
                System.out.println("Directorio de informes del proyecto: " + outputPath);
                return outputPath;
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo ruta del proyecto: " + e.getMessage());
        }

        // Fallback: usar directorio de trabajo actual
        String fallbackPath = System.getProperty("user.dir") + File.separator + "app" + File.separator + "informes-generados" + File.separator;
        System.out.println("Usando directorio fallback: " + fallbackPath);
        return fallbackPath;
    }

    /**
     * Genera el informe de Catálogo de Excursiones en formato HTML
     * 
     * @return Ruta al archivo HTML generado
     * @throws JRException Si ocurre un error al generar el informe
     */
    public String generarCatalogoExcursionesHTML() throws JRException {
        ensureOutputDirectoryExists();

        InputStream reportStream = getClass().getResourceAsStream(
                REPORTS_PATH + "CatalogoExcursiones.jrxml");

        if (reportStream == null) {
            throw new JRException("No se encontró el archivo de informe: CatalogoExcursiones.jrxml");
        }

        System.out.println("Compilando informe CatalogoExcursiones.jrxml...");
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
        System.out.println("Informe compilado correctamente");

        // Obtener y validar la conexión a la base de datos
        Connection connection = getValidConnection();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("REPORT_TITLE", "CATÁLOGO DE EXCURSIONES DISPONIBLES");

        System.out.println("Llenando informe con datos de la base de datos...");
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
        System.out.println("Informe llenado con " + jasperPrint.getPages().size() + " páginas");

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String outputPath = OUTPUT_DIR + "CatalogoExcursiones_" + timestamp + ".html";

        HtmlExporter exporter = new HtmlExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputPath));

        SimpleHtmlExporterConfiguration configuration = new SimpleHtmlExporterConfiguration();
        configuration.setHtmlHeader(getHtmlHeader());
        configuration.setHtmlFooter(getHtmlFooter());
        exporter.setConfiguration(configuration);

        // Configuración del reporte HTML
        SimpleHtmlReportConfiguration reportConfig = new SimpleHtmlReportConfiguration();
        reportConfig.setRemoveEmptySpaceBetweenRows(true);
        reportConfig.setWhitePageBackground(false);
        reportConfig.setWrapBreakWord(true);
        exporter.setConfiguration(reportConfig);

        exporter.exportReport();

        System.out.println("Catálogo de excursiones generado: " + outputPath);
        return outputPath;
    }

    /**
     * Genera el informe de Reservas por Estado en formato PDF
     * 
     * @param estadoReserva Estado a filtrar (pendiente/confirmada/cancelada/TODAS)
     * @return Ruta al archivo PDF generado
     * @throws JRException Si ocurre un error al generar el informe
     */
    public String generarInformeReservasPDF(String estadoReserva) throws JRException {
        ensureOutputDirectoryExists();

        InputStream reportStream = getClass().getResourceAsStream(
                REPORTS_PATH + "InformeReservasPorEstado.jrxml");

        if (reportStream == null) {
            throw new JRException("No se encontró el archivo de informe: InformeReservasPorEstado.jrxml");
        }

        System.out.println("Compilando informe InformeReservasPorEstado.jrxml...");
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
        System.out.println("Informe compilado correctamente");

        // Obtener y validar la conexión a la base de datos
        Connection connection = getValidConnection();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ESTADO_RESERVA", estadoReserva);
        System.out.println("Parámetro ESTADO_RESERVA: " + estadoReserva);

        System.out.println("Llenando informe con datos de la base de datos...");
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
        System.out.println("Informe llenado con " + jasperPrint.getPages().size() + " páginas");

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String outputPath = OUTPUT_DIR + "InformeReservas_" + estadoReserva + "_" + timestamp + ".pdf";

        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);

        System.out.println("Informe de reservas generado: " + outputPath);
        return outputPath;
    }

    /**
     * Obtiene una conexión válida a la base de datos
     * 
     * @return Connection válida
     * @throws JRException Si no se puede obtener una conexión válida
     */
    private Connection getValidConnection() throws JRException {
        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();

            if (connection == null) {
                System.err.println("Error: La conexión a la base de datos es null");
                throw new JRException("No se pudo obtener la conexión a la base de datos. Conexión es null.");
            }

            if (connection.isClosed()) {
                System.err.println("Error: La conexión a la base de datos está cerrada");
                // Intentar reconectar
                connection = DatabaseConnection.getInstance().getConnection();
                if (connection == null || connection.isClosed()) {
                    throw new JRException("No se pudo reconectar a la base de datos.");
                }
            }

            // Verificar que la conexión es válida con un timeout de 5 segundos
            if (!connection.isValid(5)) {
                System.err.println("Error: La conexión a la base de datos no es válida");
                throw new JRException("La conexión a la base de datos no es válida.");
            }

            System.out.println("Conexión a base de datos válida y activa");
            return connection;

        } catch (SQLException e) {
            System.err.println("Error SQL al validar conexión: " + e.getMessage());
            throw new JRException("Error al validar la conexión a la base de datos: " + e.getMessage(), e);
        }
    }

    /**
     * Abre un archivo PDF con el visor del sistema operativo
     * 
     * @param pdfPath Ruta al archivo PDF
     */
    public void abrirPDFConVisorSistema(String pdfPath) {
        try {
            File file = new File(pdfPath);
            if (Desktop.isDesktopSupported() && file.exists()) {
                Desktop.getDesktop().open(file);
                System.out.println("PDF abierto con visor del sistema");
            } else {
                System.err.println("No se puede abrir el PDF: Desktop no soportado o archivo no existe");
            }
        } catch (Exception e) {
            System.err.println("Error al abrir PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtiene la ruta del directorio de salida de informes
     * 
     * @return Ruta del directorio de salida
     */
    public String getOutputDirectory() {
        return OUTPUT_DIR;
    }

    /**
     * Asegura que el directorio de salida exista, creándolo si es necesario
     */
    private void ensureOutputDirectoryExists() {
        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            boolean created = outputDir.mkdirs();
            if (created) {
                System.out.println("Directorio de informes creado: " + OUTPUT_DIR);
            }
        }
    }

    /**
     * Carga el logo como base64 desde los recursos
     */
    private String getLogoBase64() {
        try (InputStream is = getClass().getResourceAsStream("/images/LOGO(sinFondo).png")) {
            if (is == null) {
                System.err.println("No se pudo cargar el logo");
                return "";
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int nRead;
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return Base64.getEncoder().encodeToString(buffer.toByteArray());
        } catch (Exception e) {
            System.err.println("Error cargando logo: " + e.getMessage());
            return "";
        }
    }

    /**
     * Genera el encabezado HTML con estilos CSS embebidos
     */
    private String getHtmlHeader() {
        String logoBase64 = getLogoBase64();
        String fechaActual = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String horaActual = new SimpleDateFormat("HH:mm").format(new Date());

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Catálogo de Excursiones - GranaTour</title>
                    <style>
                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background: #f5f5f5;
                            color: #333333;
                            margin: 0;
                            padding: 20px;
                            line-height: 1.5;
                        }
                        .report-wrapper {
                            max-width: 900px;
                            margin: 0 auto;
                        }
                        .header-banner {
                            background: #8B1E3F;
                            padding: 20px 30px;
                            display: flex;
                            align-items: center;
                            justify-content: space-between;
                        }
                        .header-left {
                            display: flex;
                            align-items: center;
                            gap: 20px;
                        }
                        .logo-container {
                            background: #FFFFFF;
                            padding: 8px;
                        }
                        .logo-container img {
                            width: 50px;
                            height: 50px;
                            display: block;
                        }
                        .header-text h1 {
                            color: #FFFFFF;
                            font-size: 28px;
                            font-weight: 700;
                            margin-bottom: 4px;
                        }
                        .header-text .subtitle {
                            color: #f0f0f0;
                            font-size: 14px;
                        }
                        .header-right {
                            text-align: right;
                            color: #FFFFFF;
                        }
                        .header-right .date-label {
                            font-size: 11px;
                            color: #e0e0e0;
                        }
                        .header-right .date-value {
                            font-size: 16px;
                            font-weight: 600;
                        }
                        .header-right .time-value {
                            font-size: 12px;
                            color: #e0e0e0;
                        }
                        .report-container {
                            background: #FFFFFF;
                            padding: 20px;
                        }
                        .jasper-content {
                            padding: 0;
                        }
                        table {
                            border-collapse: collapse;
                            width: 100%%;
                        }
                        th {
                            background: #8B1E3F;
                            color: #FFFFFF;
                            padding: 10px 8px;
                            text-align: left;
                            font-weight: 600;
                            font-size: 12px;
                            border: 1px solid #6d1730;
                        }
                        td {
                            padding: 8px;
                            border: 1px solid #dddddd;
                            font-size: 12px;
                        }
                        tr:nth-child(even) {
                            background-color: #f9f9f9;
                        }
                        img[src=""], img:not([src]) {
                            display: none !important;
                        }
                        span {
                            color: #333333 !important;
                        }
                        .jasper-content * {
                            visibility: visible !important;
                        }
                    </style>
                </head>
                <body>
                <div class="report-wrapper">
                    <div class="header-banner">
                        <div class="header-left">
                            <div class="logo-container">
                                <img src="data:image/png;base64,%s" alt="GranaTour Logo">
                            </div>
                            <div class="header-text">
                                <h1>GRANATOUR</h1>
                                <div class="subtitle">Catálogo de Excursiones</div>
                            </div>
                        </div>
                        <div class="header-right">
                            <div class="date-label">Generado el:</div>
                            <div class="date-value">%s</div>
                            <div class="time-value">%s h</div>
                        </div>
                    </div>
                    <div class="report-container">
                        <div class="jasper-content">
                """.formatted(logoBase64, fechaActual, horaActual);
    }

    /**
     * Genera el pie de página HTML
     */
    private String getHtmlFooter() {
        return """
                        </div>
                        <div style="padding: 15px 20px; background: #8B1E3F; margin-top: 20px;">
                            <div style="color: #FFFFFF; font-weight: 600; font-size: 13px; margin-bottom: 4px;">¿Tienes dudas? Contáctanos</div>
                            <div style="color: #e0e0e0; font-size: 12px;">info@granatour.es | +34 958 123 456</div>
                            <div style="color: #cccccc; font-size: 11px; margin-top: 8px;">GranaTour © 2025 - Turismo y Senderismo en Granada</div>
                        </div>
                    </div>
                </div>
                </body>
                </html>
                """;
    }
}
