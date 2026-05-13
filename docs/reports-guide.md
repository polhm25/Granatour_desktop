# Guía de Informes JasperReports — GranaTour

## Stack de informes

| Componente | Versión |
|---|---|
| JasperReports | 6.21.3 |
| OpenPDF (engine PDF) | 1.3.30 |
| JFreeChart (gráficos) | 1.0.19 |

## Archivos clave

```
app/src/main/resources/informes/
├── CatalogoExcursiones.jrxml          → HTML
├── InformeReservasPorEstado.jrxml     → PDF
└── EstadisticasPorZona.jrxml          → HTML

app/src/main/java/app/granatour/reports/
├── ReportGenerator.java               → Compilación, llenado y exportación
└── ReportViewerModal.java             → Modal WebView para informes HTML

app/informes-generados/                → Archivos generados (gitignore)
```

## Cómo añadir un nuevo informe (paso a paso)

### Paso 1: Crear el archivo JRXML

Crea `app/src/main/resources/informes/MiNuevoInforme.jrxml`.

**Plantilla base:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jasperReport xmlns="http://jasperreports.sourceforge.net/jasperreports"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd"
              name="MiNuevoInforme"
              pageWidth="595" pageHeight="842"
              columnWidth="555" leftMargin="20" rightMargin="20"
              topMargin="20" bottomMargin="20">

    <!-- Parámetros opcionales -->
    <parameter name="MI_PARAMETRO" class="java.lang.String">
        <defaultValueExpression><![CDATA["VALOR_DEFAULT"]]></defaultValueExpression>
    </parameter>

    <!-- Consulta SQL -->
    <queryString>
        <![CDATA[SELECT campo1, campo2 FROM TABLA WHERE condicion = $P{MI_PARAMETRO}]]>
    </queryString>

    <!-- Campos de la consulta -->
    <field name="campo1" class="java.lang.String"/>
    <field name="campo2" class="java.math.BigDecimal"/>

    <!-- Variables de cálculo opcionales -->
    <variable name="TOTAL" class="java.lang.Integer" calculation="Count">
        <variableExpression><![CDATA[$F{campo1}]]></variableExpression>
    </variable>

    <!-- Título del informe -->
    <title>
        <band height="60">
            <rectangle>
                <reportElement x="0" y="0" width="555" height="40" backcolor="#2E7D32" forecolor="#2E7D32"/>
            </rectangle>
            <staticText>
                <reportElement x="15" y="10" width="400" height="20" forecolor="#FFFFFF"/>
                <textElement verticalAlignment="Middle">
                    <font fontName="SansSerif" size="14" isBold="true"/>
                </textElement>
                <text><![CDATA[MI NUEVO INFORME]]></text>
            </staticText>
        </band>
    </title>

    <!-- Cabeceras de columnas -->
    <columnHeader>
        <band height="25">
            <rectangle>
                <reportElement x="0" y="0" width="555" height="25" backcolor="#2E7D32" forecolor="#2E7D32"/>
            </rectangle>
            <staticText>
                <reportElement x="5" y="0" width="200" height="25" forecolor="#FFFFFF"/>
                <textElement textAlignment="Left" verticalAlignment="Middle">
                    <font fontName="SansSerif" size="9" isBold="true"/>
                </textElement>
                <text><![CDATA[COLUMNA 1]]></text>
            </staticText>
        </band>
    </columnHeader>

    <!-- Detalle — una fila por cada registro -->
    <detail>
        <band height="20">
            <!-- Fila alterna (par) -->
            <rectangle>
                <reportElement x="0" y="0" width="555" height="19" backcolor="#F1F8E9" forecolor="#E0E0E0">
                    <printWhenExpression><![CDATA[$V{REPORT_COUNT} % 2 == 0]]></printWhenExpression>
                </reportElement>
            </rectangle>
            <textField isBlankWhenNull="true">
                <reportElement x="5" y="1" width="200" height="18"/>
                <textElement verticalAlignment="Middle">
                    <font fontName="SansSerif" size="8"/>
                </textElement>
                <textFieldExpression><![CDATA[$F{campo1}]]></textFieldExpression>
            </textField>
        </band>
    </detail>

    <!-- Resumen final -->
    <summary>
        <band height="40">
            <staticText>
                <reportElement x="300" y="10" width="150" height="15"/>
                <textElement textAlignment="Right" verticalAlignment="Middle">
                    <font fontName="SansSerif" size="10" isBold="true"/>
                </textElement>
                <text><![CDATA[Total registros:]]></text>
            </staticText>
            <textField>
                <reportElement x="455" y="10" width="95" height="15"/>
                <textElement verticalAlignment="Middle">
                    <font fontName="SansSerif" size="10" isBold="true"/>
                </textElement>
                <textFieldExpression><![CDATA[$V{TOTAL}]]></textFieldExpression>
            </textField>
        </band>
    </summary>

