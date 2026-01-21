package app.controllers;

import app.granatour.session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Carga las vistas FXML en cada pestaña de la aplicación
        loadTabContent();

        // Configura las acciones de los botones de iconos para cambiar de pestaña
        setupIconButtonActions();

        // Actualiza la información del usuario autenticado en la interfaz
        cargarInformacionUsuario();

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
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (alt1.match(event) || altNumpad1.match(event)) {
                        mainTabPane.getSelectionModel().select(tabUsuarios);
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
}