package app.controllers;

import app.granatour.crud.ReservaCRUD;
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
import models.Reserva;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import app.granatour.reports.ReportGenerator;
import net.sf.jasperreports.engine.JRException;

public class ReservasController implements Initializable {

    // Campo de búsqueda y botón para buscar reservas
    @FXML
    private TextField searchField;
    @FXML
    private Button buscarButton;

    // Tabla para mostrar las reservas
    @FXML
    private TableView<Reserva> reservasTable;
    @FXML
    private TableColumn<Reserva, String> clienteColumn;
    @FXML
    private TableColumn<Reserva, String> excursionColumn;
    @FXML
    private TableColumn<Reserva, LocalDateTime> fechaColumn;
    @FXML
    private TableColumn<Reserva, Integer> personasColumn;
    @FXML
    private TableColumn<Reserva, String> estadoColumn;
    @FXML
    private TableColumn<Reserva, Double> precioColumn;

    // Botones de acción para gestionar reservas
    @FXML
    private Button añadirButton;
    @FXML
    private Button editarButton;
    @FXML
    private Button eliminarButton;

    // Contador de registros en toolbar
    @FXML
    private Label recordCountLabel;

    // Componentes para generación de informes
    @FXML
    private ComboBox<String> estadoInformeComboBox;
    @FXML
    private Button generarInformeButton;

    // Instancia del CRUD para realizar operaciones con la base de datos
    private ReservaCRUD reservaCRUD;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializa el CRUD
        reservaCRUD = new ReservaCRUD();

        // Configura las columnas de la tabla con PropertyValueFactory
        setupTableColumns();

        // Configura el ComboBox de estado para informes
        setupEstadoComboBox();

        // Configura las acciones de los botones
        setupButtonActions();

        // Aplica restricciones de acceso según el rol del usuario
        aplicarRestriccionesPorRol();