</jasperReport>
```

### Paso 2: Añadir el método en ReportGenerator.java

**Para HTML:**
```java
public String generarMiNuevoInformeHTML() throws JRException {
    ensureOutputDirectoryExists();
    InputStream reportStream = getClass().getResourceAsStream(REPORTS_PATH + "MiNuevoInforme.jrxml");
    if (reportStream == null) throw new JRException("No se encontró MiNuevoInforme.jrxml");

    JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
    Connection connection = getValidConnection();

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("MI_PARAMETRO", "valor");

    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String outputPath = OUTPUT_DIR + "MiNuevoInforme_" + timestamp + ".html";

    HtmlExporter exporter = new HtmlExporter();
    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
    exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputPath));
    SimpleHtmlReportConfiguration reportConfig = new SimpleHtmlReportConfiguration();
    reportConfig.setRemoveEmptySpaceBetweenRows(true);
    reportConfig.setWhitePageBackground(false);
    exporter.setConfiguration(reportConfig);
    exporter.exportReport();

    return outputPath;
}
```

**Para PDF** (cambiar la exportación):
```java
JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
```

### Paso 3: Añadir el botón en la vista FXML

```xml
<Button fx:id="miInformeButton" styleClass="button-cancel"
        mnemonicParsing="true" text="📊 Mi _Informe">
    <tooltip><Tooltip text="Generar informe (Alt+M)" /></tooltip>
</Button>
```

### Paso 4: Añadir el handler en el controller

```java
@FXML private Button miInformeButton;

// En initialize():
miInformeButton.setOnAction(e -> generarMiInforme());

private void generarMiInforme() {
    try {
        ReportGenerator generator = new ReportGenerator();
        String path = generator.generarMiNuevoInformeHTML();
        ReportViewerModal.mostrarInformeHTML(path, "Mi Informe");
    } catch (JRException e) {
        AlertUtils.mostrarError("Error al generar informe", e.getMessage());
    }
}
```

## Tipos Java para campos JRXML

| Tipo SQL | Tipo Java JRXML |
|---|---|
| VARCHAR, TEXT | `java.lang.String` |
| INT, SERIAL | `java.lang.Integer` |
| DECIMAL, NUMERIC | `java.math.BigDecimal` |
| DATE | `java.sql.Date` |
| TIMESTAMP | `java.sql.Timestamp` |
| BYTEA | `java.io.InputStream` |

## Patrones de expresiones JRXML

**Formato de fecha:**
```xml
<textField pattern="dd/MM/yyyy">
    <textFieldExpression><![CDATA[$F{fecha_inicio}]]></textFieldExpression>
</textField>
```

**Formato monetario:**
```xml
<textField pattern="#,##0.00 €">
    <textFieldExpression><![CDATA[$F{precio_total}]]></textFieldExpression>
</textField>
```

**Expresión condicional (ternario):**
```xml
<textFieldExpression>
    <![CDATA[$P{ESTADO}.equals("TODAS") ? "TODAS LAS RESERVAS" : $P{ESTADO}.toUpperCase()]]>
</textFieldExpression>
```

**printWhenExpression (mostrar/ocultar elemento):**
```xml
<reportElement x="0" y="0" width="60" height="18" forecolor="#2E7D32">
    <printWhenExpression><![CDATA[$F{estado}.equals("CONFIRMADA")]]></printWhenExpression>
</reportElement>
```

**Variable acumulada:**
```xml
<variable name="TOTAL_IMPORTE" class="java.math.BigDecimal" calculation="Sum">
    <variableExpression><![CDATA[$F{precio_total}]]></variableExpression>
</variable>
```

## Secciones (bands) de un JRXML

| Band | Descripción | Cuándo aparece |
|---|---|---|
| `background` | Fondo de página | Cada página (detrás de todo) |
| `title` | Encabezado del informe | Solo en la primera página |
| `pageHeader` | Cabecera de página | Cada página |
| `columnHeader` | Cabeceras de columnas | Antes de cada grupo de detalle |
| `detail` | Fila de datos | Una vez por cada registro |
| `columnFooter` | Pie de columnas | Después del grupo de detalle |
| `pageFooter` | Pie de página | Cada página |
| `summary` | Resumen final | Solo en la última página |

## Tema de color corporativo en informes

```
Encabezado/header:        backcolor="#2E7D32", text forecolor="#FFFFFF"
Cabeceras de columnas:    backcolor="#2E7D32", text forecolor="#FFFFFF"
Filas alternas (par):     backcolor="#F1F8E9"  (verde muy claro)
Filas normales (impar):   backcolor="#FFFFFF"
Totales/resumen:          backcolor="#E8F5E9"  (verde claro)
Estado CONFIRMADA:        forecolor="#2E7D32"
Estado PENDIENTE:         forecolor="#E65100"
Estado CANCELADA:         forecolor="#C62828"
Líneas/bordes:            forecolor="#E0E0E0"
Texto secundario:         forecolor="#424242"
```

## Solución de problemas comunes

**Error: "No se encontró el archivo JRXML"**
- Verificar que el archivo esté en `src/main/resources/informes/`
- Verificar en `build.gradle` que `sourceSets.main.resources` incluye `**/*.jrxml`
- Limpiar y recompilar: `./gradlew clean run`

**Error: "JRNoFieldsDataSource"**
- El nombre de un `<field>` no coincide con el alias en la consulta SQL
- Los nombres son case-sensitive

**Error: "JRException" al conectar**
- Verificar que `database.properties` tiene las credenciales correctas
- La conexión de Supabase puede expirar — `DatabaseConnection` intenta reconectar

**Informe vacío (0 páginas)**
- La consulta no devuelve resultados con los parámetros dados
- Verificar con `psql` o Supabase SQL Editor directamente
