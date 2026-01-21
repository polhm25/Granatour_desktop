package app.controllers;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para la vista de Ayuda.
 * Actualmente no requiere lógica compleja, pero está preparado para futuras funcionalidades
 * como botones para copiar información de contacto, abrir enlaces externos, etc.
 */
public class AyudaController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Por ahora no hay lógica de inicialización necesaria
        // La vista es principalmente informativa con contenido estático
        System.out.println("✓ Vista de Ayuda cargada correctamente");
    }
}
