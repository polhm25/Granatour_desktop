package app.granatour.reports;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.File;

/**
 * Modal con WebView para mostrar informes HTML generados por JasperReports.
 * Muestra el informe de Catálogo de Excursiones en una ventana modal.
 */
public class ReportViewerModal {

    /**
     * Muestra un informe HTML en un modal con WebView
     * 
     * @param htmlPath Ruta al archivo HTML
     * @param title    Título de la ventana
     */
    public static void mostrarInformeHTML(String htmlPath, String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        File htmlFile = new File(htmlPath);
        if (htmlFile.exists()) {
            webEngine.load(htmlFile.toURI().toString());
        } else {
            webEngine.loadContent(
                    "<html><body style='font-family: Arial; padding: 20px;'>" +
                            "<h2 style='color: #C62828;'>Error</h2>" +
                            "<p>No se pudo cargar el informe: " + htmlPath + "</p>" +
                            "</body></html>");
        }

        // Botón para cerrar el modal
        Button cerrarButton = new Button("Cerrar");
        cerrarButton.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-padding: 8 20;");
        cerrarButton.setOnAction(e -> stage.close());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #E0E0E0; -fx-border-width: 1 0 0 0;");
        buttonBox.getChildren().add(cerrarButton);

        BorderPane root = new BorderPane();
        root.setCenter(webView);
        root.setBottom(buttonBox);

        Scene scene = new Scene(root, 1050, 750);
        stage.setScene(scene);

        try {
            stage.getIcons().add(new Image(
                    ReportViewerModal.class.getResourceAsStream("/images/LOGO(sinFondo).png")));
        } catch (Exception e) {
            System.err.println("Error al cargar el logo: " + e.getMessage());
        }

        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }
}
