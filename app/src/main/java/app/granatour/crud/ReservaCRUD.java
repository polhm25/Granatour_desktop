package app.granatour.crud;

import app.granatour.database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.Reserva;

import java.sql.*;
import java.time.LocalDateTime;

public class ReservaCRUD {

    // Obtiene todas las reservas de la base de datos incluyendo nombres de cliente
    // y excursión correspondientes
    public ObservableList<Reserva> getAllReservas() {
        ObservableList<Reserva> reservas = FXCollections.observableArrayList();
        String query = "SELECT r.id_reserva, r.id_usuario, " +
                "CONCAT(COALESCE(u.nombre, ''), ' ', COALESCE(u.ap1, '')) as nombre_cliente, " +
                "r.id_excursion, e.nombre_ruta, r.fecha_reserva, r.num_personas, r.estado, r.precio_total " +
                "FROM RESERVAS r " +
                "LEFT JOIN USUARIOS u ON r.id_usuario = u.id_usuario " +
                "LEFT JOIN EXCURSIONES e ON r.id_excursion = e.id_excursion";

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            if (connection == null || connection.isClosed()) {
                System.err.println("✗ Error: Conexión a la base de datos es nula o está cerrada");
                return reservas;
            }

            try (PreparedStatement pstmt = connection.prepareStatement(query);
                    ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Reserva reserva = mapearReserva(rs);
                    reservas.add(reserva);
                }
                System.out.println("✓ Se cargaron " + reservas.size() + " reservas de la base de datos");
            }
        } catch (SQLException e) {
            System.err.println("✗ Error al obtener reservas: " + e.getMessage());
            System.err.println("  Código de error SQL: " + e.getErrorCode());
            System.err.println("  Estado SQL: " + e.getSQLState());
            System.err.println("  La tabla RESERVAS podría no existir o estar inaccesible");
            e.printStackTrace();
        }

        return reservas;
    }

    // Busca reservas que coincidan con el término de búsqueda en nombre de cliente,
    // excursión o estado
    public ObservableList<Reserva> searchReservas(String keyword) {
        ObservableList<Reserva> reservas = FXCollections.observableArrayList();
        String query = "SELECT r.id_reserva, r.id_usuario, " +
                "CONCAT(COALESCE(u.nombre, ''), ' ', COALESCE(u.ap1, '')) as nombre_cliente, " +
                "r.id_excursion, e.nombre_ruta, r.fecha_reserva, r.num_personas, r.estado, r.precio_total " +
                "FROM RESERVAS r " +
                "LEFT JOIN USUARIOS u ON r.id_usuario = u.id_usuario " +
                "LEFT JOIN EXCURSIONES e ON r.id_excursion = e.id_excursion " +
                "WHERE CONCAT(COALESCE(u.nombre, ''), ' ', COALESCE(u.ap1, '')) LIKE ? " +
                "OR e.nombre_ruta LIKE ? OR r.estado LIKE ?";

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                String patron = "%" + keyword + "%";
                pstmt.setString(1, patron);
                pstmt.setString(2, patron);
                pstmt.setString(3, patron);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Reserva reserva = mapearReserva(rs);
                        reservas.add(reserva);
                    }
                    System.out.println("Busqueda encontro " + reservas.size() + " reservas con termino: " + keyword);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar reservas: " + e.getMessage());
            e.printStackTrace();
        }

        return reservas;
    }

    // Convierte un resultado de la consulta en un objeto Reserva con todos sus
    // datos
    private Reserva mapearReserva(ResultSet rs) throws SQLException {
        Integer idReserva = rs.getInt("id_reserva");
        Integer idUsuario = rs.getObject("id_usuario") != null ? rs.getInt("id_usuario") : null;
        String nombreCliente = rs.getString("nombre_cliente");
        Integer idExcursion = rs.getObject("id_excursion") != null ? rs.getInt("id_excursion") : null;
        String nombreExcursion = rs.getString("nombre_ruta");

        LocalDateTime fechaReserva = null;
        Timestamp tsReserva = rs.getTimestamp("fecha_reserva");
        if (tsReserva != null) {
            fechaReserva = tsReserva.toLocalDateTime();
        }

        Integer numPersonas = rs.getObject("num_personas") != null ? rs.getInt("num_personas") : null;
        String estado = rs.getString("estado");
        Double precioTotal = rs.getObject("precio_total") != null ? rs.getDouble("precio_total") : null;

        return new Reserva(idReserva, idUsuario, nombreCliente, idExcursion, nombreExcursion,
                fechaReserva, numPersonas, estado, precioTotal);
    }

    // Inserta una nueva reserva en la base de datos
    public boolean insertar(Reserva reserva) {
        String query = "INSERT INTO RESERVAS (id_usuario, id_excursion, fecha_reserva, num_personas, estado, precio_total) "
                +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setInt(1, reserva.getIdUsuario());
                pstmt.setInt(2, reserva.getIdExcursion());

                // Si la fecha de reserva es null, usamos la fecha actual (que ya es el default
                // en DB, pero lo manejamos aquí)
                if (reserva.getFechaReserva() != null) {
                    pstmt.setTimestamp(3, Timestamp.valueOf(reserva.getFechaReserva()));
                } else {
                    pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                }

                pstmt.setInt(4, reserva.getNumPersonas());
                pstmt.setString(5, reserva.getEstado());
                pstmt.setDouble(6, reserva.getPrecioTotal());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Reserva insertada exitosamente para usuario ID: " + reserva.getIdUsuario());
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar reserva: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Actualiza los datos de una reserva existente
    public boolean actualizar(Reserva reserva) {
        String query = "UPDATE RESERVAS SET id_usuario=?, id_excursion=?, fecha_reserva=?, num_personas=?, " +
                "estado=?, precio_total=? WHERE id_reserva=?";

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setInt(1, reserva.getIdUsuario());
                pstmt.setInt(2, reserva.getIdExcursion());
                pstmt.setTimestamp(3, Timestamp.valueOf(reserva.getFechaReserva()));
                pstmt.setInt(4, reserva.getNumPersonas());
                pstmt.setString(5, reserva.getEstado());
                pstmt.setDouble(6, reserva.getPrecioTotal());
                pstmt.setInt(7, reserva.getIdReserva());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Reserva actualizada exitosamente ID: " + reserva.getIdReserva());
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar reserva: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Elimina una reserva de la base de datos por su ID
    public boolean eliminar(int idReserva) {
        String query = "DELETE FROM RESERVAS WHERE id_reserva = ?";

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setInt(1, idReserva);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Reserva eliminada exitosamente con ID: " + idReserva);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar reserva: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}
