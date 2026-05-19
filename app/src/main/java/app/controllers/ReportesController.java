package app.controllers;

import app.granatour.database.DatabaseConnection;
import app.granatour.reports.ReportGenerator;
import app.utils.AlertUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import net.sf.jasperreports.engine.JRException;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class ReportesController implements Initializable {

    @FXML
    private Button actualizarButton;

    @FXML
    private BarChart<Number, String> excursionesPorZonaChart;

    @FXML
    private PieChart reservasPorEstadoChart;

    @FXML
    private ComboBox<String> estadoInformeComboBox;

    @FXML
    private Button generarInformeButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        estadoInformeComboBox.setItems(FXCollections.observableArrayList("TODAS", "pendiente", "confirmada", "cancelada"));
        estadoInformeComboBox.setValue("TODAS");

        actualizarButton.setOnAction(event -> loadChartData());
        generarInformeButton.setOnAction(event -> handleGenerarInforme());

        loadChartData();
    }

    private void loadChartData() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                Connection conn = DatabaseConnection.getInstance().getConnection();
                if (conn == null) {
                    System.err.println("No hay conexión disponible para cargar las gráficas");
                    return null;
                }

                // Acumulamos datos en listas Java normales (thread-safe) antes de tocar el FX thread
                java.util.List<XYChart.Data<Number, String>> barItems = new java.util.ArrayList<>();
                java.util.List<PieChart.Data> pieItems = new java.util.ArrayList<>();

                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT zona, COUNT(*) as total FROM EXCURSIONES GROUP BY zona ORDER BY total DESC");
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        barItems.add(new XYChart.Data<>(rs.getInt("total"), rs.getString("zona")));
                    }
                } catch (Exception e) {
                    System.err.println("Error al cargar datos de excursiones por zona: " + e.getMessage());
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT UPPER(estado::text) as estado, COUNT(*) as total FROM RESERVAS GROUP BY estado");
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        pieItems.add(new PieChart.Data(rs.getString("estado"), rs.getInt("total")));
                    }
                } catch (Exception e) {
                    System.err.println("Error al cargar datos de reservas por estado: " + e.getMessage());
                }

                Platform.runLater(() -> {
                    XYChart.Series<Number, String> series = new XYChart.Series<>();
                    series.getData().addAll(barItems);
                    excursionesPorZonaChart.getData().clear();
                    excursionesPorZonaChart.getData().add(series);
                    reservasPorEstadoChart.setData(FXCollections.observableArrayList(pieItems));
                });

                return null;
            }
        };

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void handleGenerarInforme() {
        String estadoSeleccionado = estadoInformeComboBox.getValue();
        try {
            ReportGenerator reportGenerator = new ReportGenerator();
            String pdfPath = reportGenerator.generarInformeReservasPDF(estadoSeleccionado);
            reportGenerator.abrirPDFConVisorSistema(pdfPath);
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Informe generado",
                    "El informe PDF ha sido generado correctamente.\n\nRuta: " + pdfPath);
        } catch (JRException e) {
            System.err.println("Error JasperReports al generar informe: " + e.getMessage());
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error al generar informe",
                    "No se pudo generar el informe PDF.\n\nDetalle: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado al generar informe: " + e.getMessage());
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error",
                    "Error inesperado: " + e.getMessage());
        }
    }
}
