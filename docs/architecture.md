# Arquitectura de GranaTour

## Stack tecnológico

| Capa | Tecnología | Versión |
|---|---|---|
| Lenguaje | Java | 21 |
| UI Framework | JavaFX | 21.0.5 |
| Build | Gradle | (wrapper incluido) |
| Base de datos | PostgreSQL (Supabase) | — |
| JDBC Driver | PostgreSQL JDBC | 42.7.3 |
| Iconos | Ikonli Material Design | 12.3.1 |
| UI Components | ControlsFX | 11.2.1 |
| Informes | JasperReports | 6.21.3 |
| PDF Engine | OpenPDF | 1.3.30 |
| Charts | JFreeChart | 1.0.19 |

## Estructura de paquetes

```
app/src/main/java/
├── app/
│   ├── controllers/
│   │   ├── LoginController.java
│   │   ├── MainController.java
│   │   ├── UsuariosController.java
│   │   ├── ExcursionesController.java
│   │   ├── ReservasController.java
│   │   ├── AyudaController.java
│   │   ├── AnadirUsuariosController.java
│   │   ├── EditarUsuarioController.java
│   │   ├── AnadirExcursionController.java
│   │   ├── EditarExcursionController.java
│   │   ├── AnadirReservaController.java
│   │   └── EditarReservaController.java
│   ├── granatour/
│   │   ├── Main.java                    ← JavaFX Application entry point
│   │   ├── config/DatabaseConfig.java
│   │   ├── crud/
│   │   │   ├── ExcursionCRUD.java
│   │   │   ├── ReservaCRUD.java
│   │   │   └── UsuarioCRUD.java
│   │   ├── database/DatabaseConnection.java
│   │   ├── reports/
│   │   │   ├── ReportGenerator.java
│   │   │   └── ReportViewerModal.java
│   │   └── session/SessionManager.java
│   └── utils/
│       ├── AlertUtils.java
│       ├── AnimacionUtils.java
│       └── ValidadorCampos.java
└── models/
    ├── Excursion.java
    ├── Reserva.java
    └── Usuario.java

app/src/main/resources/
├── config/database.properties
├── css/styles.css
├── fxml/                                ← 12 archivos FXML
├── images/                              ← logo.png, LOGO(sinFondo).png
└── informes/                            ← 3 archivos JRXML
```

## Flujo de inicio de la aplicación

```
main() → Application.launch()
    └── Main.start(Stage)
            └── carga Login.fxml
                    └── LoginController.handleLogin()
                            └── UsuarioCRUD.autenticar(email, password)
                                    ├── éxito → SessionManager.iniciarSesion(usuario)
                                    │           └── carga Main.fxml en el mismo Stage
                                    └── fallo → muestra errorLabel + shake animation
```

## Layout principal (post-rediseño)

```
┌──────────────────────────────────────────────────────────────┐
│  HEADER: [GranaTour]        [Usuario: X | Rol: Y]  [Salir]  │  ← HBox styleClass="header"
├──────────┬───────────────────────────────────────────────────┤
│ SIDEBAR  │                                                    │
│          │                                                    │
│ 👤       │                                                    │
│ Usuarios │           CONTENT AREA                            │  ← StackPane fx:id="contentArea"
│          │       (vista activa: Usuarios /                    │
│ 🗺️        │        Excursiones / Reservas / Ayuda)            │
│ Excurs.  │                                                    │
│          │                                                    │
│ 📅       │                                                    │
│ Reservas │                                                    │
│          │                                                    │
│ ❓       │                                                    │
│ Ayuda    │                                                    │
│          │                                                    │
└──────────┴───────────────────────────────────────────────────┘
  ↑ VBox styleClass="sidebar" (110px ancho, fondo #1B5E20)
```

## Patrón MVC

```
VIEW (FXML)           CONTROLLER (Java)           MODEL/CRUD
     │                      │                         │
     │── @FXML injection ──▶│                         │
     │                      │── CRUD.obtener() ──────▶│── SQL ──▶ PostgreSQL
     │                      │◀─ List<Entidad> ────────│
     │◀── tabla.setItems()──│                         │
```

## Sistema de roles

| Rol | Acceso a Usuarios | Acceso a Excursiones | Acceso a Reservas | Acceso a Ayuda |
|---|---|---|---|---|
| `admin` | ✅ | ✅ | ✅ | ✅ |
| `guia` | ❌ | ✅ | ✅ | ✅ |
| `cliente` | ❌ | ✅ | ✅ | ✅ |

La restricción se aplica en `MainController.aplicarRestriccionesPorRol()`:
- oculta `navUsuariosButton` con `setVisible(false)` y `setManaged(false)`

## Sistema de informes

```
Controller ──▶ ReportGenerator
                    │── compila JRXML
                    │── llena con datos de BD (JDBC Connection)
                    │── exporta a HTML o PDF
                    │── HTML → ReportViewerModal (WebView JavaFX)
                    └── PDF → abrirPDFConVisorSistema() (xdg-open en Linux)

Directorio de salida: app/informes-generados/
```

## Atajos de teclado

| Atajo | Acción |
|---|---|
| Alt+1 | Navegar a Usuarios |
| Alt+2 | Navegar a Excursiones |
| Alt+3 | Navegar a Reservas |
| Alt+4 | Navegar a Ayuda |
| Alt+S | Cerrar sesión |
| Enter / Alt+I | Iniciar sesión (en Login) |
| Escape | Salir (en Login) |
| Alt+B | Buscar (en vistas de gestión) |
| Alt+A | Añadir nuevo registro |
| Alt+E | Editar registro seleccionado |
| Alt+L | Eliminar registro seleccionado |
| Alt+C | Generar Catálogo (Excursiones) |
| Alt+I | Generar Informe PDF (Reservas) |
| Alt+Z | Generar Estadísticas por Zona (Excursiones) |
| Alt+G | Guardar (en modales) |
| Alt+C | Cancelar (en modales) |
