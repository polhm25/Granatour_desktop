# Sistema de Diseño UI — GranaTour

**Hoja de estilos**: `app/src/main/resources/css/styles.css`

## Regla principal

> **NUNCA usar `style=""` inline en FXML. Siempre usar `styleClass="nombre-clase"`.**

Esto garantiza consistencia visual y facilita cambios de tema globales.

## Paleta de colores

Las variables son **JavaFX CSS looked-up colors** definidas en `.root {}`.
Se usan directamente sin prefijo `-fx-` en propiedades de color (ej: `-fx-background-color: -primary`).

```css
/* En .root { } */
-primary:          #4CAF50   /* Verde principal */
-primary-dark:     #388E3C   /* Verde oscuro (hover) */
-primary-light:    #C8E6C9   /* Verde claro (fondos, selección) */
-primary-variant:  #2E7D32   /* Verde muy oscuro (pressed, encabezados) */
-secondary:        #81C784   /* Verde medio */
-accent:           #FFC107   /* Amarillo ámbar */

-background:       #FAFAFA   /* Fondo general de la app */
-surface:          #FFFFFF   /* Superficies (cards, campos) */
-surface-variant:  #F5F5F5   /* Fondos alternativos (toolbars) */

-text-primary:     #212121   /* Texto principal */
-text-secondary:   #757575   /* Texto secundario */
-text-disabled:    #BDBDBD   /* Texto deshabilitado */
-text-on-primary:  #FFFFFF   /* Texto sobre fondo verde */

-divider:          #E0E0E0   /* Líneas divisorias */
-border-color:     #CCCCCC   /* Bordes de campos */

-error:            #D32F2F   /* Rojo error */
-success:          #4CAF50   /* Verde éxito */
-warning:          #FF9800   /* Naranja advertencia */
-info:             #2196F3   /* Azul informativo */
```

**Colores hardcoded** (no como variables):
- `#1B5E20` — Fondo del sidebar (verde muy oscuro Material Design 900)
- `#2E7D32` — Encabezados de informes JasperReports
- `#F1F8E9` — Filas alternas en informes (verde muy claro)

## Componentes UI

### Botones