        // Carga los datos de reservas desde la base de datos
        loadReservasData();
    }

    private void setupTableColumns() {
        // Vincula cada columna de la tabla con la propiedad correspondiente del objeto
        // Reserva
        clienteColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        excursionColumn.setCellValueFactory(new PropertyValueFactory<>("nombreExcursion"));
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaReserva"));
        personasColumn.setCellValueFactory(new PropertyValueFactory<>("numPersonas"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        precioColumn.setCellValueFactory(new PropertyValueFactory<>("precioTotal"));

        System.out.println("Columnas de tabla de reservas configuradas");
    }

    /**
     * Configura el ComboBox con los estados disponibles para filtrar el informe
     */
    private void setupEstadoComboBox() {
        estadoInformeComboBox.getItems().addAll(
                "TODAS",
                "pendiente",
                "confirmada",
                "cancelada"
        );
        estadoInformeComboBox.setValue("TODAS");
    }

    private void setupButtonActions() {
        buscarButton.setOnAction(event -> handleBuscar());
        añadirButton.setOnAction(event -> handleAñadir());
        editarButton.setOnAction(event -> handleEditar());
        eliminarButton.setOnAction(event -> handleEliminar());
        generarInformeButton.setOnAction(event -> handleGenerarInforme());
    }

    private void loadReservasData() {
        reservasTable.setPlaceholder(new Label("Cargando datos..."));

        Task<ObservableList<Reserva>> task = new Task<>() {
            @Override
            protected ObservableList<Reserva> call() {
                SessionManager session = SessionManager.getInstance();
                if (session.isCliente()) {
                    return reservaCRUD.getReservasPorUsuario(session.getIdUsuario());
                }
                return reservaCRUD.getAllReservas();
            }
        };

        task.setOnSucceeded(event -> {
            reservasTable.setItems(task.getValue());
            reservasTable.setPlaceholder(new Label("No hay reservas disponibles"));
            recordCountLabel.setText(task.getValue().size() + " registros");
            System.out.println("Datos de reservas cargados en tabla");
        });

        task.setOnFailed(event -> {
            System.err.println("Error al cargar datos de reservas: " + task.getException().getMessage());
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudieron cargar las reservas: " + task.getException().getMessage());
            reservasTable.setPlaceholder(new Label("Error al cargar datos"));
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void handleBuscar() {
        String searchText = searchField.getText().trim();

        if (searchText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campo vacío", "Por favor, ingresa un término de búsqueda");
            loadReservasData(); // Recarga todos los datos
            return;
        }

        System.out.println("Buscando: " + searchText);
        ObservableList<Reserva> reservas = reservaCRUD.searchReservas(searchText);
        reservasTable.setItems(reservas);
        recordCountLabel.setText(reservas.size() + " registros");
    }

    private void handleAñadir() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AñadirReservaModal.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Añadir Reserva");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            // Recargar la tabla después de cerrar el modal
            loadReservasData();

        } catch (IOException e) {
            System.err.println("Error al abrir modal de añadir reserva: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleEditar() {
        Reserva selectedReserva = reservasTable.getSelectionModel().getSelectedItem();
        if (selectedReserva == null) {
            showAlert(Alert.AlertType.WARNING, "Ninguna reserva seleccionada",
                    "Por favor, selecciona una reserva para editar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditarReservaModal.fxml"));
            Parent root = loader.load();

            EditarReservaController controller = loader.getController();
            controller.setReservaData(selectedReserva);

            Stage stage = new Stage();
            stage.setTitle("Editar Reserva");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            // Recargar la tabla después de cerrar el modal
            loadReservasData();

        } catch (IOException e) {
            System.err.println("Error al abrir modal de editar reserva: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleEliminar() {
        Reserva selectedReserva = reservasTable.getSelectionModel().getSelectedItem();
        if (selectedReserva == null) {
            showAlert(Alert.AlertType.WARNING, "Ninguna reserva seleccionada",
                    "Por favor, selecciona una reserva para eliminar.");
            return;
        }

        if (AlertUtils.showConfirmation("Confirmar eliminación",
                "¿Estás seguro de que deseas eliminar la reserva de " + selectedReserva.getNombreCliente() + "?",
                "Esta acción no se puede deshacer.").get() == ButtonType.OK) {
            boolean success = reservaCRUD.eliminar(selectedReserva.getIdReserva());
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Reserva eliminada",
                        "La reserva ha sido eliminada correctamente.");
                loadReservasData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo eliminar la reserva.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        AlertUtils.showAlert(type, title, message);
    }

    /**
     * Genera el informe de Reservas por Estado en PDF y lo abre con el visor del sistema
     */
    private void handleGenerarInforme() {
        String estadoSeleccionado = estadoInformeComboBox.getValue();
        if (estadoSeleccionado == null || estadoSeleccionado.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Selección requerida",
                    "Por favor, selecciona un estado para el informe.");
            return;
        }

        System.out.println("=== Iniciando generación de Informe de Reservas ===");
        System.out.println("Estado seleccionado: " + estadoSeleccionado);

        try {
            ReportGenerator reportGenerator = new ReportGenerator();
            String pdfPath = reportGenerator.generarInformeReservasPDF(estadoSeleccionado);

            System.out.println("Informe generado en: " + pdfPath);

            // Abrir con visor del sistema
            reportGenerator.abrirPDFConVisorSistema(pdfPath);

            showAlert(Alert.AlertType.INFORMATION, "Informe generado",
                    "El informe se ha generado correctamente.\nArchivo: " + pdfPath);

        } catch (JRException e) {
            System.err.println("=== ERROR JasperReports ===");
            System.err.println("Mensaje: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Causa: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error al generar informe",
                    "No se pudo generar el informe de reservas.\n\nDetalle: " + e.getMessage());
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
     * Aplica restricciones de acceso a los botones CRUD según el rol del usuario.
     * - Admin: Acceso completo (añadir, editar, eliminar cualquier reserva)
     * - Guía: Solo lectura (ve todas las reservas pero no puede modificarlas)
     * - Cliente: Solo sus reservas (puede añadir, editar y eliminar sus propias reservas)
     */
    private void aplicarRestriccionesPorRol() {
        SessionManager session = SessionManager.getInstance();

        if (session.isGuia()) {
            // Guía: solo lectura, ocultar todos los botones CRUD
            añadirButton.setVisible(false);
            añadirButton.setManaged(false);

            editarButton.setVisible(false);
            editarButton.setManaged(false);

            eliminarButton.setVisible(false);
            eliminarButton.setManaged(false);

            // Ocultar generación de informes para guías
            estadoInformeComboBox.setVisible(false);
            estadoInformeComboBox.setManaged(false);
            generarInformeButton.setVisible(false);
            generarInformeButton.setManaged(false);

            System.out.println("Restricciones aplicadas en Reservas: Solo lectura para rol guia");
        } else if (session.isCliente()) {
            // Cliente: puede gestionar sus propias reservas
            // Los botones se mantienen visibles ya que solo ve sus reservas

            // Ocultar generación de informes para clientes
            estadoInformeComboBox.setVisible(false);
            estadoInformeComboBox.setManaged(false);
            generarInformeButton.setVisible(false);
            generarInformeButton.setManaged(false);

            System.out.println("Restricciones aplicadas en Reservas: Cliente solo ve sus reservas");
        }
        // Admin: sin restricciones, acceso completo
    }
}
