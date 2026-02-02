package app.controllers;

import app.granatour.crud.UsuarioCRUD;
import app.utils.AlertUtils;
import app.utils.AnimacionUtils;
import app.utils.ValidadorCampos;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import models.Usuario;
import org.controlsfx.validation.ValidationSupport;

import java.net.URL;
import java.util.ResourceBundle;

public class EditarUsuarioController implements Initializable {

    @FXML
    private TextField nombreField;
    @FXML
    private TextField ap1Field;
    @FXML
    private TextField ap2Field;
    @FXML
    private TextField dniField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField telefonoField;
    @FXML
    private ComboBox<String> rolComboBox;
    @FXML
    private PasswordField passwordField;

    @FXML
    private Button guardarButton;
    @FXML
    private Button cancelarButton;

    // Almacena el ID del usuario que se está editando
    private Integer idUsuario;

    // Instancia del CRUD para realizar operaciones con la base de datos
    private UsuarioCRUD usuarioCURD;
    private ValidationSupport validationSupport;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializa el CRUD
        usuarioCURD = new UsuarioCRUD();
        validationSupport = new ValidationSupport();

        // Llena el ComboBox con los roles disponibles
        rolComboBox.getItems().addAll("usuario", "guia", "admin");

        // Configura las acciones de los botones
        guardarButton.setOnAction(event -> handleGuardar());
        cancelarButton.setOnAction(event -> handleCancelar());

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

    private void setupValidadores() {
        validationSupport.registerValidator(nombreField,
                ValidadorCampos.noVacio("Nombre"));

        validationSupport.registerValidator(ap1Field,
                ValidadorCampos.noVacio("Primer apellido"));

        // Validador para Email (no es editable en este form, pero lo validamos)
        validationSupport.registerValidator(emailField,
                ValidadorCampos.email());

        validationSupport.registerValidator(telefonoField,
                ValidadorCampos.telefono());

        validationSupport.registerValidator(rolComboBox,
                ValidadorCampos.comboBoxNoNulo("Rol"));

        // Validador para Contraseña (opcional, solo si se ingresa una nueva)
        validationSupport.registerValidator(passwordField,
                ValidadorCampos.contrasenaMinima(6));

        // Deshabilitar botón si hay errores
        guardarButton.disableProperty().bind(validationSupport.invalidProperty());
    }

    // Carga los datos existentes del usuario en los campos del formulario
    // Este método es llamado desde UsuariosController
    public void setUsuarioData(Integer idUsuario, String nombre, String ap1, String ap2, String dni,
            String email, String telefono, String rol) {
        this.idUsuario = idUsuario;
        nombreField.setText(nombre);
        ap1Field.setText(ap1);
        ap2Field.setText(ap2);
        dniField.setText(dni);
        dniField.setEditable(false); // DNI should not be editable
        emailField.setText(email);
        telefonoField.setText(telefono);
        rolComboBox.setValue(rol);

        // Configura los validadores después de llenar los datos
        setupValidadores();
    }

    private void handleGuardar() {
        // Obtiene los valores de los campos del formulario
        String nombre = nombreField.getText().trim();
        String ap1 = ap1Field.getText().trim();
        String ap2 = ap2Field.getText().trim();
        String dni = dniField.getText().trim();
        String email = emailField.getText().trim();
        String telefono = telefonoField.getText().trim();
        String rol = rolComboBox.getValue();
        String password = passwordField.getText();

        System.out.println("Actualizando usuario con ID: " + idUsuario);
        System.out.println("   Nuevo nombre: " + nombre);
        System.out.println("   Nuevo ap1: " + ap1);
        System.out.println("   Nuevo ap2: " + ap2);
        System.out.println("   Nuevo email: " + email);
        System.out.println("   Nuevo telefono: " + telefono);
        System.out.println("   Nuevo rol: " + rol);

        // Solo actualiza la contraseña si el usuario ingresó una nueva
        if (!password.isEmpty()) {
            System.out.println("   Nueva contrasena: [sera actualizada]");
        } else {
            System.out.println("   Contrasena: sin cambios");
        }

        // Crea un objeto Usuario con los datos actualizados
        Usuario usuarioActualizado = new Usuario(
                idUsuario,
                nombre,
                ap1,
                ap2,
                dni,
                email,
                telefono,
                rol,
                password.isEmpty() ? null : password, // Mantiene null si no se cambió la contraseña
                null,
                null,
                null,
                null);

        // Actualiza los datos en la base de datos
        boolean success = usuarioCURD.actualizar(usuarioActualizado);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Exito", "Usuario actualizado correctamente");
            closeModal();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudo actualizar el usuario. Verifica que el email sea unico.");
        }
    }

    private void handleCancelar() {
        closeModal();
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