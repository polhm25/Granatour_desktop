
package app.granatour;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Carga el archivo FXML de login para autenticación inicial
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            // Crea la escena de login
            Scene scene = new Scene(root);

            // Configura la ventana de login con título, icono y propiedades
            primaryStage.setTitle("GranaTour - Inicio de Sesión");
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/LOGO(sinFondo).png")));
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al cargar Login.fxml: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}