package app.controllers;

import app.granatour.crud.ExcursionCRUD;
import app.granatour.crud.ReservaCRUD;
import app.granatour.crud.UsuarioCRUD;
import app.utils.AlertUtils;
import app.utils.AnimacionUtils;
import app.utils.ValidadorCampos;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import models.Excursion;
import models.Reserva;
import models.Usuario;
import org.controlsfx.validation.ValidationSupport;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class AnadirReservaController implements Initializable {

    @FXML
    private ComboBox<Usuario> clienteComboBox;
    @FXML
    private ComboBox<Excursion> excursionComboBox;
    @FXML
    private TextField numPersonasField;
    @FXML
    private ComboBox<String> estadoComboBox;
    @FXML
    private Label precioTotalLabel;

    @FXML
    private Button guardarButton;
    @FXML
    private Button cancelarButton;

    private ReservaCRUD reservaCRUD;
    private UsuarioCRUD usuarioCRUD;
    private ExcursionCRUD excursionCRUD;
    private ValidationSupport validationSupport;

    private Double precioPorPersona = 0.0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reservaCRUD = new ReservaCRUD();
        usuarioCRUD = new UsuarioCRUD();
        excursionCRUD = new ExcursionCRUD();
        validationSupport = new ValidationSupport();
        setupComboBoxes();
        setupValidadores();
        setupListeners();
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

    private void setupComboBoxes() {
        // Cargar clientes
        List<Usuario> usuarios = usuarioCRUD.obtenerTodos();
        for (Usuario usuario : usuarios) {
            // Idealmente filtrar por rol 'cliente' o 'usuario'
            clienteComboBox.getItems().add(usuario);
        }

        clienteComboBox.setConverter(new StringConverter<Usuario>() {
            @Override
            public String toString(Usuario usuario) {
                return usuario != null ? usuario.getNombreCompleto() : "";
            }

            @Override
            public Usuario fromString(String string) {
                return null;
            }
        });

        // Cargar excursiones
        List<Excursion> excursiones = excursionCRUD.getAllExcursiones();
        excursionComboBox.getItems().addAll(excursiones);

        excursionComboBox.setConverter(new StringConverter<Excursion>() {
            @Override
            public String toString(Excursion excursion) {
                return excursion != null ? excursion.getNombreRuta() : "";
            }

            @Override
            public Excursion fromString(String string) {
                return null;
            }
        });

        // Estados
        estadoComboBox.getItems().addAll("pendiente", "confirmada", "cancelada");
        estadoComboBox.getSelectionModel().select("pendiente");
    }

    private void setupValidadores() {
        validationSupport.registerValidator(clienteComboBox,
                ValidadorCampos.comboBoxNoNulo("Cliente"));

        validationSupport.registerValidator(excursionComboBox,
                ValidadorCampos.comboBoxNoNulo("Excursión"));

        // Validador combinado para número de personas
        validationSupport.registerValidator(numPersonasField,
                ValidadorCampos.rangoNumerico("Número de personas", 1, 500));

        validationSupport.registerValidator(estadoComboBox,
                ValidadorCampos.comboBoxNoNulo("Estado"));

        // Deshabilitar botón si hay errores
        guardarButton.disableProperty().bind(validationSupport.invalidProperty());
    }

    private void setupListeners() {
        // Actualizar precio cuando cambia la excursión
        excursionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                precioPorPersona = newVal.getPrecioPorPersona();
                calcularPrecioTotal();
            }
        });

        // Actualizar precio cuando cambia el número de personas
        numPersonasField.textProperty().addListener((obs, oldVal, newVal) -> {
            calcularPrecioTotal();
        });
    }

    private void calcularPrecioTotal() {
        try {
            int numPersonas = Integer.parseInt(numPersonasField.getText().trim());
            double total = numPersonas * precioPorPersona;
            precioTotalLabel.setText(String.format("%.2f €", total));
        } catch (NumberFormatException e) {
            precioTotalLabel.setText("0.00 €");
        }
    }

    private void setupButtonActions() {
        guardarButton.setOnAction(event -> handleGuardar());
        cancelarButton.setOnAction(event -> handleCancelar());
    }

    private void handleGuardar() {
        try {
            Usuario cliente = clienteComboBox.getValue();
            Excursion excursion = excursionComboBox.getValue();
            int numPersonas = Integer.parseInt(numPersonasField.getText().trim());
            String estado = estadoComboBox.getValue();

            // Recalcular precio total para asegurar consistencia
            double precioTotal = numPersonas * excursion.getPrecioPorPersona();

            Reserva nuevaReserva = new Reserva(
                    null, // ID autogenerado
                    cliente.getIdUsuario(),
                    cliente.getNombreCompleto(),
                    excursion.getIdExcursion(),
                    excursion.getNombreRuta(),
                    LocalDateTime.now(), // Fecha reserva actual
                    numPersonas,
                    estado,
                    precioTotal);

            boolean success = reservaCRUD.insertar(nuevaReserva);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Reserva añadida correctamente");
                closeModal();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo añadir la reserva.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error de formato", "Por favor, verifica los campos numéricos.");
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
