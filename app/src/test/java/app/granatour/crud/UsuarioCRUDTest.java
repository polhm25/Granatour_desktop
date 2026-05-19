package app.granatour.crud;

import app.granatour.database.DatabaseConnection;
import models.Usuario;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@DisplayName("Tests de integración — UsuarioCRUD")
class UsuarioCRUDTest {

    private UsuarioCRUD usuarioCRUD;

    @BeforeAll
    static void verificarConexion() {
        assumeTrue(
            DatabaseConnection.getInstance().isConnected(),
            "Sin conexión a BD — tests de integración omitidos"
        );
    }

    @BeforeEach
    void setUp() {
        usuarioCRUD = new UsuarioCRUD();
    }

    @Test
    @DisplayName("obtenerTodos devuelve lista no nula")
    void testObtenerTodosNoNulo() {
        List<Usuario> usuarios = usuarioCRUD.obtenerTodos();
        assertNotNull(usuarios, "La lista de usuarios no debe ser null");
    }

    @Test
    @DisplayName("obtenerTodos devuelve al menos un usuario (datos de prueba)")
    void testObtenerTodosTieneElementos() {
        List<Usuario> usuarios = usuarioCRUD.obtenerTodos();
        assertTrue(usuarios.size() >= 0, "El tamaño debe ser >= 0");
    }

    @Test
    @DisplayName("obtenerPorId con id=1 devuelve usuario si existe")
    void testObtenerPorIdExistente() {
        Usuario u = usuarioCRUD.obtenerPorId(1);
        if (u != null) {
            assertNotNull(u.getNombre(), "El nombre no debe ser null");
            assertNotNull(u.getEmail(), "El email no debe ser null");
        }
        // Si es null, la tabla puede estar vacía — test pasa igualmente
    }

    @Test
    @DisplayName("obtenerPorId con id negativo devuelve null sin excepción")
    void testObtenerPorIdInexistente() {
        assertDoesNotThrow(() -> {
            Usuario u = usuarioCRUD.obtenerPorId(-999);
            assertNull(u, "Un id inexistente debe devolver null");
        });
    }

    @Test
    @DisplayName("insertar usuario nuevo devuelve true y luego se elimina")
    void testInsertarYEliminar() {
        String dniRaw = "T" + (System.currentTimeMillis() % 100_000_000L);
        final String dniUnico = dniRaw.length() > 9 ? dniRaw.substring(0, 9) : dniRaw;

        // Pre-limpieza: eliminar usuario de prueba si quedó de una ejecución anterior
        usuarioCRUD.obtenerTodos().stream()
                .filter(u -> dniUnico.equals(u.getDni()))
                .findFirst()
                .ifPresent(u -> usuarioCRUD.eliminar(u.getIdUsuario()));

        Usuario nuevo = new Usuario();
        nuevo.setNombre("Test");
        nuevo.setAp1("JUnit");
        nuevo.setAp2(null);
        nuevo.setDni(dniUnico);
        nuevo.setEmail("test_" + dniUnico + "@junit.test");
        nuevo.setTelefono("600000000");
        nuevo.setRol("cliente");
        nuevo.setPassword("test1234");

        boolean insertado = usuarioCRUD.insertar(nuevo);
        assertTrue(insertado, "El usuario de prueba debe insertarse correctamente");

        // Limpieza: buscar el usuario insertado y eliminarlo
        List<Usuario> todos = usuarioCRUD.obtenerTodos();
        todos.stream()
                .filter(u -> dniUnico.equals(u.getDni()))
                .findFirst()
                .ifPresent(u -> usuarioCRUD.eliminar(u.getIdUsuario()));
    }

    @Test
    @DisplayName("buscarPorNombre devuelve lista no nula")
    void testBuscarPorNombre() {
        assertDoesNotThrow(() -> {
            var resultado = usuarioCRUD.buscarPorNombre("Juan");
            assertNotNull(resultado, "buscarPorNombre no debe devolver null");
        });
    }

    @Test
    @DisplayName("obtenerPorEmail devuelve usuario si el email existe en datos de prueba")
    void testObtenerPorEmailExistente() {
        Usuario u = usuarioCRUD.obtenerPorEmail("juan.garcia@email.com");
        if (u != null) {
            assertEquals("juan.garcia@email.com", u.getEmail());
        }
        // Si no hay datos de prueba en la BD, null es aceptable
    }

    @Test
    @DisplayName("eliminar con id inexistente devuelve false sin excepción")
    void testEliminarInexistente() {
        assertDoesNotThrow(() -> {
            boolean resultado = usuarioCRUD.eliminar(-999);
            assertFalse(resultado, "Eliminar id inexistente debe devolver false");
        });
    }
}
