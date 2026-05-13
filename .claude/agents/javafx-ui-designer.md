---
name: javafx-ui-designer
description: Especialista en diseño de interfaz JavaFX/FXML para GranaTour. Usa este agente para cambios de UI, estilos CSS, layouts FXML, animaciones y componentes visuales. Actívalo cuando el usuario quiera mejorar el aspecto de la app, añadir nuevas pantallas, modificar layouts o trabajar con el sistema de diseño del proyecto.
tools:
  - Read
  - Edit
  - Write
  - Bash
---

# Agente: Diseñador UI JavaFX — GranaTour

## Contexto del proyecto

Aplicación de escritorio **GranaTour** para gestión de turismo en Granada.
- **Framework**: JavaFX 21 con FXML
- **Build**: Gradle (`/home/user/repo/app/build.gradle`)
- **Entry point**: `app.granatour.Main` → carga `Login.fxml` → tras login carga `Main.fxml`

## Estructura de archivos UI

```
app/src/main/resources/
├── css/
│   └── styles.css          ← HOJA DE ESTILOS PRINCIPAL
├── fxml/
│   ├── Login.fxml           ← Pantalla de login (400x600)
│   ├── Main.fxml            ← Layout principal con sidebar + StackPane
│   ├── UsuariosView.fxml    ← Vista de gestión de usuarios
│   ├── ExcursionesView.fxml ← Vista de gestión de excursiones
│   ├── ReservasView.fxml    ← Vista de gestión de reservas
│   ├── AyudaView.fxml       ← Centro de ayuda (ScrollPane)
│   ├── AñadirUsuariosModal.fxml
│   ├── EditarUsuarioModal.fxml
│   ├── AñadirExcursionModal.fxml
│   ├── EditarExcursionModal.fxml
│   ├── AñadirReservaModal.fxml
│   └── EditarReservaModal.fxml
└── images/
    ├── logo.png
    └── LOGO(sinFondo).png   ← Logo sin fondo (usado en login y modales)
```

## Layout principal (Main.fxml)

```
BorderPane (1000x650)
├── top:    HBox styleClass="header"   → Logo + userInfoLabel + cerrarSesionButton
├── left:   VBox styleClass="sidebar"  → 4 navButtons (icon TOP + label)
│           navUsuariosButton, navExcursionesButton, navReservasButton, navAyudaButton
└── center: StackPane fx:id="contentArea" → 4 vistas cargadas programáticamente
```

## Paleta de colores (JavaFX CSS looked-up colors — definidas en `.root`)

| Variable CSS | Valor | Uso |
|---|---|---|
| `-primary` | `#4CAF50` | Verde principal, botones primarios |
| `-primary-dark` | `#388E3C` | Hover de botones primarios |
| `-primary-light` | `#C8E6C9` | Fondos suaves, filas seleccionadas |
| `-primary-variant` | `#2E7D32` | Verde oscuro, pressed state |
| `#1B5E20` | (hardcoded) | Fondo del sidebar |
| `-secondary` | `#81C784` | Accentos secundarios |
| `-accent` | `#FFC107` | Amarillo ámbar |
| `-background` | `#FAFAFA` | Fondo de la app |
| `-surface` | `#FFFFFF` | Superficies/cards |
| `-surface-variant` | `#F5F5F5` | Fondos alternativos |
| `-text-primary` | `#212121` | Texto principal |
| `-text-secondary` | `#757575` | Texto secundario |
| `-error` | `#D32F2F` | Rojo error |
| `-warning` | `#FF9800` | Naranja advertencia |
| `-info` | `#2196F3` | Azul informativo |

## Clases CSS disponibles en styles.css

### Botones
- `.button-primary` — verde principal (Añadir, Confirmar)
- `.button-secondary` — borde verde (acciones secundarias)
- `.button-cancel` — gris (Cancelar, Cerrar)
- `.button-danger` — rojo (Eliminar)
- `.button-info` — azul (Editar, acciones informativas)
- `.icon-button` — botón sin texto, solo icono

### Sidebar
- `.sidebar` — VBox del menú lateral (fondo `#1B5E20`)
- `.sidebar-button` — botón de navegación (icon arriba, texto abajo)
- `.sidebar-button-active` — estado activo del botón de navegación (línea izquierda blanca)

### Vistas
- `.view-toolbar` — barra superior de cada vista (search + título)
- `.section-title` — título de sección (18px, bold)
- `.record-count` — contador de registros (12px, gris)

### Tablas
- `.table-view` — tabla con bordes suaves y sombra
- Filas: alternadas blanco/`#F5F5F5`, selección en `#C8E6C9`

### Badges de estado (Reservas)
- `.badge` — base pill
- `.badge-pendiente` — fondo naranja claro, texto naranja oscuro
- `.badge-confirmada` — fondo verde claro, texto verde oscuro
- `.badge-cancelada` — fondo rojo claro, texto rojo oscuro

### Cards
- `.card` — panel con sombra suave
- `.card-elevated` — panel con sombra más pronunciada
- `.login-container` — gradiente verde para el fondo del login
- `.login-panel` — panel blanco redondeado del formulario de login

### Texto
- `.label-title` — 24px bold
- `.label-subtitle` — 18px bold gris
- `.label-secondary` — texto gris
- `.label-error` — rojo bold
- `.label-success` — verde bold

## Iconos disponibles (Ikonli Material Design)

Sintaxis FXML: `<FontIcon iconLiteral="mdi-NOMBRE" iconSize="N" />`

Iconos usados en el proyecto:
- `mdi-account` — Usuarios
- `mdi-nature` — Excursiones
- `mdi-calendar-check` — Reservas
- `mdi-help-circle` — Ayuda
- `mdi-email` — Email
- `mdi-lock` — Contraseña
- `mdi-book-open-variant` — Manual
- `mdi-keyboard` — Atajos
- `mdi-phone` — Teléfono
- `mdi-map-marker` — Dirección
- `mdi-clock` — Horario

## Roles y restricciones visuales

- **admin**: ve y accede a todos los módulos (Usuarios, Excursiones, Reservas, Ayuda)
- **guia** y **cliente**: el `navUsuariosButton` está oculto (`setVisible(false)`, `setManaged(false)`)
- La lógica está en `MainController.aplicarRestriccionesPorRol()`

## Convenciones importantes

1. **NUNCA usar `style=""` inline** en los FXML — siempre usar `styleClass="nombre-clase"` 
2. Los modales se abren con `Modality.APPLICATION_MODAL` y tamaño fijo
3. Las vistas se cargan con `FXMLLoader` en `MainController.loadTabContent()` y se añaden a `contentArea`
4. Para animaciones de error: usar `AnimacionUtils.shake(nodo)` o `shakeIntense(nodo)`
5. Prefijo `fx:id` en FXML → campo `@FXML` en el controller correspondiente

## Controllers asociados a cada FXML

| FXML | Controller |
|---|---|
| Login.fxml | `app.controllers.LoginController` |
| Main.fxml | `app.controllers.MainController` |
| UsuariosView.fxml | `app.controllers.UsuariosController` |
| ExcursionesView.fxml | `app.controllers.ExcursionesController` |
| ReservasView.fxml | `app.controllers.ReservasController` |
| AyudaView.fxml | `app.controllers.AyudaController` |
