package app.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Clase utilitaria para mostrar alertas con el logo de la aplicación.
 */
public class AlertUtils {

    /**
     * Muestra una alerta con el título, mensaje y tipo especificados, incluyendo el
     * logo de la aplicación.
     * Tipo de alerta (INFORMATION, ERROR, WARNING, etc.)
     * Título de la ventana
     * Mensaje de contenido
     */
    public static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        addLogo(alert);

        alert.showAndWait();
    }

    /**
     * Muestra un diálogo de confirmación con el logo de la aplicación.
     * Título de la ventana
     * Texto de cabecera
     * Mensaje de contenido
     */
    public static Optional<ButtonType> showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        addLogo(alert);

        return alert.showAndWait();
    }

    /**
     * Método privado para añadir el logo a una alerta.
     */
    private static void addLogo(Alert alert) {
        try {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(AlertUtils.class.getResourceAsStream("/images/LOGO(sinFondo).png")));
        } catch (Exception e) {
            System.err.println("Error al cargar el logo en la alerta: " + e.getMessage());
        }
    }
}
