package app.controllers;

import app.granatour.crud.ExcursionCRUD;
import app.granatour.session.SessionManager;
import app.utils.AlertUtils;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Excursion;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.ButtonType;
import app.granatour.reports.ReportGenerator;
import app.granatour.reports.ReportViewerModal;
import net.sf.jasperreports.engine.JRException;

public class ExcursionesController implements Initializable {

    // Campo de búsqueda y botón para buscar excursiones
    @FXML
    private TextField searchField;
    @FXML
    private Button buscarButton;

    // Tabla para mostrar las excursiones
    @FXML
    private TableView<Excursion> excursionesTable;
    @FXML
    private TableColumn<Excursion, String> nombreRutaColumn;
    @FXML
    private TableColumn<Excursion, String> zonaColumn;
    @FXML
    private TableColumn<Excursion, Double> duracionColumn;
    @FXML
    private TableColumn<Excursion, Double> precioColumn;
    @FXML
    private TableColumn<Excursion, LocalDate> fechaColumn;
    @FXML
    private TableColumn<Excursion, Integer> plazasColumn;
    @FXML
    private TableColumn<Excursion, String> guiaColumn;

    // Contador de registros en toolbar
    @FXML
    private Label recordCountLabel;

    // Botones de acción para gestionar excursiones
    @FXML
    private Button añadirButton;
    @FXML
    private Button editarButton;
    @FXML
    private Button eliminarButton;
    @FXML
    private Button generarInformeButton;
    @FXML
    private Button estadisticasButton;

    // Instancia del CRUD para realizar operaciones con la base de datos
    private ExcursionCRUD excursionCRUD;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializa el CRUD
        excursionCRUD = new ExcursionCRUD();

        // Configura las columnas de la tabla con PropertyValueFactory
        setupTableColumns();

        // Configura las acciones de los botones
        setupButtonActions();

        // Aplica restricciones de acceso según el rol del usuario
        aplicarRestriccionesPorRol();

