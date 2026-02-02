package app.controllers;

import app.granatour.database.DatabaseConnection;
import app.granatour.session.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import models.Usuario;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

/**
 * Controlador para la ventana de Login.
 * Gestiona la autenticación de usuarios contra la base de datos.
 */
public class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Button exitButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar eventos de Enter en los campos
        emailField.setOnAction(event -> handleLogin());
        passwordField.setOnAction(event -> handleLogin());

        // Ocultar el mensaje de error al inicio
        errorLabel.setVisible(false);

        // Agregar listeners para ocultar error cuando el usuario empiece a escribir
        emailField.textProperty().addListener((observable, oldValue, newValue) -> errorLabel.setVisible(false));
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> errorLabel.setVisible(false));
    }

    /**
     * Maneja el evento de clic en el botón "Iniciar Sesión"
     */
    @FXML
    private void handleLogin() {
        // Obtener valores de los campos
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validar que los campos no estén vacíos
        if (email.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor complete todos los campos");
            return;
        }

        // Validar formato básico de email
        if (!email.contains("@") || !email.contains(".")) {
            mostrarError("Por favor ingrese un email válido");
            return;
        }

        // Intentar autenticar al usuario
        Usuario usuario = autenticarUsuario(email, password);

        if (usuario != null) {
            // Autenticación exitosa
            System.out.println("✓ Login exitoso para: " + usuario.getNombreCompleto());

            // Actualizar último acceso en la base de datos
            actualizarUltimoAcceso(usuario.getIdUsuario());

            // Guardar usuario en la sesión
            SessionManager.getInstance().setUsuarioActual(usuario);

            // Abrir ventana principal
            abrirVentanaPrincipal();
        } else {
            // Autenticación fallida
            mostrarError("Email o contraseña incorrectos");
        }
    }

    /**
     * Autentica al usuario contra la base de datos
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @return Usuario si las credenciales son correctas, null si no
     */
    private Usuario autenticarUsuario(String email, String password) {
        String query = "SELECT * FROM USUARIOS WHERE email = ? AND password = ?";

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();

            if (conn == null || conn.isClosed()) {
                mostrarError("Error de conexión a la base de datos");
                return null;
            }

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Usuario encontrado, crear objeto Usuario
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setNombre(rs.getString("nombre"));
                usuario.setAp1(rs.getString("ap1"));
                usuario.setAp2(rs.getString("ap2"));
                usuario.setDni(rs.getString("dni"));
                usuario.setEmail(rs.getString("email"));
                usuario.setTelefono(rs.getString("telefono"));
                usuario.setRol(rs.getString("rol"));
                usuario.setPassword(rs.getString("password"));

                // Campos opcionales
                Double valoracion = rs.getDouble("valoracion");
                usuario.setValoracion(rs.wasNull() ? null : valoracion);
                usuario.setNumTurnos(rs.getInt("num_turnos"));

                // Fechas
                Timestamp fechaReg = rs.getTimestamp("fecha_registro");
                usuario.setFechaRegistro(fechaReg != null ? fechaReg.toLocalDateTime() : null);

                Timestamp ultimoAcc = rs.getTimestamp("ultimo_acceso");
                usuario.setUltimoAcceso(ultimoAcc != null ? ultimoAcc.toLocalDateTime() : null);

                rs.close();
                stmt.close();

                return usuario;
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("✗ Error al autenticar usuario: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error al conectar con la base de datos");
        }

        return null;
    }

    /**
     * Actualiza la fecha y hora del último acceso del usuario en la base de datos
     */
    private void actualizarUltimoAcceso(int idUsuario) {
        String query = "UPDATE USUARIOS SET ultimo_acceso = ? WHERE id_usuario = ?";

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, idUsuario);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ Último acceso actualizado");
            }

            stmt.close();

        } catch (SQLException e) {
            System.err.println("✗ Error al actualizar último acceso: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Abre la ventana principal de la aplicación
     */
    private void abrirVentanaPrincipal() {
        try {
            // Cargar Main.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
            Parent root = loader.load();

            // Crear nueva escena
            Scene scene = new Scene(root);

            // Crear nuevo Stage
            Stage mainStage = new Stage();
            mainStage.setTitle("GranaTour - Gestión de Turismo y Senderismo");
            mainStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/LOGO(sinFondo).png")));
            mainStage.setScene(scene);
            mainStage.setResizable(true);

            // Establecer tamaño mínimo y preferido
            mainStage.setMinWidth(900);
            mainStage.setMinHeight(700);
            mainStage.setWidth(1100);
            mainStage.setHeight(800);

            // Mostrar ventana principal
            mainStage.show();

            // Cerrar ventana de login
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();

        } catch (IOException e) {
            System.err.println("✗ Error al abrir ventana principal: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error al cargar la aplicación principal");
        }
    }

    /**
     * Muestra un mensaje de error en la interfaz
     */
    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
    }

    /**
     * Maneja el evento de clic en el botón "Salir"
     */
    @FXML
    private void handleExit() {
        System.out.println("Aplicación cerrada por el usuario");
        Platform.exit();
    }
}
