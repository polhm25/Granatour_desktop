package app.granatour.crud;

import app.granatour.database.DatabaseConnection;
import models.Usuario;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UsuarioCRUD {

    // Inserta un nuevo usuario en la base de datos y devuelve true si fue exitoso
    public boolean insertar(Usuario usuario) {
        String query = "INSERT INTO USUARIOS (nombre, ap1, ap2, dni, email, telefono, rol, password) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, usuario.getNombre());
                pstmt.setString(2, usuario.getAp1());
                pstmt.setString(3, usuario.getAp2());
                pstmt.setString(4, usuario.getDni());
                pstmt.setString(5, usuario.getEmail());
                pstmt.setString(6, usuario.getTelefono());
                pstmt.setString(7, usuario.getRol());
                pstmt.setString(8, usuario.getPassword());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Usuario insertado exitosamente: " + usuario.getNombreCompleto());
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar usuario: " + e.getMessage());
            if (e.getMessage().contains("Duplicate entry")) {
                if (e.getMessage().contains(usuario.getDni())) {
                    System.err.println("   DNI ya existe: " + usuario.getDni());
                } else if (e.getMessage().contains(usuario.getEmail())) {
                    System.err.println("   Email ya existe: " + usuario.getEmail());
                }
            }
            e.printStackTrace();
        }

        return false;
    }

    // Obtiene todos los usuarios almacenados en la base de datos
    public List<Usuario> obtenerTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        String query = "SELECT id_usuario, nombre, ap1, ap2, dni, email, telefono, rol, password, " +
                "valoracion, num_turnos, fecha_registro, ultimo_acceso FROM USUARIOS";

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            if (connection == null || connection.isClosed()) {
                System.err.println("✗ Error: Conexión a la base de datos es nula o está cerrada");
                return usuarios;
            }

            try (PreparedStatement pstmt = connection.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Usuario usuario = mapearUsuario(rs);
                    usuarios.add(usuario);
                }
                System.out.println("Se cargaron " + usuarios.size() + " usuarios de la base de datos");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuarios: " + e.getMessage());
            System.err.println("  Código de error SQL: " + e.getErrorCode());
            System.err.println("  Estado SQL: " + e.getSQLState());
            System.err.println("La tabla USUARIOS podría no existir o estar inaccesible");
            e.printStackTrace();
        }

        return usuarios;
    }

    // Obtiene un usuario específico buscándolo por su ID, devuelve null si no existe
    public Usuario obtenerPorId(int idUsuario) {
        String query = "SELECT id_usuario, nombre, ap1, ap2, dni, email, telefono, rol, password, " +
                "valoracion, num_turnos, fecha_registro, ultimo_acceso FROM USUARIOS WHERE id_usuario = ?";

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setInt(1, idUsuario);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearUsuario(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario por ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Obtiene un usuario buscándolo por DNI, devuelve null si no existe
    public Usuario obtenerPorDni(String dni) {
        String query = "SELECT id_usuario, nombre, ap1, ap2, dni, email, telefono, rol, password, " +
                "valoracion, num_turnos, fecha_registro, ultimo_acceso FROM USUARIOS WHERE dni = ?";

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, dni);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearUsuario(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario por DNI: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Obtiene un usuario buscándolo por email, devuelve null si no existe
    public Usuario obtenerPorEmail(String email) {
        String query = "SELECT id_usuario, nombre, ap1, ap2, dni, email, telefono, rol, password, " +
                "valoracion, num_turnos, fecha_registro, ultimo_acceso FROM USUARIOS WHERE email = ?";

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, email);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearUsuario(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario por email: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Busca usuarios que coincidan parcialmente con el término de búsqueda en nombre o apellidos
    public List<Usuario> buscarPorNombre(String termino) {
        List<Usuario> usuarios = new ArrayList<>();
        String query = "SELECT id_usuario, nombre, ap1, ap2, dni, email, telefono, rol, password, " +
                "valoracion, num_turnos, fecha_registro, ultimo_acceso FROM USUARIOS " +
                "WHERE nombre LIKE ? OR ap1 LIKE ? OR ap2 LIKE ?";

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                String patron = "%" + termino + "%";
                pstmt.setString(1, patron);
                pstmt.setString(2, patron);
                pstmt.setString(3, patron);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Usuario usuario = mapearUsuario(rs);
                        usuarios.add(usuario);
                    }
                    System.out.println("Busqueda encontro " + usuarios.size() + " usuarios con termino: " + termino);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuarios por nombre: " + e.getMessage());
            e.printStackTrace();
        }

        return usuarios;
    }

    // Actualiza los datos de un usuario existente en la base de datos
    public boolean actualizar(Usuario usuario) {
        String query = "UPDATE USUARIOS SET nombre=?, ap1=?, ap2=?, dni=?, email=?, telefono=?, " +
                "rol=?, password=?, valoracion=?, num_turnos=?, ultimo_acceso=? WHERE id_usuario=?";

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, usuario.getNombre());
                pstmt.setString(2, usuario.getAp1());
                pstmt.setString(3, usuario.getAp2());
                pstmt.setString(4, usuario.getDni());
                pstmt.setString(5, usuario.getEmail());
                pstmt.setString(6, usuario.getTelefono());
                pstmt.setString(7, usuario.getRol());
                pstmt.setString(8, usuario.getPassword());
                pstmt.setObject(9, usuario.getValoracion());
                pstmt.setObject(10, usuario.getNumTurnos());
                pstmt.setObject(11, usuario.getUltimoAcceso() != null ?
                        Timestamp.valueOf(usuario.getUltimoAcceso()) : null);
                pstmt.setInt(12, usuario.getIdUsuario());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Usuario actualizado exitosamente: " + usuario.getNombreCompleto());
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Elimina un usuario de la base de datos por su ID
    public boolean eliminar(int idUsuario) {
        String query = "DELETE FROM USUARIOS WHERE id_usuario = ?";

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setInt(1, idUsuario);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Usuario eliminado exitosamente con ID: " + idUsuario);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Convierte un resultado de la consulta en un objeto Usuario con todos sus datos
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Integer idUsuario = rs.getInt("id_usuario");
        String nombre = rs.getString("nombre");
        String ap1 = rs.getString("ap1");
        String ap2 = rs.getString("ap2");
        String dni = rs.getString("dni");
        String email = rs.getString("email");
        String telefono = rs.getString("telefono");
        String rol = rs.getString("rol");
        String password = rs.getString("password");
        Double valoracion = rs.getObject("valoracion") != null ? rs.getDouble("valoracion") : null;
        Integer numTurnos = rs.getObject("num_turnos") != null ? rs.getInt("num_turnos") : null;

        LocalDateTime fechaRegistro = null;
        Timestamp tsRegistro = rs.getTimestamp("fecha_registro");
        if (tsRegistro != null) {
            fechaRegistro = tsRegistro.toLocalDateTime();
        }

        LocalDateTime ultimoAcceso = null;
        Timestamp tsAcceso = rs.getTimestamp("ultimo_acceso");
        if (tsAcceso != null) {
            ultimoAcceso = tsAcceso.toLocalDateTime();
        }

        return new Usuario(idUsuario, nombre, ap1, ap2, dni, email, telefono, rol, password,
                valoracion, numTurnos, fechaRegistro, ultimoAcceso);
    }
}
