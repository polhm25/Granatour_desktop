package app.granatour.crud;

import app.granatour.database.DatabaseConnection;
import javafx.collections.ObservableList;
import models.Reserva;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@DisplayName("Tests de integración — ReservaCRUD")
class ReservaCRUDTest {

    private ReservaCRUD reservaCRUD;

    @BeforeAll
    static void verificarConexion() {
        assumeTrue(
            DatabaseConnection.getInstance().isConnected(),
            "Sin conexión a BD — tests de integración omitidos"
        );
    }

    @BeforeEach
    void setUp() {
        reservaCRUD = new ReservaCRUD();
    }

    @Test
    @DisplayName("getAllReservas devuelve lista no nula")
    void testGetAllReservasNoNulo() {
        ObservableList<Reserva> reservas = reservaCRUD.getAllReservas();
        assertNotNull(reservas, "La lista de reservas no debe ser null");
    }

    @Test
    @DisplayName("getAllReservas devuelve tamaño no negativo")
    void testGetAllReservasSize() {
        ObservableList<Reserva> reservas = reservaCRUD.getAllReservas();
        assertTrue(reservas.size() >= 0);
    }

    @Test
    @DisplayName("searchReservas con 'confirmada' devuelve lista no nula")
    void testSearchReservasConfirmada() {
        ObservableList<Reserva> resultado = reservaCRUD.searchReservas("confirmada");
        assertNotNull(resultado);
    }

    @Test
    @DisplayName("searchReservas con cadena vacía no lanza excepción")
    void testSearchReservasVacio() {
        assertDoesNotThrow(() -> {
            ObservableList<Reserva> resultado = reservaCRUD.searchReservas("");
            assertNotNull(resultado);
        });
    }

    @Test
    @DisplayName("searchReservas con término inexistente devuelve lista vacía")
    void testSearchReservasTerminoInexistente() {
        ObservableList<Reserva> resultado = reservaCRUD.searchReservas("EstadoQueNoExiste_xyz");
        assertNotNull(resultado);
        assertEquals(0, resultado.size());
    }

    @Test
    @DisplayName("getReservasPorUsuario con id=2 devuelve lista no nula")
    void testGetReservasPorUsuario() {
        ObservableList<Reserva> reservas = reservaCRUD.getReservasPorUsuario(2);
        assertNotNull(reservas, "La lista de reservas del usuario no debe ser null");
    }

    @Test
    @DisplayName("getReservasPorUsuario con id inexistente devuelve lista vacía")
    void testGetReservasPorUsuarioInexistente() {
        ObservableList<Reserva> reservas = reservaCRUD.getReservasPorUsuario(-999);
        assertNotNull(reservas);
        assertEquals(0, reservas.size(), "Un usuario inexistente no debe tener reservas");
    }

    @Test
    @DisplayName("actualizar reserva inexistente devuelve false sin excepción")
    void testActualizarInexistente() {
        assertDoesNotThrow(() -> {
            Reserva fantasma = new Reserva();
            fantasma.setIdReserva(-999);
            fantasma.setIdUsuario(1);
            fantasma.setIdExcursion(1);
            fantasma.setNumPersonas(1);
            fantasma.setEstado("confirmada");
            fantasma.setPrecioTotal(0.0);
            boolean resultado = reservaCRUD.actualizar(fantasma);
            assertFalse(resultado, "Actualizar reserva inexistente debe devolver false");
        });
    }

    @Test
    @DisplayName("eliminar con id inexistente devuelve false sin excepción")
    void testEliminarInexistente() {
        assertDoesNotThrow(() -> {
            boolean resultado = reservaCRUD.eliminar(-999);
            assertFalse(resultado, "Eliminar id inexistente debe devolver false");
        });
    }

    @Test
    @DisplayName("reservas de getAllReservas tienen campos esenciales no nulos")
    void testReservasConCamposNoNulos() {
        ObservableList<Reserva> reservas = reservaCRUD.getAllReservas();
        for (Reserva r : reservas) {
            assertNotNull(r.getEstado(), "El estado de la reserva no debe ser null");
            assertTrue(r.getNumPersonas() > 0, "Num personas debe ser mayor que 0");
            assertTrue(r.getPrecioTotal() >= 0, "Precio total debe ser >= 0");
        }
    }
}
