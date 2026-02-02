package models;

import java.time.LocalDateTime;

public class Usuario {
    private Integer idUsuario;
    private String nombre;
    private String ap1;
    private String ap2;
    private String dni;
    private String email;
    private String telefono;
    private String rol;
    private String password;
    private Double valoracion;
    private Integer numTurnos;
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimoAcceso;

    // Constructor vacío sin parámetros
    public Usuario() {
    }

    // Constructor que recibe todos los campos incluyendo ID, para cargar datos existentes desde la base de datos
    public Usuario(Integer idUsuario, String nombre, String ap1, String ap2, String dni,
                   String email, String telefono, String rol, String password, Double valoracion,
                   Integer numTurnos, LocalDateTime fechaRegistro, LocalDateTime ultimoAcceso) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.ap1 = ap1;
        this.ap2 = ap2;
        this.dni = dni;
        this.email = email;
        this.telefono = telefono;
        this.rol = rol;
        this.password = password;
        this.valoracion = valoracion;
        this.numTurnos = numTurnos;
        this.fechaRegistro = fechaRegistro;
        this.ultimoAcceso = ultimoAcceso;
    }

    // Constructor sin ID para crear nuevos usuarios que se insertarán en la base de datos
    public Usuario(String nombre, String ap1, String ap2, String dni,
                   String email, String telefono, String rol, String password) {
        this.nombre = nombre;
        this.ap1 = ap1;
        this.ap2 = ap2;
        this.dni = dni;
        this.email = email;
        this.telefono = telefono;
        this.rol = rol;
        this.password = password;
        this.valoracion = null;
        this.numTurnos = null;
        this.fechaRegistro = null;
        this.ultimoAcceso = null;
    }

    // Métodos GET para obtener valores de los atributos
    public Integer getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getAp1() {
        return ap1;
    }

    public String getAp2() {
        return ap2;
    }

    public String getDni() {
        return dni;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getRol() {
        return rol;
    }

    public String getPassword() {
        return password;
    }

    public Double getValoracion() {
        return valoracion;
    }

    public Integer getNumTurnos() {
        return numTurnos;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    // Métodos SET para modificar valores de los atributos
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setAp1(String ap1) {
        this.ap1 = ap1;
    }

    public void setAp2(String ap2) {
        this.ap2 = ap2;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setValoracion(Double valoracion) {
        this.valoracion = valoracion;
    }

    public void setNumTurnos(Integer numTurnos) {
        this.numTurnos = numTurnos;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    // Devuelve el nombre completo concatenando el nombre con los dos apellidos
    public String getNombreCompleto() {
        StringBuilder sb = new StringBuilder();
        if (nombre != null && !nombre.isEmpty()) {
            sb.append(nombre);
        }
        if (ap1 != null && !ap1.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(ap1);
        }
        if (ap2 != null && !ap2.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(ap2);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "idUsuario=" + idUsuario +
                ", nombre='" + nombre + '\'' +
                ", ap1='" + ap1 + '\'' +
                ", ap2='" + ap2 + '\'' +
                ", dni='" + dni + '\'' +
                ", email='" + email + '\'' +
                ", telefono='" + telefono + '\'' +
                ", rol='" + rol + '\'' +
                ", valoracion=" + valoracion +
                ", numTurnos=" + numTurnos +
                ", fechaRegistro=" + fechaRegistro +
                ", ultimoAcceso=" + ultimoAcceso +
                '}';
    }
}
