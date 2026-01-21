package app.granatour.session;

import models.Usuario;

/**
 * Clase Singleton que gestiona la sesión del usuario autenticado.
 * Mantiene información del usuario actual durante toda la ejecución de la aplicación.
 */
public class SessionManager {

    private static SessionManager instance;
    private Usuario usuarioActual;

    // Constructor privado para evitar instanciación externa
    private SessionManager() {
    }

    /**
     * Obtiene la instancia única de SessionManager
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Establece el usuario autenticado actual
     */
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
        System.out.println("✓ Sesión iniciada para usuario: " + usuario.getNombreCompleto() + " (" + usuario.getRol() + ")");
    }

    /**
     * Obtiene el usuario autenticado actual
     */
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Obtiene el ID del usuario actual
     */
    public int getIdUsuario() {
        return usuarioActual != null ? usuarioActual.getIdUsuario() : -1;
    }

    /**
     * Obtiene el nombre completo del usuario actual
     */
    public String getNombreCompleto() {
        return usuarioActual != null ? usuarioActual.getNombreCompleto() : "Usuario desconocido";
    }

    /**
     * Obtiene el rol del usuario actual
     */
    public String getRol() {
        return usuarioActual != null ? usuarioActual.getRol() : "Sin rol";
    }

    /**
     * Obtiene el email del usuario actual
     */
    public String getEmail() {
        return usuarioActual != null ? usuarioActual.getEmail() : "";
    }

    /**
     * Verifica si hay un usuario autenticado
     */
    public boolean isAuthenticated() {
        return usuarioActual != null;
    }

    /**
     * Verifica si el usuario actual es administrador
     */
    public boolean isAdmin() {
        return usuarioActual != null && "admin".equalsIgnoreCase(usuarioActual.getRol());
    }

    /**
     * Verifica si el usuario actual es guía
     */
    public boolean isGuia() {
        return usuarioActual != null && "guia".equalsIgnoreCase(usuarioActual.getRol());
    }

    /**
     * Verifica si el usuario actual es cliente
     */
    public boolean isCliente() {
        return usuarioActual != null && "cliente".equalsIgnoreCase(usuarioActual.getRol());
    }

    /**
     * Cierra la sesión actual
     */
    public void cerrarSesion() {
        if (usuarioActual != null) {
            System.out.println("✓ Sesión cerrada para usuario: " + usuarioActual.getNombreCompleto());
            usuarioActual = null;
        }
    }

    /**
     * Resetea la instancia del SessionManager (útil para testing)
     */
    public static void resetInstance() {
        if (instance != null) {
            instance.cerrarSesion();
            instance = null;
        }
    }
}
