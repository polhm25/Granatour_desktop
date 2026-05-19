package app.granatour.crud;

import app.granatour.database.DatabaseConnection;
import javafx.collections.ObservableList;
import models.Excursion;
import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@DisplayName("Tests de integración — ExcursionCRUD")
class ExcursionCRUDTest {

    private ExcursionCRUD excursionCRUD;

    @BeforeAll
    static void verificarConexion() {
        assumeTrue(
            DatabaseConnection.getInstance().isConnected(),
            "Sin conexión a BD — tests de integración omitidos"
        );
    }

    @BeforeEach
    void setUp() {
        excursionCRUD = new ExcursionCRUD();
    }

    @Test
    @DisplayName("getAllExcursiones devuelve lista no nula")
    void testGetAllExcursionesNoNulo() {
        ObservableList<Excursion> excursiones = excursionCRUD.getAllExcursiones();
        assertNotNull(excursiones, "La lista de excursiones no debe ser null");
    }

    @Test
    @DisplayName("getAllExcursiones devuelve tamaño no negativo")
    void testGetAllExcursionesSize() {
        ObservableList<Excursion> excursiones = excursionCRUD.getAllExcursiones();
        assertTrue(excursiones.size() >= 0);
    }

    @Test
    @DisplayName("searchExcursiones con término 'Sierra' devuelve lista no nula")
    void testSearchExcursionesConResultados() {
        ObservableList<Excursion> resultado = excursionCRUD.searchExcursiones("Sierra");
        assertNotNull(resultado);
    }

    @Test
    @DisplayName("searchExcursiones con cadena vacía no lanza excepción")
    void testSearchExcursionesVacio() {
        assertDoesNotThrow(() -> {
            ObservableList<Excursion> resultado = excursionCRUD.searchExcursiones("");
            assertNotNull(resultado);
        });
    }

    @Test
    @DisplayName("searchExcursiones con término inexistente devuelve lista vacía")
    void testSearchExcursionesTerminoInexistente() {
        ObservableList<Excursion> resultado = excursionCRUD.searchExcursiones("xXxTerminoQueNoExisteXxX");
        assertNotNull(resultado);
        assertEquals(0, resultado.size(), "No debe haber resultados para un término inexistente");
    }

    @Test
    @DisplayName("insertar excursión nueva y luego eliminarla")
    void testInsertarYEliminar() {
        Excursion nueva = new Excursion(
                "Ruta Test JUnit",
                "Sierra Nevada Test",
                4.0,
                30.0,
                LocalDate.of(2026, 12, 1),
                10,
                null,
                null,
                null,
                "Excursión creada por test JUnit"
        );

        boolean insertado = excursionCRUD.insertar(nueva);
        assertTrue(insertado, "La excursión de prueba debe insertarse correctamente");

        // Limpieza: buscar por nombre y eliminar
        ObservableList<Excursion> todas = excursionCRUD.getAllExcursiones();
        todas.stream()
                .filter(e -> "Ruta Test JUnit".equals(e.getNombreRuta()))
                .findFirst()
                .ifPresent(e -> excursionCRUD.eliminar(e.getIdExcursion()));
    }

    @Test
    @DisplayName("eliminar excursión con id inexistente devuelve false sin excepción")
    void testEliminarInexistente() {
        assertDoesNotThrow(() -> {
            boolean resultado = excursionCRUD.eliminar(-999);
            assertFalse(resultado, "Eliminar id inexistente debe devolver false");
        });
    }

    @Test
    @DisplayName("getAllExcursiones devuelve excursiones con campos nombre_ruta no nulos")
    void testExcursionesConNombreNoNulo() {
        ObservableList<Excursion> excursiones = excursionCRUD.getAllExcursiones();
        for (Excursion e : excursiones) {
            assertNotNull(e.getNombreRuta(), "El nombre de ruta no debe ser null");
            assertNotNull(e.getZona(), "La zona no debe ser null");
        }
    }
}