        // Carga los datos de excursiones desde la base de datos
        loadExcursionesData();
    }

    private void setupTableColumns() {
        // Vincula cada columna de la tabla con la propiedad correspondiente del objeto
        // Excursion
        nombreRutaColumn.setCellValueFactory(new PropertyValueFactory<>("nombreRuta"));
        zonaColumn.setCellValueFactory(new PropertyValueFactory<>("zona"));
        duracionColumn.setCellValueFactory(new PropertyValueFactory<>("duracionHoras"));
        precioColumn.setCellValueFactory(new PropertyValueFactory<>("precioPorPersona"));
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        plazasColumn.setCellValueFactory(new PropertyValueFactory<>("plazasDisponibles"));
        guiaColumn.setCellValueFactory(new PropertyValueFactory<>("nombreGuia"));

        System.out.println("Columnas de tabla de excursiones configuradas");
    }

    private void setupButtonActions() {
        buscarButton.setOnAction(event -> handleBuscar());
        añadirButton.setOnAction(event -> handleAñadir());
        editarButton.setOnAction(event -> handleEditar());
        eliminarButton.setOnAction(event -> handleEliminar());
        generarInformeButton.setOnAction(event -> handleGenerarInforme());
        estadisticasButton.setOnAction(event -> handleGenerarEstadisticas());
    }

    private void loadExcursionesData() {
        excursionesTable.setPlaceholder(new Label("Cargando datos..."));

        Task<ObservableList<Excursion>> task = new Task<>() {
            @Override
            protected ObservableList<Excursion> call() {
                return excursionCRUD.getAllExcursiones();
            }
        };

        task.setOnSucceeded(event -> {
            excursionesTable.setItems(task.getValue());
            excursionesTable.setPlaceholder(new Label("No hay excursiones disponibles"));
            recordCountLabel.setText(task.getValue().size() + " registros");
            System.out.println("Datos de excursiones cargados en tabla");
        });

        task.setOnFailed(event -> {
            System.err.println("Error al cargar datos de excursiones: " + task.getException().getMessage());
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudieron cargar las excursiones: " + task.getException().getMessage());
            excursionesTable.setPlaceholder(new Label("Error al cargar datos"));
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void handleBuscar() {
        String searchText = searchField.getText().trim();

        if (searchText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campo vacío", "Por favor, ingresa un término de búsqueda");
            loadExcursionesData(); // Recarga todos los datos
            return;
        }

        System.out.println("Buscando: " + searchText);
        ObservableList<Excursion> excursiones = excursionCRUD.searchExcursiones(searchText);
        excursionesTable.setItems(excursiones);
        recordCountLabel.setText(excursiones.size() + " registros");
    }

    private void handleAñadir() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AñadirExcursionModal.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Añadir Excursión");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            // Recargar la tabla después de cerrar el modal
            loadExcursionesData();

        } catch (IOException e) {
            System.err.println("Error al abrir modal de añadir excursión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleEditar() {
        Excursion selectedExcursion = excursionesTable.getSelectionModel().getSelectedItem();
        if (selectedExcursion == null) {
            showAlert(Alert.AlertType.WARNING, "Ninguna excursión seleccionada",
                    "Por favor, selecciona una excursión para editar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditarExcursionModal.fxml"));
            Parent root = loader.load();

            EditarExcursionController controller = loader.getController();
            controller.setExcursionData(selectedExcursion);

            Stage stage = new Stage();
            stage.setTitle("Editar Excursión");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            // Recargar la tabla después de cerrar el modal
            loadExcursionesData();

        } catch (IOException e) {
            System.err.println("Error al abrir modal de editar excursión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleEliminar() {
        Excursion selectedExcursion = excursionesTable.getSelectionModel().getSelectedItem();
        if (selectedExcursion == null) {
            showAlert(Alert.AlertType.WARNING, "Ninguna excursión seleccionada",
                    "Por favor, selecciona una excursión para eliminar.");
            return;
        }

        if (AlertUtils.showConfirmation("Confirmar eliminación",
                "¿Estás seguro de que deseas eliminar la excursión " + selectedExcursion.getNombreRuta() + "?",
                "Esta acción no se puede deshacer.").get() == ButtonType.OK) {
            boolean success = excursionCRUD.eliminar(selectedExcursion.getIdExcursion());
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Excursión eliminada",
                        "La excursión ha sido eliminada correctamente.");
                loadExcursionesData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo eliminar la excursión.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        AlertUtils.showAlert(type, title, message);
    }

    /**
     * Genera el informe de Catálogo de Excursiones en HTML y lo muestra en un WebView
     */
    private void handleGenerarInforme() {
        System.out.println("=== Iniciando generación de Catálogo de Excursiones ===");
        try {
            ReportGenerator reportGenerator = new ReportGenerator();
            String htmlPath = reportGenerator.generarCatalogoExcursionesHTML();

            System.out.println("Informe generado en: " + htmlPath);

            // Mostrar en WebView modal
            ReportViewerModal.mostrarInformeHTML(htmlPath, "Catálogo de Excursiones - GranaTour");

        } catch (JRException e) {
            System.err.println("=== ERROR JasperReports ===");
            System.err.println("Mensaje: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Causa: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error al generar informe",
                    "No se pudo generar el catálogo de excursiones.\n\nDetalle: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("=== ERROR inesperado ===");
            System.err.println("Tipo: " + e.getClass().getName());
            System.err.println("Mensaje: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Error inesperado: " + e.getMessage());
        }
    }

    /**
     * Genera el informe de estadísticas de excursiones por zona en HTML
     */
    private void handleGenerarEstadisticas() {
        System.out.println("=== Iniciando generación de Estadísticas por Zona ===");
        try {
            ReportGenerator reportGenerator = new ReportGenerator();
            String htmlPath = reportGenerator.generarEstadisticasPorZonaHTML();
            System.out.println("Estadísticas generadas en: " + htmlPath);
            ReportViewerModal.mostrarInformeHTML(htmlPath, "Estadísticas por Zona - GranaTour");
        } catch (JRException e) {
            System.err.println("Error JasperReports en estadísticas: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error al generar estadísticas",
                    "No se pudieron generar las estadísticas.\n\nDetalle: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado en estadísticas: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error inesperado: " + e.getMessage());
        }
    }

    /**
     * Aplica restricciones de acceso a los botones CRUD según el rol del usuario.
     * - Admin: Acceso completo (añadir, editar, eliminar)
     * - Guía y Cliente: Solo lectura (oculta botones de añadir, editar, eliminar)
     */
    private void aplicarRestriccionesPorRol() {
        SessionManager session = SessionManager.getInstance();

        if (session.isCliente() || session.isGuia()) {
            // Ocultar botones de CRUD para clientes y guías
            añadirButton.setVisible(false);
            añadirButton.setManaged(false);

            editarButton.setVisible(false);
            editarButton.setManaged(false);

            eliminarButton.setVisible(false);
            eliminarButton.setManaged(false);

            System.out.println("Restricciones aplicadas en Excursiones: Solo lectura para rol " + session.getRol());

        }
    }
}
