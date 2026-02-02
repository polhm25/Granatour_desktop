package app.controllers;

import app.granatour.crud.ExcursionCRUD;
import app.granatour.crud.ReservaCRUD;
import app.granatour.crud.UsuarioCRUD;
import app.utils.AlertUtils;
import app.utils.AnimacionUtils;
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

import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class EditarReservaController implements Initializable {

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
    private Integer idReserva;
    private LocalDateTime fechaReservaOriginal;

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
        setupComboBoxes();
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
                }
            } else {
                clienteSeleccionado = null;
            }
        });
    }

    private void setupListeners() {
        excursionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                precioPorPersona = newVal.getPrecioPorPersona();
                calcularPrecioTotal();
            }
        });

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

    public void setReservaData(Reserva reserva) {
        this.idReserva = reserva.getIdReserva();
        this.fechaReservaOriginal = reserva.getFechaReserva();

        numPersonasField.setText(String.valueOf(reserva.getNumPersonas()));
        estadoComboBox.setValue(reserva.getEstado());
        precioTotalLabel.setText(String.format("%.2f €", reserva.getPrecioTotal()));

        // Seleccionar cliente - buscar en el mapa y establecer en el campo de búsqueda
        for (Usuario u : todosLosClientes) {
            if (u.getIdUsuario().equals(reserva.getIdUsuario())) {
                clienteSeleccionado = u;
                String displayText = u.getNombreCompleto() + " - " + u.getDni();
                clienteSearchField.setText(displayText);
                break;
            }
        }

        // Seleccionar excursión
        for (Excursion e : excursionComboBox.getItems()) {
            if (e.getIdExcursion().equals(reserva.getIdExcursion())) {
                excursionComboBox.setValue(e);
                precioPorPersona = e.getPrecioPorPersona(); // Set base price
                break;
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

        // Validar que se haya seleccionado un cliente válido
        if (clienteSeleccionado == null) {
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
            double precioTotal = numPersonas * excursion.getPrecioPorPersona();

            Reserva reservaActualizada = new Reserva(
                    idReserva,
                    clienteSeleccionado.getIdUsuario(),
                    clienteSeleccionado.getNombreCompleto(),
                    excursion.getIdExcursion(),
                    excursion.getNombreRuta(),
                    fechaReservaOriginal, // Mantener fecha original
                    numPersonas,
                    estado,
                    precioTotal);

            boolean success = reservaCRUD.actualizar(reservaActualizada);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Reserva actualizada correctamente");
                closeModal();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo actualizar la reserva.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error de formato", "Por favor, verifica los campos numéricos.");
        }
    }

    private void handleCancelar() {
        closeModal();
    }

    private boolean validateFields() {
        if (clienteSearchField.getText().trim().isEmpty() || excursionComboBox.getValue() == null ||
                numPersonasField.getText().trim().isEmpty() || estadoComboBox.getValue() == null) {

            showAlert(Alert.AlertType.ERROR, "Campos vacíos", "Por favor, completa todos los campos obligatorios.");
            return false;
        }

        try {
            int num = Integer.parseInt(numPersonasField.getText().trim());
            if (num <= 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "El número de personas debe ser mayor a 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "El número de personas debe ser un número entero.");
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
