package app.utils;

import org.controlsfx.validation.Validator;

/*
  Clase utilitaria que proporciona validadores reutilizables para formularios.
  Utiliza ControlsFX para validación en tiempo real.
 */
public class ValidadorCampos {

    /*
     * Valida que un campo de texto no esté vacío
     * Validator para campos no vacíos
     */
    public static Validator<String> noVacio(String nombreCampo) {
        return Validator.createPredicateValidator(
                texto -> texto != null && !texto.trim().isEmpty(),
                nombreCampo + " no puede estar vacío");
    }

    /*
     * Valida que un campo sea un número entero positivo
     * Validator para números enteros positivos
     */
    public static Validator<String> numeroEnteroPositivo(String nombreCampo) {
        return Validator.createPredicateValidator(
                texto -> {
                    if (texto == null || texto.trim().isEmpty())
                        return true;
                    try {
                        int num = Integer.parseInt(texto.trim());
                        return num > 0;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                },
                nombreCampo + " debe ser un número entero positivo");
    }

    /*
     * Valida que un campo sea un número decimal positivo
     * Validator para números decimales positivos
     */
    public static Validator<String> numeroDecimalPositivo(String nombreCampo) {
        return Validator.createPredicateValidator(
                texto -> {
                    if (texto == null || texto.trim().isEmpty())
                        return true;
                    try {
                        double num = Double.parseDouble(texto.trim());
                        return num > 0;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                },
                nombreCampo + " debe ser un número decimal positivo");
    }

    /*
     * Valida que un campo esté en un rango numérico específico
     * Validator para rango numérico
     */
    public static Validator<String> rangoNumerico(String nombreCampo, int minimo, int maximo) {
        return Validator.createPredicateValidator(
                texto -> {
                    if (texto == null || texto.trim().isEmpty())
                        return true;
                    try {
                        int num = Integer.parseInt(texto.trim());
                        return num >= minimo && num <= maximo;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                },
                nombreCampo + " debe estar entre " + minimo + " y " + maximo);
    }

    /*
     * Valida que un campo tenga una longitud mínima
     * Validator para longitud mínima
     */
    public static Validator<String> longitudMinima(String nombreCampo, int longitudMinima) {
        return Validator.createPredicateValidator(
                texto -> texto != null && texto.trim().length() >= longitudMinima,
                nombreCampo + " debe tener al menos " + longitudMinima + " caracteres");
    }

    /*
     * Valida formato de email
     * Validator para emails
     */
    public static Validator<String> email() {
        return Validator.createPredicateValidator(
                texto -> {
                    if (texto == null || texto.trim().isEmpty())
                        return true;
                    String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
                    return texto.matches(regex);
                },
                "Formato de email inválido");
    }

    /*
     * Valida formato de teléfono (9 dígitos)
     * Validator para teléfono
     */
    public static Validator<String> telefono() {
        return Validator.createPredicateValidator(
                texto -> {
                    if (texto == null || texto.trim().isEmpty())
                        return true;
                    return texto.trim().matches("^[0-9]{9}$");
                },
                "El teléfono debe contener 9 dígitos");
    }

    /*
     * Valida que un ComboBox no tenga selección nula
     * Validator para ComboBox
     */
    public static <T> Validator<T> comboBoxNoNulo(String nombreCampo) {
        return Validator.createPredicateValidator(
                valor -> valor != null,
                nombreCampo + " debe tener una opción seleccionada");
    }

    /*
     * Valida que una fecha no sea nula
     * Validator para DatePicker
     */
    public static <T> Validator<T> datePickerNoNulo(String nombreCampo) {
        return Validator.createPredicateValidator(
                valor -> valor != null,
                nombreCampo + " debe tener una fecha seleccionada");
    }

    /*
     * Valida formato de DNI español (8 dígitos + 1 letra)
     * Validator para DNI
     */
    public static Validator<String> dni() {
        return Validator.createPredicateValidator(
                texto -> {
                    if (texto == null || texto.trim().isEmpty())
                        return false;
                    return texto.trim().matches("^[0-9]{8}[A-Za-z]$");
                },
                "DNI debe tener 8 dígitos y una letra");
    }

    /*
     * Valida que una contraseña tenga longitud mínima
     * Validator para contraseñas
     */
    public static Validator<String> contrasenaMinima(int longitudMinima) {
        return Validator.createPredicateValidator(
                texto -> texto != null && !texto.isEmpty() && texto.length() >= longitudMinima,
                "Contraseña debe tener al menos " + longitudMinima + " caracteres");
    }
}
