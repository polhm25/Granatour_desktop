---
name: jasper-reports-expert
description: Especialista en JasperReports 6.21.3 para GranaTour. Usa este agente para crear o modificar informes JRXML, cambiar estilos de informes PDF/HTML, añadir nuevos parámetros/campos/variables, o mejorar el ReportGenerator y el ReportViewerModal. También para integrar nuevos informes con la UI JavaFX.
tools:
  - Read
  - Edit
  - Write
  - Bash
---

# Agente: Experto en JasperReports — GranaTour

## Contexto del proyecto

Aplicación **GranaTour** usa JasperReports 6.21.3 para generar informes en formato PDF y HTML.
- **Base de datos**: PostgreSQL en Supabase
- **Versión**: `net.sf.jasperreports:jasperreports:6.21.3`
- **PDF engine**: OpenPDF (`com.github.librepdf:openpdf:1.3.30`)

## Archivos clave

```
app/src/main/resources/informes/
├── CatalogoExcursiones.jrxml          ← Catálogo de excursiones (salida HTML)
├── InformeReservasPorEstado.jrxml     ← Reservas filtradas por estado (salida PDF)
└── EstadisticasPorZona.jrxml          ← Estadísticas agrupadas por zona (salida HTML)

app/src/main/java/app/granatour/reports/
├── ReportGenerator.java               ← Genera los informes (compila JRXML, llena, exporta)
└── ReportViewerModal.java             ← Modal JavaFX con WebView para ver informes HTML

app/informes-generados/                ← Directorio de salida de los informes generados
```

## Clase ReportGenerator

**Ruta**: `app/src/main/java/app/granatour/reports/ReportGenerator.java`

### Métodos públicos

```java
// Genera el catálogo de excursiones en HTML y devuelve la ruta del archivo
public String generarCatalogoExcursionesHTML() throws JRException

// Genera el informe de reservas en PDF, filtrado por estado
// estadoReserva: "TODAS", "pendiente", "confirmada", "cancelada"
public String generarInformeReservasPDF(String estadoReserva) throws JRException

// Genera estadísticas de excursiones por zona en HTML
public String generarEstadisticasPorZonaHTML() throws JRException

// Abre un PDF con el visor del sistema operativo (xdg-open en Linux)
public void abrirPDFConVisorSistema(String pdfPath)

// Devuelve la ruta del directorio de salida
public String getOutputDirectory()
```

### Conexión a BD

```java
Connection connection = DatabaseConnection.getInstance().getConnection();
// La conexión ya está validada dentro de getValidConnection() — no reimplementar
```

### Flujo de generación de un informe

```java
InputStream reportStream = getClass().getResourceAsStream("/informes/MiInforme.jrxml");
JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
Connection connection = getValidConnection();
Map<String, Object> parameters = new HashMap<>();
parameters.put("MI_PARAMETRO", valor);
JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

// Para PDF:
JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);

// Para HTML:
HtmlExporter exporter = new HtmlExporter();
exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputPath));
exporter.exportReport();
```

## Clase ReportViewerModal

**Ruta**: `app/src/main/java/app/granatour/reports/ReportViewerModal.java`

Muestra informes HTML en una ventana modal con `WebView` de JavaFX.

```java
// Abre el informe HTML en una ventana modal (1050x750)
ReportViewerModal.mostrarInformeHTML(htmlPath, "Título de la ventana");
```

La ventana tiene:
- `WebView` central cargando el HTML local
- Barra inferior con botón "Cerrar" y botón "Abrir en Navegador"
- Barra superior con título del informe
- Usa `stylesheets` desde `styles.css`

## Informes existentes

### 1. CatalogoExcursiones.jrxml — HTML

**Consulta SQL:**
```sql
SELECT e.nombre_ruta, e.zona, e.duracion_horas, e.precio_persona,
       e.fecha_inicio, e.plazas_disponibles,
       CONCAT(COALESCE(u.nombre,''),' ',COALESCE(u.ap1,'')) as guia
FROM EXCURSIONES e
LEFT JOIN USUARIOS u ON e.id_guia = u.id_usuario
ORDER BY e.zona, e.nombre_ruta
```

**Campos**: `nombre_ruta`, `zona`, `duracion_horas`, `precio_persona`, `fecha_inicio`, `plazas_disponibles`, `guia`

**Parámetro**: `REPORT_TITLE` (String)

**Variable**: `TOTAL` (Count de nombre_ruta)

