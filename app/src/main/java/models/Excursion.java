package models;

import java.time.LocalDate;

public class Excursion {
    private Integer idExcursion;
    private String nombreRuta;
    private String zona;
    private Double duracionHoras;
    private Double precioPorPersona;
    private LocalDate fechaInicio;
    private Integer plazasDisponibles;
    private Integer idGuia;
    private String nombreGuia;
    private byte[] imagen;
    private String descripcion;

    // Constructor vacío sin parámetros
    public Excursion() {
    }

    // Constructor que recibe todos los campos incluyendo ID, para cargar datos
    // existentes desde la base de datos
    public Excursion(Integer idExcursion, String nombreRuta, String zona, Double duracionHoras,
            Double precioPorPersona, LocalDate fechaInicio, Integer plazasDisponibles,
            Integer idGuia, String nombreGuia, byte[] imagen, String descripcion) {
        this.idExcursion = idExcursion;
        this.nombreRuta = nombreRuta;
        this.zona = zona;
        this.duracionHoras = duracionHoras;
        this.precioPorPersona = precioPorPersona;
        this.fechaInicio = fechaInicio;
        this.plazasDisponibles = plazasDisponibles;
        this.idGuia = idGuia;
        this.nombreGuia = nombreGuia;
        this.imagen = imagen;
        this.descripcion = descripcion;
    }

    // Constructor sin ID para crear nuevas excursiones que se insertarán en la base
    // de datos
    public Excursion(String nombreRuta, String zona, Double duracionHoras,
            Double precioPorPersona, LocalDate fechaInicio, Integer plazasDisponibles,
            Integer idGuia, String nombreGuia, byte[] imagen, String descripcion) {
        this.nombreRuta = nombreRuta;
        this.zona = zona;
        this.duracionHoras = duracionHoras;
        this.precioPorPersona = precioPorPersona;
        this.fechaInicio = fechaInicio;
        this.plazasDisponibles = plazasDisponibles;
        this.idGuia = idGuia;
        this.nombreGuia = nombreGuia;
        this.imagen = imagen;
        this.descripcion = descripcion;
    }

    // Métodos GET para obtener valores de los atributos
    public Integer getIdExcursion() {
        return idExcursion;
    }

    public String getNombreRuta() {
        return nombreRuta;
    }

    public String getZona() {
        return zona;
    }

    public Double getDuracionHoras() {
        return duracionHoras;
    }

    public Double getPrecioPorPersona() {
        return precioPorPersona;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public Integer getPlazasDisponibles() {
        return plazasDisponibles;
    }

    public Integer getIdGuia() {
        return idGuia;
    }

    public String getNombreGuia() {
        return nombreGuia;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public String getDescripcion() {
        return descripcion;
    }

    // Métodos SET para modificar valores de los atributos
    public void setIdExcursion(Integer idExcursion) {
        this.idExcursion = idExcursion;
    }

    public void setNombreRuta(String nombreRuta) {
        this.nombreRuta = nombreRuta;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public void setDuracionHoras(Double duracionHoras) {
        this.duracionHoras = duracionHoras;
    }

    public void setPrecioPorPersona(Double precioPorPersona) {
        this.precioPorPersona = precioPorPersona;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public void setPlazasDisponibles(Integer plazasDisponibles) {
        this.plazasDisponibles = plazasDisponibles;
    }

    public void setIdGuia(Integer idGuia) {
        this.idGuia = idGuia;
    }

    public void setNombreGuia(String nombreGuia) {
        this.nombreGuia = nombreGuia;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "Excursion{" +
                "idExcursion=" + idExcursion +
                ", nombreRuta='" + nombreRuta + '\'' +
                ", zona='" + zona + '\'' +
                ", duracionHoras=" + duracionHoras +
                ", precioPorPersona=" + precioPorPersona +
                ", fechaInicio=" + fechaInicio +
                ", plazasDisponibles=" + plazasDisponibles +
                ", idGuia=" + idGuia +
                ", nombreGuia='" + nombreGuia + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
