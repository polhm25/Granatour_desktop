package app.controllers;

import app.granatour.crud.UsuarioCRUD;
import app.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Usuario;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.ButtonType;

public class UsuariosController implements Initializable {

    // Campo de búsqueda y botón para buscar usuarios
    @FXML
    private TextField searchField;
    @FXML
    private Button buscarButton;

    // Tabla para mostrar los usuarios
    @FXML
    private TableView<Usuario> usuariosTable;
    @FXML
    private TableColumn<Usuario, String> dniColumn;
    @FXML
    private TableColumn<Usuario, String> nombreColumn;
    @FXML
    private TableColumn<Usuario, String> apellidosColumn;
    @FXML
    private TableColumn<Usuario, String> emailColumn;
    @FXML
    private TableColumn<Usuario, String> telefonoColumn;
    @FXML
    private TableColumn<Usuario, String> rolColumn;

    // Botones de acción para gestionar usuarios
    @FXML
    private Button añadirButton;
    @FXML
    private Button editarButton;
    @FXML
    private Button eliminarButton;
    @FXML
    private Button actualizarButton;

    // Instancia del CRUD para realizar operaciones con la base de datos
    private UsuarioCRUD usuarioCRUD;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializa el CRUD
        usuarioCRUD = new UsuarioCRUD();

        // Configura las columnas de la tabla con PropertyValueFactory
        setupTableColumns();

        // Configura las acciones de los botones
        setupButtonActions();

        // Carga los datos de usuarios desde la base de datos
        loadUsuariosData();
    }

    private void setupTableColumns() {
        // Vincula cada columna de la tabla con la propiedad correspondiente del objeto
        // Usuario
        dniColumn.setCellValueFactory(new PropertyValueFactory<>("dni"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        apellidosColumn.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue();
            String ap1 = usuario.getAp1() != null ? usuario.getAp1() : "";
            String ap2 = usuario.getAp2() != null ? usuario.getAp2() : "";
            return new javafx.beans.property.SimpleStringProperty(
                    ap2.isEmpty() ? ap1 : ap1 + " " + ap2);
        });
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        telefonoColumn.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        rolColumn.setCellValueFactory(new PropertyValueFactory<>("rol"));

        System.out.println("Columnas de tabla configuradas");
    }

    private void setupButtonActions() {
        buscarButton.setOnAction(event -> handleBuscar());
        añadirButton.setOnAction(event -> handleAñadir());
        editarButton.setOnAction(event -> handleEditar());
        eliminarButton.setOnAction(event -> handleEliminar());
        actualizarButton.setOnAction(event -> handleActualizar());
    }

    private void loadUsuariosData() {
        try {
            List<Usuario> usuarios = usuarioCRUD.obtenerTodos();
            ObservableList<Usuario> observableList = FXCollections.observableArrayList(usuarios);
            usuariosTable.setItems(observableList);
            System.out.println("Datos de usuarios cargados en tabla");
        } catch (Exception e) {
            System.err.println("Error al cargar datos de usuarios: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleBuscar() {
        String searchText = searchField.getText().trim();

        if (searchText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campo vacío", "Por favor, ingresa un término de búsqueda");
            loadUsuariosData(); // Recarga todos los datos
            return;
        }

    }

    private void handleAñadir() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AñadirUsuariosModal.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Añadir Usuario");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            // Recargar la tabla después de cerrar el modal
            loadUsuariosData();

        } catch (IOException e) {
            System.err.println("Error al abrir modal de añadir usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleEditar() {
        Usuario selectedUsuario = usuariosTable.getSelectionModel().getSelectedItem();
        if (selectedUsuario == null) {
            showAlert(Alert.AlertType.WARNING, "Ningún usuario seleccionado",
                    "Por favor, selecciona un usuario para editar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditarUsuarioModal.fxml"));
            Parent root = loader.load();

            EditarUsuarioController controller = loader.getController();
            controller.setUsuarioData(
                    selectedUsuario.getIdUsuario(),
                    selectedUsuario.getNombre(),
                    selectedUsuario.getAp1(),
                    selectedUsuario.getAp2(),
                    selectedUsuario.getDni(),
                    selectedUsuario.getEmail(),
                    selectedUsuario.getTelefono(),
                    selectedUsuario.getRol());

            Stage stage = new Stage();
            stage.setTitle("Editar Usuario");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            // Recargar la tabla después de cerrar el modal
            loadUsuariosData();

        } catch (IOException e) {
            System.err.println("Error al abrir modal de editar usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleEliminar() {
        Usuario selectedUsuario = usuariosTable.getSelectionModel().getSelectedItem();
        if (selectedUsuario == null) {
            showAlert(Alert.AlertType.WARNING, "Ningún usuario seleccionado",
                    "Por favor, selecciona un usuario para eliminar.");
            return;
        }

        if (AlertUtils.showConfirmation("Confirmar eliminación",
                "¿Estás seguro de que deseas eliminar al usuario " + selectedUsuario.getNombreCompleto() + "?",
                "Esta acción no se puede deshacer.").get() == ButtonType.OK) {
            boolean success = usuarioCRUD.eliminar(selectedUsuario.getIdUsuario());
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Usuario eliminado",
                        "El usuario ha sido eliminado correctamente.");
                loadUsuariosData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el usuario.");
            }
        }
    }

    private void handleActualizar() {
        System.out.println("Actualizar tabla");
        searchField.clear();
        loadUsuariosData();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        AlertUtils.showAlert(type, title, message);
    }
}