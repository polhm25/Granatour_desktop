package app.controllers;

import app.granatour.crud.ExcursionCRUD;
import app.granatour.crud.UsuarioCRUD;
import app.utils.AlertUtils;
import app.utils.AnimacionUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import models.Excursion;
import models.Usuario;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class EditarExcursionController implements Initializable {

    @FXML
    private TextField nombreRutaField;
    @FXML
    private TextField zonaField;
    @FXML
    private TextField duracionField;
    @FXML
    private TextField precioField;
    @FXML
    private DatePicker fechaInicioPicker;
    @FXML
    private TextField plazasField;
    @FXML
    private ComboBox<Usuario> guiaComboBox;
    @FXML
    private TextArea descripcionArea;

    @FXML
    private Button guardarButton;
    @FXML
    private Button cancelarButton;

    private ExcursionCRUD excursionCRUD;
    private UsuarioCRUD usuarioCRUD;
    private Integer idExcursion;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        excursionCRUD = new ExcursionCRUD();
        usuarioCRUD = new UsuarioCRUD();
        setupGuiaComboBox();
        setupButtonActions();

        // Agrega el logo a la ventana modal
        addLogoToStage();
    }

    private void addLogoToStage() {
        // Esperamos a que el componente esté en una Scene
        guardarButton.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                // Esperamos a que la Scene tenga una Window (Stage)
                newScene.windowProperty().addListener((windowObservable, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        Stage stage = (Stage) newWindow;
                        try {
                            stage.getIcons()
                                    .add(new Image(getClass().getResourceAsStream("/images/LOGO(sinFondo).png")));
                        } catch (Exception e) {
                            System.err.println("Error al cargar el logo: " + e.getMessage());
                        }
                    }
                });
                // Si la window ya existe cuando se asigna la scene
                if (newScene.getWindow() != null) {
                    Stage stage = (Stage) newScene.getWindow();
                    try {
                        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/LOGO(sinFondo).png")));
                    } catch (Exception e) {
                        System.err.println("Error al cargar el logo: " + e.getMessage());
                    }
                }
            }
        });
    }

    private void setupGuiaComboBox() {
        List<Usuario> usuarios = usuarioCRUD.obtenerTodos();
        for (Usuario usuario : usuarios) {
            if ("guia".equalsIgnoreCase(usuario.getRol())) {
                guiaComboBox.getItems().add(usuario);
            }
        }

        guiaComboBox.setConverter(new StringConverter<Usuario>() {
            @Override
            public String toString(Usuario usuario) {
                return usuario != null ? usuario.getNombreCompleto() : "";
            }

            @Override
            public Usuario fromString(String string) {
                return null;
            }
        });
    }

    public void setExcursionData(Excursion excursion) {
        this.idExcursion = excursion.getIdExcursion();
        nombreRutaField.setText(excursion.getNombreRuta());
        zonaField.setText(excursion.getZona());
        duracionField.setText(String.valueOf(excursion.getDuracionHoras()));
        precioField.setText(String.valueOf(excursion.getPrecioPorPersona()));
        fechaInicioPicker.setValue(excursion.getFechaInicio());
        plazasField.setText(String.valueOf(excursion.getPlazasDisponibles()));
        descripcionArea.setText(excursion.getDescripcion());

        // Seleccionar el guía actual
        if (excursion.getIdGuia() != null) {
            for (Usuario u : guiaComboBox.getItems()) {
                if (u.getIdUsuario().equals(excursion.getIdGuia())) {
                    guiaComboBox.setValue(u);
                    break;
                }
            }
        }
    }

    private void setupButtonActions() {
        guardarButton.setOnAction(event -> handleGuardar());
        cancelarButton.setOnAction(event -> handleCancelar());
    }

    private void handleGuardar() {
        if (!validateFields()) {
            return;
        }

        try {
            String nombreRuta = nombreRutaField.getText().trim();
            String zona = zonaField.getText().trim();
            Double duracion = Double.parseDouble(duracionField.getText().trim());
            Double precio = Double.parseDouble(precioField.getText().trim());
            java.time.LocalDate fechaInicio = fechaInicioPicker.getValue();
            Integer plazas = Integer.parseInt(plazasField.getText().trim());
            Usuario guia = guiaComboBox.getValue();
            String descripcion = descripcionArea.getText().trim();

            Excursion excursionActualizada = new Excursion(
                    idExcursion,
                    nombreRuta,
                    zona,
                    duracion,
                    precio,
                    fechaInicio,
                    plazas,
                    guia != null ? guia.getIdUsuario() : null,
                    guia != null ? guia.getNombreCompleto() : null,
                    null, // Imagen
                    descripcion);

            boolean success = excursionCRUD.actualizar(excursionActualizada);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Excursión actualizada correctamente");
                closeModal();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo actualizar la excursión.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error de formato", "Por favor, verifica los campos numéricos.");
        }
    }

    private void handleCancelar() {
        closeModal();
    }

    private boolean validateFields() {
        if (nombreRutaField.getText().trim().isEmpty() || zonaField.getText().trim().isEmpty() ||
                duracionField.getText().trim().isEmpty() || precioField.getText().trim().isEmpty() ||
                fechaInicioPicker.getValue() == null || plazasField.getText().trim().isEmpty()) {

            showAlert(Alert.AlertType.ERROR, "Campos vacíos", "Por favor, completa todos los campos obligatorios.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        // Aplica animación shake al botón guardar si es un error
        if (type == Alert.AlertType.ERROR) {
            AnimacionUtils.shake(guardarButton);
        }

        AlertUtils.showAlert(type, title, message);
    }

    private void closeModal() {
        Stage stage = (Stage) cancelarButton.getScene().getWindow();
        stage.close();
    }
}
