package app.controllers;

import app.granatour.session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import models.Usuario;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // Componentes de FXML inyectados automáticamente desde el archivo FXML
    @FXML
    private TabPane mainTabPane;

    @FXML
    private Tab tabUsuarios;
    @FXML
    private Tab tabExcursiones;
    @FXML
    private Tab tabReservas;
    @FXML
    private Tab tabAyuda;

    @FXML
    private Button iconUsuariosButton;
    @FXML
    private Button iconExcursionesButton;
    @FXML
    private Button iconReservasButton;
    @FXML
    private Button iconAyudaButton;

    @FXML
    private Label userInfoLabel;

    @FXML
    private Button cerrarSesionButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Carga las vistas FXML en cada pestaña de la aplicación
        loadTabContent();

        // Configura las acciones de los botones de iconos para cambiar de pestaña
        setupIconButtonActions();

        // Configura el botón de cerrar sesión
        cerrarSesionButton.setOnAction(event -> handleCerrarSesion());

        // Actualiza la información del usuario autenticado en la interfaz
        cargarInformacionUsuario();

        // Aplica restricciones de acceso según el rol del usuario
        aplicarRestriccionesPorRol();

        // Configura los atajos de teclado cuando la escena esté disponible
        setupKeyboardShortcuts();
    }

    // Configura los atajos de teclado Alt+1, Alt+2, Alt+3, Alt+4 para navegar entre
    // pestañas
    private void setupKeyboardShortcuts() {
        // Esperamos a que el mainTabPane esté en una Scene
        mainTabPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                // Definimos las combinaciones de teclas
                KeyCombination alt1 = new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN);
                KeyCombination alt2 = new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.ALT_DOWN);
                KeyCombination alt3 = new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.ALT_DOWN);
                KeyCombination alt4 = new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.ALT_DOWN);

                // También soportamos el teclado numérico
                KeyCombination altNumpad1 = new KeyCodeCombination(KeyCode.NUMPAD1, KeyCombination.ALT_DOWN);
                KeyCombination altNumpad2 = new KeyCodeCombination(KeyCode.NUMPAD2, KeyCombination.ALT_DOWN);
                KeyCombination altNumpad3 = new KeyCodeCombination(KeyCode.NUMPAD3, KeyCombination.ALT_DOWN);
                KeyCombination altNumpad4 = new KeyCodeCombination(KeyCode.NUMPAD4, KeyCombination.ALT_DOWN);

                // Añadimos el filtro de eventos de teclado a la escena
                // Solo permite navegar a pestañas que están visibles (no removidas)
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (alt1.match(event) || altNumpad1.match(event)) {
                        // Solo si la pestaña de Usuarios está disponible
                        if (mainTabPane.getTabs().contains(tabUsuarios)) {
                            mainTabPane.getSelectionModel().select(tabUsuarios);
                        }
                        event.consume();
                    } else if (alt2.match(event) || altNumpad2.match(event)) {
                        mainTabPane.getSelectionModel().select(tabExcursiones);
                        event.consume();
                    } else if (alt3.match(event) || altNumpad3.match(event)) {
                        mainTabPane.getSelectionModel().select(tabReservas);
                        event.consume();
                    } else if (alt4.match(event) || altNumpad4.match(event)) {
                        mainTabPane.getSelectionModel().select(tabAyuda);
                        event.consume();
                    }
                });
            }
        });
    }

    // Carga las vistas FXML de cada módulo en las pestañas correspondientes
    private void loadTabContent() {
        try {
            // Cargo la ventana UsuariosView.fxml en la pestaña de USUARIOS
            FXMLLoader usuariosLoader = new FXMLLoader(getClass().getResource("/fxml/UsuariosView.fxml"));
            Parent usuariosView = usuariosLoader.load();
            tabUsuarios.setContent(usuariosView);

            // Cargo la ventana ExcursionesView.fxml en la pestaña de EXCURSIONES
            FXMLLoader excursionesLoader = new FXMLLoader(getClass().getResource("/fxml/ExcursionesView.fxml"));
            Parent excursionesView = excursionesLoader.load();
            tabExcursiones.setContent(excursionesView);

            // Cargo la ventana ReservasView.fxml en la pestaña de RESERVAS
            FXMLLoader reservasLoader = new FXMLLoader(getClass().getResource("/fxml/ReservasView.fxml"));
            Parent reservasView = reservasLoader.load();
            tabReservas.setContent(reservasView);

            // Cargo la ventana AyudaView.fxml en la pestaña de AYUDA
            FXMLLoader ayudaLoader = new FXMLLoader(getClass().getResource("/fxml/AyudaView.fxml"));
            Parent ayudaView = ayudaLoader.load();
            tabAyuda.setContent(ayudaView);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar el contenido de las pestañas: " + e.getMessage());
        }
    }

    // Configura los botones de iconos para que cambien a la pestaña correspondiente
    // cuando se haga clic
    private void setupIconButtonActions() {
        iconUsuariosButton.setOnAction(event -> mainTabPane.getSelectionModel().select(tabUsuarios));
        iconExcursionesButton.setOnAction(event -> mainTabPane.getSelectionModel().select(tabExcursiones));
        iconReservasButton.setOnAction(event -> mainTabPane.getSelectionModel().select(tabReservas));
        iconAyudaButton.setOnAction(event -> mainTabPane.getSelectionModel().select(tabAyuda));
    }

    // Carga la información del usuario autenticado desde SessionManager
    private void cargarInformacionUsuario() {
        Usuario usuario = SessionManager.getInstance().getUsuarioActual();

        if (usuario != null) {
            String nombreCompleto = usuario.getNombreCompleto();
            String rol = usuario.getRol();
            userInfoLabel.setText("Usuario: " + nombreCompleto + " | Rol: " + rol);
        } else {
            userInfoLabel.setText("Usuario: No autenticado | Rol: desconocido");
        }
    }

    /**
     * Aplica restricciones de acceso a las pestañas según el rol del usuario.
     * - Admin: Acceso completo a todas las pestañas
     * - Guía: Sin acceso a Usuarios
     * - Cliente: Sin acceso a Usuarios
     */
    private void aplicarRestriccionesPorRol() {
        SessionManager session = SessionManager.getInstance();

        if (session.isCliente() || session.isGuia()) {
            // Ocultar pestaña de Usuarios para clientes y guías
            mainTabPane.getTabs().remove(tabUsuarios);
            iconUsuariosButton.setVisible(false);
            iconUsuariosButton.setManaged(false);

            // Seleccionar la pestaña de Excursiones como predeterminada
            mainTabPane.getSelectionModel().select(tabExcursiones);

            System.out.println("Restricciones aplicadas: Pestaña Usuarios oculta para rol " + session.getRol());
        }
    }

    /**
     * Cierra la sesión del usuario actual y vuelve a la pantalla de login.
     */
    private void handleCerrarSesion() {
        // Cerrar la sesión en el SessionManager
        SessionManager.getInstance().cerrarSesion();

        try {
            // Cargar la vista de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent loginView = loader.load();

            // Obtener el Stage actual
            Stage stage = (Stage) cerrarSesionButton.getScene().getWindow();

            // Configurar la nueva escena con el login
            Scene loginScene = new Scene(loginView);
            stage.setScene(loginScene);
            stage.setTitle("GranaTour - Iniciar Sesión");
            stage.centerOnScreen();

            System.out.println("Sesión cerrada. Volviendo al login...");

        } catch (IOException e) {
            System.err.println("Error al cargar la vista de login: " + e.getMessage());
            e.printStackTrace();
        }
    }
}