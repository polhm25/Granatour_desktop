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
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.validation.ValidationSupport;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AnadirReservaController implements Initializable {

    @FXML
    private TextField clienteSearchField;
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

    // Mapa para relacionar el texto mostrado con el objeto Usuario
    private Map<String, Usuario> clientesMap = new HashMap<>();
    private Usuario clienteSeleccionado = null;
    private List<Usuario> todosLosClientes;

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
        // Cargar clientes y configurar autocompletado
        setupClienteAutoComplete();

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

    /**
     * Configura el campo de búsqueda de cliente con autocompletado.
     * Permite buscar por nombre completo o DNI.
     */
    private void setupClienteAutoComplete() {
        todosLosClientes = usuarioCRUD.obtenerTodos();

        // Crear mapa de texto -> Usuario para búsqueda rápida
        for (Usuario usuario : todosLosClientes) {
            // Formato: "Nombre Completo - DNI"
            String displayText = usuario.getNombreCompleto() + " - " + usuario.getDni();
            clientesMap.put(displayText, usuario);
        }

        // Configurar autocompletado con ControlsFX
        AutoCompletionBinding<String> autoCompletion = TextFields.bindAutoCompletion(
                clienteSearchField,
                request -> {
                    String userText = request.getUserText().toLowerCase();
                    return clientesMap.keySet().stream()
                            .filter(item -> item.toLowerCase().contains(userText))
                            .collect(Collectors.toList());
                });

        // Cuando el usuario selecciona una sugerencia
        autoCompletion.setOnAutoCompleted(event -> {
            String selectedText = event.getCompletion();
            clienteSeleccionado = clientesMap.get(selectedText);
            if (clienteSeleccionado != null) {
                System.out.println("Cliente seleccionado: " + clienteSeleccionado.getNombreCompleto());
            }
        });

        // Listener para detectar cuando el usuario borra o cambia el texto manualmente
        clienteSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                // Verificar si el texto coincide exactamente con un cliente del mapa
                if (clientesMap.containsKey(newVal)) {
                    clienteSeleccionado = clientesMap.get(newVal);
                } else {
                    // El texto no coincide exactamente, puede estar escribiendo
                    // No invalidamos todavía el cliente seleccionado si tenía uno
                }
            } else {
                clienteSeleccionado = null;
            }
        });
    }

    private void setupValidadores() {
        // Validador para el campo de búsqueda de cliente (no vacío)
        validationSupport.registerValidator(clienteSearchField,
                ValidadorCampos.noVacio("Cliente"));

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
        // Validar que se haya seleccionado un cliente válido
        if (clienteSeleccionado == null) {
            // Intentar buscar el cliente por el texto ingresado
            String textoIngresado = clienteSearchField.getText();
            if (clientesMap.containsKey(textoIngresado)) {
                clienteSeleccionado = clientesMap.get(textoIngresado);
            } else {
                showAlert(Alert.AlertType.ERROR, "Cliente no válido",
                        "Por favor, selecciona un cliente válido de las sugerencias.");
                return;
            }
        }

        try {
            Excursion excursion = excursionComboBox.getValue();
            int numPersonas = Integer.parseInt(numPersonasField.getText().trim());
            String estado = estadoComboBox.getValue();

            // Recalcular precio total para asegurar consistencia
            double precioTotal = numPersonas * excursion.getPrecioPorPersona();

            Reserva nuevaReserva = new Reserva(
                    null, // ID autogenerado
                    clienteSeleccionado.getIdUsuario(),
                    clienteSeleccionado.getNombreCompleto(),
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