| Clase CSS | Color | Uso |
|---|---|---|
| `button-primary` | Verde (#4CAF50) | Acción principal: Añadir, Guardar, Confirmar |
| `button-secondary` | Borde verde, fondo blanco | Acción secundaria: Ver detalle |
| `button-cancel` | Gris (#757575) | Cancelar, Cerrar, Salir |
| `button-danger` | Rojo (#D32F2F) | Eliminar |
| `button-info` | Azul (#2196F3) | Editar, Ver, Informativo |
| `icon-button` | Blanco con sombra | Solo icono, sin texto |

**Ejemplo FXML:**
```xml
<Button styleClass="button-primary" text="_Añadir" mnemonicParsing="true" fx:id="añadirButton"/>
<Button styleClass="button-info" text="✏ _Editar" mnemonicParsing="true" fx:id="editarButton"/>
<Button styleClass="button-danger" text="🗑 E_liminar" mnemonicParsing="true" fx:id="eliminarButton"/>
```

### Sidebar de navegación

```xml
<!-- En Main.fxml, dentro de <left> -->
<VBox styleClass="sidebar">
    <Button fx:id="navUsuariosButton" styleClass="sidebar-button"
            text="Usuarios" contentDisplay="TOP" graphicTextGap="6">
        <graphic><FontIcon iconLiteral="mdi-account" iconSize="26"/></graphic>
    </Button>
    <!-- ... más botones ... -->
</VBox>
```

El botón activo tiene la clase `sidebar-button-active` añadida programáticamente.

### Toolbar de vista (barra superior de cada módulo)

```xml
<HBox styleClass="view-toolbar" alignment="CENTER_LEFT" spacing="12.0">
    <FontIcon iconLiteral="mdi-account" iconSize="22" iconColor="#4CAF50"/>
    <Label text="Gestión de Usuarios" styleClass="section-title"/>
    <Label fx:id="recordCountLabel" styleClass="record-count"/>
    <Region HBox.hgrow="ALWAYS"/>
    <TextField fx:id="searchField" prefWidth="280" promptText="Buscar..."/>
    <Button fx:id="buscarButton" styleClass="button-primary" text="🔍 _Buscar"/>
</HBox>
```

### Badges de estado (para Reservas)

```xml
<!-- Requiere TableCell personalizada en el controller -->
Label badge = new Label(estado);
badge.getStyleClass().addAll("badge", "badge-" + estado.toLowerCase());
```

| Clase | Color | Para estado |
|---|---|---|
| `badge-pendiente` | Naranja claro / texto naranja oscuro | "pendiente" |
| `badge-confirmada` | Verde claro / texto verde oscuro | "confirmada" |
| `badge-cancelada` | Rojo claro / texto rojo oscuro | "cancelada" |

### Tablas

Las tablas usan la clase `table-view` (aplicada por defecto a `TableView`).

Personalización de celdas de estado en el controller:
```java
estadoColumn.setCellFactory(col -> new TableCell<>() {
    @Override
    protected void updateItem(String estado, boolean empty) {
        super.updateItem(estado, empty);
        if (empty || estado == null) { setGraphic(null); return; }
        Label badge = new Label(estado);
        badge.getStyleClass().addAll("badge", "badge-" + estado.toLowerCase());
        setGraphic(badge);
        setText(null);
    }
});
```

### Cards

```xml
<VBox styleClass="card"> ... </VBox>
<VBox styleClass="card-elevated"> ... </VBox>  <!-- sombra más pronunciada -->
```

### Login

```xml
<StackPane styleClass="login-container">       <!-- gradiente verde de fondo -->
    <VBox styleClass="login-panel"> ... </VBox> <!-- panel blanco redondeado -->
</StackPane>
```

### Labels

| Clase | Tamaño | Uso |
|---|---|---|
| `label-title` | 24px bold | Títulos de página |
| `label-subtitle` | 18px bold gris | Subtítulos |
| `section-title` | 18px bold | Títulos de módulo en toolbar |
| `label-secondary` | 14px gris | Texto secundario |
| `record-count` | 12px gris | Contador de registros |
| `label-error` | 14px bold rojo | Mensajes de error |
| `label-success` | 14px bold verde | Mensajes de éxito |

### Campos de texto

Los `TextField`, `PasswordField`, `TextArea` y `ComboBox` ya tienen estilos base aplicados.
Al recibir foco se añade borde verde y sombra verde suave.

### Tooltips

```xml
<Button>
    <tooltip><Tooltip text="Descripción de la acción (Atajo)" /></tooltip>
</Button>
```

## Iconos Ikonli Material Design

**Dependencia**: `ikonli-materialdesign-pack:12.3.1`  
**Importación FXML**: `<?import org.kordamp.ikonli.javafx.FontIcon?>`  
**Uso**: `<FontIcon iconLiteral="mdi-NOMBRE" iconSize="N" iconColor="#RRGGBB"/>`

Consultar todos los iconos disponibles: https://kordamp.org/ikonli/cheat-sheet-materialdesign.html

Iconos usados en el proyecto:

| iconLiteral | Uso |
|---|---|
| `mdi-account` | Usuarios |
| `mdi-nature` | Excursiones |
| `mdi-calendar-check` | Reservas |
| `mdi-help-circle` | Ayuda |
| `mdi-email` | Email |
| `mdi-lock` | Contraseña |
| `mdi-magnify` | Búsqueda |
| `mdi-plus` | Añadir |
| `mdi-pencil` | Editar |
| `mdi-delete` | Eliminar |
| `mdi-file-pdf` | PDF |
| `mdi-chart-bar` | Estadísticas |
| `mdi-book-open-variant` | Manual |
| `mdi-keyboard` | Atajos |
| `mdi-phone` | Teléfono |
| `mdi-map-marker` | Ubicación |

## Animaciones disponibles

```java
// En app/utils/AnimacionUtils.java
AnimacionUtils.shake(nodo);        // Sacudida suave (errores de formulario)
AnimacionUtils.shakeIntense(nodo); // Sacudida fuerte (errores críticos)
```

## Guía de modales (ventanas emergentes)

Los modales se abren con `Modality.APPLICATION_MODAL` y tienen:
- Tamaño fijo (~400px ancho, ~500px alto según contenido)
- Logo de GranaTour como icono de la ventana
- Estilo consistente con la app principal
- Botones: Guardar (`button-primary`, Alt+G) y Cancelar (`button-cancel`, Alt+C)

**Ejemplo de apertura:**
```java
FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AñadirUsuariosModal.fxml"));
Parent root = loader.load();
Stage stage = new Stage();
stage.initModality(Modality.APPLICATION_MODAL);
stage.setScene(new Scene(root));
stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/LOGO(sinFondo).png")));
stage.showAndWait();
```
