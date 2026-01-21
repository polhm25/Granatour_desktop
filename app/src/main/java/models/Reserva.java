package models;

import java.time.LocalDateTime;

public class Reserva {
    private Integer idReserva;
    private Integer idUsuario;
    private String nombreCliente;
    private Integer idExcursion;
    private String nombreExcursion;
    private LocalDateTime fechaReserva;
    private Integer numPersonas;
    private String estado;
    private Double precioTotal;

    // Constructor vacío sin parámetros
    public Reserva() {
    }

    // Constructor que recibe todos los campos incluyendo ID, para cargar datos existentes desde la base de datos
    public Reserva(Integer idReserva, Integer idUsuario, String nombreCliente, Integer idExcursion,
                   String nombreExcursion, LocalDateTime fechaReserva, Integer numPersonas,
                   String estado, Double precioTotal) {
        this.idReserva = idReserva;
        this.idUsuario = idUsuario;
        this.nombreCliente = nombreCliente;
        this.idExcursion = idExcursion;
        this.nombreExcursion = nombreExcursion;
        this.fechaReserva = fechaReserva;
        this.numPersonas = numPersonas;
        this.estado = estado;
        this.precioTotal = precioTotal;
    }

    // Constructor sin ID para crear nuevas reservas que se insertarán en la base de datos
    public Reserva(Integer idUsuario, String nombreCliente, Integer idExcursion,
                   String nombreExcursion, LocalDateTime fechaReserva, Integer numPersonas,
                   String estado, Double precioTotal) {
        this.idUsuario = idUsuario;
        this.nombreCliente = nombreCliente;
        this.idExcursion = idExcursion;
        this.nombreExcursion = nombreExcursion;
        this.fechaReserva = fechaReserva;
        this.numPersonas = numPersonas;
        this.estado = estado;
        this.precioTotal = precioTotal;
    }

    // Métodos GET para obtener valores de los atributos
    public Integer getIdReserva() {
        return idReserva;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public Integer getIdExcursion() {
        return idExcursion;
    }

    public String getNombreExcursion() {
        return nombreExcursion;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public Integer getNumPersonas() {
        return numPersonas;
    }

    public String getEstado() {
        return estado;
    }

    public Double getPrecioTotal() {
        return precioTotal;
    }

    // Métodos SET para modificar valores de los atributos
    public void setIdReserva(Integer idReserva) {
        this.idReserva = idReserva;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public void setIdExcursion(Integer idExcursion) {
        this.idExcursion = idExcursion;
    }

    public void setNombreExcursion(String nombreExcursion) {
        this.nombreExcursion = nombreExcursion;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public void setNumPersonas(Integer numPersonas) {
        this.numPersonas = numPersonas;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setPrecioTotal(Double precioTotal) {
        this.precioTotal = precioTotal;
    }

    @Override
    public String toString() {
        return "Reserva{" +
                "idReserva=" + idReserva +
                ", idUsuario=" + idUsuario +
                ", nombreCliente='" + nombreCliente + '\'' +
                ", idExcursion=" + idExcursion +
                ", nombreExcursion='" + nombreExcursion + '\'' +
                ", fechaReserva=" + fechaReserva +
                ", numPersonas=" + numPersonas +
                ", estado='" + estado + '\'' +
                ", precioTotal=" + precioTotal +
                '}';
    }
}
