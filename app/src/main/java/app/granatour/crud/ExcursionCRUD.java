package app.granatour.crud;

import app.granatour.database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.Excursion;

import java.sql.*;
import java.time.LocalDate;

public class ExcursionCRUD {

    // Obtiene todas las excursiones de la base de datos incluyendo el nombre del
    // guía correspondiente
    public ObservableList<Excursion> getAllExcursiones() {
        ObservableList<Excursion> excursiones = FXCollections.observableArrayList();
        String query = "SELECT e.id_excursion, e.nombre_ruta, e.zona, e.duracion_horas, " +
                "e.precio_persona, e.fecha_inicio, e.plazas_disponibles, e.id_guia, " +
                "CONCAT(COALESCE(u.nombre, ''), ' ', COALESCE(u.ap1, '')) as nombre_guia, " +
                "e.imagen, e.descripcion " +
                "FROM EXCURSIONES e " +
                "LEFT JOIN USUARIOS u ON e.id_guia = u.id_usuario";

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            if (connection == null || connection.isClosed()) {
                System.err.println("✗ Error: Conexión a la base de datos es nula o está cerrada");
                return excursiones;
            }

            try (PreparedStatement pstmt = connection.prepareStatement(query);
                    ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Excursion excursion = mapearExcursion(rs);
                    excursiones.add(excursion);
                }
                System.out.println("✓ Se cargaron " + excursiones.size() + " excursiones de la base de datos");
            }
        } catch (SQLException e) {
            System.err.println("✗ Error al obtener excursiones: " + e.getMessage());
            System.err.println("  Código de error SQL: " + e.getErrorCode());
            System.err.println("  Estado SQL: " + e.getSQLState());
            System.err.println("  La tabla EXCURSIONES podría no existir o estar inaccesible");
            e.printStackTrace();
        }

        return excursiones;
    }

    // Busca excursiones que coincidan con el término de búsqueda en nombre de ruta
    // o zona
    public ObservableList<Excursion> searchExcursiones(String keyword) {
        ObservableList<Excursion> excursiones = FXCollections.observableArrayList();
        String query = "SELECT e.id_excursion, e.nombre_ruta, e.zona, e.duracion_horas, " +
                "e.precio_persona, e.fecha_inicio, e.plazas_disponibles, e.id_guia, " +
                "CONCAT(COALESCE(u.nombre, ''), ' ', COALESCE(u.ap1, '')) as nombre_guia, " +
                "e.imagen, e.descripcion " +
                "FROM EXCURSIONES e " +
                "LEFT JOIN USUARIOS u ON e.id_guia = u.id_usuario " +
                "WHERE e.nombre_ruta LIKE ? OR e.zona LIKE ?";

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                String patron = "%" + keyword + "%";
                pstmt.setString(1, patron);
                pstmt.setString(2, patron);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Excursion excursion = mapearExcursion(rs);
                        excursiones.add(excursion);
                    }
                    System.out.println(
                            "Busqueda encontro " + excursiones.size() + " excursiones con termino: " + keyword);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar excursiones: " + e.getMessage());
            e.printStackTrace();
        }

        return excursiones;
    }

    // Convierte un resultado de la consulta en un objeto Excursion con todos sus
    // datos
    private Excursion mapearExcursion(ResultSet rs) throws SQLException {
        Integer idExcursion = rs.getInt("id_excursion");
        String nombreRuta = rs.getString("nombre_ruta");
        String zona = rs.getString("zona");
        Double duracionHoras = rs.getObject("duracion_horas") != null ? rs.getDouble("duracion_horas") : null;
        Double precioPorPersona = rs.getObject("precio_persona") != null ? rs.getDouble("precio_persona") : null;

        LocalDate fechaInicio = null;
        Date sqlDate = rs.getDate("fecha_inicio");
        if (sqlDate != null) {
            fechaInicio = sqlDate.toLocalDate();
        }

        Integer plazasDisponibles = rs.getObject("plazas_disponibles") != null ? rs.getInt("plazas_disponibles") : null;
        Integer idGuia = rs.getObject("id_guia") != null ? rs.getInt("id_guia") : null;
        String nombreGuia = rs.getString("nombre_guia");
        byte[] imagen = rs.getBytes("imagen");
        String descripcion = rs.getString("descripcion");

        return new Excursion(idExcursion, nombreRuta, zona, duracionHoras, precioPorPersona,
                fechaInicio, plazasDisponibles, idGuia, nombreGuia, imagen, descripcion);
    }

    // Inserta una nueva excursión en la base de datos
    public boolean insertar(Excursion excursion) {
        String query = "INSERT INTO EXCURSIONES (nombre_ruta, zona, duracion_horas, precio_persona, " +
                "fecha_inicio, plazas_disponibles, id_guia, imagen, descripcion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, excursion.getNombreRuta());
                pstmt.setString(2, excursion.getZona());
                pstmt.setDouble(3, excursion.getDuracionHoras());
                pstmt.setDouble(4, excursion.getPrecioPorPersona());
                pstmt.setDate(5, Date.valueOf(excursion.getFechaInicio()));
                pstmt.setInt(6, excursion.getPlazasDisponibles());

                if (excursion.getIdGuia() != null) {
                    pstmt.setInt(7, excursion.getIdGuia());
                } else {
                    pstmt.setNull(7, Types.INTEGER);
                }

                pstmt.setBytes(8, excursion.getImagen());
                pstmt.setString(9, excursion.getDescripcion());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Excursión insertada exitosamente: " + excursion.getNombreRuta());
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar excursión: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Actualiza los datos de una excursión existente
    public boolean actualizar(Excursion excursion) {
        String query = "UPDATE EXCURSIONES SET nombre_ruta=?, zona=?, duracion_horas=?, precio_persona=?, " +
                "fecha_inicio=?, plazas_disponibles=?, id_guia=?, imagen=?, descripcion=? WHERE id_excursion=?";

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, excursion.getNombreRuta());
                pstmt.setString(2, excursion.getZona());
                pstmt.setDouble(3, excursion.getDuracionHoras());
                pstmt.setDouble(4, excursion.getPrecioPorPersona());
                pstmt.setDate(5, Date.valueOf(excursion.getFechaInicio()));
                pstmt.setInt(6, excursion.getPlazasDisponibles());

                if (excursion.getIdGuia() != null) {
                    pstmt.setInt(7, excursion.getIdGuia());
                } else {
                    pstmt.setNull(7, Types.INTEGER);
                }

                pstmt.setBytes(8, excursion.getImagen());
                pstmt.setString(9, excursion.getDescripcion());
                pstmt.setInt(10, excursion.getIdExcursion());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Excursión actualizada exitosamente: " + excursion.getNombreRuta());
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar excursión: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Elimina una excursión de la base de datos por su ID
    public boolean eliminar(int idExcursion) {
        String query = "DELETE FROM EXCURSIONES WHERE id_excursion = ?";

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setInt(1, idExcursion);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Excursión eliminada exitosamente con ID: " + idExcursion);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar excursión: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}