**Secciones**: `columnHeader` (cabeceras) + `detail` (filas) + `summary` (total)

### 2. InformeReservasPorEstado.jrxml — PDF

**Consulta SQL:**
```sql
SELECT r.id_reserva, CONCAT(u.nombre,' ',u.ap1,' ',COALESCE(u.ap2,'')) as cliente_nombre,
       u.dni as cliente_dni, e.nombre_ruta, e.zona, r.fecha_reserva,
       r.num_personas, UPPER(r.estado) as estado, r.precio_total
FROM RESERVAS r
INNER JOIN USUARIOS u ON r.id_usuario = u.id_usuario
INNER JOIN EXCURSIONES e ON r.id_excursion = e.id_excursion
WHERE $P{ESTADO_RESERVA} = 'TODAS' OR r.estado = $P{ESTADO_RESERVA}
ORDER BY r.fecha_reserva DESC
```

**Parámetro**: `ESTADO_RESERVA` (String, default "TODAS")

**Variables**: `TOTAL_RESERVAS` (Count), `TOTAL_PERSONAS` (Sum), `TOTAL_IMPORTE` (Sum)

**Secciones**: `title` + `columnHeader` + `detail` + `pageFooter` + `summary`

### 3. EstadisticasPorZona.jrxml — HTML (nuevo)

**Consulta SQL:**
```sql
SELECT zona,
       COUNT(*) AS total_excursiones,
       SUM(plazas_disponibles) AS total_plazas,
       AVG(precio_persona) AS precio_promedio,
       MIN(precio_persona) AS precio_min,
       MAX(precio_persona) AS precio_max
FROM EXCURSIONES
GROUP BY zona
ORDER BY zona
```

**Campos**: `zona`, `total_excursiones`, `total_plazas`, `precio_promedio`, `precio_min`, `precio_max`

## Tema de color corporativo

| Uso | Color |
|---|---|
| Encabezado principal | `#2E7D32` (verde oscuro) |
| Color primario | `#4CAF50` (verde) |
| Cabeceras de columna | `#2E7D32` texto `#FFFFFF` |
| Filas alternas | `#F1F8E9` (verde muy claro) |
| CONFIRMADA | `#2E7D32` |
| PENDIENTE | `#E65100` (naranja) |
| CANCELADA | `#C62828` (rojo) |
| Totales/Resumen | `backcolor="#E8F5E9"` |

## Colores de estado con printWhenExpression (en el detail band del PDF)

Para colorear el campo `estado` según su valor, usar 3 `<textField>` superpuestos en la misma posición `(x,y,width,height)`:

```xml
<!-- Estado CONFIRMADA - verde -->
<textField isBlankWhenNull="true">
    <reportElement x="430" y="2" width="60" height="18" forecolor="#2E7D32">
        <printWhenExpression><![CDATA[$F{estado}.equals("CONFIRMADA")]]></printWhenExpression>
    </reportElement>
    <textElement textAlignment="Center" verticalAlignment="Middle">
        <font fontName="SansSerif" size="8" isBold="true"/>
    </textElement>
    <textFieldExpression><![CDATA[$F{estado}]]></textFieldExpression>
</textField>
<!-- Repetir para PENDIENTE (#E65100) y CANCELADA (#C62828) -->
```

## Convenciones JRXML importantes

1. **pageWidth**: 595 puntos (A4), **pageHeight**: 842 puntos (A4)
2. Los campos JRXML deben ser del tipo Java correcto: `java.lang.String`, `java.math.BigDecimal`, `java.sql.Date`, `java.sql.Timestamp`, `java.lang.Integer`
3. Los parámetros SQL con `$P{NOMBRE}` evitan SQL injection
4. Para textos estáticos: `<staticText>`, para datos de BD: `<textField>`
5. El `backcolor` en `<rectangle>` debe coincidir con el `backcolor` de `<reportElement>` para evitar bordes blancos
6. Al añadir un nuevo informe al proyecto: añadir el `.jrxml` a `src/main/resources/informes/`, crear el método en `ReportGenerator`, y añadir el botón en la vista FXML correspondiente

## Dónde se activan los informes

| Informe | Vista | Botón FXML |
|---|---|---|
| CatalogoExcursiones | ExcursionesView.fxml | `generarInformeButton` |
| InformeReservasPorEstado | ReservasView.fxml | `generarInformeButton` + `estadoInformeComboBox` |
| EstadisticasPorZona | ExcursionesView.fxml | `estadisticasButton` |
