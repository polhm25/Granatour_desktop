package app.controllers;

import app.granatour.session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import models.Usuario;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private StackPane contentArea;

    @FXML private Button navUsuariosButton;
    @FXML private Button navExcursionesButton;
    @FXML private Button navReservasButton;
    @FXML private Button navAyudaButton;

    @FXML private Label userInfoLabel;
    @FXML private Button cerrarSesionButton;

    private Parent usuariosView;
    private Parent excursionesView;
    private Parent reservasView;
    private Parent ayudaView;

    private Button activeNavButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadViews();
        setupNavButtonActions();
        cerrarSesionButton.setOnAction(event -> handleCerrarSesion());
        cargarInformacionUsuario();
        aplicarRestriccionesPorRol();
        setupKeyboardShortcuts();
    }

    private void loadViews() {
        try {
            usuariosView = loadFXML("/fxml/UsuariosView.fxml");
            excursionesView = loadFXML("/fxml/ExcursionesView.fxml");
            reservasView = loadFXML("/fxml/ReservasView.fxml");
            ayudaView = loadFXML("/fxml/AyudaView.fxml");

            contentArea.getChildren().addAll(usuariosView, excursionesView, reservasView, ayudaView);
            navigate(usuariosView, navUsuariosButton);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar las vistas: " + e.getMessage());
        }
    }

    private Parent loadFXML(String resource) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
        return loader.load();
    }

    private void navigate(Parent view, Button navButton) {
        usuariosView.setVisible(false);
        usuariosView.setManaged(false);
        excursionesView.setVisible(false);
        excursionesView.setManaged(false);
        reservasView.setVisible(false);
        reservasView.setManaged(false);
        ayudaView.setVisible(false);
        ayudaView.setManaged(false);

        view.setVisible(true);
        view.setManaged(true);

        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("sidebar-button-active");
        }
        navButton.getStyleClass().add("sidebar-button-active");
        activeNavButton = navButton;
    }

    private void setupNavButtonActions() {
        navUsuariosButton.setOnAction(e -> navigate(usuariosView, navUsuariosButton));
        navExcursionesButton.setOnAction(e -> navigate(excursionesView, navExcursionesButton));
        navReservasButton.setOnAction(e -> navigate(reservasView, navReservasButton));
        navAyudaButton.setOnAction(e -> navigate(ayudaView, navAyudaButton));
    }

    private void setupKeyboardShortcuts() {
        contentArea.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                KeyCombination alt1 = new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN);
                KeyCombination alt2 = new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.ALT_DOWN);
                KeyCombination alt3 = new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.ALT_DOWN);
                KeyCombination alt4 = new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.ALT_DOWN);
                KeyCombination altNumpad1 = new KeyCodeCombination(KeyCode.NUMPAD1, KeyCombination.ALT_DOWN);
                KeyCombination altNumpad2 = new KeyCodeCombination(KeyCode.NUMPAD2, KeyCombination.ALT_DOWN);
                KeyCombination altNumpad3 = new KeyCodeCombination(KeyCode.NUMPAD3, KeyCombination.ALT_DOWN);
                KeyCombination altNumpad4 = new KeyCodeCombination(KeyCode.NUMPAD4, KeyCombination.ALT_DOWN);

                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (alt1.match(event) || altNumpad1.match(event)) {
                        if (navUsuariosButton.isVisible()) navigate(usuariosView, navUsuariosButton);
                        event.consume();
                    } else if (alt2.match(event) || altNumpad2.match(event)) {
                        navigate(excursionesView, navExcursionesButton);
                        event.consume();
                    } else if (alt3.match(event) || altNumpad3.match(event)) {
                        navigate(reservasView, navReservasButton);
                        event.consume();
                    } else if (alt4.match(event) || altNumpad4.match(event)) {
                        navigate(ayudaView, navAyudaButton);
                        event.consume();
                    }
                });
            }
        });
    }

    private void cargarInformacionUsuario() {
        Usuario usuario = SessionManager.getInstance().getUsuarioActual();
        if (usuario != null) {
            userInfoLabel.setText("Usuario: " + usuario.getNombreCompleto() + " | Rol: " + usuario.getRol());
        } else {
            userInfoLabel.setText("Usuario: No autenticado");
        }
    }

    private void aplicarRestriccionesPorRol() {
        SessionManager session = SessionManager.getInstance();
        if (session.isCliente() || session.isGuia()) {
            navUsuariosButton.setVisible(false);
            navUsuariosButton.setManaged(false);
            navigate(excursionesView, navExcursionesButton);
            System.out.println("Restricciones aplicadas: botón Usuarios oculto para rol " + session.getRol());
        }
    }

    private void handleCerrarSesion() {
        SessionManager.getInstance().cerrarSesion();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent loginView = loader.load();
            Stage stage = (Stage) cerrarSesionButton.getScene().getWindow();
            stage.setScene(new Scene(loginView));
            stage.setTitle("GranaTour - Iniciar Sesión");
            stage.centerOnScreen();
            System.out.println("Sesión cerrada. Volviendo al login...");
        } catch (IOException e) {
            System.err.println("Error al cargar la vista de login: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
