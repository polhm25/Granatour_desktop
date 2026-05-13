---
name: java-backend-expert
description: Especialista en el backend Java de GranaTour. Usa este agente para modificar o crear operaciones CRUD, lógica de negocio, consultas SQL, manejo de sesión, validaciones o conexión a base de datos. También para cambios en los modelos de datos o nuevas funcionalidades de negocio.
tools:
  - Read
  - Edit
  - Write
  - Bash
---

# Agente: Experto Backend Java — GranaTour

## Contexto del proyecto

Backend Java 21 para gestión de turismo. Base de datos **PostgreSQL** alojada en **Supabase**.
Patrón arquitectónico: **MVC** con controladores JavaFX, CRUDs independientes y modelos POJO.

## Estructura de paquetes backend

```
app/src/main/java/
├── app/granatour/
│   ├── Main.java                          ← Entry point (JavaFX Application)
│   ├── config/
│   │   └── DatabaseConfig.java            ← Carga database.properties
│   ├── crud/
│   │   ├── ExcursionCRUD.java             ← CRUD de excursiones
│   │   ├── ReservaCRUD.java               ← CRUD de reservas
│   │   └── UsuarioCRUD.java               ← CRUD de usuarios + autenticación
│   ├── database/
│   │   └── DatabaseConnection.java        ← Singleton de conexión JDBC
│   ├── reports/
│   │   ├── ReportGenerator.java           ← Generador JasperReports
│   │   └── ReportViewerModal.java         ← Visor HTML modal
│   └── session/
│       └── SessionManager.java            ← Sesión del usuario autenticado
├── app/controllers/                       ← Controladores JavaFX (ver arquitectura.md)
├── app/utils/
│   ├── AlertUtils.java                    ← Alertas JavaFX estandarizadas
│   ├── AnimacionUtils.java               ← Animaciones de error (shake)
│   └── ValidadorCampos.java              ← Validaciones de formularios
└── models/
    ├── Excursion.java
    ├── Reserva.java
    └── Usuario.java
```

## Modelos de datos

### Usuario
```java
// Campos: idUsuario, nombre, ap1, ap2, dni, email, telefono, rol, password,
//          valoracion, numTurnos, fechaRegistro, ultimoAcceso
// Rol: "cliente" | "guia" | "admin"
String getNombreCompleto() // nombre + " " + ap1 + " " + ap2
```

### Excursion
```java
// Campos: idExcursion, nombreRuta, zona, duracionHoras, precioPorPersona,
//          fechaInicio (LocalDate), plazasDisponibles, idGuia, nombreGuia,
//          imagen (byte[]), descripcion
```

### Reserva
```java
// Campos: idReserva, idUsuario, nombreCliente, idExcursion, nombreExcursion,
//          fechaReserva (LocalDateTime), numPersonas, estado, precioTotal
// Estado: "pendiente" | "confirmada" | "cancelada"
```

## Configuración de base de datos

**Archivo**: `app/src/main/resources/config/database.properties`  
**Variables de entorno** (`.env`): `DB_URL`, `DB_USER`, `DB_PASSWORD`

```java
// Obtener conexión (Singleton, thread-safe):
Connection conn = DatabaseConnection.getInstance().getConnection();
```

La clase `DatabaseConnection` gestiona reconexión automática.

## CRUDs — API pública

### UsuarioCRUD
```java
List<Usuario> obtenerTodos()
List<Usuario> buscar(String criterio)              // por nombre, DNI o email
Usuario autenticar(String email, String password)  // null si credenciales incorrectas
void insertar(Usuario u)
void actualizar(Usuario u)
void eliminar(int idUsuario)
List<Usuario> obtenerGuias()                       // solo rol="guia"
```

### ExcursionCRUD
```java
List<Excursion> obtenerTodas()
List<Excursion> buscar(String criterio)            // por nombre_ruta o zona
void insertar(Excursion e)
void actualizar(Excursion e)
void eliminar(int idExcursion)
```

### ReservaCRUD
```java
List<Reserva> obtenerTodas()
List<Reserva> buscar(String criterio)              // por cliente, excursión o estado
void insertar(Reserva r)
void actualizar(Reserva r)
void eliminar(int idReserva)
```

## SessionManager (Singleton)

```java
SessionManager session = SessionManager.getInstance();
session.iniciarSesion(usuario);
session.cerrarSesion();
Usuario u = session.getUsuarioActual();
String rol = session.getRol();          // "admin", "guia", "cliente"
boolean esAdmin = session.isAdmin();
boolean esGuia = session.isGuia();
boolean esCliente = session.isCliente();
```

## Esquema de base de datos (resumen)

```sql
USUARIOS (id_usuario SERIAL PK, nombre, ap1, ap2, dni UNIQUE, email UNIQUE,
          telefono, rol CHECK IN ('cliente','guia','admin'), password,
          valoracion, num_turnos, fecha_registro, ultimo_acceso)

EXCURSIONES (id_excursion SERIAL PK, nombre_ruta, zona, duracion_horas,
             precio_persona, fecha_inicio DATE, plazas_disponibles,
             id_guia FK→USUARIOS, imagen BYTEA, descripcion TEXT)

RESERVAS (id_reserva SERIAL PK, id_usuario FK→USUARIOS, id_excursion FK→EXCURSIONES,
          fecha_reserva TIMESTAMP, num_personas, estado CHECK IN ('pendiente','confirmada','cancelada'),
          precio_total)
```

## Utilidades

### AlertUtils
```java
AlertUtils.mostrarError("Título", "Mensaje");
AlertUtils.mostrarInformacion("Título", "Mensaje");
AlertUtils.mostrarAdvertencia("Título", "Mensaje");
Optional<ButtonType> result = AlertUtils.mostrarConfirmacion("Título", "Mensaje");
```

### ValidadorCampos
```java
// Valida campos de formularios JavaFX, retorna true si todos son válidos
boolean valido = ValidadorCampos.validarObligatorio(textField, labelError);
boolean emailOk = ValidadorCampos.validarEmail(emailField, labelError);
boolean dniOk = ValidadorCampos.validarDNI(dniField, labelError);
```

### AnimacionUtils
```java
// Sacudida para indicar error en un campo
AnimacionUtils.shake(nodo);
AnimacionUtils.shakeIntense(nodo);   // Sacudida más fuerte para errores críticos
```

## Convenciones de código

1. Los métodos CRUD lanzan `RuntimeException` en caso de error SQL — capturarlos en el controller
2. Las operaciones de BD se ejecutan en el hilo de la UI JavaFX (app pequeña, sin Task async para CRUD simples) 
3. Para operaciones pesadas como generación de informes, usar `javafx.concurrent.Task` para no bloquear la UI
4. Los campos `imagen (byte[])` de Excursion pueden ser null — siempre verificar antes de usar
5. Las contraseñas en BD están en texto plano (sistema legacy) — `UsuarioCRUD.autenticar()` hace comparación directa

## Patrones de controller

```java
// Cargar datos en tabla al inicializar
@Override
public void initialize(URL location, ResourceBundle resources) {
    configurarColumnas();
    cargarDatos();
}

private void cargarDatos() {
    try {
        List<Entidad> lista = crud.obtenerTodas();
        ObservableList<Entidad> datos = FXCollections.observableArrayList(lista);
        tabla.setItems(datos);
        recordCountLabel.setText(datos.size() + " registros");
    } catch (Exception e) {
        AlertUtils.mostrarError("Error", "No se pudieron cargar los datos: " + e.getMessage());
    }
}
```
